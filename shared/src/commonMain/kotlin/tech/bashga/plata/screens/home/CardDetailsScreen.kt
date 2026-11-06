package tech.bashga.plata.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import kotlin.random.Random
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.CardThumb
import tech.bashga.plata.designsystem.components.CardThumbKind
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_dialog_reissue_spin
import plata.shared.generated.resources.raster_dialog_snowflake_spin
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.CardId
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.data.spent
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CardFace
import tech.bashga.plata.designsystem.components.CardOverview
import tech.bashga.plata.designsystem.components.CardOverviewBody
import tech.bashga.plata.designsystem.components.CardProduct
import tech.bashga.plata.designsystem.components.Dialog
import tech.bashga.plata.designsystem.components.DialogPlacement
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SpendOrb
import tech.bashga.plata.designsystem.components.SpendOrbState
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

enum class CardReveal { Hidden, Loading, Shown }

enum class CardDetailsOverlay { FreezeConfirm, ReissueConfirm, DiscardConfirm }

/** [frozen], [available], [spent] and [month] come from the repository; the rest is this sheet's own. */
@Immutable
data class CardDetailsState(
    val card: CardId = CardId.Physical,
    val frozen: Boolean = false,
    val face: CardFace = CardFace.Front,
    val reveal: CardReveal = CardReveal.Hidden,
    val overlay: CardDetailsOverlay? = null,
    val available: Long = 3_500_00,
    val spent: Long = 0,
    val month: Month = Month.June,
    val number: String = "••••",
    val cancelled: Boolean = false,
)

sealed interface CardDetailsAction {
    data object FlipCard : CardDetailsAction

    data object Freeze : CardDetailsAction

    data object ConfirmFreeze : CardDetailsAction

    data object CloseOverlay : CardDetailsAction

    data object Unfreeze : CardDetailsAction

    data object ShowDetails : CardDetailsAction

    data object DetailsLoaded : CardDetailsAction

    data object OpenTransactions : CardDetailsAction

    data object SetLimit : CardDetailsAction

    data object AddToWallet : CardDetailsAction

    data object Reissue : CardDetailsAction

    data object Discard : CardDetailsAction

    data object ConfirmReissue : CardDetailsAction

    /** What [ConfirmReissue] becomes once the ViewModel has drawn the new card's last four digits. */
    data class Reissued(val lastFour: String) : CardDetailsAction

    data object ConfirmDiscard : CardDetailsAction

    data class SecurityOption(val title: String) : CardDetailsAction
}

private val CardDetailsAction.needsLiveCard: Boolean
    get() = this is CardDetailsAction.FlipCard || this is CardDetailsAction.Freeze || this is CardDetailsAction.Unfreeze ||
        this is CardDetailsAction.Reissue || this is CardDetailsAction.Discard || this is CardDetailsAction.Reissued

fun CardDetailsState.reduce(action: CardDetailsAction): CardDetailsState = if (cancelled && action.needsLiveCard) this else when (action) {
    CardDetailsAction.Reissue -> copy(overlay = CardDetailsOverlay.ReissueConfirm)
    is CardDetailsAction.Reissued -> copy(number = action.lastFour, overlay = null)
    CardDetailsAction.Discard -> copy(overlay = CardDetailsOverlay.DiscardConfirm)
    CardDetailsAction.ConfirmDiscard -> copy(cancelled = true, frozen = false, face = CardFace.Front, overlay = null)
    CardDetailsAction.FlipCard -> if (card == CardId.Physical) copy(face = if (face == CardFace.Front) CardFace.Details else CardFace.Front) else this
    CardDetailsAction.Freeze -> copy(overlay = CardDetailsOverlay.FreezeConfirm)
    CardDetailsAction.ConfirmFreeze -> copy(frozen = true, overlay = null)
    CardDetailsAction.CloseOverlay -> copy(overlay = null)
    CardDetailsAction.Unfreeze -> copy(frozen = false)
    CardDetailsAction.ShowDetails -> when (reveal) {
        CardReveal.Hidden -> copy(reveal = CardReveal.Loading, face = CardFace.Details)
        CardReveal.Loading -> this
        CardReveal.Shown -> copy(reveal = CardReveal.Hidden, face = CardFace.Front)
    }
    CardDetailsAction.DetailsLoaded -> if (reveal == CardReveal.Loading) copy(reveal = CardReveal.Shown) else this
    else -> this
}

