package uz.universalnutri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.universalnutri.app.data.OrderWithItems
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.components.InfoTile
import uz.universalnutri.app.ui.theme.BrandGradient
import uz.universalnutri.app.ui.theme.SectionLabel
import uz.universalnutri.app.util.formatDate
import uz.universalnutri.app.util.formatSom

@Composable
fun ProfileScreen(vm: ShopViewModel, onLoggedOut: () -> Unit, onOpenFavorites: () -> Unit) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    val user by vm.currentUser.collectAsStateWithLifecycle()
    val favorites by vm.favoriteIds.collectAsStateWithLifecycle()
    val initials = user?.fullName?.split(' ')?.filter { it.isNotBlank() }?.take(2)
        ?.joinToString("") { it.first().uppercase() } ?: "?"

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(BrandGradient).padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user?.fullName ?: "", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(user?.email ?: "", color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium)
                        user?.let {
                            Text("Member since ${formatDate(it.createdAt).substringBefore(' ')}",
                                color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = { vm.logout(); onLoggedOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out", tint = Color.White)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoTile("${orders.size}", "orders", Modifier.weight(1f))
                InfoTile(formatSom(orders.sumOf { it.order.total }).removeSuffix(" so'm"), "so'm spent", Modifier.weight(1f))
                InfoTile("${favorites.size}", "favourites", Modifier.weight(1f), onClick = onOpenFavorites)
            }
        }
        item {
            Text("ORDER HISTORY", style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp))
        }
        if (orders.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No orders yet", style = MaterialTheme.typography.titleMedium)
                    Text("Your orders will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(orders, key = { it.order.id }) { OrderCard(it) }
    }
}

@Composable
private fun OrderCard(o: OrderWithItems) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Order #${o.order.id}", style = MaterialTheme.typography.titleMedium)
                    Text(formatDate(o.order.createdAt), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(o.order.status)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                o.items.take(5).forEach { item ->
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Text(item.emoji, fontSize = 20.sp) }
                }
                if (o.items.size > 5) {
                    Text("+${o.items.size - 5}", modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            Text(o.items.joinToString { "${it.quantity}× ${it.productName}" },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${o.order.paymentMethod} · ${o.order.address}", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, modifier = Modifier.weight(1f))
                Text(formatSom(o.order.total), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    Text(
        status,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold
    )
}
