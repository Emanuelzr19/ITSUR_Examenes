package mx.itsur.exams.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ITSURVerde = Color(0xFF8BC34A)
val ITSURVerdeOscuro = Color(0xFF558B2F)
val ITSURVerdeLima = Color(0xFFCDDC39)
val ITSURVerdeClaro = Color(0xFFDCEDC8)
val ITSURDorado = Color(0xFFFFC107)
val ITSURNavy = Color(0xFF1A237E)
val ITSURBlancoRoto = Color(0xFFF9FBF5)
val ITSURGrisClaro = Color(0xFFEEF2E6)
val ITSURGrisMedio = Color(0xFF8D9E7A)
val ITSURError = Color(0xFFD32F2F)
val ITSURTexto = Color(0xFF1B2B0E)

private val ITSURColorScheme = lightColorScheme(
    primary = ITSURVerde,
    onPrimary = Color.White,
    primaryContainer = ITSURVerdeClaro,
    onPrimaryContainer = ITSURVerdeOscuro,
    secondary = ITSURVerdeLima,
    onSecondary = ITSURTexto,
    secondaryContainer = Color(0xFFF0F4E0),
    onSecondaryContainer = ITSURVerdeOscuro,
    tertiary = ITSURDorado,
    onTertiary = ITSURTexto,
    background = ITSURBlancoRoto,
    onBackground = ITSURTexto,
    surface = Color.White,
    onSurface = ITSURTexto,
    surfaceVariant = ITSURGrisClaro,
    onSurfaceVariant = Color(0xFF4A5E33),
    error = ITSURError,
    onError = Color.White,
    outline = ITSURGrisMedio,
)

@Composable
fun ITSURTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ITSURColorScheme,
        typography = ITSURTypography,
        content = content
    )
}
