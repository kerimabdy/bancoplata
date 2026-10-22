package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.ChoiceCard
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.icons.ClappingHands
import tech.bashga.plata.designsystem.icons.DollarCircle
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.Smile
import tech.bashga.plata.designsystem.theme.PlataTheme

/** A reward option; amounts in MXN cents. Custom's amount entry has no reference, so it keeps the $100 placeholder. */
sealed interface Reward {
    val amount: Long

    data object AllGood : Reward {
        override val amount = 20_00L
    }

    data object ThankYou : Reward {
        override val amount = 30_00L
    }

    data object LoveIt : Reward {
        override val amount = 50_00L
    }

    data class Custom(override val amount: Long = 100_00L) : Reward
}

@Immutable
data class RewardAmbassadorState(val selected: Reward = Reward.ThankYou) {
    val cta: String get() = "Reward ${formatMoney(selected.amount)}"
}

sealed interface RewardAmbassadorAction {
    data class Select(val reward: Reward) : RewardAmbassadorAction

    data object Confirm : RewardAmbassadorAction

    data object Back : RewardAmbassadorAction
}

class RewardAmbassadorViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(RewardAmbassadorState())
    val state: StateFlow<RewardAmbassadorState> = _state.asStateFlow()

    fun select(reward: Reward) {
        _state.value = _state.value.copy(selected = reward)
    }

    /** A pending charge on the credit line that settles on the next cold launch. */
    fun reward() = repository.rewardAmbassador(_state.value.selected.amount)
}

/** activation/reward: tip the ambassador; the CTA follows the selection. */
@Composable
fun RewardAmbassadorScreen(onRewarded: () -> Unit, onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { RewardAmbassadorViewModel(repository) }
    val state by viewModel.state.collectAsState()
    RewardAmbassadorContent(state, onAction = { action ->
        when (action) {
            is RewardAmbassadorAction.Select -> viewModel.select(action.reward)
            RewardAmbassadorAction.Confirm -> {
                viewModel.reward()
                onRewarded()
            }
            RewardAmbassadorAction.Back -> onBack()
        }
    })
}

private class RewardOption(val reward: Reward, val label: String, val icon: ImageVector)

private val rewardOptions = listOf(
    RewardOption(Reward.AllGood, "All good!", PlataIcons.Smile),
    RewardOption(Reward.ThankYou, "Thank you!", PlataIcons.Heart),
    RewardOption(Reward.LoveIt, "Love it!", PlataIcons.ClappingHands),
    RewardOption(Reward.Custom(), "Custom amount", PlataIcons.DollarCircle),
)

// 3cbc958b: the title box 58 under the safe area, the body 8 under it (tighter than HeadlineBlock medium), the first option at y 306.7
private val TitleTop = 58.dp
private val TitleToBody = 8.dp
private val BodyToOptions = 40.dp

@Composable
fun RewardAmbassadorContent(state: RewardAmbassadorState, onAction: (RewardAmbassadorAction) -> Unit, modifier: Modifier = Modifier) {
    ActivationPage(
        state.cta,
        { onAction(RewardAmbassadorAction.Confirm) },
        modifier,
        onBack = { onAction(RewardAmbassadorAction.Back) },
        background = PlataTheme.colors.surfaceBackground,
        textTop = TitleTop,
        art = {},
    ) {
        HeadlineBlock(
            "Reward the ambassador,\nwho delivered your\nPlata Card",
            Modifier.fillMaxWidth().padding(horizontal = PlataTheme.spacing.s20),
            body = "Gratification goes to ambassador,\nnot Plata",
            size = HeadlineBlockSize.Medium,
            align = HeadlineBlockAlign.Center,
            gap = TitleToBody,
        )
        Spacer(Modifier.height(BodyToOptions))
        Column(
            Modifier.padding(horizontal = PlataTheme.spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s20),
        ) {
            rewardOptions.forEach { option ->
                val selected = option.reward::class == state.selected::class
                val amount = if (selected) state.selected.amount else option.reward.amount
                ChoiceCard(
                    option.label,
                    Modifier.fillMaxWidth(),
                    selected = selected,
                    icon = option.icon,
                    subtitle = formatMoney(amount),
                    valueMuted = option.reward is Reward.Custom,
                    onClick = { onAction(RewardAmbassadorAction.Select(if (selected) state.selected else option.reward)) },
                )
            }
        }
    }
}
