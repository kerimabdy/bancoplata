package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlataWordmark
import tech.bashga.plata.designsystem.theme.PlataTheme

@Composable
fun IconTile(
    modifier: Modifier = Modifier,
    fill: Brush = SolidColor(PlataTheme.colors.brandPrimary),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = ContinuousRoundedShape(PlataTheme.radii.sm)
    Box(
        modifier.size(PlataTheme.sizes.buttonSmall).clip(shape).background(fill, shape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun IconTile(
    glyph: ImageVector,
    modifier: Modifier = Modifier,
    fill: Brush = SolidColor(PlataTheme.colors.brandPrimary),
    tint: Color = PlataTheme.colors.textOnDark,
) = IconTile(modifier, fill) { Icon(glyph, size = PlataTheme.sizes.iconMedium, tint = tint) }

@Composable
fun IconTile(
    logo: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) = IconTile(modifier) { Image(logo, contentDescription, Modifier.size(PlataTheme.sizes.buttonSmall), contentScale = ContentScale.Crop) }

@Preview
@Composable
internal fun IconTileLogoPreview() = PlataTheme {
    IconTile(fill = PlataTheme.gradients.appIcon) {
        Image(
            rememberVectorPainter(PlataIcons.PlataWordmark),
            contentDescription = "Plata",
            modifier = Modifier.size(32.dp, 4.8.dp),
            colorFilter = ColorFilter.tint(PlataTheme.colors.textOnDark),
        )
    }
}

@Preview
@Composable
internal fun IconTileGlyphPreview() = PlataTheme { IconTile(PlataIcons.Percent, fill = PlataTheme.gradients.categoryOrange) }
