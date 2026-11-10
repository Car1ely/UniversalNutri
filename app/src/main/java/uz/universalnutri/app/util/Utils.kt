package uz.universalnutri.app.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 650000 -> "650 000 so'm" */
fun formatSom(amount: Long): String =
    String.format(Locale.US, "%,d", amount).replace(',', ' ') + " so'm"

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))

object Validators {
    private val emailRegex = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")
    private val phoneRegex = Regex("^\\+?998\\d{9}$")

    fun isValidEmail(email: String) = emailRegex.matches(email.trim())
    fun isStrongPassword(password: String) = password.length >= 6 && password.any { it.isDigit() }

    /** Uzbek mobile number: +998 90 123 45 67 (spaces and dashes are allowed). */
    fun isValidPhone(phone: String) = phoneRegex.matches(phone.replace(" ", "").replace("-", ""))

    /** Admin product form. null = OK, otherwise the error message to show. */
    fun productError(
        name: String, brand: String, category: String,
        price: Long?, oldPrice: Long?, stock: Int?, servings: Int?
    ): String? = when {
        name.isBlank() || brand.isBlank() || category.isBlank() -> "Name, brand and category are required"
        price == null || price <= 0 -> "Price must be a positive number"
        oldPrice != null && oldPrice <= price -> "Old price must be higher than the price (or empty)"
        stock == null || stock < 0 -> "Stock must be 0 or more"
        servings == null || servings <= 0 -> "Servings must be a positive number"
        else -> null
    }
}

object Security {
    /** SHA-256 hash with the e-mail as salt. Good for learning; use Firebase Auth / bcrypt in production. */
    fun hashPassword(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password".toByteArray())
            .joinToString("") { "%02x".format(it) }
}

/** Business rules for money: promo codes and delivery. Pure Kotlin = easy to unit-test. */
object Pricing {
    const val FREE_DELIVERY_FROM = 500_000L
    const val DELIVERY_FEE = 20_000L

    private val promoCodes = mapOf(
        "NUTRI10" to 10,     // -10 % for everybody
        "SPORT15" to 15      // -15 % from 1 000 000 so'm
    )
    private val promoMinimum = mapOf("SPORT15" to 1_000_000L)

    data class Bill(
        val subtotal: Long,
        val discount: Long,
        val delivery: Long,
        val promoCode: String?
    ) {
        val total: Long get() = subtotal - discount + delivery
    }

    /** null = code is OK, otherwise the error message to show. */
    fun checkPromo(code: String, subtotal: Long): String? {
        val c = code.trim().uppercase()
        if (c !in promoCodes) return "Unknown promo code"
        val min = promoMinimum[c] ?: 0L
        if (subtotal < min) return "$c works from ${formatSom(min)}"
        return null
    }

    fun deliveryFee(subtotal: Long): Long =
        if (subtotal == 0L || subtotal >= FREE_DELIVERY_FROM) 0L else DELIVERY_FEE

    fun bill(subtotal: Long, promoCode: String?): Bill {
        val code = promoCode?.trim()?.uppercase()?.takeIf { checkPromo(it, subtotal) == null }
        val percent = code?.let { promoCodes.getValue(it) } ?: 0
        val discount = subtotal * percent / 100
        return Bill(subtotal, discount, deliveryFee(subtotal - discount), code)
    }
}
