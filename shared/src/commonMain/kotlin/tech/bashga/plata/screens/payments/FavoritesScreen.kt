package tech.bashga.plata.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.BottomSheet
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.ModalScrim
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SearchField
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.BillPayDiagonal
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.Transfer
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay
import kotlin.math.roundToInt

enum class FavoritesOverlay { AddFavorite }

/** Always in iOS edit mode (2be9ff97): delete discs and reorder handles. [phone] is the mobile line's number. */
@Immutable
data class FavoritesState(
    val items: List<Favorite> = listOf(Favorite.MyMobile),
    val phone: String = "",
    val query: String = "",
    val overlay: FavoritesOverlay? = null,
) {
    val shown: List<Favorite>
        get() = if (query.isBlank()) items else items.filter { it.name.contains(query.trim(), ignoreCase = true) }
}

sealed interface FavoritesAction {
    data class QueryChanged(val query: String) : FavoritesAction

    data class Delete(val id: String) : FavoritesAction

    data class Move(val from: Int, val to: Int) : FavoritesAction

    data object AddToFavorites : FavoritesAction

    data object AddTransfer : FavoritesAction

    /** AddFavorite › Bill payments: (no ref). */
    data object AddBillPayment : FavoritesAction

    data object CloseOverlay : FavoritesAction

    data object Close : FavoritesAction
}

fun FavoritesState.reduce(action: FavoritesAction): FavoritesState = when (action) {
    is FavoritesAction.QueryChanged -> copy(query = action.query)
    FavoritesAction.AddToFavorites -> copy(overlay = FavoritesOverlay.AddFavorite)
    FavoritesAction.AddTransfer, FavoritesAction.CloseOverlay -> copy(overlay = null)
    is FavoritesAction.Delete, is FavoritesAction.Move, FavoritesAction.AddBillPayment, FavoritesAction.Close -> this
}

class FavoritesViewModel(private val repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = combine(local, repository.state) { state, snapshot ->
        state.copy(items = snapshot.favorites, phone = snapshot.session.phone)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FavoritesState(repository.snapshot.favorites, repository.snapshot.session.phone))

    fun onAction(action: FavoritesAction) {
        when (action) {
            is FavoritesAction.Delete -> repository.removeFavorite(action.id)
            is FavoritesAction.Move -> {
                val size = repository.snapshot.favorites.size
                if (action.from != action.to && action.from in 0 until size && action.to in 0 until size) repository.moveFavorite(action.from, action.to)
            }
            else -> Unit
        }
        local.value = local.value.reduce(action)
    }
}

/** payments/favorites: the editable favorites list; Add to Favorites › Transfer hands over to [onAddTransfer]. */
@Composable
fun FavoritesScreen(onClose: () -> Unit, onAddTransfer: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { FavoritesViewModel(repository) }
    val state by viewModel.state.collectAsState()
    FavoritesContent(state, { action ->
        viewModel.onAction(action)
        when (action) {
            FavoritesAction.Close -> onClose()
            FavoritesAction.AddTransfer -> onAddTransfer()
            else -> Unit
        }
    })
}

// 2be9ff97: large title box 5.5 under the 54 sheet bar, search 8.5 below it, the row 13 below the search
private val LargeTitleTop = 5.5.dp
private val SearchTop = 8.5.dp
private val RowTop = 13.dp
private val FooterBottom = 21.dp
private val RowPitch = 68.dp
private val HandleWidth = 56.dp

// e94a49a9: two-line rows on a 65 pitch, 14 under the title, 36 above the home-indicator inset
private val AddRowHeight = 65.dp
private val AddTitleGap = 14.dp
private val AddSheetBottom = 36.dp

