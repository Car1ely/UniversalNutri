package uz.universalnutri.app.ui

import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import uz.universalnutri.app.R
import uz.universalnutri.app.data.OrderStatus

/*
 * The data layer (Repository, DAO, Pricing) is plain Kotlin and reports errors in English,
 * so it stays easy to unit-test. These helpers translate those texts on the screen
 * into the language of the phone (English, Russian or Uzbek).
 */

private val exactErrors = mapOf(
    "Wrong e-mail or password" to R.string.err_wrong_login,
    "Enter your name" to R.string.err_enter_name,
    "Invalid e-mail" to R.string.err_invalid_email,
    "Password: min 6 characters and 1 digit" to R.string.err_password_rules,
    "This e-mail is already registered" to R.string.err_email_taken,
    "Unknown promo code" to R.string.err_unknown_promo,
    "Cart is empty" to R.string.err_cart_empty,
    "Please log in first" to R.string.err_login_first,
    "Choose from 1 to 5 stars" to R.string.err_review_stars,
    "Write at least a few words" to R.string.err_review_short,
    "You have already reviewed this product" to R.string.err_already_reviewed,
    "Only administrators can edit products" to R.string.err_only_admin,
    "Order not found" to R.string.err_order_not_found,
    "Name, brand and category are required" to R.string.err_product_text,
    "Price must be a positive number" to R.string.err_product_price,
    "Old price must be higher than the price (or empty)" to R.string.err_product_old_price,
    "Stock must be 0 or more" to R.string.err_product_stock,
    "Servings must be a positive number" to R.string.err_product_servings,
    "Old price must be a number" to R.string.err_old_price_number
)
private val outOfStock = Regex("^Not enough \"(.+)\" in stock$")
private val promoMinimum = Regex("^(\\S+) works from (.+)$")

/** English error from the data layer -> text in the current language (unknown texts are shown as they are). */
fun translateError(context: Context, message: String): String {
    exactErrors[message]?.let { return context.getString(it) }
    outOfStock.find(message)?.let { return context.getString(R.string.err_out_of_stock, it.groupValues[1]) }
    promoMinimum.find(message)?.let {
        return context.getString(R.string.err_promo_min, it.groupValues[1], it.groupValues[2])
    }
    if (message.startsWith("This order is already")) return context.getString(R.string.err_order_closed)
    return message
}

/** Same, for use directly inside a composable. */
@Composable
fun localizedError(message: String?): String? = message?.let { translateError(LocalContext.current, it) }

/** Order status stored in the database (NEW/DELIVERED/CANCELLED) -> label. */
@Composable
fun statusLabel(status: String): String = when (status) {
    OrderStatus.DELIVERED -> stringResource(R.string.status_delivered)
    OrderStatus.CANCELLED -> stringResource(R.string.status_cancelled)
    else -> stringResource(R.string.status_new)
}

/** Goal stored in the database (English key) -> label. */
@Composable
fun goalLabel(goal: String): String = when (goal) {
    "Muscle gain" -> stringResource(R.string.goal_muscle)
    "Weight loss" -> stringResource(R.string.goal_loss)
    "Endurance" -> stringResource(R.string.goal_endurance)
    "Health" -> stringResource(R.string.goal_health)
    else -> goal
}

/** Payment method stored in the order (Cash/Card/Payme/Click) -> label. */
@Composable
fun paymentLabel(method: String): String = when (method) {
    "Cash" -> stringResource(R.string.pay_cash)
    "Card" -> stringResource(R.string.pay_card)
    else -> method      // Payme, Click are brand names
}

/** Languages offered in the profile. Empty tag = follow the phone. */
val appLanguages = listOf("" to "Auto", "en" to "English", "ru" to "Русский", "uz" to "O‘zbekcha")

/** Switches the app language (Android 13+ also shows it in system settings). The screen is re-created. */
fun setAppLanguage(tag: String) {
    AppCompatDelegate.setApplicationLocales(
        if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
    )
}

fun currentAppLanguage(): String = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-')
