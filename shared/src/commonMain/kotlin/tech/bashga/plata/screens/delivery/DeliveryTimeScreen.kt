package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import plata.shared.generated.resources.raster_delivery_calendar
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.DeliveryDay
import tech.bashga.plata.data.DeliverySlot
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.Chip
import tech.bashga.plata.designsystem.components.ChoiceCard
import tech.bashga.plata.designsystem.components.ChoiceCardLayout
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SectionHeader
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@Immutable
data class DeliveryTimeState(
    val day: DeliveryDay = DeliveryDay.Today,
    val slot: DeliverySlot? = null,
    val slots: Map<DeliveryDay, List<DeliverySlot>> = emptyMap(),
) {
    val daySlots: List<DeliverySlot> get() = slots[day].orEmpty()

    /** The first day with a free slot, offered when the chosen day has none. */
    internal val closest: Pair<DayOption, DeliverySlot>?
        get() = deliveryDays.firstNotNullOfOrNull { option -> slots[option.day]?.firstOrNull()?.let { option to it } }
}

sealed interface DeliveryTimeAction {
    data class Day(val day: DeliveryDay) : DeliveryTimeAction

    data object SelectClosest : DeliveryTimeAction

    data class Slot(val slot: DeliverySlot) : DeliveryTimeAction

    data object SetUp : DeliveryTimeAction

    data object Back : DeliveryTimeAction

    data object Close : DeliveryTimeAction
}

fun DeliveryTimeState.reduce(action: DeliveryTimeAction): DeliveryTimeState = when (action) {
    is DeliveryTimeAction.Day -> copy(day = action.day, slot = null)
    DeliveryTimeAction.SelectClosest -> closest?.let { (option, slot) -> copy(day = option.day, slot = slot) } ?: this
    is DeliveryTimeAction.Slot -> if (action.slot in daySlots) copy(slot = action.slot) else this
    DeliveryTimeAction.SetUp, DeliveryTimeAction.Back, DeliveryTimeAction.Close -> this
}

class DeliveryTimeViewModel(private val repository: AppRepository) : ViewModel() {
    private val picked = MutableStateFlow(DeliveryTimeState())
    val state: StateFlow<DeliveryTimeState> = combine(picked, repository.state) { state, snapshot -> state.copy(slots = snapshot.deliverySlots) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, picked.value.copy(slots = repository.snapshot.deliverySlots))

    fun onAction(action: DeliveryTimeAction) {
        picked.value = state.value.reduce(action)
    }

    /** Saves the chosen time; false while no slot is picked. */
    fun setUp(): Boolean {
        val current = state.value
        val slot = current.slot ?: return false
        repository.setUpDelivery(current.day, slot, current.day.date)
        return true
    }
}

/** delivery/time, presented over its caller (DeliveryAddress or RescheduleReason): ‹ dismisses it, Close dismisses all. */
@Composable
fun DeliveryTimeScreen(onSetUp: () -> Unit, onBack: () -> Unit, onClose: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { DeliveryTimeViewModel(repository) }
    val state by viewModel.state.collectAsState()
    DeliveryTimeContent(state, onAction = { action ->
        when (action) {
            DeliveryTimeAction.SetUp -> if (viewModel.setUp()) onSetUp()
            DeliveryTimeAction.Back -> onBack()
            DeliveryTimeAction.Close -> onClose()
            else -> viewModel.onAction(action)
        }
    })
}

// eb4cdf05: the calendar art 79 under the day tiles; the title 18 under the sheet bar
private val CalendarTop = 79.dp
private val TitleTop = 18.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliveryTimeContent(state: DeliveryTimeState, onAction: (DeliveryTimeAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    Column(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            onBack = { onAction(DeliveryTimeAction.Back) },
            trailing = { TextButton("Close", { onAction(DeliveryTimeAction.Close) }) },
        )
        Column(Modifier.fillMaxSize().padding(horizontal = spacing.screenMargin)) {
            Spacer(Modifier.height(TitleTop))
            HeadlineBlock("Delivery time", Modifier.fillMaxWidth(), body = "The interview will take about 10 minutes")
            Spacer(Modifier.height(spacing.s24 + spacing.s4))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.s12)) {
                deliveryDays.forEach { option ->
                    ChoiceCard(
                        option.title,
                        Modifier.weight(1f),
                        layout = ChoiceCardLayout.Tile,
                        selected = option.day == state.day,
                        subtitle = option.subtitle,
                        onClick = { onAction(DeliveryTimeAction.Day(option.day)) },
                    )
                }
            }
            if (state.daySlots.isEmpty()) {
                NoSlots(state, onAction)
            } else {
                Spacer(Modifier.height(spacing.s40))
                SectionHeader("Available time slots")
                Spacer(Modifier.height(spacing.s20))
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s12),
                    verticalArrangement = Arrangement.spacedBy(spacing.s12),
                    maxItemsInEachRow = 2,
                ) {
                    state.daySlots.forEach { slot ->
                        Chip(slot.label, Modifier.weight(1f), selected = slot == state.slot, onClick = { onAction(DeliveryTimeAction.Slot(slot)) })
                    }
                    if (state.daySlots.size % 2 == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.weight(1f))
                Button(
                    "Set up delivery",
                    { onAction(DeliveryTimeAction.SetUp) },
                    Modifier.fillMaxWidth().padding(bottom = spacing.s20 + PlataSafeArea.current.bottom),
                    enabled = state.slot != null,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.NoSlots(state: DeliveryTimeState, onAction: (DeliveryTimeAction) -> Unit) {
    val spacing = PlataTheme.spacing
    val day = deliveryDays.first { it.day == state.day }
    val closest = state.closest
    Spacer(Modifier.height(CalendarTop))
    Image(
        painterResource(Res.drawable.raster_delivery_calendar),
        contentDescription = null,
        modifier = Modifier.align(Alignment.CenterHorizontally).size(100.dp, 94.dp),
    )
    Spacer(Modifier.height(spacing.s40))
    HeadlineBlock(
        if (closest != null) "The closest time slot available is\non ${closest.first.longDate}, ${closest.second.start}" else "No time slots available",
        Modifier.fillMaxWidth(),
        body = "Sorry, there are no time slots available\nfor ${if (day.day == DeliveryDay.Today) "today" else "this day"}",
        size = HeadlineBlockSize.Small,
        align = HeadlineBlockAlign.Center,
    )
    if (closest != null) {
        Spacer(Modifier.height(spacing.s32 + spacing.s4))
        Button(
            "Select the closest slot",
            { onAction(DeliveryTimeAction.SelectClosest) },
            Modifier.align(Alignment.CenterHorizontally).height(46.dp),
            size = ButtonSize.Medium,
            icon = PlataIcons.Lightning,
            iconSize = PlataTheme.sizes.iconLarge,
            labelStyle = PlataTheme.typography.headline,
            contentPadding = PaddingValues(start = 10.dp, end = spacing.s12),
        )
    }
}

/** "Tue, 27 May" → "May 27". */
private val DayOption.longDate: String
    get() = subtitle.substringAfter(", ").split(' ').let { (day, month) -> "$month $day" }

private val DeliverySlot.start: String get() = label.substringBefore(" –")
