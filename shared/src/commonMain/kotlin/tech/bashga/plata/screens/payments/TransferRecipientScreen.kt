package tech.bashga.plata.screens.payments

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.BottomSheet
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CheckCircle
import tech.bashga.plata.designsystem.components.CheckCircleState
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.MethodCardBank
import tech.bashga.plata.designsystem.components.ModalScrim
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonTone
import tech.bashga.plata.designsystem.components.TextButtonWeight
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.components.TextFieldClearButton
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

enum class TransferPurpose { Send, AddFavorite }

enum class TransferExtra(val label: String, val placeholder: String) {
    Reference("Reference number", "Reference number"),
    Reason("Reason for payment", "Reason for payment"),
}

data class AdditionalFields(val draft: Set<TransferExtra>)

/** Who the money goes to, handed from TransferRecipient to TransferAmount. [digits] is the CLABE or card number. */
@Immutable
data class TransferRecipient(val name: String, val digits: String, val method: TransferMethod) {
    val bank: MethodCardBank? get() = detectBank(digits, method)
}

/** [number] is what the field shows (cards grouped in fours); the bank and a Plata card's holder follow from it. */
@Immutable
data class TransferRecipientState(
    val purpose: TransferPurpose = TransferPurpose.Send,
    val method: TransferMethod = TransferMethod.Clabe,
    val number: String = "",
    val name: String = "",
    val extras: Set<TransferExtra> = emptySet(),
    val reference: String = "",
    val reason: String = "",
    val overlay: AdditionalFields? = null,
) {
    val digits: String get() = number.filter(Char::isDigit)
    val bank: MethodCardBank? get() = detectBank(digits, method)
    val isComplete: Boolean get() = digits.length == method.digits && name.isNotBlank()
    val recipient: TransferRecipient get() = TransferRecipient(name.trim(), digits, method)
}

sealed interface TransferRecipientAction {
    data class NumberChanged(val text: String) : TransferRecipientAction

    data class NameChanged(val text: String) : TransferRecipientAction

    data class ExtraChanged(val extra: TransferExtra, val text: String) : TransferRecipientAction

    data object OpenAdditionalFields : TransferRecipientAction

    data class ToggleDraft(val extra: TransferExtra) : TransferRecipientAction

    data object ConfirmAdditionalFields : TransferRecipientAction

    data object CloseOverlay : TransferRecipientAction

    data object Continue : TransferRecipientAction

    data object AddToFavorites : TransferRecipientAction

    data object Cancel : TransferRecipientAction
}

fun TransferRecipientState.reduce(action: TransferRecipientAction): TransferRecipientState = when (action) {
    is TransferRecipientAction.NumberChanged -> {
        val digits = action.text.filter(Char::isDigit).take(method.digits)
        val wasPlata = bank == MethodCardBank.Plata
        val next = copy(number = formatAccountNumber(digits, method))
        when {
            next.bank == MethodCardBank.Plata && name.isBlank() -> next.copy(name = PLATA_CARD_HOLDER)
            wasPlata && next.bank != MethodCardBank.Plata && name == PLATA_CARD_HOLDER -> next.copy(name = "")
            else -> next
        }
    }
    is TransferRecipientAction.NameChanged -> copy(name = action.text)
    is TransferRecipientAction.ExtraChanged -> when (action.extra) {
        TransferExtra.Reference -> copy(reference = action.text.filter(Char::isDigit).take(7))
        TransferExtra.Reason -> copy(reason = action.text.take(40))
    }
    TransferRecipientAction.OpenAdditionalFields -> copy(overlay = AdditionalFields(extras))
    is TransferRecipientAction.ToggleDraft -> overlay?.let { copy(overlay = AdditionalFields(if (action.extra in it.draft) it.draft - action.extra else it.draft + action.extra)) } ?: this
    TransferRecipientAction.ConfirmAdditionalFields -> overlay?.let {
        copy(
            extras = it.draft,
            reference = if (TransferExtra.Reference in it.draft) reference else "",
            reason = if (TransferExtra.Reason in it.draft) reason else "",
            overlay = null,
        )
    } ?: this
    TransferRecipientAction.CloseOverlay -> copy(overlay = null)
    TransferRecipientAction.Continue, TransferRecipientAction.AddToFavorites, TransferRecipientAction.Cancel -> this
}

class TransferRecipientViewModel(
    private val repository: AppRepository,
    purpose: TransferPurpose,
    method: TransferMethod,
) : ViewModel() {
    private val _state = MutableStateFlow(TransferRecipientState(purpose, method))
    val state: StateFlow<TransferRecipientState> = _state.asStateFlow()

    fun onAction(action: TransferRecipientAction) {
        _state.value = _state.value.reduce(action)
    }

    /** Add to Favorites: prepends the recipient and returns it, or null while the form is incomplete. */
    fun addFavorite(): Favorite? {
        val state = _state.value
        if (!state.isComplete) return null
        val favorite = Favorite("${state.bank.favoriteKind(state.method).name.lowercase()}-${state.digits}", state.bank.favoriteKind(state.method), state.name.trim(), state.digits)
        repository.addFavorite(favorite)
        return favorite
    }
}

/**
 * transfer/recipient: the CLABE or card form. Send continues to [onContinue]; AddFavorite saves the favorite and hands
 * its id to [onFavoriteAdded].
 */
@Composable
fun TransferRecipientScreen(
    purpose: TransferPurpose,
    method: TransferMethod,
    onContinue: (TransferRecipient) -> Unit,
    onFavoriteAdded: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { TransferRecipientViewModel(repository, purpose, method) }
    val state by viewModel.state.collectAsState()
    TransferRecipientContent(state, { action ->
        when (action) {
            TransferRecipientAction.Continue -> if (state.isComplete) onContinue(state.recipient)
            TransferRecipientAction.AddToFavorites -> viewModel.addFavorite()?.let { onFavoriteAdded(it.id) }
            TransferRecipientAction.Cancel -> onCancel()
            else -> viewModel.onAction(action)
        }
    }, autoFocus = true)
}

