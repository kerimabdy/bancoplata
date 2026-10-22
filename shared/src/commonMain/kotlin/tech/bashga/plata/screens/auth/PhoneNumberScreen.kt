package tech.bashga.plata.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AuthStep
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CodeField
import tech.bashga.plata.designsystem.components.Footnote
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.SheetOverlay

const val PHONE_LENGTH = 10
const val OTP_LENGTH = 4
const val OTP_RESEND_SECONDS = 4

sealed interface PhoneOverlay {
    data class Otp(val code: String = "", val resendIn: Int = OTP_RESEND_SECONDS) : PhoneOverlay
}

@Immutable
data class PhoneNumberState(
    val phone: String = "",
    val overlay: PhoneOverlay? = null,
) {
    val canContinue: Boolean get() = phone.length == PHONE_LENGTH
}

sealed interface PhoneNumberAction {
    data class TypePhone(val text: String) : PhoneNumberAction

    data object Continue : PhoneNumberAction

    data object PrivacyNotice : PhoneNumberAction

    data class TypeCode(val code: String) : PhoneNumberAction

    data object Resend : PhoneNumberAction

    data object CancelOtp : PhoneNumberAction
}

fun PhoneNumberState.reduce(action: PhoneNumberAction): PhoneNumberState {
    val otp = overlay as? PhoneOverlay.Otp
    return when (action) {
        is PhoneNumberAction.TypePhone -> copy(phone = action.text.filter(Char::isDigit).take(PHONE_LENGTH))
        PhoneNumberAction.Continue -> if (canContinue) copy(overlay = PhoneOverlay.Otp()) else this
        PhoneNumberAction.PrivacyNotice -> this
        is PhoneNumberAction.TypeCode -> otp?.let { copy(overlay = it.copy(code = action.code.filter(Char::isDigit).take(OTP_LENGTH))) } ?: this
        PhoneNumberAction.Resend -> if (otp != null && otp.resendIn == 0) copy(overlay = otp.copy(resendIn = OTP_RESEND_SECONDS)) else this
        PhoneNumberAction.CancelOtp -> copy(overlay = null)
    }
}

fun PhoneNumberState.tick(): PhoneNumberState {
    val otp = overlay as? PhoneOverlay.Otp ?: return this
    return copy(overlay = otp.copy(resendIn = (otp.resendIn - 1).coerceAtLeast(0)))
}

/** The fake backend accepts any complete code; [steps] then says where auth goes next. */
class PhoneNumberViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(PhoneNumberState(phone = repository.snapshot.session.phone))
    val state: StateFlow<PhoneNumberState> = _state.asStateFlow()

    private val _steps = Channel<AuthStep>(Channel.BUFFERED)
    val steps: Flow<AuthStep> = _steps.receiveAsFlow()

    private var countdown: Job? = null

    fun onAction(action: PhoneNumberAction) {
        val before = _state.value
        var next = before.reduce(action)
        val otp = next.overlay as? PhoneOverlay.Otp
        if (otp != null && otp.code.length == OTP_LENGTH) {
            next = next.copy(overlay = null)
            _steps.trySend(repository.confirmOtp(next.phone))
        }
        _state.value = next
        when {
            next.overlay == null -> countdown?.cancel()
            action == PhoneNumberAction.Continue || action == PhoneNumberAction.Resend -> if (next != before) startCountdown()
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            while ((_state.value.overlay as? PhoneOverlay.Otp)?.resendIn?.let { it > 0 } == true) {
                delay(1_000)
                _state.value = _state.value.tick()
            }
        }
    }
}

