package tech.bashga.plata.screens.payments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
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
import plata.shared.generated.resources.raster_favorite_added
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.FavoriteKind
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.ActionTile
import tech.bashga.plata.designsystem.components.BottomSheet
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.Dialog
import tech.bashga.plata.designsystem.components.DialogPlacement
import tech.bashga.plata.designsystem.components.GridItem
import tech.bashga.plata.designsystem.components.GridItemWidth
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.ModalScrim
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SearchField
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.Cfe
import tech.bashga.plata.designsystem.icons.DoubleChevronDuotone
import tech.bashga.plata.designsystem.icons.Gas
import tech.bashga.plata.designsystem.icons.Internet
import tech.bashga.plata.designsystem.icons.Parking
import tech.bashga.plata.designsystem.icons.PhoneDuotone
import tech.bashga.plata.designsystem.icons.PhoneHandset
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.Toll
import tech.bashga.plata.designsystem.icons.TopUp
import tech.bashga.plata.designsystem.icons.Transfer
import tech.bashga.plata.designsystem.icons.Tv
import tech.bashga.plata.designsystem.icons.Water
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

sealed interface PaymentsOverlay {
    data object TransferMethod : PaymentsOverlay

    data class FavoriteAdded(val favorite: Favorite) : PaymentsOverlay
}

@Immutable
data class PaymentsState(
    val favorites: List<Favorite> = listOf(Favorite.MyMobile),
    val query: String = "",
    val overlay: PaymentsOverlay? = null,
)

sealed interface PaymentsAction {
    data class QueryChanged(val query: String) : PaymentsAction

    data object TransferTile : PaymentsAction

    data class ChooseMethod(val method: TransferMethod) : PaymentsAction

    data class ShowFavoriteAdded(val favoriteId: String) : PaymentsAction

    data object CloseOverlay : PaymentsAction

    data object EditFavorites : PaymentsAction

    /** Top-up, a favorite, a biller, Show all: (no ref). */
    data object NotBuilt : PaymentsAction
}

fun PaymentsState.reduce(action: PaymentsAction): PaymentsState = when (action) {
    is PaymentsAction.QueryChanged -> copy(query = action.query)
    PaymentsAction.TransferTile -> copy(overlay = PaymentsOverlay.TransferMethod)
    is PaymentsAction.ChooseMethod -> if (action.method == TransferMethod.Phone) this else copy(overlay = null)
    is PaymentsAction.ShowFavoriteAdded -> favorites.firstOrNull { it.id == action.favoriteId }
        ?.let { copy(overlay = PaymentsOverlay.FavoriteAdded(it)) } ?: this
    PaymentsAction.CloseOverlay -> copy(overlay = null)
    PaymentsAction.EditFavorites, PaymentsAction.NotBuilt -> this
}

class PaymentsViewModel(private val repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(PaymentsState(favorites = repository.snapshot.favorites))
    val state: StateFlow<PaymentsState> = combine(local, repository.state) { state, snapshot -> state.copy(favorites = snapshot.favorites) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, local.value)

    fun onAction(action: PaymentsAction) {
        local.value = local.value.copy(favorites = repository.snapshot.favorites).reduce(action)
    }
}

/**
 * The Payments tab root. [addedFavoriteId] is the favorite TransferRecipient just saved; it opens FavoriteAdded once,
 * then [onAddedFavoriteShown] clears it.
 */
@Composable
fun PaymentsScreen(
    addedFavoriteId: String?,
    onAddedFavoriteShown: () -> Unit,
    onTransfer: (TransferMethod) -> Unit,
    onEditFavorites: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PaymentsViewModel(repository) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(addedFavoriteId) {
        if (addedFavoriteId != null) {
            viewModel.onAction(PaymentsAction.ShowFavoriteAdded(addedFavoriteId))
            onAddedFavoriteShown()
        }
    }
    PaymentsContent(state, { action ->
        viewModel.onAction(action)
        when (action) {
            is PaymentsAction.ChooseMethod -> if (action.method != TransferMethod.Phone) onTransfer(action.method)
            PaymentsAction.EditFavorites -> onEditFavorites()
            else -> Unit
        }
    })
}

// the grid cells sit 12 from the card edges, 8 outside its 20 padding (a5a647ee, b7b04c52)
private val GridBleed = 8.dp
private val FavoritesGap = 8.dp
private const val BillerColumns = 4
// four 72 cells across the 329 bled card at 393 (a5a647ee)
private val BillerGap = 13.67.dp
private val CardHeaderGap = 19.dp
private val SearchTop = 7.dp
private val TilesTop = 22.dp
private val TileIcon = 32.dp
private val TileGap = 21.dp

// iOS lets a large-title page scroll its title away even when the content is shorter than the screen (0b2e699a)
private val TitleScrollAway = 52.dp

// the inline title takes over once the 46 large-title row has scrolled under the bar (0b2e699a)
private val CollapseAfter = 40.dp

// the art raster keeps 2.3 of white under the card, so the title starts 46.5 below the raster (873d0226)
private val AddedArtGap = 46.5.dp
private val AddedArtWidth = 98.dp
private val AddedArtHeight = 73.dp

// this dialog's bottom sits 818.6, 2.5 above where DialogPlacement puts the others
private val AddedDialogLift = (-2.5).dp

private val Billers: List<Pair<String, ImageVector>>
    get() = listOf(
        "Mobile" to PlataIcons.PhoneDuotone,
        "Electricity" to PlataIcons.Cfe,
        "Water" to PlataIcons.Water,
        "Gas" to PlataIcons.Gas,
        "TV" to PlataIcons.Tv,
        "Internet" to PlataIcons.Internet,
        "Toll" to PlataIcons.Toll,
        "Parking" to PlataIcons.Parking,
    )

