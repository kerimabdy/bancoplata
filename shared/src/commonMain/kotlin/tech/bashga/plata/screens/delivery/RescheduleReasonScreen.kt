package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ChoiceCard
import tech.bashga.plata.designsystem.components.ChoiceCardLayout
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class RescheduleReason(val label: String) {
    InconvenientTime("Inconvenient delivery time"),
    PlansChanged("Plans changed"),
    AmbassadorAsked("Ambassador asked for it"),
    ForgotIne("Forgot INE"),
    AmbassadorNoContact("Ambassador didn't call/write"),
    Other("Other reasons"),
}

@Immutable
data class RescheduleReasonState(val reason: RescheduleReason? = null, val otherText: String = "") {
    val canContinue: Boolean get() = reason != null && (reason != RescheduleReason.Other || otherText.isNotBlank())
}

sealed interface RescheduleReasonAction {
    data class Pick(val reason: RescheduleReason) : RescheduleReasonAction

    data class OtherText(val text: String) : RescheduleReasonAction

    data object Continue : RescheduleReasonAction

    data object Close : RescheduleReasonAction
}

fun RescheduleReasonState.reduce(action: RescheduleReasonAction): RescheduleReasonState = when (action) {
    is RescheduleReasonAction.Pick -> copy(reason = action.reason)
    is RescheduleReasonAction.OtherText -> copy(reason = RescheduleReason.Other, otherText = action.text)
    RescheduleReasonAction.Continue, RescheduleReasonAction.Close -> this
}

class RescheduleReasonViewModel : ViewModel() {
    private val _state = MutableStateFlow(RescheduleReasonState())
    val state: StateFlow<RescheduleReasonState> = _state.asStateFlow()

    fun onAction(action: RescheduleReasonAction) {
        _state.value = _state.value.reduce(action)
    }
}

/** delivery/reschedule: why the user moves the delivery, then DeliveryTime. The reason isn't sent anywhere (no backend). */
@Composable
fun RescheduleReasonScreen(onContinue: () -> Unit, onClose: () -> Unit) {
    val viewModel = viewModel { RescheduleReasonViewModel() }
    val state by viewModel.state.collectAsState()
    RescheduleReasonContent(state, onAction = { action ->
        when (action) {
            RescheduleReasonAction.Continue -> if (state.canContinue) onContinue()
            RescheduleReasonAction.Close -> onClose()
            else -> viewModel.onAction(action)
        }
    })
}

// 586ac28a: title 21 under the sheet bar, reasons 29 under the title and 8 apart
private val TitleTop = 21.dp
private val ReasonsTop = 29.dp

@Composable
fun RescheduleReasonContent(state: RescheduleReasonState, onAction: (RescheduleReasonAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.padding(top = PlataTheme.sizes.navSheet, start = spacing.screenMargin, end = spacing.screenMargin)) {
            Spacer(Modifier.height(TitleTop))
            HeadlineBlock("Why do you want to\nreschedule your\ndelivery?")
            Spacer(Modifier.height(ReasonsTop))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s8)) {
                RescheduleReason.entries.filter { it != RescheduleReason.Other }.forEach { reason ->
                    ChoiceCard(
                        reason.label,
                        Modifier.fillMaxWidth(),
                        layout = ChoiceCardLayout.Centered,
                        selected = state.reason == reason,
                        onClick = { onAction(RescheduleReasonAction.Pick(reason)) },
                    )
                }
                TextField(
                    state.otherText,
                    { onAction(RescheduleReasonAction.OtherText(it)) },
                    Modifier.fillMaxWidth().testTag("other"),
                    placeholder = RescheduleReason.Other.label,
                    label = null,
                    placeholderColor = PlataTheme.colors.controlOff,
                )
            }
        }
        NavigationBar(style = NavigationBarStyle.Sheet, trailing = { TextButton("Close", { onAction(RescheduleReasonAction.Close) }) })
        // the source draws Continue at full strength before a reason is picked; a tap without one does nothing
        Button(
            "Continue",
            { onAction(RescheduleReasonAction.Continue) },
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = PlataSafeArea.current.bottom + spacing.s20),
        )
    }
}
