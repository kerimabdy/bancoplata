package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformMap(latitude: Double, longitude: Double, interactive: Boolean, modifier: Modifier) {
    val pin = remember { MKPointAnnotation() }
    UIKitView(
        factory = { MKMapView().apply { addAnnotation(pin) } },
        modifier = modifier,
        update = { map ->
            map.scrollEnabled = interactive
            map.zoomEnabled = interactive
            map.rotateEnabled = interactive
            map.pitchEnabled = interactive
            map.userInteractionEnabled = interactive
            val moved = pin.coordinate.useContents { this.latitude != latitude || this.longitude != longitude }
            if (moved) {
                val center = CLLocationCoordinate2DMake(latitude, longitude)
                pin.setCoordinate(center)
                map.setRegion(MKCoordinateRegionMakeWithDistance(center, PlataMapSpanMeters, PlataMapSpanMeters), animated = false)
            }
        },
        properties = UIKitInteropProperties(isInteractive = interactive, isNativeAccessibilityEnabled = true),
    )
}
