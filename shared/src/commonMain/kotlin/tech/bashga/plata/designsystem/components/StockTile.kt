package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_microsoft
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * 136-high grey ticker tile, as wide as its grid cell (98 in the source), top-aligned: 56 [logo] 16 from the top, the 15 semibold ticker 11 below, then the trend in
 * 13 (3d1d2b9b). Sits on white cards.
 */
@Composable
fun StockTile(
    ticker: String,
    trend: String,
    direction: TrendDirection,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    logo: @Composable () -> Unit,
) {
    Column(
        modifier
            .height(136.dp)
            .tilePressable(onClick)
            .background(PlataTheme.colors.surfaceBackground, ContinuousRoundedShape(PlataTheme.radii.lg))
            .padding(top = PlataTheme.spacing.s16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        logo()
        Spacer(Modifier.height(11.dp))
        PlataText(ticker, PlataTheme.typography.subheadTightStrong, PlataTheme.colors.textPrimary, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        TrendLabel(trend, direction, textStyle = PlataTheme.typography.footnote)
    }
}

@Preview
@Composable
internal fun StockTilePreview() = PlataTheme {
    StockTile("MSFT", "1.72%", TrendDirection.Up, Modifier.width(98.dp)) {
        BrandLogo(painterResource(Res.drawable.raster_logo_microsoft), BrandLogoSize.S56, light = true)
    }
}