@Composable
fun FavoritesContent(state: FavoritesState, onAction: (FavoritesAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize()) {
            NavigationBar(style = NavigationBarStyle.Sheet, leading = { TextButton("Close", { onAction(FavoritesAction.Close) }) })
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = safeArea.bottom + FooterBottom + PlataTheme.sizes.buttonLarge + spacing.s20),
            ) {
                Spacer(Modifier.height(LargeTitleTop))
                PlataText(
                    "Favorites",
                    PlataTheme.typography.largeTitle,
                    PlataTheme.colors.textPrimaryTitle,
                    Modifier.padding(horizontal = spacing.searchMargin),
                )
                Spacer(Modifier.height(SearchTop))
                SearchField(
                    state.query,
                    { onAction(FavoritesAction.QueryChanged(it)) },
                    Modifier.padding(horizontal = spacing.searchMargin),
                    placeholder = "Name, provider, reference",
                )
                Spacer(Modifier.height(RowTop))
                ReorderableRows(state, onAction)
            }
        }
        Button(
            "Add to Favorites",
            { onAction(FavoritesAction.AddToFavorites) },
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = spacing.screenMargin)
                .padding(bottom = safeArea.bottom + FooterBottom)
                .fillMaxWidth(),
            style = ButtonStyle.White,
            icon = PlataIcons.Plus,
        )
    }
    Overlay(state.overlay, onDismiss = { onAction(FavoritesAction.CloseOverlay) }) {
        ModalScrim(onDismiss = { onAction(FavoritesAction.CloseOverlay) }) {
            BottomSheet(title = "Add to Favorites", titleGap = AddTitleGap, bottomPadding = AddSheetBottom) {
                AddRow("Transfer", "By CLABE or card number", { IconCircle(PlataIcons.Transfer, IconCircleSize.S40) }) {
                    onAction(FavoritesAction.AddTransfer)
                }
                AddRow("Bill payments", "Water, electricity, etc.", { IconCircle(PlataIcons.BillPayDiagonal, IconCircleSize.S40) }) {
                    onAction(FavoritesAction.AddBillPayment)
                }
            }
        }
    }
}

/** The rows with live drag-to-reorder on their handles; a drag settles on the nearest slot. */
@Composable
private fun ReorderableRows(state: FavoritesState, onAction: (FavoritesAction) -> Unit) {
    val pitch = with(LocalDensity.current) { RowPitch.toPx() }
    var dragging by remember { mutableStateOf<String?>(null) }
    var offset by remember { mutableFloatStateOf(0f) }
    val reorderable = state.query.isBlank()
    state.shown.forEachIndexed { index, favorite ->
        val isDragged = dragging == favorite.id
        Box(
            Modifier
                .fillMaxWidth()
                .zIndex(if (isDragged) 1f else 0f)
                .offset { IntOffset(0, if (isDragged) offset.roundToInt() else 0) }
                .alpha(if (isDragged) 0.9f else 1f),
        ) {
            ListRow(
                favorite.name,
                Modifier.fillMaxWidth(),
                subtitle = favorite.subtitle(state.phone),
                editing = true,
                onDelete = { onAction(FavoritesAction.Delete(favorite.id)) },
                leading = { FavoriteRowArt(favorite) },
            )
            if (reorderable) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .width(HandleWidth)
                        .fillMaxHeight()
                        .pointerInput(favorite.id, index, state.items.size) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    dragging = favorite.id
                                    offset = 0f
                                },
                                onDragEnd = {
                                    val to = (index + (offset / pitch).roundToInt()).coerceIn(0, state.items.lastIndex)
                                    onAction(FavoritesAction.Move(index, to))
                                    dragging = null
                                    offset = 0f
                                },
                                onDragCancel = {
                                    dragging = null
                                    offset = 0f
                                },
                            ) { change, amount ->
                                change.consume()
                                offset += amount
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun AddRow(title: String, subtitle: String, leading: @Composable () -> Unit, onClick: () -> Unit) {
    ListRow(
        title,
        Modifier.height(AddRowHeight),
        subtitle = subtitle,
        showChevron = true,
        horizontalPadding = 0.dp,
        subtitleColor = PlataTheme.colors.textPrimary,
        onClick = onClick,
        leading = leading,
    )
}
