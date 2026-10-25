package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/** Street-level zoom: a few blocks around the pin. */
internal const val PlataMapZoom = 16.5

/** Spans the zoom covers on iOS, which sizes regions in metres rather than zoom levels. */
internal const val PlataMapSpanMeters = 600.0

/** A live map centred on [latitude], [longitude] with a pin there; [interactive] false lets a scrolling parent keep the gestures. */
@Composable
fun PlataMap(latitude: Double, longitude: Double, modifier: Modifier = Modifier, interactive: Boolean = true) {
    Box(modifier) {
        PlatformMap(latitude, longitude, interactive, Modifier.matchParentSize())
        // interop views take touches before Compose parents; a hit-tested layer above keeps them for the scroll
        if (!interactive) Box(Modifier.matchParentSize().pointerInput(Unit) {})
    }
}

@Composable
internal expect fun PlatformMap(latitude: Double, longitude: Double, interactive: Boolean, modifier: Modifier)