@Composable
fun PaymentsContent(state: PaymentsState, onAction: (PaymentsAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    BoxWithConstraints(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("payments-scroll")
                .verticalScroll(scroll)
                .heightIn(min = maxHeight + TitleScrollAway)
                .padding(bottom = PlataSafeArea.current.bottom + spacing.cardGap),
        ) {
            NavigationBar(style = NavigationBarStyle.Large, title = "Payments", includeStatusBar = true)
            Spacer(Modifier.height(SearchTop))
            SearchField(
                state.query,
                { onAction(PaymentsAction.QueryChanged(it)) },
                Modifier.padding(horizontal = spacing.searchMargin),
                placeholder = "Favorites, bills, providers",
            )
            Spacer(Modifier.height(TilesTop))
            Row(Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin), horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                ActionTile("Top-up", PlataIcons.TopUp, Modifier.weight(1f), iconSize = TileIcon, onClick = { onAction(PaymentsAction.NotBuilt) })
                ActionTile("Transfer", PlataIcons.Transfer, Modifier.weight(1f), iconSize = TileIcon, onClick = { onAction(PaymentsAction.TransferTile) })
            }
            Spacer(Modifier.height(spacing.cardGap))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                SectionCard(
                    title = "Favorites",
                    headerGap = CardHeaderGap,
                    headerTrailing = if (state.favorites.isEmpty()) null else { { SectionCardAction("Edit", { onAction(PaymentsAction.EditFavorites) }) } },
                ) {
                    Row(Modifier.bleed(GridBleed).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(FavoritesGap)) {
                        state.favorites.forEach { favorite ->
                            GridItem(favorite.name, Modifier.width(GridItemWidth), onClick = { onAction(PaymentsAction.NotBuilt) }) { FavoriteArt(favorite) }
                        }
                        GridItem("Add", Modifier.width(GridItemWidth), onClick = { onAction(PaymentsAction.EditFavorites) }) { IconCircle(PlataIcons.Plus, IconCircleSize.S56) }
                    }
                }
                SectionCard(
                    title = "Bill payments",
                    headerGap = CardHeaderGap,
                    headerTrailing = { SectionCardAction("Show all", { onAction(PaymentsAction.NotBuilt) }) },
                ) {
                    Column(Modifier.bleed(GridBleed), verticalArrangement = Arrangement.spacedBy(spacing.s20)) {
                        Billers.chunked(BillerColumns).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BillerGap)) {
                                row.forEach { (label, glyph) ->
                                    GridItem(label, Modifier.weight(1f), onClick = { onAction(PaymentsAction.NotBuilt) }) { IconCircle(glyph, IconCircleSize.S56) }
                                }
                                repeat(BillerColumns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
        // opaque, not the material: the source shows no large title under the inline one (0b2e699a)
        if (collapsed) {
            Box(Modifier.fillMaxWidth().background(PlataTheme.colors.surfaceBackground)) {
                NavigationBar(title = "Payments", includeStatusBar = true)
            }
        }
    }
    Overlay(state.overlay as? PaymentsOverlay.TransferMethod, onDismiss = { onAction(PaymentsAction.CloseOverlay) }) {
        TransferMethodSheet(onAction)
    }
    Overlay(state.overlay as? PaymentsOverlay.FavoriteAdded, onDismiss = { onAction(PaymentsAction.CloseOverlay) }) { added ->
        FavoriteAddedDialog(added.favorite, onClose = { onAction(PaymentsAction.CloseOverlay) })
    }
}

@Composable
private fun TransferMethodSheet(onAction: (PaymentsAction) -> Unit) {
    ModalScrim(onDismiss = { onAction(PaymentsAction.CloseOverlay) }) {
        BottomSheet(Modifier.fillMaxWidth(), title = "Transfer", contentSpacing = PlataTheme.spacing.s4) {
            MethodRow("By CLABE", PlataIcons.DoubleChevronDuotone) { onAction(PaymentsAction.ChooseMethod(TransferMethod.Clabe)) }
            MethodRow("By card number", PlataIcons.Card) { onAction(PaymentsAction.ChooseMethod(TransferMethod.Card)) }
            MethodRow("By phone number to Plata user", PlataIcons.PhoneHandset) { onAction(PaymentsAction.ChooseMethod(TransferMethod.Phone)) }
        }
    }
}

@Composable
private fun MethodRow(title: String, glyph: ImageVector, onClick: () -> Unit) {
    ListRow(
        title,
        showChevron = true,
        onClick = onClick,
        horizontalPadding = 0.dp,
        leading = { IconCircle(glyph, IconCircleSize.S40) },
    )
}

@Composable
private fun FavoriteAddedDialog(favorite: Favorite, onClose: () -> Unit) {
    val what = if (favorite.kind == FavoriteKind.Clabe) "account" else "card"
    DialogPlacement(onDismiss = onClose) {
        Dialog(
            "You added ${favorite.name} to Favorites",
            Modifier.offset(y = AddedDialogLift),
            body = "Transfer money quickly to this $what\nfrom your Favorites list",
            onClose = onClose,
            mediaContent = {
                Image(painterResource(Res.drawable.raster_favorite_added), "Plata card with a star", Modifier.size(AddedArtWidth, AddedArtHeight))
            },
            mediaGap = AddedArtGap,
            actions = { Button("Done", onClose, Modifier.fillMaxWidth()) },
        )
    }
}

private fun Modifier.bleed(amount: Dp) = layout { measurable, constraints ->
    val extra = (amount * 2).roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-amount.roundToPx(), 0) }
}
