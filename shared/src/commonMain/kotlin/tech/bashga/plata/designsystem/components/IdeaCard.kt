package tech.bashga.plata.designsystem.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_idea_vanguard
import plata.shared.generated.resources.raster_logo_vanguard
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.CardMotion
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.PressScaleState
import tech.bashga.plata.designsystem.foundation.ProgressiveBlurImage
import tech.bashga.plata.designsystem.foundation.pressScale
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/** What an [IdeaCard] reads as once grown into its page's header (cc4f1c47): the page's title, broken for the page. */
@Immutable
class IdeaCardPage(val title: String)

private val CardHeight = 232.dp
// the page header runs this far below the safe-area top, as MediaHeader's title variant does
private val PageHeightBelowSafeArea = 373.dp
private val CardTitleToTrend = 3.dp
private val PageTitleToTrend = 6.dp

/**
 * 232-tall photo card, radius 20, padding 20 (09c22ffa): [logo] (a 56 BrandLogo) top-left, glass [status] Pill top-right,
 * a white 20pt title (one or two lines) and an on-media TrendLabel pinned to the bottom. Pressed, it sinks; [pressState]
 * reads how far.
 *
 * The card is also its [page]'s header: at the window's width it is the full-bleed header under the status bar, the
 * logo above a 34pt title over the same photo and trend; at any width in between, every part sits where the two
 * layouts interpolate. Each title is laid out once, at the width it rests at, and only moves and scales on the way, so
 * its lines never rewrap. A page that grows out of the card draws this same card at its growing width, so the two
 * never crossfade.
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
    page: IdeaCardPage? = null,
    closing: Boolean = false,
    pressState: PressScaleState? = null,
    onClick: (() -> Unit)? = null,
) {
    val spacing = PlataTheme.spacing
    val typography = PlataTheme.typography
    val radii = PlataTheme.radii
    val safeTop = PlataSafeArea.current.top
    val windowWidth = LocalWindowInfo.current.containerSize.width
    // how far the card has grown into its page, from the width it is laid out at this frame: written as the card is
    // measured and read only while laying out and drawing, so every part keeps up with the bounds in the same frame
    val growth = remember { mutableFloatStateOf(0f) }
    fun grown() = growth.floatValue
    val pageTitle = page?.title
    // Swap At counts from where the motion starts: the card when opening, the page when closing
    val swapAt = if (closing) 1f - CardMotion.titleSwapAt else CardMotion.titleSwapAt
    val fadeLength = CardMotion.titleFadeLength
    val titleGrowth = typography.largeTitle.fontSize.value / typography.title3.fontSize.value
    Box(
        modifier
            // before the clip, so the rounded corners shrink with the card
            .then(if (onClick != null) Modifier.pressScale(state = pressState) else Modifier)
            .layout { measurable, constraints ->
                // only the pass that is drawn: the lookahead pass's width is where it lands, and two values a frame would
                // invalidate each other's readers forever
                if (page != null && !isLookingAhead) {
                    // the card's width is the window's less the feed's margins
                    val cardWidth = windowWidth - 2 * spacing.screenMargin.roundToPx()
                    growth.floatValue = ((constraints.maxWidth - cardWidth).toFloat() / (windowWidth - cardWidth).coerceAtLeast(1)).coerceIn(0f, 1f)
                }
                val height = lerp(CardHeight, safeTop + PageHeightBelowSafeArea, grown()).roundToPx()
                val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                layout(placeable.width, height) { placeable.place(0, 0) }
            }
            .graphicsLayer {
                shape = ContinuousRoundedShape(lerp(radii.xl, 0.dp, grown()))
                clip = true
            }
            .then(if (onClick != null) Modifier.pressable(indication = null, onClick = onClick) else Modifier),
    ) {
        // one photo on card and page alike: growing, only the card's blur along its bottom fades away
        if (page != null) Image(image, null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        ProgressiveBlurImage(image, Modifier.matchParentSize().graphicsLayer { alpha = 1f - grown() })
        if (status != null) {
            Pill(status, Modifier.align(Alignment.TopEnd).padding(spacing.cardPadding).graphicsLayer { alpha = 1f - grown() }, tone = PillTone.Glass)
        }
        Layout(
            content = {
                Box { logo?.invoke() }
                PlataText(
                    title,
                    typography.title3,
                    PlataTheme.colors.textOnDark,
                    Modifier.titleScale { lerp(1f, titleGrowth, grown()) to 1f - titleFade(grown(), swapAt, fadeLength) },
                )
                if (pageTitle != null) {
                    PlataText(
                        pageTitle,
                        typography.largeTitle,
                        PlataTheme.colors.textOnDark,
                        Modifier.titleScale { lerp(1f / titleGrowth, 1f, grown()) to titleFade(grown(), swapAt, fadeLength) },
                    )
                }
                TrendLabel(trendValue, trendDirection, tone = TrendLabelTone.OnMedia, caption = trendCaption)
            },
            modifier = Modifier.matchParentSize().padding(horizontal = spacing.cardPadding),
        ) { measurables, constraints ->
            val progress = grown()
            val loose = constraints.copy(minWidth = 0, minHeight = 0)
            val inset = 2 * spacing.cardPadding.roundToPx()
            val cardTextWidth = if (pageTitle == null) constraints.maxWidth else windowWidth - 2 * spacing.screenMargin.roundToPx() - inset
            val logoPlaceable = measurables[0].measure(loose)
            val cardTitle = measurables[1].measure(Constraints(maxWidth = cardTextWidth.coerceAtLeast(0)))
            val pageTitlePlaceable = if (pageTitle != null) measurables[2].measure(Constraints(maxWidth = (windowWidth - inset).coerceAtLeast(0))) else null
            val trend = measurables.last().measure(loose)
            val trendY = constraints.maxHeight - lerp(spacing.cardPadding, spacing.s24, progress).roundToPx() - trend.height
            val titleBottom = trendY - lerp(CardTitleToTrend, PageTitleToTrend, progress).roundToPx()
            val titleHeight = lerp(cardTitle.height.toFloat(), (pageTitlePlaceable?.height ?: cardTitle.height).toFloat(), progress)
            // on the card the logo hangs from the top corner; on the page it stands on the title
            val logoY = lerp(spacing.cardPadding.toPx(), titleBottom - titleHeight - spacing.s12.toPx() - logoPlaceable.height, progress)
            layout(constraints.maxWidth, constraints.maxHeight) {
                logoPlaceable.place(0, logoY.roundToInt())
                cardTitle.place(0, titleBottom - cardTitle.height)
                pageTitlePlaceable?.let { it.place(0, titleBottom - it.height) }
                trend.place(0, trendY)
            }
        }
    }
}

// how far the page's title has replaced the card's: an ease-in-out over [length] centred on [at], or a swap at [at]
private fun titleFade(grown: Float, at: Float, length: Float): Float {
    if (length <= 0f) return if (grown >= at) 1f else 0f
    return EaseInOutCubic.transform(((grown - at) / length + 0.5f).coerceIn(0f, 1f))
}

// grows from the title's first line's start along its last line, where it sits on the trend; [pose] is its scale and alpha
private fun Modifier.titleScale(pose: () -> Pair<Float, Float>): Modifier = graphicsLayer {
    val (scale, alpha) = pose()
    transformOrigin = TransformOrigin(0f, 1f)
    scaleX = scale
    scaleY = scale
    this.alpha = alpha
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
