package tech.bashga.plata.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_lock_background
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Keypad
import tech.bashga.plata.designsystem.components.KeypadKey
import tech.bashga.plata.designsystem.components.KeypadStyle
import tech.bashga.plata.designsystem.components.PasscodeDotTone
import tech.bashga.plata.designsystem.components.PasscodeDots
import tech.bashga.plata.designsystem.components.PasscodeDotsState
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class LockStatus { Entering, Error, Unlocked, LoggedOut }

@Immutable
data class LockState(
    val entered: String = "",
    val length: Int = PASSCODE_LENGTH,
    val status: LockStatus = LockStatus.Entering,
)

sealed interface LockAction {
    data class Digit(val digit: Int) : LockAction

    data object LogOut : LockAction

    data object FaceId : LockAction
}

/** [passcode] is the stored one; a full entry either unlocks or clears with an error. */
fun LockState.reduce(action: LockAction, passcode: String): LockState {
    if (status == LockStatus.Unlocked || status == LockStatus.LoggedOut) return this
    return when (action) {
        is LockAction.Digit -> {
            val next = entered.plusDigit(action.digit, length)
            when {
                next.length < length -> copy(entered = next, status = LockStatus.Entering)
                next == passcode -> copy(entered = next, status = LockStatus.Unlocked)
                else -> copy(entered = "", status = LockStatus.Error)
            }
        }
        LockAction.FaceId -> copy(status = LockStatus.Unlocked)
        LockAction.LogOut -> copy(status = LockStatus.LoggedOut)
    }
}

class LockViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(LockState(length = repository.snapshot.session.passcode.length.takeIf { it > 0 } ?: PASSCODE_LENGTH))
    val state: StateFlow<LockState> = _state.asStateFlow()

    fun onAction(action: LockAction) {
        val next = _state.value.reduce(action, repository.snapshot.session.passcode)
        if (next.status == LockStatus.LoggedOut && _state.value.status != LockStatus.LoggedOut) repository.logOut()
        _state.value = next
    }
}

/** lock: the app-entry gate. [splashLockups] when it replaced Splash, so the launch lockups fade out over it. */
@Composable
fun LockScreen(splashLockups: Boolean, onUnlocked: () -> Unit, onLoggedOut: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { LockViewModel(repository) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.status) {
        when (state.status) {
            LockStatus.Unlocked -> onUnlocked()
            LockStatus.LoggedOut -> onLoggedOut()
            LockStatus.Entering, LockStatus.Error -> Unit
        }
    }
    LockContent(state, viewModel::onAction, splashLockups = splashLockups)
}

// b76b9dda: title box at y 215.5, dots at 298, keypad bottom 14 above the home-indicator inset
private val TitleTop = 156.5.dp
private val TitleToDots = 57.5.dp
private val KeypadBottom = 14.dp

@Composable
fun LockContent(state: LockState, onAction: (LockAction) -> Unit, modifier: Modifier = Modifier, splashLockups: Boolean = false) {
    val safeArea = PlataSafeArea.current
    val accent = PlataTheme.colors.accent
    Box(modifier.fillMaxSize()) {
        Image(painterResource(Res.drawable.raster_lock_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Column(Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(safeArea.top + TitleTop))
            PlataText("Enter passcode", PlataTheme.typography.title3, PlataTheme.colors.onGradientInk)
            Spacer(Modifier.height(TitleToDots))
            PasscodeDots(
                filled = state.entered.length,
                count = state.length,
                state = if (state.status == LockStatus.Error) PasscodeDotsState.Error else PasscodeDotsState.Entry,
                tone = PasscodeDotTone.OnGradient,
            )
        }
        Keypad(
            onDigit = { onAction(LockAction.Digit(it)) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = safeArea.bottom + KeypadBottom),
            style = KeypadStyle.OnGradient,
            leading = { KeypadKey(PlataIcons.Logout, { onAction(LockAction.LogOut) }, tint = accent, contentDescription = "Log out") },
            trailing = { KeypadKey(PlataIcons.FaceId, { onAction(LockAction.FaceId) }, tint = accent, contentDescription = "Face ID") },
        )
        if (splashLockups) SplashLockupsFadingOut()
    }
}
