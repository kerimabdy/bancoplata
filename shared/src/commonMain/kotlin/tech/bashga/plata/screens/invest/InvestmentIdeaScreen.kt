package tech.bashga.plata.screens.invest

import androidx.compose.animation.EnterExitState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.painterResource
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.BulletItem
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.IdeaCard
import tech.bashga.plata.designsystem.components.IdeaCardPage
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.ListRowSurface
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.foundation.CardMotion
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.LocalSharedVisibilityScope
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.expandingPage
import tech.bashga.plata.navigation.rememberExpandingPageOverscroll
import tech.bashga.plata.navigation.navSpring
import kotlin.math.roundToInt

@Immutable
data class InvestmentIdeaState(val idea: InvestmentIdea)

sealed interface InvestmentIdeaAction {
    data object OpenReport : InvestmentIdeaAction
    data object OpenFund : InvestmentIdeaAction
    data object Back : InvestmentIdeaAction
}

/** Opening an idea pushes its fund to the recents (VGK for the Vanguard idea). */
class InvestmentIdeaViewModel(repository: AppRepository, ideaId: String) : ViewModel() {
    val state = InvestmentIdeaState(InvestIdeas.idea(ideaId))

    init {
        repository.addRecent(state.idea.fund.ticker)
    }
}

@Composable
fun InvestmentIdeaScreen(ideaId: String, onFund: (String) -> Unit, onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { InvestmentIdeaViewModel(repository, ideaId) }
    val state = viewModel.state
    InvestmentIdeaContent(state, onAction = { action ->
        when (action) {
            InvestmentIdeaAction.OpenFund -> onFund(state.idea.fund.ticker)
            InvestmentIdeaAction.Back -> onBack()
            InvestmentIdeaAction.OpenReport -> Unit
        }
    })
}

// cc4f1c47: the heading 20 under the photo, the article 14.7 under the heading; the release date 64 under it, the fund
// row floats 21 above the inset. a4ac3ec6: the bullets 10.4 apart, the report 35.6 under them
private val HeadingTop = 20.dp
private val ArticleTop = 14.7.dp
private val ReleaseTop = 64.dp
private val ReleaseValueGap = 2.dp
private val BulletsTop = 28.dp
private val BulletGap = 10.4.dp
private val ReportTop = 35.6.dp
private val FloatingRowBottom = 21.dp
// scrolled to the end, the report card stops 130 above the home indicator, clear of the fund row (a4ac3ec6)
private val FeedBottom = 130.dp

// the back disc and fund row come in over the last stretch of the card growing into the page
private const val ChromeFadeFrom = 0.7f

// the glass back disc stays put over the photo and the article alike (cc4f1c47, a4ac3ec6): 17 under the safe-area top
private val BackLift = 11.dp

