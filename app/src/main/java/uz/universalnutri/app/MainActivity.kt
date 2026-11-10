package uz.universalnutri.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import uz.universalnutri.app.ui.AppNavHost
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.theme.UniversalNutriTheme

class MainActivity : AppCompatActivity() {
    private val vm: ShopViewModel by viewModels { ShopViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniversalNutriTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost(vm)
                }
            }
        }
    }
}
