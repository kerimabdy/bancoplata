package tech.bashga.plata.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.AccountCard
import tech.bashga.plata.designsystem.components.AmountField
import tech.bashga.plata.designsystem.components.AmountFieldLayout
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.MethodCardInfoButton
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.Toggle
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.InfoFill
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.graph.TransferResultRoute

/** [ownBalance] is the Plata Crédito own balance the amount leaves, in MXN cents. */
@Immutable
data class TransferAmountState(
    val recipient: TransferRecipient,
    val ownBalance: Long,
    val amount: String = "",
    val addToFavorites: Boolean = false,
) {
    val cents: Long get() = amountCents(amount)
    val canTransfer: Boolean get() = cents in 1..ownBalance
    val note: String? get() = if (cents > 0) "Commission up to ${formatMoney(TRANSFER_COMMISSION)}" else null
}

sealed interface TransferAmountAction {
    data class AmountChanged(val text: String) : TransferAmountAction

    data class AddToFavoritesChanged(val on: Boolean) : TransferAmountAction

    data object Transfer : TransferAmountAction

    data object Back : TransferAmountAction

    data object Cancel : TransferAmountAction
}

fun TransferAmountState.reduce(action: TransferAmountAction): TransferAmountState = when (action) {
    is TransferAmountAction.AmountChanged -> copy(amount = sanitizeAmount(action.text))
    is TransferAmountAction.AddToFavoritesChanged -> copy(addToFavorites = action.on)
    TransferAmountAction.Transfer, TransferAmountAction.Back, TransferAmountAction.Cancel -> this
}

// reads the balance once: observing its own transfer write recomposes this entry after the dismissed sheet destroyed it
class TransferAmountViewModel(private val repository: AppRepository, recipient: TransferRecipient) : ViewModel() {
    private val local = MutableStateFlow(TransferAmountState(recipient, repository.snapshot.accounts.ownBalance))
    val state: StateFlow<TransferAmountState> = local.asStateFlow()

    fun onAction(action: TransferAmountAction) {
        local.value = local.value.reduce(action)
    }

    /** Transfer: writes the ledger and returns what TransferResult shows, or null when the amount isn't payable. */
    fun transfer(): TransferResultRoute? {
        val state = local.value.copy(ownBalance = repository.snapshot.accounts.ownBalance)
        if (!state.canTransfer) return null
        val recipient = state.recipient
        val bank = recipient.bank
        val receipt = repository.transfer(bank?.displayName ?: recipient.name, state.cents, TRANSFER_COMMISSION)
        if (state.addToFavorites) {
            val kind = bank.favoriteKind(recipient.method)
            repository.addFavorite(Favorite("${kind.name.lowercase()}-${recipient.digits}", kind, recipient.name, recipient.digits))
        }
        return TransferResultRoute(
            recipient = recipient.name,
            amount = receipt.amount,
            usd = false,
            commission = receipt.commission,
            balanceBefore = receipt.balanceBefore,
            balanceAfter = receipt.balanceAfter,
            account = bank?.let { "${it.displayName} •• ${lastFour(recipient.digits)}" } ?: accountLabel(recipient.digits, recipient.method),
            bank = bank?.name.orEmpty(),
        )
    }
}

/** transfer/amount: how much to send to [recipient] from the own balance; [onTransferred] gets the result. */
@Composable
fun TransferAmountScreen(
    recipient: TransferRecipient,
    onTransferred: (TransferResultRoute) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { TransferAmountViewModel(repository, recipient) }
    val state by viewModel.state.collectAsState()
    TransferAmountContent(state, { action ->
        when (action) {
            TransferAmountAction.Transfer -> viewModel.transfer()?.let(onTransferred)
            TransferAmountAction.Back -> onBack()
            TransferAmountAction.Cancel -> onCancel()
            else -> viewModel.onAction(action)
        }
    }, autoFocus = true)
}

// a6fae664: the first card 22 under the sheet bar before the focused field scrolls the form to its end
private val FormTop = 22.dp


@Composable
fun TransferAmountContent(
    state: TransferAmountState,
    onAction: (TransferAmountAction) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val spacing = PlataTheme.spacing
    val bottom = PlataSafeArea.current.bottom
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val amountFocus = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { amountFocus.requestFocus() }
    val recipient = state.recipient
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = PlataTheme.sizes.navSheet + FormTop, start = spacing.screenMargin, end = spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.s20),
        ) {
            AccountCard(formatMoney(state.ownBalance, showCents = true), "From Plata Crédito", Modifier.fillMaxWidth())
            RecipientMethodCard(
                recipient.name,
                recipient.bank,
                Modifier.fillMaxWidth(),
                subtitle = accountLabel(recipient.digits, recipient.method),
                trailing = { MethodCardInfoButton({}) },
            )
            ListGroup {
                ListRow(
                    "Add to Favorites",
                    trailing = { Toggle(state.addToFavorites, { onAction(TransferAmountAction.AddToFavoritesChanged(it)) }) },
                )
            }
            AmountField(
                state.amount,
                { onAction(TransferAmountAction.AmountChanged(it)) },
                Modifier
                    .fillMaxWidth()
                    .testTag("amount")
                    .onFocusChanged { if (it.hasFocus) scope.launch { scroll.scrollToEnd() } },
                prefix = "$",
                note = state.note,
                noteIcon = PlataIcons.InfoFill,
                layout = AmountFieldLayout.SingleRow,
                focusRequester = amountFocus,
            )
            Spacer(Modifier.height(PlataTheme.sizes.buttonLarge + bottom + FocusedFormEndGap))
        }
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            title = "Transfer",
            onBack = { onAction(TransferAmountAction.Back) },
            trailing = { TextButton("Cancel", { onAction(TransferAmountAction.Cancel) }) },
            collapsed = rememberCollapsed(scroll),
        )
        Button(
            "Transfer",
            { onAction(TransferAmountAction.Transfer) },
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = bottom + spacing.s20),
        )
    }
}
