package tech.bashga.plata.screens.invest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tech.bashga.plata.designsystem.components.AmountRow
import tech.bashga.plata.designsystem.components.AmountSecondaryTone
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.StockTile
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonSize
import tech.bashga.plata.designsystem.components.TextButtonTone
import tech.bashga.plata.designsystem.components.TrendDirection
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

// asset rows sit on a 60 pitch, 17.7 under the card title and 10 above the card's bottom (5ab1e0c9, fef10b1d)
private val AssetRowHeight = 60.dp
private val AssetHeaderGap = 17.7.dp
private val AssetCardBottom = 10.dp
private val TileRowGap = 8.dp
private val TileColumnGap = 9.5.dp

@Composable
internal fun AssetLogo(asset: Asset, size: BrandLogoSize) {
    BrandLogo(painterResource(asset.logo), size, light = asset.light)
}

/** "By VestFi ⓘ", the partner credit at the top right of every Investments bar. */
@Composable
internal fun VestFiCredit(onClick: () -> Unit) {
    TextButton("By VestFi", onClick, tone = TextButtonTone.Secondary, size = TextButtonSize.Subhead, trailingIcon = PlataIcons.Info)
}

/** A card of StockTiles, three to a row, under a "See all" header; [header] sits between the title and the tiles. */
@Composable
internal fun ListingTileCard(
    title: String,
    listings: List<Listing>,
    onSeeAll: (() -> Unit)?,
    onListing: (String) -> Unit,
    modifier: Modifier = Modifier,
    headerGap: Dp = PlataTheme.spacing.s20,
    header: (@Composable () -> Unit)? = null,
) {
    SectionCard(
        modifier,
        title = title,
        headerTrailing = onSeeAll?.let { { SectionCardAction("See all", it) } },
        headerGap = headerGap,
    ) {
        if (header != null) {
            header()
            Spacer(Modifier.height(PlataTheme.spacing.s20))
        }
        Column(verticalArrangement = Arrangement.spacedBy(TileRowGap)) {
            listings.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TileColumnGap)) {
                    row.forEach { listing ->
                        StockTile(listing.ticker, listing.quote.changeLabel, listing.quote.direction, Modifier.weight(1f), onClick = { onListing(listing.ticker) }) {
                            AssetLogo(listing.asset, BrandLogoSize.S56)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** One 60-pitch row: logo, name over ticker, price over the change. */
@Composable
internal fun ListingRow(listing: Listing, onClick: () -> Unit, modifier: Modifier = Modifier, horizontalPadding: Dp = 0.dp, minHeight: Dp = AssetRowHeight) {
    AmountRow(
        listing.asset.name,
        listing.quote.priceLabel,
        modifier.fillMaxWidth(),
        subtitle = listing.ticker,
        secondary = listing.quote.changeLabel,
        secondaryTone = listing.quote.direction.tone,
        horizontalPadding = horizontalPadding,
        onClick = onClick,
        minHeight = minHeight,
    ) { AssetLogo(listing.asset, BrandLogoSize.S40) }
}

internal val TrendDirection.tone: AmountSecondaryTone
    get() = if (this == TrendDirection.Up) AmountSecondaryTone.Positive else AmountSecondaryTone.Negative

/** A card of ListingRows (Commodities ETFs, All stocks). */
@Composable
internal fun ListingRowCard(title: String, listings: List<Listing>, onSeeAll: () -> Unit, onListing: (String) -> Unit, modifier: Modifier = Modifier) {
    SectionCard(
        modifier,
        title = title,
        headerTrailing = { SectionCardAction("See all", onSeeAll) },
        headerGap = AssetHeaderGap,
        contentPadding = PaddingValues(
            start = PlataTheme.spacing.cardPadding,
            top = PlataTheme.spacing.cardPadding,
            end = PlataTheme.spacing.cardPadding,
            bottom = AssetCardBottom,
        ),
    ) {
        listings.forEach { listing -> ListingRow(listing, onClick = { onListing(listing.ticker) }) }
    }
}