/** auth/phone: the number, then the Otp sheet; [onOtpConfirmed] gets where auth goes next. */
@Composable
fun PhoneNumberScreen(
    splashLockups: Boolean,
    onOtpConfirmed: (AuthStep) -> Unit,
    onPrivacyNotice: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PhoneNumberViewModel(repository) }
    val state by viewModel.state.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current
    val confirmed by rememberUpdatedState(onOtpConfirmed)
    LaunchedEffect(viewModel) {
        viewModel.steps.collect { step ->
            keyboard?.hide()
            confirmed(step)
        }
    }
    PhoneNumberContent(
        state,
        onAction = { action ->
            if (action == PhoneNumberAction.PrivacyNotice) onPrivacyNotice()
            viewModel.onAction(action)
        },
        splashLockups = splashLockups,
    )
}

/** "55 1234 5678" for a complete number. */
internal fun formatPhone(digits: String): String =
    if (digits.length == PHONE_LENGTH) "${digits.take(2)} ${digits.substring(2, 6)} ${digits.substring(6)}" else digits

// f8f29bdc: title box 40 under the safe area, field at y 244, footnote 28 above the home-indicator inset
private val BodyToField = 32.dp
private val FootnoteBottom = 28.dp

/** [focusCode] focuses the code field when the Otp sheet opens; a0a0ef2b is captured without its caret. */
@Composable
fun PhoneNumberContent(
    state: PhoneNumberState,
    onAction: (PhoneNumberAction) -> Unit,
    modifier: Modifier = Modifier,
    splashLockups: Boolean = false,
    focusCode: Boolean = true,
) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = spacing.s40)) {
            Spacer(Modifier.height(safeArea.top + spacing.s40))
            HeadlineBlock("Enter your\nphone number", Modifier.fillMaxWidth(), body = "To log in or join Plata")
            Spacer(Modifier.height(BodyToField))
            TextField(
                state.phone,
                { onAction(PhoneNumberAction.TypePhone(it)) },
                Modifier.fillMaxWidth().testTag("phone"),
                placeholder = "Phone number",
                label = null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Spacer(Modifier.height(spacing.s20))
            Button("Continue", { onAction(PhoneNumberAction.Continue) }, Modifier.fillMaxWidth(), enabled = state.canContinue)
        }
        Footnote(
            "By continuing you agree to our",
            Modifier.align(Alignment.BottomCenter).padding(bottom = safeArea.bottom + FootnoteBottom),
            link = "Privacy notice",
            onLinkClick = { onAction(PhoneNumberAction.PrivacyNotice) },
        )
        if (splashLockups) SplashLockupsFadingOut()
    }
    SheetOverlay(state.overlay as? PhoneOverlay.Otp, onDismiss = { onAction(PhoneNumberAction.CancelOtp) }) { otp ->
        OtpSheet(otp, state.phone, onAction, focusCode)
    }
}

// a0a0ef2b: the prompt starts 96 under the bar, the code cells at y 288, the timer 35 below them
private val BarToPrompt = 96.dp
private val PromptToCells = 25.dp
private val CellsToTimer = 35.dp

@Composable
private fun OtpSheet(otp: PhoneOverlay.Otp, phone: String, onAction: (PhoneNumberAction) -> Unit, focusCode: Boolean) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (focusCode) focusRequester.requestFocus() }
    Column(Modifier.fillMaxSize().background(colors.surfaceBackground), horizontalAlignment = Alignment.CenterHorizontally) {
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            title = "Confirmation",
            leading = { TextButton("Cancel", { onAction(PhoneNumberAction.CancelOtp) }) },
        )
        Spacer(Modifier.height(BarToPrompt))
        PlataText("Enter the code sent to\n+52 ${formatPhone(phone)}", typography.body, colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(PromptToCells))
        CodeField(
            otp.code,
            { onAction(PhoneNumberAction.TypeCode(it)) },
            Modifier.testTag("otp"),
            length = OTP_LENGTH,
            focusRequester = focusRequester,
        )
        Spacer(Modifier.height(CellsToTimer))
        if (otp.resendIn > 0) {
            PlataText("Resend in ${otp.resendIn} sec", typography.body, colors.textSecondary)
        } else {
            TextButton("Resend code", { onAction(PhoneNumberAction.Resend) })
        }
    }
}
