package uz.universalnutri.app

import android.app.Application
import android.content.Context
import uz.universalnutri.app.data.AppDatabase
import uz.universalnutri.app.data.ShopRepository

/** Created once when the app starts. Holds objects the whole app shares. */
class UniversalNutriApp : Application() {
    val repository: ShopRepository by lazy {
        ShopRepository(
            db = AppDatabase.get(this),
            prefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        )
    }
}
