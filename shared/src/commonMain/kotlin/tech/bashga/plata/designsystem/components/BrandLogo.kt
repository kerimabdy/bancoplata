package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_mercadolibre
import plata.shared.generated.resources.raster_logo_microsoft
import plata.shared.generated.resources.raster_logo_santander
import plata.shared.generated.resources.raster_logo_vanguard
import tech.bashga.plata.designsystem.theme.PlataTheme

/** Badge top-left, diameter and ring per size from Figma (79:113); at 96 the badge overhangs the edge (372e6fd9). */
enum class BrandLogoSize(val diameter: Dp, val badgeX: Dp, val badgeY: Dp, val badge: Dp, val ring: Dp) {
    // inventory size=28; the source logo on 3d1d2b9b measures 32, badge geometry scaled from the Figma 28
    S32(32.dp, 23.4.dp, 22.1.dp, 9.1.dp, 1.1.dp),
    S40(40.dp, 29.dp, 27.33.dp, 12.dp, 1.dp),
    S56(56.dp, 41.dp, 38.67.dp, 16.dp, 1.dp),
    S72(72.dp, 52.5.dp, 49.5.dp, 21.dp, 1.5.dp),
    S96(96.dp, 68.dp, 64.dp, 32.dp, 3.5.dp),
}

/** A [light] mark (Microsoft) sits at half size on surface/logo; [ringColor] is the surface the badge cuts into. */
@Composable
fun BrandLogo(
    logo: Painter,
    size: BrandLogoSize,
    modifier: Modifier = Modifier,
    light: Boolean = false,
    showCheck: Boolean = false,
    ringColor: Color = PlataTheme.colors.surfaceBackground,
    contentDescription: String? = null,
) {
    val colors = PlataTheme.colors
    Box(modifier.size(size.diameter)) {
        Box(
            Modifier
                .size(size.diameter)
                .clip(CircleShape)
                .then(
                    if (light) {
                        Modifier.background(colors.surfaceLogo).border(1.dp, colors.borderSubtle, CircleShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                logo,
                contentDescription,
                Modifier.size(if (light) size.diameter / 2 else size.diameter),
                contentScale = if (light) ContentScale.Fit else ContentScale.Crop,
            )
        }
        if (showCheck) CheckBadge(size, ringColor, Modifier.offset(size.badgeX - size.ring, size.badgeY - size.ring))
    }
}

@Composable
private fun CheckBadge(size: BrandLogoSize, ring: Color, modifier: Modifier) {
    val colors = PlataTheme.colors
    val fill = colors.success
    val check = colors.textOnDark
    Canvas(modifier.size(size.badge + size.ring * 2)) {
        val ringPx = size.ring.toPx()
        val radius = size.badge.toPx() / 2
        val center = Offset(this.size.width / 2, this.size.height / 2)
        drawCircle(ring, radius + ringPx, center)
        drawCircle(fill, radius, center)
        // Figma's 12×10 check at stroke 2 is for the 32 disc; smaller discs scale it
        val unit = radius * 2 / 32f
        val path = Path().apply {
            moveTo(center.x - 6f * unit, center.y + 0.2f * unit)
            lineTo(center.x - 1.8f * unit, center.y + 4.4f * unit)
            lineTo(center.x + 6f * unit, center.y - 4.4f * unit)
        }
        drawPath(path, check, style = Stroke(width = 2f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Preview
@Composable
internal fun BrandLogo32Preview() = PlataTheme {
    BrandLogo(painterResource(Res.drawable.raster_logo_vanguard), BrandLogoSize.S32)
}

@Preview
@Composable
internal fun BrandLogo40Preview() = PlataTheme {
    BrandLogo(painterResource(Res.drawable.raster_logo_microsoft), BrandLogoSize.S40, light = true)
}

@Preview
@Composable
internal fun BrandLogo56Preview() = PlataTheme {
    BrandLogo(painterResource(Res.drawable.raster_logo_microsoft), BrandLogoSize.S56, light = true)
}

@Preview
@Composable
internal fun BrandLogo72Preview() = PlataTheme {
    BrandLogo(painterResource(Res.drawable.raster_logo_mercadolibre), BrandLogoSize.S72)
}

@Preview
@Composable
internal fun BrandLogo96CheckPreview() = PlataTheme {
    BrandLogo(painterResource(Res.drawable.raster_logo_santander), BrandLogoSize.S96, showCheck = true)
}
