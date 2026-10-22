package tech.bashga.plata.screens.invest

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
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
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.SearchField
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataBackHandler

@Immutable
data class StockListState(
    val list: StockListKind = StockListKind.Popular,
    val searching: Boolean = false,
    val query: String = "",
    val month: Month = Month.June,
) {
    val rows: List<Listing>
        get() = if (searching && query.isNotBlank()) InvestCatalog.search(query, month) else InvestCatalog.list(list, month)
}

sealed interface StockListAction {
    data object StartSearch : StockListAction
    data class Type(val query: String) : StockListAction
    data object Cancel : StockListAction
    data class OpenStock(val ticker: String) : StockListAction
    data object Back : StockListAction
}

fun StockListState.reduce(action: StockListAction): StockListState = when (action) {
    StockListAction.StartSearch -> copy(searching = true)
    is StockListAction.Type -> copy(searching = true, query = action.query)
    StockListAction.Cancel -> copy(searching = false, query = "")
    is StockListAction.OpenStock, StockListAction.Back -> this
}

private data class StockListLocal(val searching: Boolean, val query: String = "")

class StockListViewModel(repository: AppRepository, private val list: StockListKind, searching: Boolean) : ViewModel() {
    private val local = MutableStateFlow(StockListLocal(searching))

    val state: StateFlow<StockListState> = combine(repository.state, local) { snapshot, l ->
        StockListState(list, l.searching, l.query, snapshot.month)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StockListState(list, searching, "", repository.snapshot.month))

    fun onAction(action: StockListAction) {
        val next = state.value.reduce(action)
        local.value = StockListLocal(next.searching, next.query)
    }
}

/** invest/stocks/{list}: a stock list with iOS search; results come from every stock as you type. */
@Composable
fun StockListScreen(list: StockListKind, searching: Boolean, onStock: (String) -> Unit, onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { StockListViewModel(repository, list, searching) }
    val state by viewModel.state.collectAsState()
    PlataBackHandler(enabled = state.searching) { viewModel.onAction(StockListAction.Cancel) }
    StockListContent(state, onAction = { action ->
        when (action) {
            is StockListAction.OpenStock -> onStock(action.ticker)
            StockListAction.Back -> onBack()
            else -> viewModel.onAction(action)
        }
    })
}

// the focused search bar replaces the nav bar: its 36 field sits 3 below the safe-area top (7e1c7742)
private val SearchBarTop = 3.dp

@Composable
fun StockListContent(state: StockListState, onAction: (StockListAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.searching) {
        if (state.searching) focus.requestFocus() else focusManager.clearFocus()
    }
    Column(
        modifier
            .fillMaxSize()
            .background(PlataTheme.colors.surfaceBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        if (state.searching) {
            Spacer(Modifier.height(safeArea.top + SearchBarTop))
            SearchField(
                state.query,
                { onAction(StockListAction.Type(it)) },
                Modifier.fillMaxWidth().padding(start = spacing.searchMargin, end = spacing.screenMargin).testTag("stock-search"),
                placeholder = "Stocks or ETFs",
                active = true,
                onCancel = { onAction(StockListAction.Cancel) },
                focusRequester = focus,
            )
            Spacer(Modifier.height(spacing.s8))
        } else {
            NavigationBar(title = state.list.title, onBack = { onAction(StockListAction.Back) }, includeStatusBar = true)
            SearchField(
                state.query,
                { onAction(StockListAction.Type(it)) },
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.searchMargin)
                    .testTag("stock-search")
                    .onFocusChanged { if (it.hasFocus) onAction(StockListAction.StartSearch) },
                placeholder = "Stocks or ETFs",
            )
            Spacer(Modifier.height(spacing.s16))
        }
        state.rows.forEach { listing ->
            ListingRow(
                listing,
                onClick = { onAction(StockListAction.OpenStock(listing.ticker)) },
                horizontalPadding = spacing.screenMargin,
                minHeight = PlataTheme.sizes.rowAsset,
            )
        }
        Spacer(Modifier.height(safeArea.bottom))
    }
}
