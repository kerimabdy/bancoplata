package tech.bashga.plata.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class CardOverviewState { Active, Frozen }

/** Under the card: the wallet button and tiles, or the pending digital card's reveal button and About list (202bee50). */
sealed interface CardOverviewBody {
    data object Actions : CardOverviewBody

    /** [loading] spins the button while the details face shows its empty wells (7464b63b). */
    data class DigitalReveal(val loading: Boolean = false) : CardOverviewBody
}

private enum class OverviewHeader { Limit, Plain, Frozen, Cancelled }

private const val HeaderFadeMillis = 350

private val HeaderToCard = 47.dp
private val HintToButton = 48.dp
private val RevealButtonTop = 24.dp
private val AboutTop = 41.dp
private val AboutHeaderGap = 32.dp
private val AboutItemGap = 24.dp

@Composable
fun CardOverview(
    state: CardOverviewState,
    modifier: Modifier = Modifier,
    amount: String = "$3,500",
    caption: String = "No spending limit",
    card: CardArtKind = if (state == CardOverviewState.Frozen) CardArtKind.Frozen else CardArtKind.Physical,
    showHint: Boolean = true,
    onSetLimit: () -> Unit = {},
    onCardClick: (() -> Unit)? = null,
    onAddToWallet: () -> Unit = {},
    onFreeze: () -> Unit = {},
    onReissue: () -> Unit = {},
    onDiscard: () -> Unit = {},
) = CardOverview(
    product = if (card == CardArtKind.Digital || card == CardArtKind.Details) CardProduct.Digital else CardProduct.Physical,
    modifier = modifier,
    frozen = state == CardOverviewState.Frozen,
    face = if (card == CardArtKind.Details || card == CardArtKind.DetailsSilver) CardFace.Details else CardFace.Front,
    amount = amount,
    caption = caption,
    showHint = showHint,
    onSetLimit = onSetLimit,
    onCardClick = onCardClick,
    onAddToWallet = onAddToWallet,
    onFreeze = onFreeze,
    onReissue = onReissue,
    onDiscard = onDiscard,
)

/**
 * The card sheet's top block (3b3eaed7, 697dbf17, 202bee50): the limit header and the card 40 in from the edges, then
 * the [body]. [CardOverviewBody.Actions] is the hint, the black "Add to Apple Wallet" Button and three ActionTiles 20 in;
 * [frozen] swaps the header for the snowflake "Frozen" status, frosts the card and turns Freeze into Unfreeze.
 * [face] is the side the card shows (tap it to flip: pass [onCardClick]); [showHint] = false keeps the hint's space
 * empty, as on the details face (a634583b). [CardOverviewBody.DigitalReveal] has no limit action and shows
 * "Show digital card details" ([onShowDetails]) and the About list instead. [cardNumber], [expires] and [cvc] fill the
 * details face once revealed.
 */
