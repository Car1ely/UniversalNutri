package uz.universalnutri.app.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.ui.translateError
import uz.universalnutri.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uz.universalnutri.app.data.Product
import uz.universalnutri.app.data.Review
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.goalLabel
import uz.universalnutri.app.ui.localizedError
import uz.universalnutri.app.ui.paymentLabel
import uz.universalnutri.app.ui.components.StarPicker
import uz.universalnutri.app.ui.theme.NutriStar
import uz.universalnutri.app.util.formatDate
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
fun DetailScreen(
    vm: ShopViewModel,
    productId: Long,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onProductClick: (Long) -> Unit
) {
    val product by remember(productId) { vm.product(productId) }.collectAsStateWithLifecycle(null)
    val reviews by remember(productId) { vm.reviews(productId) }.collectAsStateWithLifecycle(emptyList())
    val user by vm.currentUser.collectAsStateWithLifecycle()
    val favorites by vm.favoriteIds.collectAsStateWithLifecycle()
    var writingReview by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current      // for texts inside click handlers
    var quantity by rememberSaveable { mutableIntStateOf(1) }

    val p = product
    if (p == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val similar by remember(p.id, p.category) { vm.similar(p) }.collectAsStateWithLifecycle(emptyList())
    val alreadyReviewed = reviews.any { it.userId == user?.id }

    if (writingReview) {
        ReviewDialog(
            productName = p.name,
            onDismiss = { writingReview = false },
            onSubmit = { stars, text, done ->
                vm.addReview(p.id, stars, text) { err ->
                    done(err)
                    if (err == null) {
                        writingReview = false
                        scope.launch { snackbar.showSnackbar(ctx.getString(R.string.review_thanks)) }
                    }
                }
            }
        )
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
                                val r = snackbar.showSnackbar(ctx.getString(R.string.added_qty, quantity, p.name), actionLabel = ctx.getString(R.string.open_cart))
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
                        Text(if (p.stock > 0) stringResource(R.string.add_price, formatSom(p.price * quantity)) else stringResource(R.string.sold_out),
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        Text("  ·  ${p.category}  ·  ${goalLabel(p.goal)}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.weight(1f)) { PriceTag(p.price, p.oldPrice, big = true) }
                    StockPill(p.stock)
                }

                // ---------- facts ----------
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (p.proteinPerServing > 0) InfoTile("${p.proteinPerServing} g", stringResource(R.string.fact_protein), Modifier.weight(1f))
                    InfoTile("${p.servings}", stringResource(R.string.fact_servings), Modifier.weight(1f))
                    InfoTile(p.weight, stringResource(R.string.fact_size), Modifier.weight(1f))
                    InfoTile(formatSom(p.price / p.servings).removeSuffix(" so'm"), stringResource(R.string.fact_per_serving), Modifier.weight(1f))
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
                            Text(stringResource(R.string.flavour_label), style = SectionLabel, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(p.flavor, style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                Text(stringResource(R.string.about_label), style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(p.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)

                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.delivery_info, formatSom(Pricing.FREE_DELIVERY_FROM)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // ---------- reviews ----------
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Text(stringResource(R.string.reviews_count, reviews.size), style = SectionLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (!alreadyReviewed) {
                        TextButton(onClick = { writingReview = true }) {
                            Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.write_review))
                        }
                    }
                }
                if (reviews.isEmpty()) {
                    Text(stringResource(R.string.no_reviews),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                reviews.forEach { r -> ReviewCard(r) }

                // ---------- similar products ----------
                if (similar.isNotEmpty()) {
                    Text(stringResource(R.string.similar_label), style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp))
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        similar.forEach { s -> SimilarCard(s, onClick = { onProductClick(s.id) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(r: Review) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r.authorName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(formatDate(r.createdAt).substringBefore(' '), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("★".repeat(r.rating) + "☆".repeat(5 - r.rating), color = NutriStar, fontSize = 14.sp)
        Text(r.text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SimilarCard(p: Product, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(140.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Column {
            ProductImage(p, Modifier.fillMaxWidth().height(96.dp).padding(5.dp).clip(RoundedCornerShape(14.dp)),
                emojiSize = 40.sp)
            Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                Text(p.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, minLines = 2,
                    overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                PriceTag(p.price, p.oldPrice)
            }
        }
    }
}

/** Dialog with 5 stars and a text field. onSubmit gets a callback to report the result (null = OK). */
@Composable
private fun ReviewDialog(
    productName: String,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, text: String, done: (String?) -> Unit) -> Unit
) {
    var stars by rememberSaveable { mutableIntStateOf(5) }
    var text by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var sending by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rate_fmt, productName), maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            val err = error
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StarPicker(stars) { stars = it }
                OutlinedTextField(
                    value = text, onValueChange = { text = it; error = null },
                    placeholder = { Text(stringResource(R.string.review_hint)) },
                    minLines = 3,
                    isError = err != null,
                    supportingText = if (err != null) { { Text(localizedError(err) ?: err) } } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !sending && text.isNotBlank(),
                onClick = {
                    sending = true
                    onSubmit(stars, text) { err -> sending = false; error = err }
                }
            ) { Text(stringResource(R.string.publish)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun StockPill(stock: Int) {
    val (text, ok) = when {
        stock == 0 -> stringResource(R.string.sold_out) to false
        stock < 10 -> stringResource(R.string.only_left, stock) to false
        else -> stringResource(R.string.in_stock) to true
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
