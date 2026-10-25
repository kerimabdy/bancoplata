package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_chart_portfolio_donut
import plata.shared.generated.resources.raster_lesson_fixed_income
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.portfolioCost
import tech.bashga.plata.data.portfolioValue
import tech.bashga.plata.designsystem.components.AmountRow
import tech.bashga.plata.designsystem.components.BalanceHeader
import tech.bashga.plata.designsystem.components.BalanceHeaderAlign
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconTile
import tech.bashga.plata.designsystem.components.LessonCard
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.ProgressBar
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardBorder
import tech.bashga.plata.designsystem.components.SectionHeaderEmphasis
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.Coins
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.ShieldHalf
import tech.bashga.plata.designsystem.theme.PlataTheme

/** PortfolioAllocation: everything at cost (USD 2.00 where Portfolio shows the market value 2.01). */
@Immutable
data class PortfolioAllocationState(
    val total: Long = 0,
    val equity: Long = 0,
    val fixedIncome: Long = 0,
    val cash: Long = 0,
    val rebalance: Long = 0,
) {
    fun share(part: Long): String = if (total == 0L) "0%" else "${(part * 100.0 / total).roundToInt()}%"
    val analysisLeft: Long get() = (ANALYSIS_THRESHOLD_USD - total).coerceAtLeast(0)

    companion object {
        fun from(snapshot: AppSnapshot): PortfolioAllocationState {
            val equity = snapshot.invest.holdings.sumOf { it.costBasis }
            val equityMarket = snapshot.invest.holdings.sumOf { it.marketValue }
            // the advice is on market value: 1.01 of 2.01 is 0.21 over the 40% ceiling
            val over = (equityMarket - snapshot.portfolioValue * MODERATE_EQUITY_SHARE).roundToLong().coerceAtLeast(0)
            return PortfolioAllocationState(snapshot.portfolioCost, equity, 0, snapshot.accounts.inversionesCashUsd, over)
        }
    }
}

sealed interface PortfolioAllocationAction {
    data object EditProfile : PortfolioAllocationAction
    data object SeeAllEtfs : PortfolioAllocationAction
    data class OpenStock(val ticker: String) : PortfolioAllocationAction
    data object AddMoreCash : PortfolioAllocationAction
    data object OpenLesson : PortfolioAllocationAction
    data object Back : PortfolioAllocationAction
}

class PortfolioAllocationViewModel(repository: AppRepository) : ViewModel() {
    val state: StateFlow<PortfolioAllocationState> = repository.state.map(PortfolioAllocationState::from)
        .stateIn(viewModelScope, SharingStarted.Eagerly, PortfolioAllocationState.from(repository.snapshot))
}

@Composable
fun PortfolioAllocationScreen(onStock: (String) -> Unit, onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PortfolioAllocationViewModel(repository) }
    val state by viewModel.state.collectAsState()
    PortfolioAllocationContent(state, onAction = { action ->
        when (action) {
            is PortfolioAllocationAction.OpenStock -> onStock(action.ticker)
            PortfolioAllocationAction.Back -> onBack()
            else -> Unit
        }
    })
}

// b9c441c0 / baff34c6 / f1b38f8d are one scroll view at three offsets; these are the source's off-grid measurements
private val HeaderBelowSafeArea = 38.67.dp
private val DonutSize = 313.dp
private val DonutHeight = 262.dp
private val AllocationRowGap = 12.dp
private val AllocationPadding = 18.dp
private val RecommendationHeaderGap = 10.dp
private val RecommendationBottom = 18.dp
private val EtfHeaderGap = 19.dp
private val AnalysisHeaderGap = 22.dp
private val ProgressLabelGap = 9.dp
private val AnalysisButtonGap = 21.dp

// scrolled to the end, the lesson card stops 64 above the tab bar (f1b38f8d)
private val FeedBottom = 64.dp

// the bar takes the amount as its title once the header has gone under it
private val CollapseAfter = 60.dp

