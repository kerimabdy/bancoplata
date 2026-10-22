package tech.bashga.plata.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Keypad
import tech.bashga.plata.designsystem.components.KeypadKey
import tech.bashga.plata.designsystem.components.PasscodeDots
import tech.bashga.plata.designsystem.components.PasscodeDotsState
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

const val PASSCODE_LENGTH = 6

private const val SuccessDelayMillis = 800L

enum class PasscodeMode { Create, Confirm }

enum class PasscodeStatus { Entering, Mismatch, Success }

/** [first] is the Create entry, kept for the Confirm comparison; it is never drawn. */
@Immutable
data class PasscodeState(
    val mode: PasscodeMode = PasscodeMode.Create,
    val entered: String = "",
    val first: String? = null,
    val status: PasscodeStatus = PasscodeStatus.Entering,
) {
    val title: String
        get() = when {
            status == PasscodeStatus.Success -> "Passcode was set!"
            mode == PasscodeMode.Create -> "Create passcode"
            else -> "Confirm passcode"
        }
}

sealed interface PasscodeAction {
    data class Digit(val digit: Int) : PasscodeAction

    data object Delete : PasscodeAction
}

/** Keypad digit entry, shared with Lock: appends a digit until [length] are in. */
fun String.plusDigit(digit: Int, length: Int = PASSCODE_LENGTH): String = if (this.length >= length) this else this + digit

fun PasscodeState.reduce(action: PasscodeAction): PasscodeState {
    if (status == PasscodeStatus.Success) return this
    return when (action) {
        is PasscodeAction.Digit -> {
            val next = entered.plusDigit(action.digit)
            when {
                next.length < PASSCODE_LENGTH -> copy(entered = next, status = PasscodeStatus.Entering)
                mode == PasscodeMode.Create -> copy(mode = PasscodeMode.Confirm, first = next, entered = "", status = PasscodeStatus.Entering)
                next == first -> copy(entered = next, status = PasscodeStatus.Success)
                else -> copy(entered = "", status = PasscodeStatus.Mismatch)
            }
        }
        PasscodeAction.Delete -> copy(entered = entered.dropLast(1))
    }
}

class PasscodeViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(PasscodeState())
    val state: StateFlow<PasscodeState> = _state.asStateFlow()

    fun onAction(action: PasscodeAction) {
        val next = _state.value.reduce(action)
        if (next.status == PasscodeStatus.Success && _state.value.status != PasscodeStatus.Success) repository.savePasscode(next.entered)
        _state.value = next
    }
}

/** auth/passcode: create a 6-digit passcode, confirm it, then [onPasscodeSet] after the green "set" beat. */
@Composable
fun PasscodeScreen(onPasscodeSet: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PasscodeViewModel(repository) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.status) {
        if (state.status == PasscodeStatus.Success) {
            delay(SuccessDelayMillis)
            onPasscodeSet()
        }
    }
    PasscodeContent(state, viewModel::onAction)
}

// 1bad6d9f, efe5d661, c126aa45: title box at y 205, dots at 288, keypad bottom 14 above the home-indicator inset
private val TitleTop = 146.dp
private val TitleToDots = 58.dp
private val KeypadBottom = 14.dp

@Composable
fun PasscodeContent(state: PasscodeState, onAction: (PasscodeAction) -> Unit, modifier: Modifier = Modifier) {
    val safeArea = PlataSafeArea.current
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceCard)) {
        Column(Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(safeArea.top + TitleTop))
            PlataText(state.title, PlataTheme.typography.title3, PlataTheme.colors.textPrimary)
            Spacer(Modifier.height(TitleToDots))
            PasscodeDots(
                filled = state.entered.length,
                count = PASSCODE_LENGTH,
                state = when (state.status) {
                    PasscodeStatus.Entering -> PasscodeDotsState.Entry
                    PasscodeStatus.Mismatch -> PasscodeDotsState.Error
                    PasscodeStatus.Success -> PasscodeDotsState.Success
                },
            )
        }
        Keypad(
            onDigit = { onAction(PasscodeAction.Digit(it)) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = safeArea.bottom + KeypadBottom),
            trailing = if (state.entered.isNotEmpty()) {
                { KeypadKey(PlataIcons.Delete, { onAction(PasscodeAction.Delete) }, contentDescription = "Delete") }
            } else {
                null
            },
        )
    }
}
