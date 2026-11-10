package uz.universalnutri.app.ui.screens

import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.R
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.universalnutri.app.data.CartLine
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.goalLabel
import uz.universalnutri.app.ui.localizedError
import uz.universalnutri.app.ui.paymentLabel
import uz.universalnutri.app.ui.components.EmptyState
import uz.universalnutri.app.ui.components.ProductImage
import uz.universalnutri.app.ui.components.QuantityStepper
import uz.universalnutri.app.ui.theme.NutriSale
import uz.universalnutri.app.ui.theme.SectionLabel
import uz.universalnutri.app.util.Pricing
import uz.universalnutri.app.util.Validators
import uz.universalnutri.app.util.formatSom

private data class PaymentMethod(val name: String, val emoji: String)

private val paymentMethods = listOf(
    PaymentMethod("Cash", "💵"),
    PaymentMethod("Card", "💳"),
    PaymentMethod("Payme", "🟦"),
    PaymentMethod("Click", "🔷")
)

@Composable
fun CartScreen(vm: ShopViewModel, onOrderPlaced: (Long) -> Unit, onGoShopping: () -> Unit) {
    val lines by vm.cart.collectAsStateWithLifecycle()
    val bill by vm.bill.collectAsStateWithLifecycle()
    val appliedPromo by vm.promoCode.collectAsStateWithLifecycle()
    var address by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("+998 ") }
    var payment by rememberSaveable { mutableStateOf(paymentMethods.first().name) }
    var promoInput by rememberSaveable { mutableStateOf("") }
    var promoError by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var placing by rememberSaveable { mutableStateOf(false) }

    if (lines.isEmpty()) {
        EmptyState("🛒", stringResource(R.string.cart_empty_title), stringResource(R.string.cart_empty_text), stringResource(R.string.go_shopping), onGoShopping)
        return
    }

    val phoneOk = Validators.isValidPhone(phone)
    val canOrder = address.trim().length >= 5 && phoneOk && !placing

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(stringResource(R.string.my_cart), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.items_count, lines.sumOf { it.quantity }), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(lines, key = { it.product.id }) { line ->
            CartRow(
                line = line,
                onPlus = { vm.setQuantity(line.product.id, line.quantity + 1) },
                onMinus = { vm.setQuantity(line.product.id, line.quantity - 1) },
                onRemove = { vm.setQuantity(line.product.id, 0) }
            )
        }

        // ---------- promo code ----------
        item {
            Section(stringResource(R.string.promo_section)) {
                if (appliedPromo != null) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.promo_applied, appliedPromo ?: "", formatSom(bill.discount)), fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { vm.removePromo() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_promo))
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it.uppercase(); promoError = null },
                            placeholder = { Text("NUTRI10") },
                            leadingIcon = { Icon(Icons.Filled.LocalOffer, contentDescription = null) },
                            singleLine = true,
                            isError = promoError != null,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { promoError = vm.applyPromo(promoInput); if (promoError == null) promoInput = "" },
                            enabled = promoInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(56.dp)
                        ) { Text(stringResource(R.string.apply)) }
                    }
                    promoError?.let { Text(localizedError(it) ?: it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        // ---------- delivery ----------
        item {
            Section(stringResource(R.string.delivery_section)) {
                OutlinedTextField(
                    value = address, onValueChange = { address = it; error = null },
                    label = { Text(stringResource(R.string.address)) },
                    placeholder = { Text(stringResource(R.string.address_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it; error = null },
                    label = { Text(stringResource(R.string.phone)) },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    singleLine = true,
                    isError = phone.length > 6 && !phoneOk,
                    supportingText = if (phone.length > 6 && !phoneOk) { { Text(stringResource(R.string.phone_format)) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ---------- payment ----------
        item {
            Section(stringResource(R.string.payment_section)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { m ->
                        PaymentTile(m, selected = payment == m.name, modifier = Modifier.weight(1f)) { payment = m.name }
                    }
                }
            }
        }

        // ---------- summary + button ----------
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondary) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val onCard = MaterialTheme.colorScheme.onSecondary
                    SummaryRow(stringResource(R.string.subtotal), formatSom(bill.subtotal), onCard)
                    if (bill.discount > 0) SummaryRow(stringResource(R.string.discount_fmt, bill.promoCode ?: ""), "−${formatSom(bill.discount)}", NutriSaleLight)
                    SummaryRow(stringResource(R.string.delivery), if (bill.delivery == 0L) stringResource(R.string.free) else formatSom(bill.delivery), onCard)
                    if (bill.delivery > 0) {
                        Text(stringResource(R.string.free_delivery_left, formatSom(Pricing.FREE_DELIVERY_FROM - (bill.subtotal - bill.discount))),
                            style = MaterialTheme.typography.labelSmall, color = onCard.copy(alpha = 0.6f))
                    }
                    HorizontalDivider(color = onCard.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.total), color = onCard, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(formatSom(bill.total), color = onCard, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                    error?.let { Text(localizedError(it) ?: it, color = NutriSaleLight, style = MaterialTheme.typography.bodyMedium) }
                    Button(
                        onClick = {
                            placing = true
                            vm.checkout(address, phone, payment) { result ->
                                placing = false
                                result.onSuccess { id -> error = null; address = ""; onOrderPlaced(id) }
                                    .onFailure { error = it.message }
                            }
                        },
                        enabled = canOrder,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp)
                    ) {
                        if (placing) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text(stringResource(R.string.place_order, formatSom(bill.total)), fontSize = 16.sp)
                    }
                    if (!canOrder && !placing) {
                        Text(stringResource(R.string.fill_address_phone),
                            style = MaterialTheme.typography.labelSmall, color = onCard.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

private val NutriSaleLight = Color(0xFFFF8A93)

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text(title, style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color) {
    Row {
        Text(label, color = color.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
        Text(value, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PaymentTile(method: PaymentMethod, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = modifier.border(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            RoundedCornerShape(16.dp)
        )
    ) {
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(method.emoji, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(paymentLabel(method.name), style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun CartRow(line: CartLine, onPlus: () -> Unit, onMinus: () -> Unit, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(line.product, Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)), emojiSize = 36.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(line.product.name, style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${line.product.weight} · ${formatSom(line.product.price)}",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityStepper(line.quantity, onMinus, onPlus, plusEnabled = line.quantity < line.product.stock)
                    Spacer(Modifier.weight(1f))
                    Text(formatSom(line.lineTotal), fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall)
                }
                if (line.quantity > line.product.stock) {
                    Text(if (line.product.stock == 0) stringResource(R.string.sold_out_remove) else stringResource(R.string.only_in_stock, line.product.stock),
                        color = NutriSale, style = MaterialTheme.typography.labelSmall)
                }
            }
            Box(Modifier.align(Alignment.Top)) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
