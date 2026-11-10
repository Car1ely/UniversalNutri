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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.theme.BrandGradient
import uz.universalnutri.app.ui.theme.NutriGraphite
import uz.universalnutri.app.ui.theme.NutriLime
import uz.universalnutri.app.util.formatSom

/** Shown right after the @Transaction placeOrder() succeeded. */
@Composable
fun OrderSuccessScreen(vm: ShopViewModel, orderId: Long, onMyOrders: () -> Unit, onContinue: () -> Unit) {
    val order by remember(orderId) { vm.order(orderId) }.collectAsStateWithLifecycle(null)

    Column(
        Modifier
            .fillMaxSize()
            .background(BrandGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Box(
            Modifier.size(112.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(80.dp).clip(CircleShape).background(NutriLime), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = NutriGraphite, modifier = Modifier.size(44.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Order placed!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))
        Text("Thank you! Our courier will call you before delivery.", color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))

        order?.let { o ->
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Order #${o.order.id}", style = MaterialTheme.typography.titleLarge)
                    o.items.forEach {
                        Row {
                            Text("${it.emoji}  ${it.quantity}× ${it.productName}", modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(formatSom(it.unitPrice * it.quantity), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (o.order.discount > 0) {
                        Row {
                            Text("Discount ${o.order.promoCode}", modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("−${formatSom(o.order.discount)}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Row {
                        Text("Delivery", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (o.order.delivery == 0L) "Free" else formatSom(o.order.delivery))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total · ${o.order.paymentMethod}", modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium)
                        Text(formatSom(o.order.total), fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    Text("📍 ${o.order.address}   📞 ${o.order.phone}", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onMyOrders,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NutriGraphite, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("My orders", fontSize = 16.sp) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onContinue,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Continue shopping", fontSize = 16.sp) }
    }
}
