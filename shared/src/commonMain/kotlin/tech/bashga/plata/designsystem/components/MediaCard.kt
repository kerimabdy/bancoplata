package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_media_unpacking
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * Dark card, radius 32, on the dark sheet (8397cca1, dd6c539e): the [image] slot runs edge to edge at the top, then a
 * centred 20pt semibold title and 15/18 body in white 80%, 40 above the bottom edge.
 */
@Composable
fun MediaCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    Column(
        modifier
            .clip(ContinuousRoundedShape(PlataTheme.radii.xxl))
            .background(colors.surfaceDarkCard),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        image()
        Column(
            Modifier.fillMaxWidth().padding(start = spacing.s20, end = spacing.s20, bottom = spacing.s40),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlataText(title, PlataTheme.typography.value, colors.textOnDark, textAlign = TextAlign.Center)
            Spacer(Modifier.height(spacing.s4))
            PlataText(body, PlataTheme.typography.subheadTight, colors.textOnDark.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
internal fun MediaCardPreview() = PlataTheme {
    MediaCard(
        "Unpacking",
        "Open the bag in a private place after the delivery to protect your card details",
        Modifier.fillMaxWidth(),
    ) {
        Image(
            painterResource(Res.drawable.raster_media_unpacking),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    }
}
