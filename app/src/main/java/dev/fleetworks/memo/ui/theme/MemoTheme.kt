package dev.fleetworks.memo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

private val Amber = Color(0xFFFFB000)
private val Plum = Color(0xFF201A2E)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF7A5900),
    onPrimary = Color.White,
    primaryContainer = Amber,
    onPrimaryContainer = Plum,
    secondary = Color(0xFF5E4B6E),
    secondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFF8A4A00),
    surface = Color(0xFFFFFBF2),
    onSurface = Plum,
    surfaceVariant = Color(0xFFF0E6D2),
    background = Color(0xFFFFFBF2),
    onBackground = Plum
)

private val DarkScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Plum,
    primaryContainer = Color(0xFF5C4300),
    onPrimaryContainer = Color(0xFFFFDF9E),
    secondary = Color(0xFFCDBCE8),
    tertiary = Color(0xFFFFB871),
    surface = Plum,
    onSurface = Color(0xFFEDE6F5),
    surfaceVariant = Color(0xFF342B4A),
    background = Plum,
    onBackground = Color(0xFFEDE6F5)
)

private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MemoTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scheme = when {
        dynamic && Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, shapes = ExpressiveShapes, content = content)
}
