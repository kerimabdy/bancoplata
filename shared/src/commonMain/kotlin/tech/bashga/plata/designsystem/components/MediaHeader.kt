package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_nike
import plata.shared.generated.resources.raster_logo_vanguard
import plata.shared.generated.resources.raster_media_partners
import plata.shared.generated.resources.raster_media_vanguard
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProgressiveBlurImage
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class MediaHeaderContent { Carousel, Title }

private val CarouselHeight = 420.dp
private val TitleHeightBelowSafeArea = 373.dp
// the OverMedia bar row centres its disc 28 down; under the status bar the source disc centres 17 below the safe-area top
private val NavStatusBarLift = 11.dp
private val CarouselBottom = 21.dp
private val TitleToTrend = 6.dp

/**
 * Full-bleed photo header. [background] fills it (a raster with its text painted out), with the glass back control
 * and the white [navTitle] on top.
 * - [MediaHeaderContent.Carousel] (816019bf, top of a page sheet, 420 high): [title] in 24 heavy uppercase, the
 *   [featured] PartnerCard 20 in, 16 below it, and [pageCount] PageDots 16 below that, 21 from the bottom.
 * - [MediaHeaderContent.Title] (cc4f1c47, under the status bar, safe area + 373 high): the 56 [logo], [title] 34 bold
 *   12 below it, the on-media [trend] 6 below, all 20 in and 24 from the bottom.
 */
@Composable
fun MediaHeader(
    title: String,
    modifier: Modifier = Modifier,
    content: MediaHeaderContent = MediaHeaderContent.Title,
    navTitle: String? = null,
    onBack: (() -> Unit)? = null,
    height: Dp = if (content == MediaHeaderContent.Carousel) CarouselHeight else PlataSafeArea.current.top + TitleHeightBelowSafeArea,
    logo: (@Composable () -> Unit)? = null,
    trend: BalanceTrend? = null,
    featured: (@Composable () -> Unit)? = null,
    pageCount: Int = 0,
    currentPage: Int = 0,
    background: @Composable BoxScope.() -> Unit,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val navTop = (PlataSafeArea.current.top - NavStatusBarLift).coerceAtLeast(0.dp)
    Box(modifier.fillMaxWidth().height(height).clipToBounds()) {
        background()
        Column(Modifier.fillMaxSize()) {
            NavigationBar(Modifier.padding(top = navTop), style = NavigationBarStyle.OverMedia, title = navTitle, onBack = onBack)
            Spacer(Modifier.weight(1f))
            when (content) {
                MediaHeaderContent.Carousel -> Column(
                    Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin).padding(bottom = CarouselBottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlataText(title.uppercase(), PlataTheme.typography.displaySmall, colors.textOnDark, textAlign = TextAlign.Center)
                    if (featured != null) {
                        Spacer(Modifier.height(spacing.s16))
                        featured()
                    }
                    if (pageCount > 1) {
                        Spacer(Modifier.height(spacing.s16))
                        PageDots(pageCount, currentPage)
                    }
                }
                MediaHeaderContent.Title -> Column(Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin).padding(bottom = spacing.s24)) {
                    if (logo != null) {
                        logo()
                        Spacer(Modifier.height(spacing.s12))
                    }
                    PlataText(title, PlataTheme.typography.largeTitle, colors.textOnDark)
                    if (trend != null) {
                        Spacer(Modifier.height(TitleToTrend))
                        TrendLabel(trend.value, trend.direction, tone = TrendLabelTone.OnMedia, caption = trend.caption)
                    }
                }
            }
        }
    }
}

/**
 * The carousel header as a real pager (816019bf → 8417fbd4): each page's [background], [title] (24 heavy uppercase) and
 * [featured] card swipe together under the fixed glass back control, [navTitle] and the [PageDots] of [pagerState].
 * Collapsing on scroll is the screen's: draw `NavigationBar(collapsed = rememberCollapsed(scroll))` over the page.
 */
@Composable
fun MediaHeader(
    pagerState: PagerState,
    title: (page: Int) -> String,
    modifier: Modifier = Modifier,
    navTitle: String? = null,
    onBack: (() -> Unit)? = null,
    height: Dp = CarouselHeight,
    featured: (@Composable (page: Int) -> Unit)? = null,
    background: @Composable BoxScope.(page: Int) -> Unit,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val navTop = (PlataSafeArea.current.top - NavStatusBarLift).coerceAtLeast(0.dp)
    Box(modifier.fillMaxWidth().height(height).clipToBounds()) {
        HorizontalPager(pagerState, Modifier.fillMaxSize()) { page ->
            Box(Modifier.fillMaxSize()) {
                background(page)
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = spacing.screenMargin)
                        .padding(bottom = CarouselBottom + PageDotsHeight + spacing.s16),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlataText(title(page).uppercase(), PlataTheme.typography.displaySmall, colors.textOnDark, textAlign = TextAlign.Center)
                    if (featured != null) {
                        Spacer(Modifier.height(spacing.s16))
                        featured(page)
                    }
                }
            }
        }
        NavigationBar(Modifier.padding(top = navTop), style = NavigationBarStyle.OverMedia, title = navTitle, onBack = onBack)
        PageDots(pagerState, Modifier.align(Alignment.BottomCenter).padding(bottom = CarouselBottom))
    }
}

private val PageDotsHeight = 8.dp

@Composable
private fun MediaBackground(image: DrawableResource) {
    ProgressiveBlurImage(painterResource(image), Modifier.fillMaxSize())
}

@Preview
@Composable
internal fun MediaHeaderCarouselPreview() = PlataTheme {
    MediaHeader(
        "Unleash your potential",
        Modifier.width(393.dp),
        content = MediaHeaderContent.Carousel,
        navTitle = "Partners",
        onBack = {},
        featured = {
            PartnerCard("Nike", "Sporting goods", "10%", Modifier.fillMaxWidth()) {
                BrandLogo(painterResource(Res.drawable.raster_logo_nike), BrandLogoSize.S40)
            }
        },
        pageCount = 9,
    ) { MediaBackground(Res.drawable.raster_media_partners) }
}

@Preview
@Composable
internal fun MediaHeaderPagerPreview() = PlataTheme {
    MediaHeader(
        rememberPagerState { 9 },
        title = { "Unleash your potential" },
        modifier = Modifier.width(393.dp),
        navTitle = "Partners",
        onBack = {},
        featured = {
            PartnerCard("Nike", "Sporting goods", "10%", Modifier.fillMaxWidth()) {
                BrandLogo(painterResource(Res.drawable.raster_logo_nike), BrandLogoSize.S40)
            }
        },
    ) { MediaBackground(Res.drawable.raster_media_partners) }
}

@Preview
@Composable
internal fun MediaHeaderTitlePreview() = PlataTheme {
    MediaHeader(
        "Vanguard FTSE\nEurope ETF",
        Modifier.width(393.dp),
        onBack = {},
        logo = { BrandLogo(painterResource(Res.drawable.raster_logo_vanguard), BrandLogoSize.S56) },
        trend = BalanceTrend("1.47%", TrendDirection.Up, "since advice release"),
    ) { MediaBackground(Res.drawable.raster_media_vanguard) }
}
