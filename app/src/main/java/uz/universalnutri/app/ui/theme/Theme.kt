package uz.universalnutri.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- UniversalNutri brand: energetic orange + graphite + lime ----------
val NutriOrange = Color(0xFFFF5A1F)
val NutriOrangeDark = Color(0xFFE0440C)
val NutriGraphite = Color(0xFF15171C)
val NutriLime = Color(0xFFB6F23A)
val NutriSale = Color(0xFFE5283A)
val NutriStar = Color(0xFFFFB400)

private val LightColors = lightColorScheme(
    primary = NutriOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE6DC),
    onPrimaryContainer = Color(0xFF5A1A00),
    secondary = NutriGraphite,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9EAEE),
    onSecondaryContainer = NutriGraphite,
    tertiary = Color(0xFF4C7A00),
    tertiaryContainer = Color(0xFFE6FBC2),
    onTertiaryContainer = Color(0xFF1C3000),
    background = Color(0xFFF6F6F8),
    onBackground = NutriGraphite,
    surface = Color.White,
    onSurface = NutriGraphite,
    surfaceVariant = Color(0xFFEFEFF3),
    onSurfaceVariant = Color(0xFF5D6170),
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFF1F1F4),
    outline = Color(0xFFD6D8DF),
    outlineVariant = Color(0xFFE6E7EC),
    error = NutriSale
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF7A45),
    onPrimary = Color(0xFF3A0E00),
    primaryContainer = Color(0xFF4A1C08),
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFE4E6EC),
    onSecondary = NutriGraphite,
    secondaryContainer = Color(0xFF2A2D35),
    onSecondaryContainer = Color(0xFFE4E6EC),
    tertiary = NutriLime,
    tertiaryContainer = Color(0xFF2B3D0C),
    onTertiaryContainer = Color(0xFFDDF9A8),
    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFECEDF1),
    surface = Color(0xFF17191E),
    onSurface = Color(0xFFECEDF1),
    surfaceVariant = Color(0xFF23262D),
    onSurfaceVariant = Color(0xFFA6AAB6),
    surfaceContainer = Color(0xFF17191E),
    surfaceContainerLow = Color(0xFF17191E),
    surfaceContainerHigh = Color(0xFF20232A),
    outline = Color(0xFF3A3E48),
    outlineVariant = Color(0xFF2C2F37),
    error = Color(0xFFFF5D6C)
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.Bold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold)
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/** Small caps-style label used above sections ("CATEGORIES", "ORDER SUMMARY"...). */
val SectionLabel = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

/** Hero gradient used on the login header, the promo banner and the success screen. */
val BrandGradient = Brush.linearGradient(listOf(Color(0xFFFF7A3D), NutriOrange, Color(0xFFD9360B)))

@Composable
fun UniversalNutriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
