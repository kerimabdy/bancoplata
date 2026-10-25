package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
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
import plata.shared.generated.resources.raster_lesson_stocks_etfs
import plata.shared.generated.resources.raster_lesson_why_investing
import plata.shared.generated.resources.raster_promo_investing
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.portfolioValue
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.Chip
import tech.bashga.plata.designsystem.components.ChipTone
import tech.bashga.plata.designsystem.components.IdeaCard
import tech.bashga.plata.designsystem.components.LessonCard
import tech.bashga.plata.designsystem.components.LessonCardSize
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.PromoCard
import tech.bashga.plata.designsystem.components.SearchField
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.SectionHeader
import tech.bashga.plata.designsystem.components.SegmentedControl
import tech.bashga.plata.designsystem.components.StockTile
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonWeight
import tech.bashga.plata.designsystem.components.ToastHost
import tech.bashga.plata.designsystem.components.ToastTone
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.backdropBlur
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

enum class Movers { Gainers, Losers }

enum class InvestToast(val message: String) {
    VestFiInfo("Powered by VestFi, Asesor en Inversiones Independiente, S.A.P.I. de C.V."),
}

object InvestLessons {
    const val HOW_TO_BUY_STOCKS = "how-to-buy-stocks"
    const val WHY_INVESTING_MATTERS = "why-investing-matters"
    const val STOCKS_VS_ETFS = "stocks-vs-etfs"
    const val BUILD_A_PORTFOLIO = "build-a-strong-portfolio"
    const val UNDERSTANDING_THE_CHART = "understanding-the-chart"
    const val FIXED_INCOME_ETFS = "fixed-income-etfs"
    const val PLATA_INVERSIONES = "what-is-plata-inversiones"
}

@Immutable
data class InvestmentsState(
    val account: InvestAccount = InvestAccount.NotOpened,
    val portfolioValue: Long = 0,
    val recents: List<String> = emptyList(),
    val month: Month = Month.June,
    val movers: Movers = Movers.Gainers,
    val dismissedLessons: Set<String> = emptySet(),
    val toast: InvestToast? = null,
) {
    val popular: List<Listing> get() = InvestCatalog.popular(month).take(6)
    val moverListings: List<Listing> get() = if (movers == Movers.Gainers) InvestCatalog.gainers else InvestCatalog.losers(month)

    companion object {
        fun from(snapshot: AppSnapshot) = InvestmentsState(
            account = snapshot.invest.account,
            portfolioValue = snapshot.portfolioValue,
            recents = snapshot.invest.recents,
            month = snapshot.month,
        )
    }
}

sealed interface InvestmentsAction {
    data object ShowVestFiInfo : InvestmentsAction
    data object DismissToast : InvestmentsAction
    data class SelectMovers(val movers: Movers) : InvestmentsAction
    data class DismissLesson(val lessonId: String) : InvestmentsAction

    data object OpenSearch : InvestmentsAction
    data object OpenAccount : InvestmentsAction
    data object OpenPortfolio : InvestmentsAction
    data class OpenStock(val ticker: String) : InvestmentsAction
    data class SeeAll(val list: StockListKind) : InvestmentsAction
    data class OpenIdea(val ideaId: String) : InvestmentsAction
    data class OpenLesson(val lessonId: String) : InvestmentsAction
}

/** The part of the state no repository write touches: what taps on this screen change. */
private data class InvestmentsLocal(
    val movers: Movers = Movers.Gainers,
    val dismissedLessons: Set<String> = emptySet(),
    val toast: InvestToast? = null,
)

class InvestmentsViewModel(repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(InvestmentsLocal())

    val state: StateFlow<InvestmentsState> = combine(repository.state, local) { snapshot, l ->
        InvestmentsState.from(snapshot).copy(movers = l.movers, dismissedLessons = l.dismissedLessons, toast = l.toast)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, InvestmentsState.from(repository.snapshot))

    fun onAction(action: InvestmentsAction) {
        val l = local.value
        local.value = when (action) {
            InvestmentsAction.ShowVestFiInfo -> l.copy(toast = InvestToast.VestFiInfo)
            InvestmentsAction.DismissToast -> l.copy(toast = null)
            is InvestmentsAction.SelectMovers -> l.copy(movers = action.movers)
            is InvestmentsAction.DismissLesson -> l.copy(dismissedLessons = l.dismissedLessons + action.lessonId)
            else -> l
        }
    }
}

