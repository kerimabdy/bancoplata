package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_map_preview
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.theme.PlataTheme

/** The map artwork carries its own pin, as in Figma. */
@Composable
fun MapPreview(map: Painter, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(
        map,
        contentDescription,
        modifier.fillMaxWidth().height(100.dp).clip(ContinuousRoundedShape(PlataTheme.radii.lg)),
        contentScale = ContentScale.Crop,
    )
}

@Preview
@Composable
internal fun MapPreviewPreview() = PlataTheme {
    MapPreview(painterResource(Res.drawable.raster_map_preview), Modifier.width(313.dp), contentDescription = "Map")
}
