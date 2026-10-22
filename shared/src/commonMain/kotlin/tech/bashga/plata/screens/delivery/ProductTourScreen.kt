package tech.bashga.plata.screens.delivery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_intro_billers
import plata.shared.generated.resources.raster_intro_digital_cards
import plata.shared.generated.resources.raster_intro_pin_phone
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataBackHandler

enum class TourPage { ViewPin, GoDigital, CommissionFree }

@Immutable
data class ProductTourState(val page: TourPage = TourPage.ViewPin)

sealed interface ProductTourAction {
    data object Next : ProductTourAction

    data object Back : ProductTourAction

    data object ShowPin : ProductTourAction

    data object Finish : ProductTourAction
}

/** Back on the first page and Next on the last leave the tour; the screen handles those before reducing. */
fun ProductTourState.reduce(action: ProductTourAction): ProductTourState = when (action) {
    ProductTourAction.Next -> copy(page = TourPage.entries.getOrElse(page.ordinal + 1) { page })
    ProductTourAction.Back -> copy(page = TourPage.entries.getOrElse(page.ordinal - 1) { page })
    ProductTourAction.ShowPin, ProductTourAction.Finish -> this
}

class ProductTourViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(ProductTourState())
    val state: StateFlow<ProductTourState> = _state.asStateFlow()

    fun onAction(action: ProductTourAction) {
        _state.value = _state.value.reduce(action)
    }

    /** Let's Plata! or Close: the modal dismisses, event AvatarGenerated. */
    fun finish() = repository.finishActivation()
}

/** activation/tour: three pages; back from the first pops to RewardAmbassador. */
@Composable
fun ProductTourScreen(onShowPin: () -> Unit, onBack: () -> Unit, onFinished: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { ProductTourViewModel(repository) }
    val state by viewModel.state.collectAsState()
    PlataBackHandler(enabled = state.page != TourPage.ViewPin) { viewModel.onAction(ProductTourAction.Back) }
    ProductTourContent(state, onAction = { action ->
        when {
            action == ProductTourAction.ShowPin -> onShowPin()
            action == ProductTourAction.Back && state.page == TourPage.ViewPin -> onBack()
            action == ProductTourAction.Finish || (action == ProductTourAction.Next && state.page == TourPage.CommissionFree) -> {
                viewModel.finish()
                onFinished()
            }
            else -> viewModel.onAction(action)
        }
    })
}

// f5984c79: the phone art at (60, 120), "Show PIN" 19 under the body
private val PinArtX = 60.dp
private val PinArtY = 61.dp
private val BodyToButton = 19.dp

// 72a00afe: the tilted cards and their haze span the width from y 120
private val DigitalArtY = 61.dp

// f4ff1f66: the biller cluster at (50, 125)
private val BillersArtX = 50.dp
private val BillersArtY = 66.dp

@Composable
fun ProductTourContent(state: ProductTourState, onAction: (ProductTourAction) -> Unit, modifier: Modifier = Modifier) {
    AnimatedContent(
        state.page,
        modifier,
        transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            slideInHorizontally(tween()) { if (forward) it else -it } togetherWith slideOutHorizontally(tween()) { if (forward) -it else it }
        },
        label = "tour",
    ) { page ->
        val top = PlataSafeArea.current.top
        ActivationPage(
            if (page == TourPage.CommissionFree) "Let’s Plata!" else "Next",
            { onAction(ProductTourAction.Next) },
            onBack = { onAction(ProductTourAction.Back) },
            onClose = if (page != TourPage.CommissionFree) ({ onAction(ProductTourAction.Finish) }) else null,
            art = {
                when (page) {
                    TourPage.ViewPin -> Image(painterResource(Res.drawable.raster_intro_pin_phone), null, Modifier.offset(PinArtX, top + PinArtY).size(273.dp, 376.dp))
                    TourPage.GoDigital -> Image(painterResource(Res.drawable.raster_intro_digital_cards), null, Modifier.offset(y = top + DigitalArtY).size(393.dp, 380.dp))
                    TourPage.CommissionFree -> Image(painterResource(Res.drawable.raster_intro_billers), null, Modifier.offset(BillersArtX, top + BillersArtY).size(300.dp, 342.dp))
                }
            },
        ) {
            val (title, body) = when (page) {
                TourPage.ViewPin -> "View your PIN in\ncard details" to "Tap the physical card and scroll to\nSecurity options"
                TourPage.GoDigital -> "Go digital!" to "Safer online purchases!\nUse a digital card to shop online"
                TourPage.CommissionFree -> "Commission-free\npayments" to "Pay your bills with no fees"
            }
            HeadlineBlock(
                title,
                Modifier.fillMaxWidth().padding(horizontal = PlataTheme.spacing.s20),
                body = body,
                size = HeadlineBlockSize.Medium,
                align = HeadlineBlockAlign.Center,
                gap = IntroBodyGap,
            )
            if (page == TourPage.ViewPin) {
                Spacer(Modifier.height(BodyToButton))
                Button("Show PIN", { onAction(ProductTourAction.ShowPin) }, style = ButtonStyle.SecondaryOnCard, size = ButtonSize.Small)
            }
        }
    }
}