private fun CardDetailsState.withRepository(snapshot: AppSnapshot) = copy(
    frozen = card in snapshot.cards.frozen,
    available = snapshot.creditAvailable,
    spent = snapshot.spent(),
    month = snapshot.month,
)

class CardDetailsViewModel(private val repository: AppRepository, card: CardId) : ViewModel() {
    private val local = MutableStateFlow(CardDetailsState(card = card))
    val state: StateFlow<CardDetailsState> = combine(repository.state, local) { snapshot, state -> state.withRepository(snapshot) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, local.value.withRepository(repository.snapshot))

    fun onAction(action: CardDetailsAction) {
        val current = state.value
        val resolved = if (action == CardDetailsAction.ConfirmReissue) CardDetailsAction.Reissued(Random.nextInt(10_000).toString().padStart(4, '0')) else action
        val next = current.reduce(resolved)
        if (next.frozen != current.frozen) repository.setFrozen(current.card, next.frozen)
        local.value = next
    }
}

// revealing the pending digital card's numbers is a fake backend call
private const val RevealMillis = 1_200L

/** card/{cardId}: the physical card (flip, freeze) or the pending digital card (reveal). Close is the sheet's swipe down. */
@Composable
fun CardDetailsScreen(card: CardId, onTransactions: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { CardDetailsViewModel(repository, card) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.reveal) {
        if (state.reveal == CardReveal.Loading) {
            delay(RevealMillis)
            viewModel.onAction(CardDetailsAction.DetailsLoaded)
        }
    }
    CardDetailsContent(state, onAction = { action ->
        viewModel.onAction(action)
        if (action == CardDetailsAction.OpenTransactions) onTransactions()
    })
}

// CardOverview's 51-high header box starts 44 below the sheet top (3b3eaed7: y 113)
private val OverviewTop = 44.dp
private val SwipeVelocity = 300f
// fa8cfa51: the 3D snowflake art (82×84 crop) starts 69 under the dialog top; the reissue art shares its box
private val SpinArtWidth = 82.dp
private val SpinArtHeight = 84.33.dp
private val SnowflakeTop = 69.dp
// fa8cfa51: inside the Dimmed sheet the dialog sits 3 higher than DialogPlacement's screen-level inset
private val FreezeDialogLift = 3.dp

// the source never shows revealed numbers; these are placeholders
private const val DigitalNumber = "5274 0193 4482 6150"
private const val DigitalExpires = "08/29"
private const val DigitalCvc = "381"

