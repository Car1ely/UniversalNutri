package uz.universalnutri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uz.universalnutri.app.data.Order
import uz.universalnutri.app.data.OrderStatus
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.components.OrderStatusChip
import uz.universalnutri.app.ui.theme.NutriSale
import uz.universalnutri.app.ui.theme.SectionLabel
import uz.universalnutri.app.util.formatDate
import uz.universalnutri.app.util.formatSom

/** One order: items, bill, delivery info and the actions that change its status. */
@Composable
fun OrderDetailScreen(
    vm: ShopViewModel,
    orderId: Long,
    onBack: () -> Unit,
    onProductClick: (Long) -> Unit,
    onOpenCart: () -> Unit
) {
    val data by remember(orderId) { vm.order(orderId) }.collectAsStateWithLifecycle(null)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var askCancel by rememberSaveable { mutableStateOf(false) }
    var busy by rememberSaveable { mutableStateOf(false) }

    val o = data
    if (o == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val order = o.order

    fun show(message: String) = scope.launch {
        snackbar.currentSnackbarData?.dismiss()
        snackbar.showSnackbar(message)
    }

    if (askCancel) {
        AlertDialog(
            onDismissRequest = { askCancel = false },
            title = { Text("Cancel order #${order.id}?") },
            text = { Text("The products will go back to the store. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    askCancel = false
                    busy = true
                    vm.cancelOrder(order.id) { err -> busy = false; show(err ?: "Order cancelled") }
                }) { Text("Cancel order", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { askCancel = false }) { Text("Keep it") } }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- header ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Order #${order.id}", style = MaterialTheme.typography.headlineSmall)
                    Text(formatDate(order.createdAt), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OrderStatusChip(order.status)
            }

            StatusTimeline(order)

            // ---------- items ----------
            Text("ITEMS", style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                o.items.forEachIndexed { i, item ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        Modifier.fillMaxWidth().clickable { onProductClick(item.productId) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(item.emoji, fontSize = 22.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.productName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            Text("${item.quantity} × ${formatSom(item.unitPrice)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatSom(item.unitPrice * item.quantity), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ---------- bill ----------
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BillRow("Subtotal", formatSom(order.subtotal))
                    if (order.discount > 0) BillRow("Discount ${order.promoCode ?: ""}", "−${formatSom(order.discount)}", NutriSale)
                    BillRow("Delivery", if (order.delivery == 0L) "Free" else formatSom(order.delivery))
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(formatSom(order.total), fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("💳 ${order.paymentMethod}   📍 ${order.address}   📞 ${order.phone}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ---------- actions ----------
            if (order.status == OrderStatus.NEW) {
                Button(
                    onClick = {
                        busy = true
                        vm.markDelivered(order.id) { err -> busy = false; show(err ?: "Thank you! Enjoy your order 💪") }
                    },
                    enabled = !busy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("I received the order", fontSize = 16.sp) }
                OutlinedButton(
                    onClick = { askCancel = true },
                    enabled = !busy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Cancel order", color = MaterialTheme.colorScheme.error, fontSize = 16.sp) }
            }
            OutlinedButton(
                onClick = {
                    vm.repeatOrder(order.id) { added ->
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            if (added == 0) {
                                snackbar.showSnackbar("These products are out of stock right now")
                            } else {
                                val r = snackbar.showSnackbar("$added item(s) added to cart", actionLabel = "Open cart")
                                if (r == SnackbarResult.ActionPerformed) onOpenCart()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Buy again", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

/** Placed → Delivered (or Placed → Cancelled) as three dots with labels. */
@Composable
private fun StatusTimeline(order: Order) {
    val cancelled = order.status == OrderStatus.CANCELLED
    val steps = if (cancelled) listOf("Placed", "Cancelled") else listOf("Placed", "On the way", "Delivered")
    val reached = when (order.status) {
        OrderStatus.DELIVERED -> 3
        OrderStatus.CANCELLED -> 2
        else -> 2          // a NEW order is already being prepared / delivered
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { i, label ->
            val done = i < reached
            val color = when {
                !done -> MaterialTheme.colorScheme.outlineVariant
                cancelled && i == steps.lastIndex -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                    color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (i < steps.lastIndex) {
                Box(
                    Modifier.weight(1f).padding(horizontal = 6.dp).padding(bottom = 18.dp).height(2.dp)
                        .background(if (i + 1 < reached) color else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}
