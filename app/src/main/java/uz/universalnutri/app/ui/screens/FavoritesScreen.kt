package uz.universalnutri.app.ui.screens

import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.components.EmptyState
import uz.universalnutri.app.ui.components.ProductCard

@Composable
fun FavoritesScreen(
    vm: ShopViewModel,
    snackbar: SnackbarHostState,
    onProductClick: (Long) -> Unit,
    onGoShopping: () -> Unit
) {
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val addedText = stringResource(R.string.added_to_cart)

    if (favorites.isEmpty()) {
        EmptyState("💛", stringResource(R.string.fav_empty_title), stringResource(R.string.fav_empty_text), stringResource(R.string.browse_products), onGoShopping)
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.padding(bottom = 4.dp)) {
                Text(stringResource(R.string.favourites_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.saved_count, favorites.size), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(favorites, key = { it.id }) { p ->
            ProductCard(
                product = p,
                isFavorite = true,
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
