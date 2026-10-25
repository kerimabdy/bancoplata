package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import tech.bashga.plata.designsystem.foundation.ContinuousTopRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.foundation.SafeAreaInsets
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class PageSheetBackdrop { Stacked, Dimmed }

private val SheetDrop = 10.dp

/**
 * iOS page-sheet presentation, full screen. The sheet starts 10 below the safe-area top with 10pt top corners.
 * [PageSheetBackdrop.Stacked]: black backdrop with the presenting screen as a card peeking above the sheet, 16 in from the
 * edges (73a046d0, 2be9ff97, e97d2568). [PageSheetBackdrop.Dimmed]: the [presenter] stays full size under a dim
 * (3b3eaed7). The [content] fills the sheet and sees a safe area whose top is the sheet's top edge.
 */
@Composable
fun PageSheet(
    modifier: Modifier = Modifier,
    backdrop: PageSheetBackdrop = PageSheetBackdrop.Stacked,
    sheetColor: Color = PlataTheme.colors.surfaceBackground,
    stackedColor: Color = PlataTheme.colors.surfaceSheetStacked,
    presenter: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = PlataTheme.colors
    val radius = PlataTheme.radii.sm
    val safeArea = PlataSafeArea.current
    Box(modifier.fillMaxSize().background(colors.sheetBackdrop)) {
        when (backdrop) {
            PageSheetBackdrop.Stacked -> Box(
                Modifier
                    .padding(start = PlataTheme.spacing.s16, end = PlataTheme.spacing.s16, top = safeArea.top)
                    .fillMaxWidth()
                    .height(SheetDrop + radius)
                    .background(stackedColor, ContinuousTopRoundedShape(radius)),
            )
            PageSheetBackdrop.Dimmed -> {
                presenter?.invoke(this)
                Box(Modifier.fillMaxSize().background(colors.sheetDim))
            }
        }
        PageSheetSurface(color = sheetColor, content = content)
    }
}

/**
 * The sheet itself, for presenters that draw the backdrop elsewhere (the nav host, overlays): it starts 10 below the
 * safe-area top with 10pt top corners, and [content] sees a safe area whose top is the sheet's top edge.
 */
@Composable
fun PageSheetSurface(
    modifier: Modifier = Modifier,
    color: Color = PlataTheme.colors.surfaceBackground,
    content: @Composable BoxScope.() -> Unit,
) {
    val safeArea = PlataSafeArea.current
    Box(
        modifier
            .padding(top = safeArea.top + SheetDrop)
            .fillMaxSize()
            .clip(ContinuousTopRoundedShape(PlataTheme.radii.sm))
            .background(color),
    ) {
        ProvideSafeArea(SafeAreaInsets(top = 0.dp, bottom = safeArea.bottom)) { content() }
    }
}

/** Where a [PageSheetSurface] starts: the presenter's card top when the presenter is itself a sheet. */
val PageSheetTop: Dp
    @Composable @ReadOnlyComposable get() = PlataSafeArea.current.top + SheetDrop

/**
 * The live presenter under a page sheet. [stacked] (0–1) shrinks it into the card a Stacked sheet leaves visible above
 * itself: 16 in from the sides with its top at the safe-area top, its corners going from the device's to the card's.
 * The black around the card is the app background, so it never fades in. [dimmed] (0–1) keeps it full size under
 * the sheet dim instead. [cardTop] is where the presenter's own card starts: 0 for a full-screen page,
 * [PageSheetTop] when the presenter is a sheet.
 */
@Composable
fun SheetPresenter(
    stacked: Float,
    dimmed: Float,
    modifier: Modifier = Modifier,
    cardTop: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val colors = PlataTheme.colors
    val inset = PlataTheme.spacing.s16
    val radius = PlataTheme.radii.sm
    val safeTop = PlataSafeArea.current.top
    val deviceRadius = PlataTheme.radii.device
    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1f - stacked * (2 * inset.toPx() / size.width)
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    translationY = stacked * (safeTop.toPx() - scale * cardTop.toPx())
                    shape = ContinuousTopRoundedShape(lerp(deviceRadius, radius, stacked) / scale)
                    // at rest the presenter is the full screen, unclipped
                    clip = cardTop == 0.dp && stacked > 0f
                },
        ) {
            content()
            Box(Modifier.fillMaxSize().background(colors.stackedPresenterDim.copy(alpha = colors.stackedPresenterDim.alpha * stacked)))
        }
        Box(Modifier.fillMaxSize().background(colors.sheetDim.copy(alpha = colors.sheetDim.alpha * dimmed)))
    }
}

@Preview
@Composable
internal fun PageSheetStackedPreview() = PlataTheme {
    ProvideSafeArea(PlataSafeArea.IPhone) {
        PageSheet(Modifier.size(393.dp, 852.dp), sheetColor = PlataTheme.colors.surfaceCard) {}
    }
}

@Preview
@Composable
internal fun PageSheetDimmedPreview() = PlataTheme {
    ProvideSafeArea(PlataSafeArea.IPhone) {
        PageSheet(
            Modifier.size(393.dp, 852.dp),
            backdrop = PageSheetBackdrop.Dimmed,
            presenter = { Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) },
        ) {}
    }
}
