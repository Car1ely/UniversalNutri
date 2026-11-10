package uz.universalnutri.app.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.universalnutri.app.util.Security
import uz.universalnutri.app.util.Validators

/** The single place that talks to the database. Screens never use DAOs directly. */
class ShopRepository(
    private val db: AppDatabase,
    private val prefs: SharedPreferences
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ---------- start-up ----------
    suspend fun seedIfEmpty() {
        if (db.productDao().count() == 0) db.productDao().insertAll(SampleData.products)
    }

    suspend fun restoreSession() {
        val id = prefs.getLong(KEY_USER_ID, -1)
        if (id > 0) _currentUser.value = db.userDao().findById(id)
    }

    // ---------- accounts ----------
    suspend fun register(fullName: String, email: String, password: String): Result<User> {
        val mail = email.trim().lowercase()
        if (fullName.isBlank()) return Result.failure(IllegalArgumentException("Enter your name"))
        if (!Validators.isValidEmail(mail)) return Result.failure(IllegalArgumentException("Invalid e-mail"))
        if (!Validators.isStrongPassword(password)) {
            return Result.failure(IllegalArgumentException("Password: min 6 characters and 1 digit"))
        }
        if (db.userDao().findByEmail(mail) != null) {
            return Result.failure(IllegalArgumentException("This e-mail is already registered"))
        }
        val user = User(fullName = fullName.trim(), email = mail,
            passwordHash = Security.hashPassword(password, mail))
        val saved = user.copy(id = db.userDao().insert(user))
        startSession(saved)
        return Result.success(saved)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val mail = email.trim().lowercase()
        val user = db.userDao().findByEmail(mail)
        if (user == null || user.passwordHash != Security.hashPassword(password, mail)) {
            return Result.failure(IllegalArgumentException("Wrong e-mail or password"))
        }
        startSession(user)
        return Result.success(user)
    }

    fun logout() {
        prefs.edit().remove(KEY_USER_ID).apply()
        _currentUser.value = null
    }

    private fun startSession(user: User) {
        prefs.edit().putLong(KEY_USER_ID, user.id).apply()
        _currentUser.value = user
    }

    // ---------- catalogue ----------
    fun products(query: String, category: String?, goal: String?): Flow<List<Product>> =
        db.productDao().search(query.trim(), category, goal)

    fun categories(): Flow<List<String>> = db.productDao().categories()
    fun product(id: Long): Flow<Product?> = db.productDao().observe(id)

    // ---------- favourites ----------
    fun favoriteIds(userId: Long): Flow<List<Long>> = db.favoriteDao().observeIds(userId)
    fun favorites(userId: Long): Flow<List<Product>> = db.favoriteDao().observeProducts(userId)

    suspend fun toggleFavorite(productId: Long) {
        val userId = requireUserId()
        val dao = db.favoriteDao()
        if (dao.exists(userId, productId)) dao.remove(userId, productId)
        else dao.add(Favorite(userId, productId))
    }

    // ---------- cart ----------
    fun cart(userId: Long): Flow<List<CartLine>> = db.cartDao().observeCart(userId)

    suspend fun addToCart(productId: Long, quantity: Int = 1) {
        val userId = requireUserId()
        val existing = db.cartDao().find(userId, productId)
        db.cartDao().upsert(CartItem(userId, productId, (existing?.quantity ?: 0) + quantity))
    }

    suspend fun setQuantity(productId: Long, quantity: Int) {
        val userId = requireUserId()
        if (quantity <= 0) db.cartDao().remove(userId, productId)
        else db.cartDao().upsert(CartItem(userId, productId, quantity))
    }

    // ---------- orders ----------
    fun orders(userId: Long): Flow<List<OrderWithItems>> = db.orderDao().observeOrders(userId)
    fun order(orderId: Long): Flow<OrderWithItems?> = db.orderDao().observeOrder(orderId)

    suspend fun checkout(address: String, phone: String, paymentMethod: String, promoCode: String?): Long =
        db.orderDao().placeOrder(requireUserId(), address.trim(), phone.trim(), paymentMethod, promoCode)

    private fun requireUserId(): Long =
        currentUser.value?.id ?: throw IllegalStateException("Please log in first")

    private companion object {
        const val KEY_USER_ID = "user_id"
    }
}
