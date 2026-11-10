package uz.universalnutri.app.ui.components

import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.universalnutri.app.data.OrderStatus
import uz.universalnutri.app.data.Product
import uz.universalnutri.app.ui.statusLabel
import uz.universalnutri.app.ui.theme.NutriSale
import uz.universalnutri.app.ui.theme.NutriStar
import uz.universalnutri.app.util.formatSom

// ---------- category look: every category has its own soft gradient + icon ----------

data class CategoryStyle(val emoji: String, val light: List<Color>, val dark: List<Color>)

private val categoryStyles = mapOf(
    "Protein" to CategoryStyle("🥛", listOf(Color(0xFFFFE9DD), Color(0xFFFFD2BC)), listOf(Color(0xFF3A2218), Color(0xFF2A1A13))),
    "Gainer" to CategoryStyle("💪", listOf(Color(0xFFFFF3D1), Color(0xFFFFE29A)), listOf(Color(0xFF3A3115), Color(0xFF2A2410))),
    "Creatine" to CategoryStyle("🧪", listOf(Color(0xFFE2EEFF), Color(0xFFC6DCFF)), listOf(Color(0xFF1A2740), Color(0xFF131D30))),
    "Pre-workout" to CategoryStyle("⚡", listOf(Color(0xFFFFE1E4), Color(0xFFFFC2C8)), listOf(Color(0xFF3D1A1E), Color(0xFF2C1316))),
    "Amino acids" to CategoryStyle("🧬", listOf(Color(0xFFE3F8F1), Color(0xFFC3EFDF)), listOf(Color(0xFF15332A), Color(0xFF10261F))),
    "Vitamins" to CategoryStyle("🌿", listOf(Color(0xFFEDFAD6), Color(0xFFD7F3A8)), listOf(Color(0xFF26331A), Color(0xFF1C2613))),
    "Bars" to CategoryStyle("🍫", listOf(Color(0xFFF3E8E1), Color(0xFFE6D2C4)), listOf(Color(0xFF33261F), Color(0xFF261C17))),
    "Fat burners" to CategoryStyle("🔥", listOf(Color(0xFFFFEBD6), Color(0xFFFFD3A8)), listOf(Color(0xFF3B2814), Color(0xFF2B1E0F)))
)
private val defaultStyle = CategoryStyle("🏋️", listOf(Color(0xFFF0F0F4), Color(0xFFE2E3EA)), listOf(Color(0xFF25272E), Color(0xFF1C1E24)))

fun categoryStyle(category: String) = categoryStyles[category] ?: defaultStyle

@Composable
fun categoryBrush(category: String): Brush {
    val s = categoryStyle(category)
    return Brush.linearGradient(if (isSystemInDarkTheme()) s.dark else s.light)
}

// ---------- product "photo": emoji on the category gradient ----------

@Composable
fun ProductImage(product: Product, modifier: Modifier = Modifier, emojiSize: TextUnit = 52.sp) {
    Box(modifier.background(categoryBrush(product.category)), contentAlignment = Alignment.Center) {
        Text(product.emoji, fontSize = emojiSize)
    }
}

@Composable
fun SaleBadge(percent: Int, modifier: Modifier = Modifier) {
    Text(
        "−$percent%",
        modifier = modifier
            .background(NutriSale, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
    )
}

@Composable
fun RatingRow(rating: Double, reviews: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = NutriStar, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text("$rating", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(" ($reviews)", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PriceTag(price: Long, oldPrice: Long?, big: Boolean = false) {
    Column {
        if (oldPrice != null && oldPrice > price) {
            Text(
                formatSom(oldPrice),
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
        }
        Text(
            formatSom(price),
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (oldPrice != null) NutriSale else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(if (isFavorite) R.string.fav_remove else R.string.fav_add),
                tint = if (isFavorite) NutriSale else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

// ---------- product card used in the Home grid and in Favourites ----------

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onAdd: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
    ) {
        Box {
            ProductImage(
                product,
                Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            if (product.discountPercent > 0) {
                SaleBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(14.dp))
            }
            FavoriteButton(isFavorite, onFavorite, Modifier.align(Alignment.TopEnd).padding(12.dp), size = 32.dp)
            if (product.stock == 0) {
                Text(
                    stringResource(R.string.sold_out),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp)) {
            Text(
                product.brand.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
            Spacer(Modifier.height(4.dp))
            RatingRow(product.rating, product.reviews)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Box(Modifier.weight(1f)) { PriceTag(product.price, product.oldPrice) }
                Surface(
                    onClick = onAdd,
                    enabled = product.stock > 0,
                    shape = RoundedCornerShape(12.dp),
                    color = if (product.stock > 0) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_to_cart),
                            tint = if (product.stock > 0) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------- quantity stepper used in the cart and on the detail screen ----------

@Composable
fun QuantityStepper(quantity: Int, onMinus: () -> Unit, onPlus: () -> Unit, plusEnabled: Boolean = true) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMinus, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.less), modifier = Modifier.size(18.dp))
        }
        Text("$quantity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
        IconButton(onClick = onPlus, enabled = plusEnabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.more), modifier = Modifier.size(18.dp))
        }
    }
}

// ---------- friendly empty state ----------

@Composable
fun EmptyState(emoji: String, title: String, text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(112.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 52.sp) }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(14.dp)) { Text(action) }
        }
    }
}

/** Little "label · value" box used for nutrition facts and profile stats. */
@Composable
fun InfoTile(value: String, label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Coloured pill with the order status: NEW (orange), DELIVERED (green), CANCELLED (red). */
@Composable
fun OrderStatusChip(status: String) {
    val (bg, fg) = when (status) {
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    Text(
        statusLabel(status),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold
    )
}

/** Row of 5 tappable stars (for writing a review). */
@Composable
fun StarPicker(rating: Int, onChange: (Int) -> Unit) {
    Row {
        for (i in 1..5) {
            IconButton(onClick = { onChange(i) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = stringResource(R.string.stars_fmt, i),
                    tint = NutriStar, modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
