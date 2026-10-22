package tech.bashga.plata.designsystem.foundation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Insets screens keep clear of: the status bar / Dynamic Island on top, the home indicator at the bottom. */
@Immutable
data class SafeAreaInsets(val top: Dp, val bottom: Dp)

val LocalPlataSafeArea = staticCompositionLocalOf { SafeAreaInsets(0.dp, 0.dp) }

object PlataSafeArea {
    /** iPhone 15/16 portrait insets; screenshot tests use these so renders line up with the source screens. */
    val IPhone = SafeAreaInsets(top = 59.dp, bottom = 34.dp)

    val current: SafeAreaInsets
        @Composable @ReadOnlyComposable get() = LocalPlataSafeArea.current
}

@Composable
fun ProvideSafeArea(insets: SafeAreaInsets, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPlataSafeArea provides insets, content = content)
}

@Composable
fun ProvideWindowSafeArea(content: @Composable () -> Unit) {
    val padding = WindowInsets.safeDrawing.asPaddingValues()
    ProvideSafeArea(SafeAreaInsets(padding.calculateTopPadding(), padding.calculateBottomPadding()), content)
}