// 43e51dd1: first card 22 under the sheet bar
private val FormTop = 22.dp

// the link's 44 hit box would put its text 31 under the last field; the source has 21
private val AdditionalFieldsLift = 10.dp

/**
 * [autoFocus] puts the caret in the number field on first composition, as the sheet opens with the keyboard up.
 * The CTA rides on the safe-area bottom, which includes the IME.
 */
@Composable
fun TransferRecipientContent(
    state: TransferRecipientState,
    onAction: (TransferRecipientAction) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val spacing = PlataTheme.spacing
    val bottom = PlataSafeArea.current.bottom
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val numberFocus = remember { FocusRequester() }
    var focused by remember { mutableStateOf<String?>(null) }
    if (autoFocus) LaunchedEffect(Unit) { numberFocus.requestFocus() }
    fun Modifier.tracksFocus(tag: String, scrollToEnd: Boolean = false) = testTag(tag).onFocusChanged {
        if (it.hasFocus) {
            focused = tag
            if (scrollToEnd) scope.launch { scroll.scrollToEnd() }
        } else if (focused == tag) {
            focused = null
        }
    }
    val send = state.purpose == TransferPurpose.Send
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .backdropSource()
                .verticalScroll(scroll)
                .padding(top = PlataTheme.sizes.navSheet + FormTop, start = spacing.screenMargin, end = spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.s20),
        ) {
            RecipientMethodCard(
                if (state.method == TransferMethod.Card) "By card number" else "By CLABE",
                state.bank,
                Modifier.fillMaxWidth(),
                field = {
                    TextField(
                        state.number,
                        { onAction(TransferRecipientAction.NumberChanged(it)) },
                        Modifier.fillMaxWidth().tracksFocus("number"),
                        placeholder = "${state.method.digits} digits",
                        label = if (state.method == TransferMethod.Card) "Card number" else "CLABE",
                        alwaysShowLabel = true,
                        keyboardOptions = NumberKeyboard,
                        trailing = clearButton(focused == "number" && state.number.isNotEmpty()) { onAction(TransferRecipientAction.NumberChanged("")) },
                        focusRequester = numberFocus,
                    )
                },
            )
            TextField(
                state.name,
                { onAction(TransferRecipientAction.NameChanged(it)) },
                Modifier.fillMaxWidth().tracksFocus("name"),
                placeholder = "Beneficiary name",
                trailing = clearButton(focused == "name" && state.name.isNotEmpty()) { onAction(TransferRecipientAction.NameChanged("")) },
            )
            TransferExtra.entries.filter { it in state.extras }.forEach { extra ->
                val value = if (extra == TransferExtra.Reference) state.reference else state.reason
                val tag = extra.name.lowercase()
                TextField(
                    value,
                    { onAction(TransferRecipientAction.ExtraChanged(extra, it)) },
                    Modifier.fillMaxWidth().tracksFocus(tag, scrollToEnd = true),
                    placeholder = extra.placeholder,
                    keyboardOptions = if (extra == TransferExtra.Reference) NumberKeyboard else KeyboardOptions.Default,
                    trailing = clearButton(focused == tag && value.isNotEmpty()) { onAction(TransferRecipientAction.ExtraChanged(extra, "")) },
                )
            }
            Box(Modifier.fillMaxWidth().offset(y = -AdditionalFieldsLift), contentAlignment = Alignment.Center) {
                TextButton(
                    "Additional fields",
                    { onAction(TransferRecipientAction.OpenAdditionalFields) },
                    tone = TextButtonTone.Primary,
                    weight = TextButtonWeight.Semibold,
                    leadingIcon = PlataIcons.Plus,
                )
            }
            Spacer(Modifier.height(PlataTheme.sizes.buttonLarge + bottom + FocusedFormEndGap))
        }
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            title = "Transfer",
            leading = if (send) null else { { TextButton("Cancel", { onAction(TransferRecipientAction.Cancel) }) } },
            trailing = if (send) { { TextButton("Cancel", { onAction(TransferRecipientAction.Cancel) }) } } else null,
            collapsed = rememberCollapsed(scroll),
        )
        Button(
            if (send) "Continue" else "Add to Favorites",
            { onAction(if (send) TransferRecipientAction.Continue else TransferRecipientAction.AddToFavorites) },
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = bottom + spacing.s20),
        )
    }
    Overlay(state.overlay, onDismiss = { onAction(TransferRecipientAction.CloseOverlay) }) { overlay ->
        ModalScrim(onDismiss = { onAction(TransferRecipientAction.CloseOverlay) }) {
            BottomSheet(
                Modifier.fillMaxWidth(),
                title = "Additional fields",
                footer = { Button("Done", { onAction(TransferRecipientAction.ConfirmAdditionalFields) }, Modifier.fillMaxWidth()) },
            ) {
                TransferExtra.entries.forEach { extra ->
                    ListRow(
                        extra.label,
                        Modifier.fillMaxWidth(),
                        onClick = { onAction(TransferRecipientAction.ToggleDraft(extra)) },
                        horizontalPadding = 0.dp,
                        trailing = { CheckCircle(if (extra in overlay.draft) CheckCircleState.Checked else CheckCircleState.Unchecked) },
                    )
                }
            }
        }
    }
}

private fun clearButton(show: Boolean, onClear: () -> Unit): (@Composable () -> Unit)? =
    if (show) { { TextFieldClearButton(onClear) } } else null

internal suspend fun ScrollState.scrollToEnd() = animateScrollTo(maxValue)