/** invest: the Invest tab root. Navigation goes out through the lambdas; everything else stays in the ViewModel. */
@Composable
fun InvestmentsScreen(
    onSearch: () -> Unit,
    onOpenAccount: () -> Unit,
    onPortfolio: () -> Unit,
    onStock: (String) -> Unit,
    onSeeAll: (StockListKind) -> Unit,
    onIdea: (String) -> Unit,
    onLesson: (String) -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { InvestmentsViewModel(repository) }
    val state by viewModel.state.collectAsState()
    InvestmentsContent(state, onAction = { action ->
        when (action) {
            InvestmentsAction.OpenSearch -> onSearch()
            InvestmentsAction.OpenAccount -> onOpenAccount()
            InvestmentsAction.OpenPortfolio -> onPortfolio()
            is InvestmentsAction.OpenStock -> onStock(action.ticker)
            is InvestmentsAction.SeeAll -> onSeeAll(action.list)
            is InvestmentsAction.OpenIdea -> onIdea(action.ideaId)
            is InvestmentsAction.OpenLesson -> onLesson(action.lessonId)
            else -> viewModel.onAction(action)
        }
    })
}

// 73569385, 3d1d2b9b: search 7 under the large title row, the first card 35 under the search; the portfolio card pads 18
// above its title and Recents 19, Recents sets its chips 12 under the title, Popular stocks its tiles 21 under it
private val SearchTop = 7.dp
private val FirstCardTop = 35.dp
private val CardTop = 18.dp
private val RecentsTop = 19.dp
private val RecentsGap = 12.dp
private val StocksGap = 21.dp
private val StockRowGap = 8.dp
private val StockColumnGap = 9.5.dp

// the collapsed bar keeps the search 0.6 under the inline row and its material runs 15 below it (fdc6c570: 0–149.5)
private val CollapsedSearchTop = 0.6.dp
private val CollapsedMaterialBottom = 15.dp

// the large title row (46, NavigationBar) plus the gap to the search, less the collapsed gap: scrolled this far, the
// search sits exactly where the collapsed bar pins it, so the swap doesn't move it
private val CollapseDistance = 46.dp + SearchTop - CollapsedSearchTop

// 73569385: the VestFi toast 8 from the screen sides, its bottom 6.67 above the tab bar
private val ToastWidth = 377.dp
private val ToastAboveBar = 6.67.dp

// the lesson pager's cards are 349 wide, the next one peeking 12 after
private val LessonPageWidth = 349.dp

@Composable
fun InvestmentsContent(state: InvestmentsState, onAction: (InvestmentsAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val safeBottom = PlataSafeArea.current.bottom
    val scroll = rememberScrollState()
    val collapsePx = with(LocalDensity.current) { CollapseDistance.toPx() }
    val collapsed by remember(scroll, collapsePx) { derivedStateOf { scroll.value >= collapsePx } }
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("investments-scroll").backdropSource().verticalScroll(scroll)) {
            NavigationBar(
                style = NavigationBarStyle.Large,
                title = "Investments",
                trailing = { VestFiCredit { onAction(InvestmentsAction.ShowVestFiInfo) } },
                includeStatusBar = true,
            )
            Spacer(Modifier.height(SearchTop))
            SearchButton(onClick = { onAction(InvestmentsAction.OpenSearch) })
            Spacer(Modifier.height(FirstCardTop))
            Column(Modifier.padding(horizontal = spacing.screenMargin)) {
                InvestmentsFeed(state, onAction)
            }
            Spacer(Modifier.height(safeBottom + spacing.cardGap))
        }
        if (collapsed) {
            Column(Modifier.fillMaxWidth().backdropBlur(colors.surfaceNavMaterialFrost, fallback = colors.surfaceNavMaterial)) {
                NavigationBar(
                    title = "Investments",
                    trailing = { VestFiCredit { onAction(InvestmentsAction.ShowVestFiInfo) } },
                    includeStatusBar = true,
                )
                Spacer(Modifier.height(CollapsedSearchTop))
                SearchButton(onClick = { onAction(InvestmentsAction.OpenSearch) })
                Spacer(Modifier.height(CollapsedMaterialBottom))
            }
        }
    }
    Overlay(state.toast, onDismiss = { onAction(InvestmentsAction.DismissToast) }, dismissOnBack = false, coversContent = false) { toast ->
        Box(Modifier.fillMaxSize()) {
            ToastHost(
                toast.message,
                onDismiss = { onAction(InvestmentsAction.DismissToast) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = safeBottom + ToastAboveBar).width(ToastWidth),
                tone = ToastTone.Neutral,
            )
        }
    }
}

