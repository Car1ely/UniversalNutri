package uz.universalnutri.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uz.universalnutri.app.ui.screens.AdminScreen
import uz.universalnutri.app.ui.screens.CartScreen
import uz.universalnutri.app.ui.screens.OrderDetailScreen
import uz.universalnutri.app.ui.screens.ProductEditScreen
import uz.universalnutri.app.ui.screens.DetailScreen
import uz.universalnutri.app.ui.screens.FavoritesScreen
import uz.universalnutri.app.ui.screens.HomeScreen
import uz.universalnutri.app.ui.screens.LoginScreen
import uz.universalnutri.app.ui.screens.Logo
import uz.universalnutri.app.ui.screens.OrderSuccessScreen
import uz.universalnutri.app.ui.screens.ProfileScreen
import uz.universalnutri.app.ui.screens.RegisterScreen

/** All screen addresses ("routes") in one place */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{productId}"
    const val SUCCESS = "success/{orderId}"
    const val ORDER = "order/{orderId}"
    const val ADMIN = "admin"
    const val ADMIN_EDIT = "admin/edit/{productId}"     // productId 0 = new product
    fun detail(id: Long) = "detail/$id"
    fun success(id: Long) = "success/$id"
    fun order(id: Long) = "order/$id"
    fun adminEdit(id: Long) = "admin/edit/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Shop", Icons.Filled.Home),
    Tab(Routes.FAVORITES, "Saved", Icons.Filled.Favorite),
    Tab(Routes.CART, "Cart", Icons.Filled.ShoppingCart),
    Tab(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

@Composable
fun AppNavHost(vm: ShopViewModel) {
    val ready by vm.ready.collectAsStateWithLifecycle()
    if (!ready) {   // database is being prepared - show the logo + spinner
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Logo(size = 72)
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator()
            }
        }
        return
    }
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    // decided once: logged-in users go straight to the shop
    val start = remember { if (vm.currentUser.value != null) Routes.HOME else Routes.LOGIN }
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val cartCount = vm.cart.collectAsStateWithLifecycle().value.sumOf { it.quantity }
    val favCount = vm.favoriteIds.collectAsStateWithLifecycle().value.size

    Scaffold(
        contentWindowInsets = WindowInsets(0),      // every screen handles the status bar itself (edge-to-edge)
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        val badge = when (tab.route) {
                            Routes.CART -> cartCount
                            Routes.FAVORITES -> favCount
                            else -> 0
                        }
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { nav.navigateToTab(tab.route) },
                            label = { Text(tab.label) },
                            icon = {
                                if (badge > 0) {
                                    BadgedBox(badge = { Badge { Text("$badge") } }) { Icon(tab.icon, null) }
                                } else Icon(tab.icon, null)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = start, modifier = Modifier.padding(padding)) {
            composable(Routes.LOGIN) {
                LoginScreen(vm,
                    onLoggedIn = { nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                    onRegisterClick = { nav.navigate(Routes.REGISTER) })
            }
            composable(Routes.REGISTER) {
                RegisterScreen(vm,
                    onRegistered = { nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } },
                    onBack = { nav.popBackStack() })
            }
            composable(Routes.HOME) {
                HomeScreen(vm, snackbar, onProductClick = { id -> nav.navigate(Routes.detail(id)) })
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(vm, snackbar,
                    onProductClick = { id -> nav.navigate(Routes.detail(id)) },
                    onGoShopping = { nav.navigateToTab(Routes.HOME) })
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                DetailScreen(vm, productId = entry.arguments?.getLong("productId") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onOpenCart = { nav.navigateToTab(Routes.CART) },
                    onProductClick = { id -> nav.navigate(Routes.detail(id)) })
            }
            composable(Routes.CART) {
                CartScreen(vm,
                    onOrderPlaced = { id -> nav.navigate(Routes.success(id)) },
                    onGoShopping = { nav.navigateToTab(Routes.HOME) })
            }
            composable(
                Routes.SUCCESS,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { entry ->
                OrderSuccessScreen(vm, orderId = entry.arguments?.getLong("orderId") ?: 0L,
                    onMyOrders = { nav.navigateToTab(Routes.PROFILE) },
                    onContinue = { nav.navigateToTab(Routes.HOME) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(vm,
                    onLoggedOut = { nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                    onOpenFavorites = { nav.navigateToTab(Routes.FAVORITES) },
                    onOrderClick = { id -> nav.navigate(Routes.order(id)) },
                    onOpenAdmin = { nav.navigate(Routes.ADMIN) })
            }
            composable(
                Routes.ORDER,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { entry ->
                OrderDetailScreen(vm, orderId = entry.arguments?.getLong("orderId") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onProductClick = { id -> nav.navigate(Routes.detail(id)) },
                    onOpenCart = { nav.navigateToTab(Routes.CART) })
            }
            composable(Routes.ADMIN) {
                AdminScreen(vm,
                    onBack = { nav.popBackStack() },
                    onEdit = { id -> nav.navigate(Routes.adminEdit(id)) })
            }
            composable(
                Routes.ADMIN_EDIT,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                ProductEditScreen(vm, productId = entry.arguments?.getLong("productId") ?: 0L,
                    onDone = { nav.popBackStack() })
            }
        }
    }
}

/** Standard bottom-bar navigation: keep one copy of each tab, restore its state */
private fun NavHostController.navigateToTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
