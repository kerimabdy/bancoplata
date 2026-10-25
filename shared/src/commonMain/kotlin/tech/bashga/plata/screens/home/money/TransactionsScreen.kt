package tech.bashga.plata.screens.home.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerEntry
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.data.received
import tech.bashga.plata.data.spent
import tech.bashga.plata.designsystem.components.AmountRow
import tech.bashga.plata.designsystem.components.AmountSecondaryTone
import tech.bashga.plata.designsystem.components.AmountTone
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.IconCircleTone
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.SearchField
import tech.bashga.plata.designsystem.components.SectionHeader
import tech.bashga.plata.designsystem.components.ShortcutTile
import tech.bashga.plata.designsystem.components.ShortcutTileSize
import tech.bashga.plata.designsystem.components.ShortcutTitleTone
import tech.bashga.plata.designsystem.components.SpendOrb
import tech.bashga.plata.designsystem.components.SpendOrbState
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.HouseDollar
import tech.bashga.plata.designsystem.icons.MsiBadge
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** One day of the list, newest entry first. */
data class TransactionDay(val day: Int, val label: String, val rows: List<TransactionRow>)

/** [ledger] is the whole ledger; the screen shows [period]'s entries, filtered by [query]. */
@Immutable
data class TransactionsState(
    val period: Month = Month.June,
    val ledger: List<LedgerEntry> = emptyList(),
    val query: String = "",
) {
    private val snapshot: AppSnapshot get() = AppSnapshot(ledger = ledger, month = period)
    val spent: Long get() = snapshot.spent()
    val received: Long get() = snapshot.received()
    val cashback: Long get() = snapshot.cashbackEarned()

    val days: List<TransactionDay>
        get() {
            val needle = query.trim()
            return ledger
                .filter { it.month == period }
                .reversed()
                .map { it.day to it.row }
                .filter { (_, row) -> needle.isEmpty() || row.title.contains(needle, ignoreCase = true) || row.category.contains(needle, ignoreCase = true) }
                .groupBy({ it.first }, { it.second })
                .toList()
                .sortedByDescending { it.first }
                .map { (day, rows) -> TransactionDay(day, period.dayLabel(day), rows) }
        }
}

sealed interface TransactionsAction {
    data class Search(val query: String) : TransactionsAction

    data object Back : TransactionsAction
}

class TransactionsViewModel(repository: AppRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val state: StateFlow<TransactionsState> = combine(repository.state, query) { snapshot, query ->
        TransactionsState(snapshot.month, snapshot.ledger, query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TransactionsState(repository.snapshot.month, repository.snapshot.ledger))

    fun onAction(action: TransactionsAction) {
        if (action is TransactionsAction.Search) query.value = action.query
    }
}

/** home/transactions: the fake clock's month of ledger entries. */
@Composable
fun TransactionsScreen(onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { TransactionsViewModel(repository) }
    val state by viewModel.state.collectAsState()
    TransactionsContent(state, { if (it == TransactionsAction.Back) onBack() else viewModel.onAction(it) })
}

// 894b1b01: the month 35 under the search field, tiles 13 apart, the MSI row 30 under them, the first day 40 under it
private val MonthTop = 35.dp
private val TileGap = 13.dp
private val CompactGap = 12.5.dp
private val MsiRowTop = 30.33.dp
private val GroupsTop = 39.83.dp
private val DayToRows = 19.33.dp
private val FeedBottom = 37.dp

@Composable
fun TransactionsContent(state: TransactionsState, onAction: (TransactionsAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("transactions")
                .verticalScroll(scroll)
                .padding(top = safeArea.top - spacing.s4 + PlataTheme.sizes.nav, bottom = safeArea.bottom + FeedBottom),
        ) {
            SearchField(
                state.query,
                { onAction(TransactionsAction.Search(it)) },
                Modifier.fillMaxWidth().padding(horizontal = spacing.searchMargin),
                placeholder = "Search for operations",
            )
            Spacer(Modifier.height(MonthTop))
            HeadlineBlock(state.period.label, Modifier.padding(horizontal = spacing.screenMargin))
            Spacer(Modifier.height(spacing.s20))
            Row(Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin), horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                ShortcutTile(
                    formatMoney(state.spent),
                    Modifier.weight(1f),
                    size = ShortcutTileSize.Tall,
                    titleTone = ShortcutTitleTone.Primary,
                    subtitle = "Spent",
                    showChevron = true,
                    onClick = {},
                ) { SpendOrb(if (state.spent > 0) SpendOrbState.Active else SpendOrbState.Empty) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CompactGap)) {
                    ShortcutTile(
                        formatMoney(state.received),
                        Modifier.fillMaxWidth(),
                        size = ShortcutTileSize.Compact,
                        titleTone = ShortcutTitleTone.Success,
                        subtitle = "Received",
                        showChevron = true,
                        onClick = {},
                    )
                    ShortcutTile(
                        formatMoney(state.cashback),
                        Modifier.fillMaxWidth(),
                        size = ShortcutTileSize.Compact,
                        subtitle = "Cashback",
                        showChevron = true,
                        onClick = {},
                    )
                }
            }
            Spacer(Modifier.height(MsiRowTop))
            ListRow(
                "Defer to MSI",
                Modifier.fillMaxWidth(),
                value = "No operations",
                showChevron = true,
                onClick = {},
                leading = { Icon(PlataIcons.MsiBadge, size = PlataTheme.sizes.leadingTile) },
            )
            Spacer(Modifier.height(GroupsTop))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s20)) {
                state.days.forEach { day ->
                    Column(verticalArrangement = Arrangement.spacedBy(DayToRows)) {
                        SectionHeader(day.label, Modifier.padding(horizontal = spacing.screenMargin).testTag("day-${day.day}"))
                        Column { day.rows.forEach { TransactionRowItem(it) } }
                    }
                }
            }
        }
        NavigationBar(title = "Transactions", onBack = { onAction(TransactionsAction.Back) }, collapsed = rememberCollapsed(scroll), includeStatusBar = true)
    }
}

@Composable
private fun TransactionRowItem(row: TransactionRow) {
    AmountRow(
        row.title,
        row.amount,
        Modifier.fillMaxWidth(),
        subtitle = row.category,
        // an empty second line keeps a lone amount level with the title, as the references draw it
        secondary = row.secondary ?: "",
        amountTone = if (row.incoming) AmountTone.Positive else AmountTone.Default,
        secondaryTone = when (row.secondaryTone) {
            TransactionSecondaryTone.Neutral -> AmountSecondaryTone.Neutral
            TransactionSecondaryTone.Primary -> AmountSecondaryTone.Primary
            TransactionSecondaryTone.Gradient -> AmountSecondaryTone.Gradient
        },
        horizontalPadding = PlataTheme.spacing.screenMargin,
        onClick = {},
    ) {
        val logo = row.logo
        when {
            logo != null -> BrandLogo(painterResource(logo), BrandLogoSize.S40)
            row.kind == EntryKind.Commission ->
                IconCircle(PlataIcons.HouseDollar, IconCircleSize.S40, tone = IconCircleTone.Filled, fill = PlataTheme.gradients.violet)
            else -> IconCircle(if (row.kind == EntryKind.Reward) PlataIcons.Heart else PlataIcons.Card, IconCircleSize.S40)
        }
    }
}
