package uz.universalnutri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.components.FavoriteButton
import uz.universalnutri.app.ui.components.InfoTile
import uz.universalnutri.app.ui.components.PriceTag
import uz.universalnutri.app.ui.components.ProductImage
import uz.universalnutri.app.ui.components.QuantityStepper
import uz.universalnutri.app.ui.components.RatingRow
import uz.universalnutri.app.ui.components.SaleBadge
import uz.universalnutri.app.ui.theme.SectionLabel
import uz.universalnutri.app.util.Pricing
import uz.universalnutri.app.util.formatSom

@Composable
fun DetailScreen(vm: ShopViewModel, productId: Long, onBack: () -> Unit, onOpenCart: () -> Unit) {
    val product by remember(productId) { vm.product(productId) }.collectAsStateWithLifecycle(null)
    val favorites by vm.favoriteIds.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quantity by rememberSaveable { mutableIntStateOf(1) }

    val p = product
    if (p == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (p.stock > 0) {
                        QuantityStepper(
                            quantity,
                            onMinus = { if (quantity > 1) quantity-- },
                            onPlus = { quantity++ },
                            plusEnabled = quantity < p.stock
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Button(
                        onClick = {
                            vm.addToCart(p.id, quantity)
                            scope.launch {
                                snackbar.currentSnackbarData?.dismiss()
                                val r = snackbar.showSnackbar("$quantity × ${p.name} added", actionLabel = "Open cart")
                                if (r == androidx.compose.material3.SnackbarResult.ActionPerformed) onOpenCart()
                            }
                            quantity = 1
                        },
                        enabled = p.stock > 0,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(54.dp)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (p.stock > 0) "Add · ${formatSom(p.price * quantity)}" else "Sold out",
                            fontSize = 15.sp, maxLines = 1)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- hero ----------
            Box {
                ProductImage(
                    p,
                    Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)),
                    emojiSize = 128.sp
                )
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    FavoriteButton(p.id in favorites, { vm.toggleFavorite(p.id) }, size = 44.dp)
                }
                if (p.discountPercent > 0) {
                    SaleBadge(p.discountPercent, Modifier.align(Alignment.BottomStart).padding(20.dp))
                }
            }

            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text(p.brand.uppercase(), style = SectionLabel, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(p.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingRow(p.rating, p.reviews)
                        Text("  ·  ${p.category}  ·  ${p.goal}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.weight(1f)) { PriceTag(p.price, p.oldPrice, big = true) }
                    StockPill(p.stock)
                }

                // ---------- facts ----------
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (p.proteinPerServing > 0) InfoTile("${p.proteinPerServing} g", "protein", Modifier.weight(1f))
                    InfoTile("${p.servings}", "servings", Modifier.weight(1f))
                    InfoTile(p.weight, "size", Modifier.weight(1f))
                    InfoTile(formatSom(p.price / p.servings).removeSuffix(" so'm"), "so'm / serv.", Modifier.weight(1f))
                }

                if (p.flavor != "—") {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👅", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("FLAVOUR", style = SectionLabel, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(p.flavor, style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                Text("ABOUT", style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(p.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)

                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Delivery across Tashkent in 2–4 hours. Free from ${formatSom(Pricing.FREE_DELIVERY_FROM)}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun StockPill(stock: Int) {
    val (text, ok) = when {
        stock == 0 -> "Sold out" to false
        stock < 10 -> "Only $stock left" to false
        else -> "In stock" to true
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (ok) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning, contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            color = if (ok) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer)
    }
}
