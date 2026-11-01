package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_app_icon_festive
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlataWordmark
import tech.bashga.plata.designsystem.theme.PlataTheme

private val AppIconSize = 56.dp
private val AppIconRadius = 14.dp
private val RingWidth = 2.dp
private val RingGap = 3.dp

/**
 * App-icon choice (Settings, 59741a55): a 56 icon with radius 14 and a 13pt label 6 below. [selected] draws the 2pt
 * accent ring 3 outside the icon; it doesn't take layout space, so selected and plain options line up.
 */
@Composable
fun AppIconOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val accent = PlataTheme.colors.accent
    Column(
        modifier
            .width(72.dp)
            .then(if (onClick != null) Modifier.pressable(role = Role.RadioButton, onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(AppIconSize)
                .drawBehind {
                    if (!selected) return@drawBehind
                    val inset = (RingGap + RingWidth / 2).toPx()
                    val ringSize = Size(size.width + inset * 2, size.height + inset * 2)
                    val outline = ContinuousRoundedShape(AppIconRadius + RingGap + RingWidth / 2)
                        .createOutline(ringSize, layoutDirection, this)
                    translate(-inset, -inset) { drawOutline(outline, accent, style = Stroke(RingWidth.toPx())) }
                }
                .clip(ContinuousRoundedShape(AppIconRadius)),
        ) { icon() }
        PlataText(label, PlataTheme.typography.footnote, PlataTheme.colors.textPrimary, maxLines = 1)
    }
}

/** A raster app icon filling the 56 square. */
@Composable
fun AppIconImage(painter: Painter, contentDescription: String?) {
    Image(painter, contentDescription, Modifier.size(AppIconSize), contentScale = ContentScale.Crop)
}

/** The default PLATA app icon: the app-icon gradient with the white wordmark. */
@Composable
fun PlataAppIcon() {
    Box(Modifier.size(AppIconSize).background(PlataTheme.gradients.appIcon), contentAlignment = Alignment.Center) {
        Image(
            rememberVectorPainter(PlataIcons.PlataWordmark),
            contentDescription = "Plata",
            modifier = Modifier.size(44.8.dp, 6.72.dp),
            colorFilter = ColorFilter.tint(PlataTheme.colors.textOnDark),
        )
    }
}

@Preview
@Composable
internal fun AppIconOptionSelectedPreview() = PlataTheme {
    AppIconOption("Main", selected = true) { PlataAppIcon() }
}

@Preview
@Composable
internal fun AppIconOptionDefaultPreview() = PlataTheme {
    AppIconOption("Festive", selected = false) { AppIconImage(painterResource(Res.drawable.raster_app_icon_festive), "Festive") }
}
