package uz.universalnutri.app.ui.screens

import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.components.ProductCard
import uz.universalnutri.app.ui.components.categoryStyle
import uz.universalnutri.app.ui.theme.BrandGradient
import uz.universalnutri.app.ui.theme.NutriGraphite
import uz.universalnutri.app.ui.theme.NutriLime
import uz.universalnutri.app.ui.theme.SectionLabel

/** Training goals shown as big tiles on the home screen. */
// name = key stored in the database (English), label/hint = translated text from strings.xml
private data class Goal(val name: String, val emoji: String, val label: Int, val hint: Int)

private val goals = listOf(
    Goal("Muscle gain", "💪", R.string.goal_muscle, R.string.goal_muscle_hint),
    Goal("Weight loss", "🔥", R.string.goal_loss, R.string.goal_loss_hint),
    Goal("Endurance", "🏃", R.string.goal_endurance, R.string.goal_endurance_hint),
    Goal("Health", "🌿", R.string.goal_health, R.string.goal_health_hint)
)

@Composable
fun HomeScreen(vm: ShopViewModel, snackbar: SnackbarHostState, onProductClick: (Long) -> Unit) {
    val products by vm.products.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val selectedCategory by vm.category.collectAsStateWithLifecycle()
    val selectedGoal by vm.goal.collectAsStateWithLifecycle()
    val favorites by vm.favoriteIds.collectAsStateWithLifecycle()
    val user by vm.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val addedText = stringResource(R.string.added_to_cart)   // read here: onClick lambdas are not composable
    val filtering = query.isNotBlank() || selectedCategory != null || selectedGoal != null

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        fullWidth {
            Column(Modifier.padding(top = 16.dp)) {
                Text(stringResource(R.string.hello, user?.fullName?.substringBefore(' ') ?: stringResource(R.string.athlete)),
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.fuel_goals), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(14.dp))
                SearchField(query, onChange = { vm.query.value = it })
            }
        }
        if (!filtering) {
            fullWidth { PromoBanner() }
        }
        fullWidth {
            Column {
                Text(stringResource(R.string.your_goal), style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(goals) { g ->
                        GoalTile(g, selected = selectedGoal == g.name) {
                            vm.goal.value = if (selectedGoal == g.name) null else g.name
                        }
                    }
                }
            }
        }
        fullWidth {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip(stringResource(R.string.all), "✨", selected = selectedCategory == null) { vm.category.value = null }
                }
                items(categories) { c ->
                    CategoryChip(c, categoryStyle(c).emoji, selected = selectedCategory == c) {
                        vm.category.value = if (selectedCategory == c) null else c
                    }
                }
            }
        }
        fullWidth {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (filtering) stringResource(R.string.results) else stringResource(R.string.popular_now), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                Text(stringResource(R.string.items_count, products.size), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (products.isEmpty()) {
            fullWidth {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.nothing_found), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.try_another), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(products, key = { it.id }) { p ->
            ProductCard(
                product = p,
                isFavorite = p.id in favorites,
                onClick = { onProductClick(p.id) },
                onFavorite = { vm.toggleFavorite(p.id) },
                onAdd = {
                    vm.addToCart(p.id)
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(addedText.format(p.name))
                    }
                }
            )
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) { content() }

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { onChange("") }) {
                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear))
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    )
}

@Composable
private fun PromoBanner() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(BrandGradient)
            .padding(20.dp)
    ) {
        Text("🏋️", fontSize = 84.sp, modifier = Modifier.align(Alignment.CenterEnd))
        Column(Modifier.fillMaxWidth(0.68f)) {
            Text(stringResource(R.string.new_member), style = SectionLabel, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.first_order_offer), color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Black, lineHeight = 26.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "NUTRI10",
                    modifier = Modifier
                        .background(NutriGraphite, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = NutriLime, fontWeight = FontWeight.Black, letterSpacing = 1.sp
                )
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.free_delivery_from), color = Color.White, fontSize = 11.sp, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun GoalTile(goal: Goal, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    Column(
        Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, if (selected) bg else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(if (selected) NutriLime else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(goal.emoji, fontSize = 20.sp) }
        Spacer(Modifier.height(10.dp))
        Text(stringResource(goal.label), color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(goal.hint), color = fg.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CategoryChip(label: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("$emoji  $label", modifier = Modifier.padding(vertical = 8.dp)) },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