@Composable
fun CardOverview(
    product: CardProduct,
    modifier: Modifier = Modifier,
    frozen: Boolean = false,
    face: CardFace = CardFace.Front,
    body: CardOverviewBody = CardOverviewBody.Actions,
    amount: String = "$3,500",
    caption: String = "No spending limit",
    showHint: Boolean = face == CardFace.Front,
    onSetLimit: () -> Unit = {},
    onCardClick: (() -> Unit)? = null,
    onAddToWallet: () -> Unit = {},
    onFreeze: () -> Unit = {},
    onReissue: () -> Unit = {},
    onDiscard: () -> Unit = {},
    onShowDetails: () -> Unit = {},
    cardNumber: String? = null,
    expires: String? = null,
    cvc: String? = null,
    cancelled: Boolean = false,
    number: String = "••••",
) {
    val spacing = PlataTheme.spacing
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val headerModifier = Modifier.fillMaxWidth().padding(horizontal = spacing.s40)
        val header = when {
            cancelled -> OverviewHeader.Cancelled
            frozen -> OverviewHeader.Frozen
            body is CardOverviewBody.DigitalReveal -> OverviewHeader.Plain
            else -> OverviewHeader.Limit
        }
        Crossfade(header, animationSpec = tween(HeaderFadeMillis), label = "header") { shown ->
            when (shown) {
                OverviewHeader.Cancelled -> BalanceHeader("Cancelled", headerModifier, size = BalanceHeaderSize.Medium, caption = "No operations can be performed")
                OverviewHeader.Frozen -> BalanceHeader("Frozen", headerModifier, size = BalanceHeaderSize.Status, caption = "Charges can’t be made to your card")
                OverviewHeader.Plain -> BalanceHeader(amount, headerModifier, size = BalanceHeaderSize.Medium, caption = caption)
                OverviewHeader.Limit -> BalanceHeader(
                    amount,
                    headerModifier,
                    size = BalanceHeaderSize.Medium,
                    caption = caption,
                    trailing = { TextButton("Set limit", onSetLimit, weight = TextButtonWeight.Semibold, size = TextButtonSize.Subhead) },
                )
            }
        }
        Spacer(Modifier.height(HeaderToCard))
        CardArt(product, face = face, frozen = frozen, cardNumber = cardNumber, expires = expires, cvc = cvc, onClick = onCardClick, cancelled = cancelled, number = number)
        when (body) {
            CardOverviewBody.Actions -> {
                Spacer(Modifier.height(spacing.s24))
                HintLabel("Swipe or tap to see details", Modifier.alpha(if (showHint) 1f else 0f))
                Spacer(Modifier.height(HintToButton))
                Column(Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.s20)) {
                    Button("Add to Apple Wallet", onAddToWallet, Modifier.fillMaxWidth(), style = ButtonStyle.Black, icon = PlataIcons.Wallet)
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.s20)) {
                        val tile = Modifier.weight(1f)
                        Crossfade(frozen, tile, animationSpec = tween(HeaderFadeMillis), label = "freeze tile") { isFrozen ->
                            // fillMaxWidth keeps the weighted cell width, which a Crossfade box would otherwise relax to 104
                            if (isFrozen) {
                                ActionTile("Unfreeze", PlataIcons.Unfreeze, Modifier.fillMaxWidth(), layout = ActionTileLayout.Third, onClick = onFreeze)
                            } else {
                                ActionTile("Freeze", PlataIcons.Snowflake, Modifier.fillMaxWidth(), layout = ActionTileLayout.Third, onClick = onFreeze)
                            }
                        }
                        ActionTile("Reissue", PlataIcons.Refresh, tile, layout = ActionTileLayout.Third, onClick = onReissue)
                        ActionTile("Discard", PlataIcons.Discard, tile, layout = ActionTileLayout.Third, onClick = onDiscard)
                    }
                }
            }
            is CardOverviewBody.DigitalReveal -> {
                Spacer(Modifier.height(RevealButtonTop))
                Button(
                    "Show digital card details",
                    onShowDetails,
                    Modifier.padding(horizontal = spacing.s40).fillMaxWidth(),
                    loading = body.loading,
                )
                Spacer(Modifier.height(AboutTop))
                SectionCard(
                    Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth(),
                    title = "About digital card",
                    headerGap = AboutHeaderGap,
                    bodySpacing = AboutItemGap,
                    contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = spacing.s24),
                ) {
                    listOf("Works just like physical card", "Safer for online purchases", "Refresh card details with a tap").forEachIndexed { index, text ->
                        BulletItem(text, marker = BulletMarker.Number, number = index + 1, markerAlignment = Alignment.CenterVertically)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun CardOverviewActivePreview() = PlataTheme {
    CardOverview(CardOverviewState.Active, Modifier.width(393.dp))
}

@Preview
@Composable
internal fun CardOverviewFrozenPreview() = PlataTheme {
    CardOverview(CardOverviewState.Frozen, Modifier.width(393.dp))
}

@Preview
@Composable
internal fun CardOverviewDetailsPreview() = PlataTheme {
    CardOverview(CardOverviewState.Active, Modifier.width(393.dp), card = CardArtKind.DetailsSilver, showHint = false)
}

@Preview
@Composable
internal fun CardOverviewDigitalRevealPreview() = PlataTheme {
    CardOverview(CardProduct.Digital, Modifier.width(393.dp), body = CardOverviewBody.DigitalReveal(), amount = "$3,450")
}

@Preview
@Composable
internal fun CardOverviewDigitalLoadingPreview() = PlataTheme {
    CardOverview(CardProduct.Digital, Modifier.width(393.dp), face = CardFace.Details, body = CardOverviewBody.DigitalReveal(loading = true), amount = "$3,450")
}
