package tech.bashga.plata

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.status_battery
import plata.shared.generated.resources.status_cellular
import plata.shared.generated.resources.status_wifi

private val BarHeight = 62.dp
private const val SampleWidth = 40
private const val SampleHeight = 6

/**
 * The iOS status bar over [content] while [visible], black or white by what the app draws under it, as iOS picks it
 * for an app that leaves the style on automatic.
 */
@Composable
internal fun DeviceStatusBar(visible: Boolean, content: @Composable () -> Unit) {
    val layer = rememberGraphicsLayer()
    val density = LocalDensity.current
    var lightContent by remember { mutableStateOf(false) }

    LaunchedEffect(visible, layer, density) {
        if (!visible) return@LaunchedEffect
        val sample = ImageBitmap(SampleWidth, SampleHeight)
        val canvas = Canvas(sample)
        val pixels = IntArray(SampleWidth * SampleHeight)
        val scope = CanvasDrawScope()
        while (true) {
            delay(120)
            val width = layer.size.width
            if (width == 0) continue
            val band = with(density) { BarHeight.toPx() }
            scope.draw(density, LayoutDirection.Ltr, canvas, Size(SampleWidth.toFloat(), SampleHeight.toFloat())) {
                // the screen behind the canvas is black, so whatever the app leaves transparent reads as black
                drawRect(Color.Black, blendMode = BlendMode.Src)
                scale(SampleWidth / width.toFloat(), SampleHeight / band, Offset.Zero) { drawLayer(layer) }
            }
            sample.readPixels(pixels)
            val luminance = pixels.sumOf { luminance(it) } / pixels.size
            // hysteresis, so a mid-grey background doesn't flicker between the two
            if (lightContent && luminance > 0.6) lightContent = false
            if (!lightContent && luminance < 0.4) lightContent = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize().drawWithContent {
                layer.record { this@drawWithContent.drawContent() }
                drawLayer(layer)
            },
        ) { content() }
        if (visible) {
            val color by animateColorAsState(if (lightContent) Color.White else Color.Black, tween(300))
            StatusBar(color)
        }
    }
}

@Composable
private fun StatusBar(color: Color) {
    Box(Modifier.fillMaxWidth().height(BarHeight)) {
        Box(Modifier.offset(24.dp, 21.dp).size(100.dp, 22.dp).padding(top = 1.5.dp), contentAlignment = Alignment.Center) {
            BasicText(
                "9:41",
                style = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                color = { color },
            )
        }
        Box(Modifier.offset(278.dp, 21.dp).size(100.dp, 22.dp)) {
            Level(Res.drawable.status_cellular, color, 10.665.dp, 5.387.dp, 19.2.dp, 12.226.dp)
            Level(Res.drawable.status_wifi, color, 36.865.dp, 5.336.dp, 17.142.dp, 12.328.dp)
            Level(Res.drawable.status_battery, color, 61.007.dp, 5.dp, 27.328.dp, 13.dp)
        }
    }
}

@Composable
private fun Level(icon: DrawableResource, color: Color, x: Dp, y: Dp, width: Dp, height: Dp) {
    Image(painterResource(icon), null, Modifier.offset(x, y).size(width, height), colorFilter = ColorFilter.tint(color, BlendMode.SrcIn))
}

private fun luminance(argb: Int): Double {
    val color = Color(argb)
    return 0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue
}
