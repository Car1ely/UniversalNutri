package uz.universalnutri.app.ui.screens

import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.R
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flowOf
import uz.universalnutri.app.data.Product
import uz.universalnutri.app.data.SampleData
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.goalLabel
import uz.universalnutri.app.ui.localizedError
import uz.universalnutri.app.ui.paymentLabel
import uz.universalnutri.app.ui.components.ProductImage
import uz.universalnutri.app.ui.theme.NutriSale
import uz.universalnutri.app.util.Validators
import uz.universalnutri.app.util.formatSom

/** Admin: list of all products. Tap a row to edit it, the button adds a new one. */
@Composable
fun AdminScreen(vm: ShopViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val products by vm.allProducts.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEdit(0L) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_product)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ScreenHeader(stringResource(R.string.admin_panel), stringResource(R.string.products_summary, products.size, products.count { it.stock == 0 }), onBack)
            }
            items(products, key = { it.id }) { p ->
                Surface(
                    onClick = { onEdit(p.id) },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProductImage(p, Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)), emojiSize = 26.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text("${p.category} · ${formatSom(p.price)}", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Text(
                            stringResource(R.string.pcs, p.stock),
                            fontWeight = FontWeight.Bold,
                            color = if (p.stock == 0) NutriSale else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/** Admin: form to create (productId == 0) or edit a product. */
@Composable
fun ProductEditScreen(vm: ShopViewModel, productId: Long, onDone: () -> Unit) {
    val isNew = productId == 0L
    val existing by remember(productId) { if (isNew) flowOf(null) else vm.product(productId) }
        .collectAsStateWithLifecycle(null)
    val categories by vm.categories.collectAsStateWithLifecycle()

    // every field is text while typing; converted to numbers on Save
    var loaded by rememberSaveable { mutableStateOf(isNew) }
    var name by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Protein") }
    var goal by rememberSaveable { mutableStateOf(SampleData.goals.first()) }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var oldPrice by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var flavor by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("🥛") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var saving by rememberSaveable { mutableStateOf(false) }

    // fill the form once, when the product arrives from the database
    LaunchedEffect(existing) {
        val p = existing
        if (!loaded && p != null) {
            name = p.name; brand = p.brand; category = p.category; goal = p.goal
            description = p.description; price = p.price.toString(); oldPrice = p.oldPrice?.toString() ?: ""
            stock = p.stock.toString(); weight = p.weight; servings = p.servings.toString()
            protein = if (p.proteinPerServing > 0) p.proteinPerServing.toString() else ""
            flavor = p.flavor; emoji = p.emoji
            loaded = true
        }
    }
    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    fun save() {
        val priceNum = price.trim().toLongOrNull()
        val oldNum = oldPrice.trim().takeIf { it.isNotEmpty() }?.toLongOrNull()
        val stockNum = stock.trim().toIntOrNull()
        val servingsNum = servings.trim().toIntOrNull()
        if (oldPrice.isNotBlank() && oldNum == null) { error = "Old price must be a number"; return }
        error = Validators.productError(name, brand, category, priceNum, oldNum, stockNum, servingsNum)
        if (error != null) return
        val base = existing ?: Product(
            name = "", brand = "", category = "", goal = "", description = "", price = 0, stock = 0,
            weight = "", servings = 1, flavor = "", emoji = ""
        )
        val product = base.copy(
            name = name.trim(), brand = brand.trim(), category = category.trim(), goal = goal,
            description = description.trim(), price = priceNum!!, oldPrice = oldNum, stock = stockNum!!,
            weight = weight.trim().ifEmpty { "—" }, servings = servingsNum!!,
            proteinPerServing = protein.trim().toIntOrNull() ?: 0,
            flavor = flavor.trim().ifEmpty { "—" }, emoji = emoji.trim().ifEmpty { "📦" }
        )
        saving = true
        vm.saveProduct(product) { err ->
            saving = false
            error = err
            if (err == null) onDone()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(if (isNew) stringResource(R.string.new_product) else stringResource(R.string.edit_product), if (isNew) stringResource(R.string.fill_card) else "#$productId", onDone)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text(emoji.ifBlank { "📦" }, fontSize = 36.sp) }
            Spacer(Modifier.width(12.dp))
            FormField(emoji, { emoji = it.take(4) }, stringResource(R.string.emoji_label), Modifier.weight(1f))
        }
        FormField(name, { name = it }, stringResource(R.string.name_req))
        FormField(brand, { brand = it }, stringResource(R.string.brand_req))
        FormField(category, { category = it }, stringResource(R.string.category_req))
        ChipsRow(categories, selected = category) { category = it }
        Text(stringResource(R.string.goal_label), style = MaterialTheme.typography.labelLarge)
        ChipsRow(SampleData.goals, selected = goal) { goal = it }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormField(price, { price = it.filter(Char::isDigit) }, stringResource(R.string.price_req), Modifier.weight(1f), number = true)
            FormField(oldPrice, { oldPrice = it.filter(Char::isDigit) }, stringResource(R.string.old_price), Modifier.weight(1f), number = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormField(stock, { stock = it.filter(Char::isDigit) }, stringResource(R.string.stock_req), Modifier.weight(1f), number = true)
            FormField(servings, { servings = it.filter(Char::isDigit) }, stringResource(R.string.servings_req), Modifier.weight(1f), number = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormField(weight, { weight = it }, stringResource(R.string.size_label), Modifier.weight(1f))
            FormField(protein, { protein = it.filter(Char::isDigit) }, stringResource(R.string.protein_label), Modifier.weight(1f), number = true)
        }
        FormField(flavor, { flavor = it }, stringResource(R.string.flavour))
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text(stringResource(R.string.description)) }, minLines = 3,
            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
        )
        error?.let { Text(localizedError(it) ?: it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        Button(
            onClick = { save() },
            enabled = !saving,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (saving) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text(if (isNew) stringResource(R.string.add_product) else stringResource(R.string.save_changes), fontSize = 16.sp)
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FormField(
    value: String, onChange: (String) -> Unit, label: String,
    modifier: Modifier = Modifier.fillMaxWidth(), number: Boolean = false
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        modifier = modifier
    )
}

@Composable
private fun ChipsRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(o) })
        }
    }
}
