package uz.universalnutri.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// Each @Entity class becomes one table in the SQLite database.

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,           // never store the plain password!
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val category: String,               // Protein, Creatine, Pre-workout...
    val goal: String,                   // Muscle gain, Weight loss, Endurance, Health
    val description: String,
    val price: Long,                    // in so'm (whole numbers, no decimals)
    val oldPrice: Long? = null,         // not null = product is on sale
    val stock: Int,
    val weight: String,                 // "2.27 kg", "60 caps"
    val servings: Int,
    val proteinPerServing: Int = 0,     // grams, 0 = not a protein product
    val flavor: String,
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val emoji: String                   // simple offline "image"
) {
    val discountPercent: Int
        get() = if (oldPrice != null && oldPrice > price) ((oldPrice - price) * 100 / oldPrice).toInt() else 0
}

@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "productId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Product::class, parentColumns = ["id"], childColumns = ["productId"])
    ],
    indices = [Index("productId")]
)
data class CartItem(
    val userId: Long,
    val productId: Long,
    val quantity: Int
)

@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "productId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Product::class, parentColumns = ["id"], childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("productId")]
)
data class Favorite(
    val userId: Long,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subtotal: Long,
    val discount: Long,
    val delivery: Long,
    val total: Long,
    val address: String,
    val phone: String,
    val paymentMethod: String,
    val promoCode: String? = null,
    val status: String = "NEW",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    primaryKeys = ["orderId", "productId"],
    foreignKeys = [ForeignKey(entity = Order::class, parentColumns = ["id"], childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("productId")]
)
data class OrderItem(
    val orderId: Long,
    val productId: Long,
    val productName: String,            // copied: the order must not change if the product changes
    val emoji: String,
    val quantity: Int,
    val unitPrice: Long                 // copied: price at the moment of the order
)

// ---------- not tables: results of JOIN / @Relation queries ----------

data class CartLine(
    @Embedded val product: Product,
    val quantity: Int
) {
    val lineTotal: Long get() = product.price * quantity
}

data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItem>
)
