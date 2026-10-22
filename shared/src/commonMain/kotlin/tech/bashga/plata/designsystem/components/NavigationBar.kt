package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.ChevronBack
import tech.bashga.plata.designsystem.icons.ChevronLeftHeavy
import tech.bashga.plata.designsystem.icons.MsiBadge
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * Standard / Progress / Large sit under the status bar (44 rows); Sheet / SheetDark / OverMedia sit at the top of a
 * sheet (54 rows). SheetDark and OverMedia draw white ink; the sheet or photo behind them is the caller's.
 */
enum class NavigationBarStyle { Standard, Sheet, SheetDark, Progress, Large, OverMedia }

private val NavigationBarStyle.isSheet: Boolean
    get() = this == NavigationBarStyle.Sheet || this == NavigationBarStyle.SheetDark || this == NavigationBarStyle.OverMedia

// iOS draws the bar from the status-bar bottom (54), 5pt above the safe-area top (59).
private val StatusBarOverlap = 5.dp

// Sheet bars centre their content 28 below the sheet top (40d2b403, 34ec0c7a, 8417fbd4), 1 below the 54 row's middle.
private val SheetContentTop = 2.dp

// Back chevron frames (24×26): x 6 on standard and sheet bars (11cfd731, 80971162), x 14 on the progress bar (7ab581a2, 8de66942).
private val BackStandardX = 6.dp
private val BackProgressX = 14.dp

/** Large: the 34 bold title row under the 44 bar row (083f8017: bar 54–144). */
private val LargeTitleRow = 46.dp

// the 41 line box sits 3 into that row so the large title's caps land where 083f8017 has them
private val LargeTitleTop = 3.dp

// centred content keeps clear of the bar buttons: 16 margin plus a 56 control slot each side, as UINavigationBar does
private val CenterInset = 72.dp

// the 13/18 and 15/20 lines overlap 2 so the two-line title fits the 44 row (537a4b5a, 5f6bf512)
private val TwoLineOverlap = (-2).dp

