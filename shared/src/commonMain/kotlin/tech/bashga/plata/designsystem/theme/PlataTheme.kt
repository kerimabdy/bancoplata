package tech.bashga.plata.designsystem.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import tech.bashga.plata.designsystem.foundation.PlataPressIndication

val LocalPlataColors = staticCompositionLocalOf { PlataColors() }
val LocalPlataTypography = staticCompositionLocalOf { PlataTypography() }
val LocalPlataSpacing = staticCompositionLocalOf { PlataSpacing() }
val LocalPlataSizes = staticCompositionLocalOf { PlataSizes() }
val LocalPlataRadii = staticCompositionLocalOf { PlataRadii() }
val LocalPlataShadows = staticCompositionLocalOf { PlataShadows() }
val LocalPlataGradients = staticCompositionLocalOf { PlataGradients() }

@Composable
fun PlataTheme(content: @Composable () -> Unit) {
    val colors = PlataColors()
    CompositionLocalProvider(
        LocalPlataColors provides colors,
        LocalPlataTypography provides PlataTypography(),
        LocalPlataSpacing provides PlataSpacing(),
        LocalPlataSizes provides PlataSizes(),
        LocalPlataRadii provides PlataRadii(),
        LocalPlataShadows provides PlataShadows(),
        LocalPlataGradients provides PlataGradients(),
        LocalIndication provides PlataPressIndication,
        LocalTextSelectionColors provides TextSelectionColors(colors.accent, colors.accent.copy(alpha = 0.3f)),
        content = content,
    )
}

object PlataTheme {
    val colors: PlataColors
        @Composable @ReadOnlyComposable get() = LocalPlataColors.current
    val typography: PlataTypography
        @Composable @ReadOnlyComposable get() = LocalPlataTypography.current
    val spacing: PlataSpacing
        @Composable @ReadOnlyComposable get() = LocalPlataSpacing.current
    val sizes: PlataSizes
        @Composable @ReadOnlyComposable get() = LocalPlataSizes.current
    val radii: PlataRadii
        @Composable @ReadOnlyComposable get() = LocalPlataRadii.current
    val shadows: PlataShadows
        @Composable @ReadOnlyComposable get() = LocalPlataShadows.current
    val gradients: PlataGradients
        @Composable @ReadOnlyComposable get() = LocalPlataGradients.current
}
