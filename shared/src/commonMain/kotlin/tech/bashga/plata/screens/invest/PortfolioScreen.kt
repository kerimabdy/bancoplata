package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_lesson_portfolio
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.portfolioCost
import tech.bashga.plata.data.portfolioValue
import tech.bashga.plata.designsystem.components.AmountRow
import tech.bashga.plata.designsystem.components.BalanceHeader
import tech.bashga.plata.designsystem.components.BalanceTrend
import tech.bashga.plata.designsystem.components.BottomSheet
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.Chevron
import tech.bashga.plata.designsystem.components.ChevronDirection
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonSize
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.LessonCard
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.ModalScrim
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.ProgressBar
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.components.TrendDirection
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.ArrowUpRightDuotone
import tech.bashga.plata.designsystem.icons.DoubleChevronDuotone
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlusOutlineDuotone
import tech.bashga.plata.designsystem.icons.SwapArrows
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

/** A holding as Portfolio lists it: market value, shares at the session quote, the quote's day change. */
@Immutable
data class HoldingRow(val asset: Asset, val value: Long, val shares: String, val dayChange: Int)

enum class PortfolioOverlay { TransferMethod }

@Immutable
data class PortfolioState(
    val cash: Long = 0,
    val holdings: List<HoldingRow> = emptyList(),
    val total: Long = 0,
    val change: Long = 0,
    val changePercent: Int = 0,
    val month: Month = Month.June,
    val transactions: Int = 0,
    val limitLeft: Long = INVESTMENT_LIMIT_USD,
    val overlay: PortfolioOverlay? = null,
    val lessonDismissed: Boolean = false,
) {
    val stocksValue: Long get() = holdings.sumOf { it.value }
    val limitUsed: Float get() = 1f - limitLeft.toFloat() / INVESTMENT_LIMIT_USD

    companion object {
        fun from(snapshot: AppSnapshot): PortfolioState {
            val cost = snapshot.portfolioCost
            val change = snapshot.portfolioValue - cost
            return PortfolioState(
                cash = snapshot.accounts.inversionesCashUsd,
                holdings = snapshot.invest.holdings.map { h ->
                    val asset = InvestCatalog.asset(h.ticker)
                    HoldingRow(asset, h.marketValue, "${formatShares(snapshot.sharesOf(h.ticker))} ${h.ticker}", InvestCatalog.quote(h.ticker, snapshot.month).change)
                },
                total = snapshot.portfolioValue,
                change = change,
                changePercent = if (cost == 0L) 0 else (change * 10_000 / cost).toInt(),
                month = snapshot.month,
                transactions = snapshot.investTransactionCount,
                limitLeft = snapshot.investmentLimitLeft,
            )
        }
    }
}

sealed interface PortfolioAction {
    data object AddCash : PortfolioAction
    data object CloseOverlay : PortfolioAction
    data object DismissLesson : PortfolioAction

    data object OpenAllocation : PortfolioAction
    data object ToMyPlataAccount : PortfolioAction
    data object ByClabe : PortfolioAction
    data object Withdraw : PortfolioAction
    data class OpenStock(val ticker: String) : PortfolioAction
    data object OpenLesson : PortfolioAction
    data object Transactions : PortfolioAction
    data object HowItWorks : PortfolioAction
    data object IncreaseLimit : PortfolioAction
    data object AccountDocuments : PortfolioAction
    data object Back : PortfolioAction
}

private data class PortfolioLocal(val overlay: PortfolioOverlay? = null, val lessonDismissed: Boolean = false)