/**
 * Top bar. [title] is centred (17 semibold), or the 34 bold large title for [NavigationBarStyle.Large]. With [subtitle]
 * the centre becomes two lines: [title] 13 regular over [subtitle] 15 semibold in [subtitleColor] (537a4b5a, 5f6bf512).
 * [onBack] draws the back control for the style (accent chevron, glass disc over media); otherwise [leading] holds a
 * TextButton (Close, Cancel). [center] replaces the title (ProgressBar, Avatar, BrandLogo, IconCircle). [trailing] takes
 * a TextButton ("Close", "By VestFi ⓘ").
 * [material] is the translucent scroll-edge fill; with [includeStatusBar] the bar adds the status-bar band above itself
 * (safe-area top minus 5), so the material runs up to the screen edge and the bar row lands at y 54 on an iPhone.
 * [divider] is the one-pixel hairline under the bar (537a4b5a). The source blurs what scrolls under the material;
 * Compose has no backdrop blur, so the material is the translucent fill only.
 * [collapsed] is the scrolled state: a [NavigationBarStyle.Large] bar drops its large-title row and centres [title]
 * inline, and every style takes the material. Derive it with [rememberCollapsed].
 */
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    style: NavigationBarStyle = NavigationBarStyle.Standard,
    title: String? = null,
    subtitle: String? = null,
    subtitleColor: Color = PlataTheme.colors.textPrimary,
    onBack: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    center: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    material: Boolean = false,
    includeStatusBar: Boolean = false,
    divider: Boolean = false,
    collapsed: Boolean = false,
) {
    val filled = material || collapsed
    val large = style == NavigationBarStyle.Large && !collapsed
    val colors = PlataTheme.colors
    val sizes = PlataTheme.sizes
    val spacing = PlataTheme.spacing
    val onDark = style == NavigationBarStyle.SheetDark || style == NavigationBarStyle.OverMedia
    val ink = if (onDark) colors.textOnDark else colors.textPrimary
    val statusBand: Dp = if (includeStatusBar && !style.isSheet) {
        (PlataSafeArea.current.top - StatusBarOverlap).coerceAtLeast(0.dp)
    } else {
        0.dp
    }
    val separator = colors.navSeparator
    Column(
        modifier
            .fillMaxWidth()
            .then(if (filled) Modifier.background(colors.surfaceNavMaterial) else Modifier)
            .then(
                if (divider) {
                    Modifier.drawBehind {
                        val y = size.height - 0.5f
                        drawLine(separator, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }
                } else {
                    Modifier
                },
            )
            .padding(top = statusBand),
    ) {
        val rowHeight = if (style.isSheet) sizes.navSheet else sizes.nav
        val contentTop = if (style.isSheet) SheetContentTop else 0.dp
        Box(Modifier.fillMaxWidth().height(rowHeight).padding(top = contentTop)) {
            if (onBack != null) {
                NavigationBarBack(style, onBack)
            } else if (leading != null) {
                Box(Modifier.align(Alignment.CenterStart).padding(start = spacing.s16)) { leading() }
            }
            val centerContent: (@Composable () -> Unit)? = when {
                center != null -> center
                large || title == null -> null
                subtitle != null -> { { NavigationBarTwoLineTitle(title, subtitle, ink, subtitleColor) } }
                else -> { { PlataText(title, PlataTheme.typography.headline, ink, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
            }
            if (centerContent != null) {
                Box(Modifier.align(Alignment.Center).padding(horizontal = CenterInset)) { centerContent() }
            }
            if (trailing != null) {
                Box(Modifier.align(Alignment.CenterEnd).padding(end = spacing.s16)) { trailing() }
            }
        }
        if (large && title != null) {
            Box(Modifier.fillMaxWidth().height(LargeTitleRow).padding(horizontal = spacing.s16)) {
                PlataText(
                    title,
                    PlataTheme.typography.largeTitle,
                    colors.textPrimaryTitle,
                    Modifier.align(Alignment.TopStart).offset(y = LargeTitleTop),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * True once [scrollState] has moved more than [threshold]: the bar takes its material and a large title goes inline
 * (f5db1665, 59741a55, 0b2e699a). Reads as a derived state, so the caller recomposes only when it flips.
 */
@Composable
fun rememberCollapsed(scrollState: ScrollState, threshold: Dp = 1.dp): Boolean {
    val px = with(LocalDensity.current) { threshold.toPx() }
    return remember(scrollState, px) { derivedStateOf { scrollState.value > px } }.value
}

@Composable
fun rememberCollapsed(listState: LazyListState, threshold: Dp = 1.dp): Boolean {
    val px = with(LocalDensity.current) { threshold.toPx() }
    return remember(listState, px) {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > px }
    }.value
}

@Composable
private fun BoxScope.NavigationBarBack(style: NavigationBarStyle, onBack: () -> Unit) {
    if (style == NavigationBarStyle.OverMedia) {
        IconButton(
            PlataIcons.ChevronLeftHeavy,
            onBack,
            Modifier.align(Alignment.CenterStart).padding(start = PlataTheme.spacing.s16),
            style = IconButtonStyle.Glass,
            size = IconButtonSize.S32,
            contentDescription = "Back",
        )
        return
    }
    val sizes = PlataTheme.sizes
    val x = if (style == NavigationBarStyle.Progress) BackProgressX else BackStandardX
    val tint = if (style == NavigationBarStyle.SheetDark) PlataTheme.colors.textOnDark else PlataTheme.colors.accent
    // the chevron frame sits 1pt above the row's centre, as on 11cfd731
    Box(
        Modifier
            .align(Alignment.CenterStart)
            .padding(start = x - 6.dp)
            .size(sizes.nav)
            .pressable(onClickLabel = "Back", onClick = onBack),
    ) {
        Icon(PlataIcons.ChevronBack, Modifier.align(Alignment.CenterStart).offset(6.dp, (-1).dp), size = sizes.iconMedium, tint = tint)
    }
}

@Composable
private fun NavigationBarTwoLineTitle(title: String, subtitle: String, titleColor: Color, subtitleColor: Color) {
    val typography = PlataTheme.typography
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TwoLineOverlap)) {
        PlataText(title, typography.footnote, titleColor, maxLines = 1, textAlign = TextAlign.Center)
        PlataText(subtitle, typography.subheadStrong, subtitleColor, maxLines = 1, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
internal fun NavigationBarStandardPreview() = PlataTheme {
    NavigationBar(title = "Popular stocks", onBack = {})
}

@Preview
@Composable
internal fun NavigationBarSheetPreview() = PlataTheme {
    NavigationBar(
        style = NavigationBarStyle.Sheet,
        title = "Between accounts",
        leading = { TextButton("Cancel", {}) },
    )
}

@Preview
@Composable
internal fun NavigationBarSheetDarkPreview() = PlataTheme {
    Box(Modifier.background(PlataTheme.colors.surfaceSheetDark)) {
        NavigationBar(
            style = NavigationBarStyle.SheetDark,
            leading = { TextButton("Close", {}, tone = TextButtonTone.OnDark) },
        )
    }
}

@Preview
@Composable
internal fun NavigationBarProgressPreview() = PlataTheme {
    NavigationBar(
        style = NavigationBarStyle.Progress,
        onBack = {},
        center = { ProgressBar(0.25f, Modifier.width(140.dp), size = ProgressBarSize.Nav) },
        trailing = { TextButton("Close", {}) },
    )
}

@Preview
@Composable
internal fun NavigationBarLargePreview() = PlataTheme {
    NavigationBar(
        style = NavigationBarStyle.Large,
        title = "Investments",
        trailing = {
            TextButton("By VestFi", {}, tone = TextButtonTone.Secondary, size = TextButtonSize.Subhead, trailingIcon = PlataIcons.Info)
        },
    )
}

@Preview
@Composable
internal fun NavigationBarOverMediaPreview() = PlataTheme {
    Box(Modifier.background(PlataTheme.colors.surfaceDark)) {
        NavigationBar(style = NavigationBarStyle.OverMedia, title = "Partners", onBack = {})
    }
}

@Preview
@Composable
internal fun NavigationBarMaterialPreview() = PlataTheme {
    NavigationBar(title = "Transactions", onBack = {}, material = true, includeStatusBar = true)
}

@Preview
@Composable
internal fun NavigationBarLargeCollapsedPreview() = PlataTheme {
    NavigationBar(style = NavigationBarStyle.Large, title = "Investments", includeStatusBar = true, collapsed = true)
}

@Preview
@Composable
internal fun NavigationBarTwoLinePreview() = PlataTheme {
    NavigationBar(title = "Investment portfolio", subtitle = "USD 2.01", onBack = {}, divider = true)
}

@Preview
@Composable
internal fun NavigationBarSheetTwoLinePreview() = PlataTheme {
    NavigationBar(
        style = NavigationBarStyle.Sheet,
        title = "Bring a friend",
        subtitle = "Get $3,000 for 3 friends",
        subtitleColor = PlataTheme.colors.brandPrimary,
        leading = { TextButton("Close", {}) },
        material = true,
    )
}

@Preview
@Composable
internal fun NavigationBarCenterIconPreview() = PlataTheme {
    NavigationBar(onBack = {}, center = { Icon(PlataIcons.MsiBadge, size = 36.dp) })
}
