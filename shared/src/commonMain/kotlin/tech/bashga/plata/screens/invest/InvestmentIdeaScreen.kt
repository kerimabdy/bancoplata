package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.painterResource
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.BalanceTrend
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.BulletItem
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.ListRowSurface
import tech.bashga.plata.designsystem.components.MediaHeader
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TrendDirection
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

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

// the glass back disc stays put over the photo and the article alike (cc4f1c47, a4ac3ec6): 17 under the safe-area top
private val BackLift = 11.dp

@Composable
fun InvestmentIdeaContent(state: InvestmentIdeaState, onAction: (InvestmentIdeaAction) -> Unit, modifier: Modifier = Modifier) {
    val idea = state.idea
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val margin = PlataTheme.spacing.screenMargin
    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("idea-scroll").verticalScroll(scroll)) {
            MediaHeader(
                idea.headerTitle,
                Modifier.fillMaxWidth(),
                logo = { BrandLogo(painterResource(idea.fund.logo), BrandLogoSize.S56) },
                trend = BalanceTrend(formatPercent(idea.pageTrend), TrendDirection.Up, "since advice release"),
            ) {
                Image(painterResource(idea.headerImage), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(Modifier.padding(horizontal = margin)) {
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
        NavigationBar(Modifier.padding(top = PlataSafeArea.current.top - BackLift), style = NavigationBarStyle.OverMedia, onBack = { onAction(InvestmentIdeaAction.Back) })
        FundRow(idea.fund, { onAction(InvestmentIdeaAction.OpenFund) }, Modifier.align(Alignment.BottomCenter))
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
