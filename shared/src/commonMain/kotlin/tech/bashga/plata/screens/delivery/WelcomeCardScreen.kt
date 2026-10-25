package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.bashga.plata.designsystem.components.CardArt
import tech.bashga.plata.designsystem.components.CardProduct
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.ToastHost
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

enum class WelcomeToast(val message: String) { FeedbackThanks("Thanks for your feedback") }

@Immutable
data class WelcomeCardState(val toast: WelcomeToast? = null)

sealed interface WelcomeCardAction {
    data object Explore : WelcomeCardAction

    data object ContactSupport : WelcomeCardAction

    data object DismissToast : WelcomeCardAction
}

class WelcomeCardViewModel(feedbackThanks: Boolean) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeCardState(if (feedbackThanks) WelcomeToast.FeedbackThanks else null))
    val state: StateFlow<WelcomeCardState> = _state.asStateFlow()

    fun dismissToast() {
        _state.value = _state.value.copy(toast = null)
    }
}

/** activation/welcome, the root of the Activation modal; [feedbackThanks] shows the toast Send feedback leads to. */
@Composable
fun WelcomeCardScreen(feedbackThanks: Boolean, onExplore: () -> Unit, onContactSupport: () -> Unit) {
    val viewModel = viewModel { WelcomeCardViewModel(feedbackThanks) }
    val state by viewModel.state.collectAsState()
    WelcomeCardContent(state, onAction = { action ->
        when (action) {
            WelcomeCardAction.Explore -> onExplore()
            WelcomeCardAction.ContactSupport -> onContactSupport()
            WelcomeCardAction.DismissToast -> viewModel.dismissToast()
        }
    })
}

// dc6f5c96: the card at y 195, the title box 7 above the other intros', the body 15 under the title
private val CardTop = 136.dp
private val TextTop = 452.dp
private val TitleToBody = 15.dp

// 5b9474e2: the toast hangs 8 under the safe-area top
private val ToastTop = 8.dp

@Composable
fun WelcomeCardContent(state: WelcomeCardState, onAction: (WelcomeCardAction) -> Unit, modifier: Modifier = Modifier) {
    val body = buildAnnotatedString {
        append("For your security, make sure the last 4\ndigits of your card are: $CardLastDigits. If not,\n")
        withLink(
            LinkAnnotation.Clickable("contact-support", TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline))) {
                onAction(WelcomeCardAction.ContactSupport)
            },
        ) { append("contact support") }
    }
    ActivationPage(
        "Explore Plata",
        { onAction(WelcomeCardAction.Explore) },
        modifier,
        textTop = TextTop,
        art = {
            CardArt(CardProduct.Credito, Modifier.align(Alignment.TopCenter).offset(y = PlataSafeArea.current.top + CardTop))
        },
    ) {
        HeadlineBlock(
            "Welcome to\nPlata, $CustomerName",
            Modifier.fillMaxWidth().padding(horizontal = PlataTheme.spacing.s20),
            size = HeadlineBlockSize.Medium,
            align = HeadlineBlockAlign.Center,
        )
        Box(Modifier.padding(top = TitleToBody).fillMaxWidth().wrapContentWidth()) {
            PlataText(body, PlataTheme.typography.bodyCompact, PlataTheme.colors.textPrimary, textAlign = TextAlign.Center)
        }
    }
    Overlay(state.toast, onDismiss = { onAction(WelcomeCardAction.DismissToast) }, dismissOnBack = false, coversContent = false) { toast ->
        Box(Modifier.fillMaxSize().padding(top = PlataSafeArea.current.top + ToastTop), contentAlignment = Alignment.TopCenter) {
            ToastHost(toast.message, onDismiss = { onAction(WelcomeCardAction.DismissToast) })
        }
    }
}