@Composable
fun CardDetailsContent(state: CardDetailsState, onAction: (CardDetailsAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val flip = { onAction(CardDetailsAction.FlipCard) }
    Column(
        modifier
            .fillMaxSize()
            .background(PlataTheme.colors.surfaceBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(OverviewTop))
        when (state.card) {
            CardId.Physical -> {
                CardOverview(
                    CardProduct.Physical,
                    Modifier.draggable(
                        rememberDraggableState {},
                        Orientation.Horizontal,
                        onDragStopped = { velocity -> if (kotlin.math.abs(velocity) > SwipeVelocity) flip() },
                    ),
                    frozen = state.frozen,
                    face = state.face,
                    amount = formatMoney(state.available),
                    onSetLimit = { onAction(CardDetailsAction.SetLimit) },
                    onCardClick = flip,
                    onAddToWallet = { onAction(CardDetailsAction.AddToWallet) },
                    onFreeze = { onAction(if (state.frozen) CardDetailsAction.Unfreeze else CardDetailsAction.Freeze) },
                    onReissue = { onAction(CardDetailsAction.Reissue) },
                    onDiscard = { onAction(CardDetailsAction.Discard) },
                    cancelled = state.cancelled,
                    number = state.number,
                )
                Column(
                    Modifier.padding(start = spacing.screenMargin, end = spacing.screenMargin, top = spacing.cardGap),
                    verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
                ) {
                    SummaryCard("Transactions", subtitle = spentLabel(state.spent, state.month), onClick = { onAction(CardDetailsAction.OpenTransactions) }) {
                        SpendOrb(if (state.spent > 0) SpendOrbState.Active else SpendOrbState.Empty)
                    }
                    SectionCard(title = "Security options", headerGap = spacing.s8) {
                        listOf("Show PIN", "Change PIN").forEach { title ->
                            ListRow(title, showChevron = true, horizontalPadding = 0.dp, onClick = { onAction(CardDetailsAction.SecurityOption(title)) })
                        }
                    }
                }
            }
            CardId.Digital -> {
                val shown = state.reveal == CardReveal.Shown
                CardOverview(
                    CardProduct.Digital,
                    face = state.face,
                    body = CardOverviewBody.DigitalReveal(loading = state.reveal == CardReveal.Loading),
                    amount = formatMoney(state.available),
                    onShowDetails = { onAction(CardDetailsAction.ShowDetails) },
                    cardNumber = DigitalNumber.takeIf { shown },
                    expires = DigitalExpires.takeIf { shown },
                    cvc = DigitalCvc.takeIf { shown },
                )
            }
        }
        Spacer(Modifier.height(PlataSafeArea.current.bottom + spacing.s20))
    }
    Overlay(state.overlay, onDismiss = { onAction(CardDetailsAction.CloseOverlay) }) { overlay ->
        val close = { onAction(CardDetailsAction.CloseOverlay) }
        DialogPlacement(onDismiss = close) {
            Box(Modifier.padding(bottom = FreezeDialogLift)) {
                when (overlay) {
                    CardDetailsOverlay.FreezeConfirm -> Dialog(
                        "Freeze card",
                        body = "No charges can be made. Unfreeze it\nwhenever you want.",
                        onClose = close,
                        mediaContent = { SpinArt(Res.drawable.raster_dialog_snowflake_spin, SnowflakeSpin) },
                        contentTop = SnowflakeTop,
                        actions = { Button("Freeze", { onAction(CardDetailsAction.ConfirmFreeze) }, Modifier.fillMaxWidth()) },
                    )
                    CardDetailsOverlay.ReissueConfirm -> Dialog(
                        "Reissue card",
                        body = "Get new card details for safer online\npurchases. Services linked to your current\ndetails won’t need updating.",
                        onClose = close,
                        mediaContent = { SpinArt(Res.drawable.raster_dialog_reissue_spin, ReissueSpin) },
                        actions = { Button("Reissue", { onAction(CardDetailsAction.ConfirmReissue) }, Modifier.fillMaxWidth()) },
                    )
                    CardDetailsOverlay.DiscardConfirm -> Dialog(
                        "Discard card",
                        body = "You won’t be able to use this card after\ndiscarding it. This won’t affect your account.",
                        onClose = close,
                        mediaContent = { CardThumb(CardThumbKind.Physical, number = state.number) },
                        actions = { Button("Discard", { onAction(CardDetailsAction.ConfirmDiscard) }, Modifier.fillMaxWidth(), style = ButtonStyle.Destructive) },
                    )
                }
            }
        }
    }
}

/** A frame sheet rendered by the plata-3d-icon spin preset: [frames] cells of [cell] laid out [columns] wide. */
private class SpinSheet(val frames: Int, val columns: Int, val cell: Dp)

// the snowflake repeats every 60°, so 48 frames of a 60° turn make the whole loop at half the arrows' speed
private val SnowflakeSpin = SpinSheet(frames = 48, columns = 7, cell = 88.dp)
private val ReissueSpin = SpinSheet(frames = 72, columns = 9, cell = 97.67.dp)
private const val SpinFps = 60

// the cell overhangs the box so the art keeps fa8cfa51's scale
@Composable
private fun SpinArt(sheet: DrawableResource, spin: SpinSheet) {
    val bitmap = imageResource(sheet)
    val frame by rememberInfiniteTransition(label = "spin").animateFloat(
        0f,
        spin.frames.toFloat(),
        infiniteRepeatable(tween(spin.frames * 1_000 / SpinFps, easing = LinearEasing)),
        label = "frame",
    )
    Spacer(
        Modifier.size(SpinArtWidth, SpinArtHeight).drawBehind {
            val index = frame.toInt().coerceAtMost(spin.frames - 1)
            val source = bitmap.width / spin.columns
            val side = spin.cell.toPx()
            drawImage(
                bitmap,
                srcOffset = IntOffset(index % spin.columns * source, index / spin.columns * source),
                srcSize = IntSize(source, source),
                dstOffset = IntOffset(((size.width - side) / 2).roundToInt(), ((size.height - side) / 2).roundToInt()),
                dstSize = IntSize(side.roundToInt(), side.roundToInt()),
            )
        },
    )
}