/** The tab's search field only opens StockList's search mode; it never takes focus here. */
@Composable
private fun SearchButton(onClick: () -> Unit) {
    Box(Modifier.padding(horizontal = PlataTheme.spacing.searchMargin)) {
        SearchField("", {}, Modifier.fillMaxWidth(), placeholder = "Stocks or ETFs")
        Box(Modifier.matchParentSize().pressable(onClickLabel = "Search stocks or ETFs", onClick = onClick))
    }
}

@Composable
private fun InvestmentsFeed(state: InvestmentsState, onAction: (InvestmentsAction) -> Unit) {
    val spacing = PlataTheme.spacing
    val cardGap = spacing.cardGap
    val sectionGap = spacing.sectionGap
    val openStock: (String) -> Unit = { onAction(InvestmentsAction.OpenStock(it)) }
    if (state.account == InvestAccount.NotOpened) {
        PromoCard(
            "Investing is easy",
            Modifier.fillMaxWidth(),
            body = "Start investing with as little as USD 1.\nOpen an account in 5 minutes.",
            actionLabel = "Open an account",
            onAction = { onAction(InvestmentsAction.OpenAccount) },
        ) {
            Image(
                painterResource(Res.drawable.raster_promo_investing),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 6.33.dp).size(143.dp, 148.33.dp),
            )
        }
    } else {
        SummaryCard(
            usd(state.portfolioValue).replace("USD ", "USD "),
            Modifier.fillMaxWidth(),
            subtitle = "Investment portfolio",
            subtitleStyle = PlataTheme.typography.bodyCompact,
            onClick = { onAction(InvestmentsAction.OpenPortfolio) },
            contentPadding = PaddingValues(start = spacing.cardPadding, top = CardTop, end = spacing.cardPadding, bottom = spacing.cardPadding),
        )
    }
    if (state.recents.isNotEmpty()) {
        Spacer(Modifier.height(cardGap))
        SectionCard(
            Modifier.fillMaxWidth(),
            title = "Recents",
            headerGap = RecentsGap,
            contentPadding = PaddingValues(start = spacing.cardPadding, top = RecentsTop, end = spacing.cardPadding, bottom = spacing.cardPadding),
        ) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(spacing.s8)) {
                state.recents.forEach { ticker ->
                    Chip(ticker, painterResource(InvestCatalog.asset(ticker).logo), tone = ChipTone.Grey, onClick = { openStock(ticker) })
                }
            }
        }
    }
    Spacer(Modifier.height(if (state.account == InvestAccount.NotOpened && state.recents.isEmpty()) StocksGap else cardGap))
    SectionCard(
        Modifier.fillMaxWidth().testTag("popular-stocks"),
        title = "Popular stocks",
        headerTrailing = { SectionCardAction("See all", { onAction(InvestmentsAction.SeeAll(StockListKind.Popular)) }) },
        headerGap = StocksGap,
        bodySpacing = StockRowGap,
    ) {
        state.popular.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StockColumnGap)) {
                row.forEach { listing ->
                    StockTile(listing.ticker, listing.quote.changeLabel, listing.quote.direction, Modifier.weight(1f), onClick = { openStock(listing.ticker) }) {
                        AssetLogo(listing.asset, BrandLogoSize.S56)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    Spacer(Modifier.height(cardGap))
    ListingTileCard(
        "Top movers",
        state.moverListings,
        onSeeAll = { onAction(InvestmentsAction.SeeAll(StockListKind.All)) },
        onListing = openStock,
        modifier = Modifier.fillMaxWidth().testTag("top-movers"),
        header = {
            SegmentedControl(
                Movers.entries.map { it.name },
                state.movers.ordinal,
                { onAction(InvestmentsAction.SelectMovers(Movers.entries[it])) },
                Modifier.fillMaxWidth(),
            )
        },
    )
    Spacer(Modifier.height(sectionGap))
    Column(Modifier.testTag("investment-ideas"), verticalArrangement = Arrangement.spacedBy(cardGap)) {
        SectionHeader("Investment ideas", Modifier.fillMaxWidth(), trailing = { SectionCardAction("See all", {}) })
        InvestIdeas.all.forEach { idea ->
            IdeaCard(
                idea.title,
                painterResource(idea.cardImage),
                formatPercent(idea.cardTrend),
                Modifier.fillMaxWidth(),
                logo = { BrandLogo(painterResource(idea.fund.logo), BrandLogoSize.S56) },
                onClick = { onAction(InvestmentsAction.OpenIdea(idea.id)) },
            )
        }
    }
    Spacer(Modifier.height(sectionGap))
    ListingTileCard(
        "Crypto ETFs",
        InvestCatalog.crypto,
        onSeeAll = { onAction(InvestmentsAction.SeeAll(StockListKind.Crypto)) },
        onListing = openStock,
        modifier = Modifier.fillMaxWidth().testTag("crypto-etfs"),
    )
    if (InvestLessons.STOCKS_VS_ETFS !in state.dismissedLessons) {
        Spacer(Modifier.height(cardGap))
        LessonCard(
            "Stocks vs ETFs",
            "3-minute read",
            Modifier.fillMaxWidth(),
            onClose = { onAction(InvestmentsAction.DismissLesson(InvestLessons.STOCKS_VS_ETFS)) },
            onClick = { onAction(InvestmentsAction.OpenLesson(InvestLessons.STOCKS_VS_ETFS)) },
            thumbnail = { Image(painterResource(Res.drawable.raster_lesson_stocks_etfs), null, Modifier.size(64.dp, 80.dp)) },
        )
    }
    Spacer(Modifier.height(cardGap))
    ListingRowCard(
        "Commodities ETFs",
        InvestCatalog.commodities,
        onSeeAll = { onAction(InvestmentsAction.SeeAll(StockListKind.Commodities)) },
        onListing = openStock,
        modifier = Modifier.fillMaxWidth().testTag("commodities-etfs"),
    )
    Spacer(Modifier.height(sectionGap))
    SectionHeader(
        "Learn investment",
        Modifier.fillMaxWidth(),
        trailing = { TextButton("See all", { onAction(InvestmentsAction.OpenLesson(InvestLessons.WHY_INVESTING_MATTERS)) }, weight = TextButtonWeight.Semibold) },
    )
    Spacer(Modifier.height(spacing.s20))
    Row(
        Modifier
            // the pager runs to the screen edge, past the feed's 20 margin
            .layout { measurable, constraints ->
                val extra = spacing.screenMargin.roundToPx()
                val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra))
                layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
            }
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.s12),
    ) {
        LessonCard(
            "Why investing\nmatters",
            "3-minute read",
            Modifier.width(LessonPageWidth),
            size = LessonCardSize.Large,
            onClick = { onAction(InvestmentsAction.OpenLesson(InvestLessons.WHY_INVESTING_MATTERS)) },
            thumbnail = { Image(painterResource(Res.drawable.raster_lesson_why_investing), null, Modifier.size(139.dp, 175.dp)) },
            action = {
                Button("Lesson 2", { onAction(InvestmentsAction.OpenLesson(InvestLessons.HOW_TO_BUY_STOCKS)) }, size = ButtonSize.Small, icon = PlataIcons.Play)
            },
        )
        LessonCard(
            "Stocks vs ETFs",
            "3-minute read",
            Modifier.width(LessonPageWidth),
            size = LessonCardSize.Large,
            onClick = { onAction(InvestmentsAction.OpenLesson(InvestLessons.STOCKS_VS_ETFS)) },
            thumbnail = { Image(painterResource(Res.drawable.raster_lesson_stocks_etfs), null, Modifier.size(139.dp, 175.dp)) },
            action = {
                Button("Lesson 3", { onAction(InvestmentsAction.OpenLesson(InvestLessons.STOCKS_VS_ETFS)) }, size = ButtonSize.Small, icon = PlataIcons.Play)
            },
        )
        Spacer(Modifier.width(spacing.s8))
    }
    Spacer(Modifier.height(sectionGap))
    ListingTileCard(
        "Short term bonds ETFs",
        InvestCatalog.shortTermBonds,
        onSeeAll = { onAction(InvestmentsAction.SeeAll(StockListKind.ShortTermBonds)) },
        onListing = openStock,
        modifier = Modifier.fillMaxWidth().testTag("short-term-bonds"),
    )
    Spacer(Modifier.height(cardGap))
    ListingRowCard(
        "All stocks",
        InvestCatalog.allStocks.take(5),
        onSeeAll = { onAction(InvestmentsAction.SeeAll(StockListKind.All)) },
        onListing = openStock,
        modifier = Modifier.fillMaxWidth(),
    )
}