@Composable
fun InvestmentIdeaContent(state: InvestmentIdeaState, onAction: (InvestmentIdeaAction) -> Unit, modifier: Modifier = Modifier) {
    val idea = state.idea
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val margin = PlataTheme.spacing.screenMargin
    val scroll = rememberScrollState()
    // closing, the page rolls back to its top so its header lands on the card
    val closing = LocalSharedVisibilityScope.current?.transition?.targetState == EnterExitState.PostExit
    LaunchedEffect(closing) { if (closing) scroll.animateScrollTo(0, navSpring()) }
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val cardWidth = windowWidth - margin * 2
    val arriving = LocalSharedVisibilityScope.current?.transition?.currentState == EnterExitState.PreEnter
    var width by remember { mutableStateOf(if (arriving) cardWidth else windowWidth) }
    // how far the page has grown from the card: 0 at the card's width, 1 at the window's. The width is written as the page
    // is measured, so this is read only while drawing, never composing, to keep up with the bounds in the same frame
    fun expansion() = ((width - cardWidth) / (windowWidth - cardWidth)).coerceIn(0f, 1f)
    Box(
        modifier
            .expandingPage(ideaCardKey(idea.id))
            // the width it is measured at while it travels; the lookahead pass only knows where it ends up
            .layout { measurable, constraints ->
                if (!isLookingAhead) width = constraints.maxWidth.toDp()
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .fillMaxSize()
            .background(colors.surfaceBackground),
    ) {
        fun chrome() = ((expansion() - ChromeFadeFrom) / (1f - ChromeFadeFrom)).coerceIn(0f, 1f)
        Column(Modifier.fillMaxSize().testTag("idea-scroll").verticalScroll(scroll, rememberExpandingPageOverscroll())) {
            IdeaCard(
                idea.title,
                painterResource(idea.cardImage),
                formatPercent(idea.cardTrend),
                Modifier.fillMaxWidth(),
                logo = { BrandLogo(painterResource(idea.fund.logo), BrandLogoSize.S56) },
                page = IdeaCardPage(idea.headerTitle),
                closing = closing,
            )
            Column(
                Modifier
                    .graphicsLayer {
                        // hidden and lowered at the card's width, in place at the page's: rising in as it opens, sinking as it closes
                        val away = ((1f - expansion()) / CardMotion.contentFade).coerceIn(0f, 1f)
                        alpha = 1f - away
                        translationY = away * CardMotion.contentSlide.dp.toPx()
                    }
                    .atLandingWidth()
                    .padding(horizontal = margin),
            ) {
                Spacer(Modifier.height(HeadingTop))
                PlataText("We think it's time to invest", typography.title3, colors.textPrimary)
                Spacer(Modifier.height(ArticleTop))
                PlataText(idea.summary, typography.bodyRelaxed, colors.textPrimary)
                Spacer(Modifier.height(ReleaseTop))
                PlataText("Release date", typography.footnote, colors.textSecondary)
                Spacer(Modifier.height(ReleaseValueGap))
                PlataText(idea.releaseDate, typography.value.copy(fontWeight = FontWeight.Normal), colors.textPrimary)
                if (idea.bullets.isNotEmpty()) {
                    Spacer(Modifier.height(BulletsTop))
                    Column(verticalArrangement = Arrangement.spacedBy(BulletGap)) {
                        idea.bullets.forEach { BulletItem(it, textStyle = typography.bodyRelaxed) }
                    }
                }
                Spacer(Modifier.height(ReportTop))
                ListGroup {
                    ListRow(
                        idea.report,
                        onClick = { onAction(InvestmentIdeaAction.OpenReport) },
                        leading = { IconCircle(PlataIcons.Document, IconCircleSize.S40, tint = colors.textPrimary) },
                    )
                }
            }
            Spacer(Modifier.height(FeedBottom + PlataSafeArea.current.bottom))
        }
        NavigationBar(
            Modifier.padding(top = PlataSafeArea.current.top - BackLift).graphicsLayer { alpha = chrome() },
            style = NavigationBarStyle.OverMedia,
            onBack = { onAction(InvestmentIdeaAction.Back) },
        )
        FundRow(idea.fund, { onAction(InvestmentIdeaAction.OpenFund) }, Modifier.align(Alignment.BottomCenter).graphicsLayer { alpha = chrome() })
    }
}

/**
 * Lays the article out once, at the width the lookahead pass says the page lands at, and scales it to the width the
 * page has on the way, as the App Store's stories do: its lines never rewrap while the page grows or shrinks.
 */
private fun Modifier.atLandingWidth(): Modifier = approachLayout(isMeasurementApproachInProgress = { true }) { measurable, constraints ->
    val landing = lookaheadSize.width.coerceAtLeast(1)
    val placeable = measurable.measure(Constraints.fixedWidth(landing))
    val scale = constraints.maxWidth / landing.toFloat()
    layout(constraints.maxWidth, (placeable.height * scale).roundToInt()) {
        placeable.placeWithLayer(0, 0) {
            transformOrigin = TransformOrigin(0f, 0f)
            scaleX = scale
            scaleY = scale
        }
    }
}

/** The fund row floating above the article. */
@Composable
private fun FundRow(fund: Asset, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val margin = PlataTheme.spacing.screenMargin
    ListRow(
        fund.name,
        modifier
            .padding(start = margin, end = margin, bottom = PlataSafeArea.current.bottom + FloatingRowBottom)
            .fillMaxWidth()
            .plataShadow(PlataTheme.shadows.knob, ContinuousRoundedShape(PlataTheme.radii.xl)),
        subtitle = fund.ticker,
        showChevron = true,
        surface = ListRowSurface.Grey,
        onClick = onClick,
        leading = { BrandLogo(painterResource(fund.logo), BrandLogoSize.S40) },
    )
}
