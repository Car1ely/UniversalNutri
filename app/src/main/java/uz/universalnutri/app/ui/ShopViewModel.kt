package uz.universalnutri.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.universalnutri.app.UniversalNutriApp
import uz.universalnutri.app.data.CartLine
import uz.universalnutri.app.data.OrderWithItems
import uz.universalnutri.app.data.Product
import uz.universalnutri.app.data.Review
import uz.universalnutri.app.data.ShopRepository
import uz.universalnutri.app.data.User
import uz.universalnutri.app.util.Pricing

/** Holds screen state and survives screen rotation. Screens call its functions. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModel(private val repo: ShopRepository) : ViewModel() {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()     // true after start-up work

    val currentUser: StateFlow<User?> = repo.currentUser

    // search + filter inputs from the Home screen
    val query = MutableStateFlow("")
    val category = MutableStateFlow<String?>(null)
    val goal = MutableStateFlow<String?>(null)

    val products: StateFlow<List<Product>> =
        combine(query, category, goal) { q, c, g -> Triple(q, c, g) }
            .flatMapLatest { (q, c, g) -> repo.products(q, c, g) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<String>> = repo.categories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteIds: StateFlow<Set<Long>> = currentUser
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.favoriteIds(u.id) }
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val favorites: StateFlow<List<Product>> = currentUser
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.favorites(u.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cart: StateFlow<List<CartLine>> = currentUser
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.cart(u.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // promo code typed in the cart; the bill is recalculated whenever the cart or the code changes
    private val _promoCode = MutableStateFlow<String?>(null)
    val promoCode: StateFlow<String?> = _promoCode.asStateFlow()

    val bill: StateFlow<Pricing.Bill> =
        combine(cart, _promoCode) { lines, code -> Pricing.bill(lines.sumOf { it.lineTotal }, code) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Pricing.bill(0, null))

    val orders: StateFlow<List<OrderWithItems>> = currentUser
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.orders(u.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.seedIfEmpty()
            repo.restoreSession()
            _ready.value = true
        }
    }

    fun product(id: Long): Flow<Product?> = repo.product(id)
    fun order(id: Long): Flow<OrderWithItems?> = repo.order(id)
    fun similar(product: Product): Flow<List<Product>> = repo.similar(product)
    fun reviews(productId: Long): Flow<List<Review>> = repo.reviews(productId)

    /** Whole catalogue for the admin screen (no filters). */
    val allProducts: StateFlow<List<Product>> = repo.products("", null, null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // helper: run a suspend action, report null (OK) or the error text
    private fun act(onResult: (String?) -> Unit, block: suspend () -> Unit) = viewModelScope.launch {
        onResult(runCatching { block() }.exceptionOrNull()?.message)
    }

    fun addReview(productId: Long, rating: Int, text: String, onResult: (String?) -> Unit) =
        act(onResult) { repo.addReview(productId, rating, text) }

    fun cancelOrder(orderId: Long, onResult: (String?) -> Unit) = act(onResult) { repo.cancelOrder(orderId) }
    fun markDelivered(orderId: Long, onResult: (String?) -> Unit) = act(onResult) { repo.markDelivered(orderId) }

    /** onResult(number of units put back into the cart) */
    fun repeatOrder(orderId: Long, onResult: (Int) -> Unit) = viewModelScope.launch {
        onResult(runCatching { repo.repeatOrder(orderId) }.getOrDefault(0))
    }

    fun saveProduct(product: Product, onResult: (String?) -> Unit) = act(onResult) { repo.saveProduct(product) }

    // onResult(null) = success, onResult("text") = error message to show
    fun login(email: String, password: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        onResult(repo.login(email, password).exceptionOrNull()?.message)
    }

    fun register(name: String, email: String, password: String, onResult: (String?) -> Unit) =
        viewModelScope.launch {
            onResult(repo.register(name, email, password).exceptionOrNull()?.message)
        }

    fun logout() {
        _promoCode.value = null
        repo.logout()
    }

    fun toggleFavorite(productId: Long) = viewModelScope.launch { repo.toggleFavorite(productId) }

    fun addToCart(productId: Long, quantity: Int = 1) =
        viewModelScope.launch { repo.addToCart(productId, quantity) }

    fun setQuantity(productId: Long, quantity: Int) =
        viewModelScope.launch { repo.setQuantity(productId, quantity) }

    /** Returns null if the code was applied, otherwise the error text. */
    fun applyPromo(code: String): String? {
        val error = Pricing.checkPromo(code, bill.value.subtotal)
        if (error == null) _promoCode.value = code.trim().uppercase()
        return error
    }

    fun removePromo() {
        _promoCode.value = null
    }

    fun checkout(address: String, phone: String, paymentMethod: String, onResult: (Result<Long>) -> Unit) =
        viewModelScope.launch {
            val result = runCatching { repo.checkout(address, phone, paymentMethod, _promoCode.value) }
            if (result.isSuccess) _promoCode.value = null
            onResult(result)
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as UniversalNutriApp
                ShopViewModel(app.repository)
            }
        }
    }
}
