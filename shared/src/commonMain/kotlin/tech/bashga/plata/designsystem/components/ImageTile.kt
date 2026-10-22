package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_mercadolibre
import plata.shared.generated.resources.raster_logo_nike
import plata.shared.generated.resources.raster_tile_mercadolibre
import plata.shared.generated.resources.raster_tile_nike
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class ImageTileFooter { Minimum, Percent }

/**
 * 130×164 partner photo tile, radius 12, padding 12, 40 [logo] top-left. The name is 17 bold on 20pt lines (b50f7634,
 * b68fc03a: the inventory's 20pt measures 17), then the [value]: white 15pt "Min $5" (Minimum) or a white Pill "10%"
 * with a gradient label, 10 below (Percent).
 */
@Composable
fun ImageTile(
    name: String,
    value: String,
    image: Painter,
    modifier: Modifier = Modifier,
    footer: ImageTileFooter = ImageTileFooter.Minimum,
    logo: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    Box(
        modifier
            .size(130.dp, 164.dp)
            .clip(ContinuousRoundedShape(PlataTheme.radii.md))
            .pressable(onClick = onClick),
    ) {
        Image(image, null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        if (logo != null) Box(Modifier.padding(PlataTheme.spacing.s12)) { logo() }
        Column(Modifier.align(Alignment.BottomStart).padding(PlataTheme.spacing.s12)) {
            PlataText(name, typography.bodyCompact.copy(fontWeight = FontWeight.Bold), colors.textOnDark)
            when (footer) {
                // the source caps sit 10 under the name's baseline, 2 lower than the 20 boxes stack (b50f7634)
                ImageTileFooter.Minimum -> PlataText(value, typography.subhead, colors.textOnDark, Modifier.offset(y = 2.dp), maxLines = 1)
                ImageTileFooter.Percent -> {
                    Spacer(Modifier.height(10.dp))
                    Pill(value, tone = PillTone.White, labelStyle = typography.headline)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun ImageTileMinimumPreview() = PlataTheme {
    ImageTile(
        "Mercado Libre",
        "Min $5",
        painterResource(Res.drawable.raster_tile_mercadolibre),
        logo = { BrandLogo(painterResource(Res.drawable.raster_logo_mercadolibre), BrandLogoSize.S40) },
    )
}

@Preview
@Composable
internal fun ImageTilePercentPreview() = PlataTheme {
    ImageTile(
        "Nike",
        "10%",
        painterResource(Res.drawable.raster_tile_nike),
        footer = ImageTileFooter.Percent,
        logo = { BrandLogo(painterResource(Res.drawable.raster_logo_nike), BrandLogoSize.S40) },
    )
}
