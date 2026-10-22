package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.theme.PlataTheme

private val Dot = 8.dp
private val DotGap = 9.6.dp

private const val MAX_FULL_DOTS = 9

/** The 17.6 pitch is from 816019bf; like UIPageControl, past nine pages the last two of ten shrink to 6 and 4 (9e2d2593). */
@Composable
fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val visible = if (count > MAX_FULL_DOTS) MAX_FULL_DOTS + 1 else count
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(DotGap), verticalAlignment = Alignment.CenterVertically) {
        repeat(visible) { index ->
            val diameter: Dp = when {
                count <= MAX_FULL_DOTS -> Dot
                index == visible - 2 -> 6.dp
                index == visible - 1 -> 4.dp
                else -> Dot
            }
            Box(Modifier.size(Dot), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(diameter)
                        .background(
                            if (index == current.coerceIn(0, visible - 1)) colors.textOnDark else colors.textOnDark.copy(alpha = 0.5f),
                            CircleShape,
                        ),
                )
            }
        }
    }
}

/** Dots driven by a real pager: the current page follows the swipe. */
@Composable
fun PageDots(pagerState: PagerState, modifier: Modifier = Modifier) = PageDots(pagerState.pageCount, pagerState.currentPage, modifier)

@Preview
@Composable
internal fun PageDotsPreview() = PlataTheme { PageDots(count = 9, current = 0) }

@Preview
@Composable
internal fun PageDotsOverflowPreview() = PlataTheme { PageDots(count = 12, current = 0) }
