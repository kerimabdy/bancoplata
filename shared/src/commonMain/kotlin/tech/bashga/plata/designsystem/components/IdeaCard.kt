package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_idea_vanguard
import plata.shared.generated.resources.raster_logo_vanguard
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.ProgressiveBlurImage
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * 232-tall photo card, radius 20, padding 20 (09c22ffa): [logo] (a 56 BrandLogo) top-left, glass [status] Pill top-right,
 * a white 20pt title (one or two lines) and an on-media TrendLabel pinned to the bottom.
 */
@Composable
fun IdeaCard(
    title: String,
    image: Painter,
    trendValue: String,
    modifier: Modifier = Modifier,
    trendDirection: TrendDirection = TrendDirection.Up,
    trendCaption: String? = "since advice release",
    status: String? = "Active",
    logo: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val spacing = PlataTheme.spacing
    Box(
        modifier
            .height(232.dp)
            .clip(ContinuousRoundedShape(PlataTheme.radii.xl))
            .pressable(onClick = onClick),
    ) {
        ProgressiveBlurImage(image, Modifier.matchParentSize())
        if (logo != null) Box(Modifier.padding(spacing.cardPadding)) { logo() }
        if (status != null) Pill(status, Modifier.align(Alignment.TopEnd).padding(spacing.cardPadding), tone = PillTone.Glass)
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(spacing.cardPadding)) {
            PlataText(title, PlataTheme.typography.title3, PlataTheme.colors.textOnDark)
            Spacer(Modifier.height(3.dp))
            TrendLabel(trendValue, trendDirection, tone = TrendLabelTone.OnMedia, caption = trendCaption)
        }
    }
}

@Preview
@Composable
internal fun IdeaCardPreview() = PlataTheme {
    IdeaCard(
        "Vanguard FTSE Europe ETF",
        painterResource(Res.drawable.raster_idea_vanguard),
        "0.38%",
        Modifier.fillMaxWidth(),
        logo = { BrandLogo(painterResource(Res.drawable.raster_logo_vanguard), BrandLogoSize.S56) },
    )
}