class PortfolioViewModel(repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(PortfolioLocal())

    init {
        repository.refreshQuotes()
    }

    val state: StateFlow<PortfolioState> = combine(repository.state, local) { snapshot, l ->
        PortfolioState.from(snapshot).copy(overlay = l.overlay, lessonDismissed = l.lessonDismissed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PortfolioState.from(repository.snapshot))

    fun onAction(action: PortfolioAction) {
        local.value = when (action) {
            PortfolioAction.AddCash -> local.value.copy(overlay = PortfolioOverlay.TransferMethod)
            PortfolioAction.CloseOverlay, PortfolioAction.ToMyPlataAccount, PortfolioAction.ByClabe -> local.value.copy(overlay = null)
            PortfolioAction.DismissLesson -> local.value.copy(lessonDismissed = true)
            else -> local.value
        }
    }
}

/** invest/portfolio: opening it refreshes the quotes once (DNUT 1.00 → 1.01). */
@Composable
fun PortfolioScreen(
    onAllocation: () -> Unit,
    onTransferIn: () -> Unit,
    onStock: (String) -> Unit,
    onLesson: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PortfolioViewModel(repository) }
    val state by viewModel.state.collectAsState()
    PortfolioContent(state, onAction = { action ->
        viewModel.onAction(action)
        when (action) {
            PortfolioAction.OpenAllocation -> onAllocation()
            PortfolioAction.ToMyPlataAccount -> onTransferIn()
            is PortfolioAction.OpenStock -> onStock(action.ticker)
            PortfolioAction.OpenLesson -> onLesson()
            PortfolioAction.Back -> onBack()
            else -> Unit
        }
    })
}

// b658c9da: the caption's box starts 58 under the safe area, the lesson card 39 under the trend line
private val HeaderTop = 58.dp
private val HeaderToCards = 39.dp

// Investment limit (537a4b5a): the 17pt line 17 under the title, the bar 8 under it, the Button 20.5 under the bar
private val LimitTextGap = 17.dp
private val LimitBarGap = 8.dp
private val LimitButtonGap = 20.5.dp

// the two-line lesson card is 106 high (17.5 above and below its text); the stocks card's 60 row sits 17 under the
// subtitle and 7 above the card's bottom
private val LessonPaddingV = 17.5.dp
private val StocksBodyGap = 17.dp
private val StocksBottom = 7.dp
private val StockRowHeight = 60.dp
private val LessonThumbWidth = 68.dp
private val LessonThumbHeight = 87.dp
private val SwapGlyph = 28.dp

// the two-line bar takes over once the amount has gone under it
private val CollapseAfter = 120.dp

