package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.bashga.plata.designsystem.theme.PlataTheme

// no map view on desktop: the space it takes, as the screenshot tests draw it
@Composable
internal actual fun PlatformMap(latitude: Double, longitude: Double, interactive: Boolean, modifier: Modifier) {
    Box(modifier.background(PlataTheme.colors.surfaceInset))
}