@Composable
fun PortfolioAllocationContent(state: PortfolioAllocationState, onAction: (PortfolioAllocationAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val gradients = PlataTheme.gradients
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("allocation-scroll")
                .backdropSource()
                .verticalScroll(scroll)
                .padding(top = PlataSafeArea.current.top + HeaderBelowSafeArea),
        ) {
            BalanceHeader(
                usd(state.total),
                Modifier.fillMaxWidth(),
                align = BalanceHeaderAlign.Center,
                sublink = "Moderate profile",
                onSublinkClick = { onAction(PortfolioAllocationAction.EditProfile) },
            )
            Spacer(Modifier.height(spacing.s24))
            Image(
                painterResource(Res.drawable.raster_chart_portfolio_donut),
                "Equity ${state.share(state.equity)}, cash ${state.share(state.cash)}",
                Modifier.align(Alignment.CenterHorizontally).size(DonutSize, DonutHeight),
            )
            Spacer(Modifier.height(spacing.s20))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                SectionCard(
                    Modifier.fillMaxWidth(),
                    bodySpacing = AllocationRowGap,
                    contentPadding = PaddingValues(horizontal = spacing.cardPadding, vertical = AllocationPadding),
                ) {
                    AllocationRow("Equity", "Stocks and variable ETFs", usd(state.equity), state.share(state.equity)) {
                        IconTile(PlataIcons.Lightning, fill = gradients.allocationEquity)
                    }
                    AllocationRow("Fixed income", "Fixed ETFs", usd(state.fixedIncome), state.share(state.fixedIncome)) {
                        IconTile(PlataIcons.ShieldHalf, fill = gradients.allocationFixedIncome)
                    }
                    AllocationRow("Cash", "Available", usd(state.cash), state.share(state.cash)) {
                        IconTile(PlataIcons.Coins, fill = gradients.allocationCash)
                    }
                }
                if (state.rebalance > 0) {
                    SectionCard(
                        Modifier.fillMaxWidth(),
                        title = "Invest in Fixed income ETFs",
                        headerGap = RecommendationHeaderGap,
                        contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = RecommendationBottom),
                    ) {
                        PlataText(
                            "You have more than ${(MODERATE_EQUITY_SHARE * 100).roundToInt()}% of your portfolio in\nequity assets.\n" +
                                "Sell ${usd(state.rebalance)} and invest it in fixed\nincome ETFs.",
                            PlataTheme.typography.subheadTight,
                            colors.textPrimary,
                        )
                    }
                }
                ListingTileCard(
                    "Consider these Fixed\nIncome ETFs",
                    InvestCatalog.fixedIncomeEtfs,
                    onSeeAll = { onAction(PortfolioAllocationAction.SeeAllEtfs) },
                    onListing = { onAction(PortfolioAllocationAction.OpenStock(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    headerGap = EtfHeaderGap,
                )
                SectionCard(
                    Modifier.fillMaxWidth(),
                    title = "Portfolio analysis",
                    border = SectionCardBorder.Gradient,
                    titleEmphasis = SectionHeaderEmphasis.Full,
                    headerLeading = { Icon(PlataIcons.AiBadge, size = PlataTheme.sizes.iconMedium) },
                    headerGap = AnalysisHeaderGap,
                ) {
                    PlataText(
                        "AI analysis is for portfolios over\n${usd(ANALYSIS_THRESHOLD_USD).removeSuffix(".00")}. Keep building your portfolio\nto unlock it.",
                        PlataTheme.typography.bodyCompact,
                        colors.textPrimary,
                    )
                    Spacer(Modifier.height(spacing.s20))
                    ProgressBar(
                        state.total.toFloat() / ANALYSIS_THRESHOLD_USD,
                        Modifier.fillMaxWidth(),
                        trackBrush = gradients.progressTrackTinted,
                        showLabels = true,
                        leadingLabel = "Already ${usd(state.total).removePrefix("USD ")}",
                        trailingLabel = "${usd(state.analysisLeft).removePrefix("USD ")} left",
                        labelGap = ProgressLabelGap,
                    )
                    Spacer(Modifier.height(AnalysisButtonGap))
                    Button("Add more cash", { onAction(PortfolioAllocationAction.AddMoreCash) }, Modifier.fillMaxWidth(), style = ButtonStyle.SecondaryOnCard)
                }
                LessonCard(
                    "Fixed-income ETFs",
                    "3-minute read",
                    Modifier.fillMaxWidth(),
                    onClick = { onAction(PortfolioAllocationAction.OpenLesson) },
                    thumbnail = { Image(painterResource(Res.drawable.raster_lesson_fixed_income), null, Modifier.size(68.dp, 86.dp)) },
                )
            }
            Spacer(Modifier.height(PlataSafeArea.current.bottom + FeedBottom))
        }
        if (collapsed) {
            NavigationBar(title = usd(state.total), onBack = { onAction(PortfolioAllocationAction.Back) }, material = true, includeStatusBar = true)
        } else {
            NavigationBar(onBack = { onAction(PortfolioAllocationAction.Back) }, includeStatusBar = true)
        }
    }
}

@Composable
private fun AllocationRow(title: String, subtitle: String, amount: String, share: String, tile: @Composable () -> Unit) {
    AmountRow(
        title,
        amount,
        Modifier.fillMaxWidth(),
        subtitle = subtitle,
        secondary = share,
        minHeight = PlataTheme.sizes.leadingTile,
        subtitleStyle = PlataTheme.typography.subheadTight,
        leading = tile,
    )
}