@Composable
fun PortfolioContent(state: PortfolioState, onAction: (PortfolioAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val safeArea = PlataSafeArea.current
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("portfolio-scroll")
                .backdropSource()
                .verticalScroll(scroll)
                .padding(top = safeArea.top + HeaderTop),
        ) {
            BalanceHeader(
                usd(state.total),
                Modifier.fillMaxWidth().padding(horizontal = spacing.s40),
                caption = "Investment portfolio",
                trend = BalanceTrend(
                    "${usd(kotlin.math.abs(state.change))} · ${formatPercent(state.changePercent)}",
                    if (state.changePercent < 0 || state.change < 0) TrendDirection.Down else TrendDirection.Up,
                    "· All time",
                ),
                trailing = {
                    IconButton(
                        PlataIcons.Pie,
                        { onAction(PortfolioAction.OpenAllocation) },
                        style = IconButtonStyle.White,
                        size = IconButtonSize.S40,
                        contentDescription = "Allocation",
                    )
                },
            )
            Spacer(Modifier.height(HeaderToCards))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                if (!state.lessonDismissed) {
                    LessonCard(
                        "How to build a\nstrong portfolio",
                        "4-minute read",
                        Modifier.fillMaxWidth(),
                        onClose = { onAction(PortfolioAction.DismissLesson) },
                        onClick = { onAction(PortfolioAction.OpenLesson) },
                        contentPadding = PaddingValues(horizontal = spacing.cardPadding, vertical = LessonPaddingV),
                        thumbnail = {
                            Image(painterResource(Res.drawable.raster_lesson_portfolio), null, Modifier.size(LessonThumbWidth, LessonThumbHeight))
                        },
                    )
                }
                SummaryCard("Cash", Modifier.fillMaxWidth().testTag("portfolio-cash"), subtitle = usd(state.cash), subtitleStyle = PlataTheme.typography.bodyCompact) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.s8)) {
                        IconButton(
                            PlataIcons.PlusOutlineDuotone,
                            { onAction(PortfolioAction.AddCash) },
                            style = IconButtonStyle.Tinted,
                            size = IconButtonSize.S48,
                            contentDescription = "Add cash",
                        )
                        IconButton(
                            PlataIcons.ArrowUpRightDuotone,
                            { onAction(PortfolioAction.Withdraw) },
                            style = IconButtonStyle.Tinted,
                            size = IconButtonSize.S48,
                            contentDescription = "Withdraw",
                        )
                    }
                }
                if (state.holdings.isNotEmpty()) {
                    SummaryCard(
                        "Your stocks",
                        Modifier.fillMaxWidth(),
                        subtitle = usd(state.stocksValue),
                        subtitleStyle = PlataTheme.typography.bodyCompact,
                        contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = StocksBottom),
                        bodyGap = StocksBodyGap,
                        body = {
                            state.holdings.forEach { holding ->
                                AmountRow(
                                    holding.asset.name,
                                    usd(holding.value),
                                    subtitle = holding.shares,
                                    secondary = formatPercent(holding.dayChange),
                                    secondaryTone = (if (holding.dayChange < 0) TrendDirection.Down else TrendDirection.Up).tone,
                                    onClick = { onAction(PortfolioAction.OpenStock(holding.asset.ticker)) },
                                    minHeight = StockRowHeight,
                                ) { AssetLogo(holding.asset, BrandLogoSize.S40) }
                            }
                        },
                    )
                }
                SummaryCard(
                    "Transactions",
                    Modifier.fillMaxWidth(),
                    subtitle = "${state.transactions} in ${state.month.label}",
                    subtitleStyle = PlataTheme.typography.bodyCompact,
                    onClick = { onAction(PortfolioAction.Transactions) },
                ) { Chevron(ChevronDirection.Right) }
                SectionCard(
                    Modifier.fillMaxWidth(),
                    title = "Investment limit",
                    headerTrailing = { SectionCardAction("How it works", { onAction(PortfolioAction.HowItWorks) }) },
                    headerGap = LimitTextGap,
                ) {
                    PlataText("${usd(state.limitLeft)} left in ${state.month.label}", PlataTheme.typography.body, colors.textPrimary)
                    Spacer(Modifier.height(LimitBarGap))
                    ProgressBar(state.limitUsed, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(LimitButtonGap))
                    Button("Increase limit", { onAction(PortfolioAction.IncreaseLimit) }, Modifier.fillMaxWidth())
                }
                SectionCard(Modifier.fillMaxWidth(), title = "Details", headerGap = 0.dp) {
                    ListRow("Account documents", showChevron = true, horizontalPadding = 0.dp, onClick = { onAction(PortfolioAction.AccountDocuments) })
                }
            }
            Spacer(Modifier.height(safeArea.bottom + spacing.cardGap))
        }
        if (collapsed) {
            NavigationBar(
                title = "Investment portfolio",
                subtitle = usd(state.total),
                onBack = { onAction(PortfolioAction.Back) },
                divider = true,
                includeStatusBar = true,
                modifier = Modifier.background(colors.surfaceBackground),
            )
        } else {
            NavigationBar(onBack = { onAction(PortfolioAction.Back) }, includeStatusBar = true)
        }
    }
    Overlay(state.overlay, onDismiss = { onAction(PortfolioAction.CloseOverlay) }) {
        ModalScrim(onDismiss = { onAction(PortfolioAction.CloseOverlay) }) {
            BottomSheet(Modifier.fillMaxWidth(), title = "Transfer", contentSpacing = spacing.s4) {
                ListRow(
                    "By CLABE",
                    showChevron = true,
                    onClick = { onAction(PortfolioAction.ByClabe) },
                    horizontalPadding = 0.dp,
                    leading = { IconCircle(PlataIcons.DoubleChevronDuotone, IconCircleSize.S40) },
                )
                ListRow(
                    "To my Plata account",
                    showChevron = true,
                    onClick = { onAction(PortfolioAction.ToMyPlataAccount) },
                    horizontalPadding = 0.dp,
                    leading = {
                        IconCircle(IconCircleSize.S40) {
                            Icon(PlataIcons.SwapArrows, size = SwapGlyph, brush = PlataTheme.gradients.glyph)
                        }
                    },
                )
            }
        }
    }
}
