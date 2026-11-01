package tech.bashga.plata.screens.payments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Currency
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.PESOS_PER_DOLLAR_CENTS
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.AccountCard
import tech.bashga.plata.designsystem.components.AmountField
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.IconTile
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlataWordmark
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.graph.TransferResultRoute

/** [amount] is typed in USD; [ownBalance] (MXN cents) pays for it at [rate] pesos per dollar, into [inversionesCash] (USD cents). */
@Immutable
data class BetweenAccountsState(
    val ownBalance: Long,
    val inversionesCash: Long,
    val amount: String = "",
    val rate: Long = PESOS_PER_DOLLAR_CENTS,
) {
    val usdCents: Long get() = amountCents(amount)
    val pesos: Long get() = usdCents * rate / 100
    val canTransfer: Boolean get() = usdCents > 0 && pesos <= ownBalance
    val conversion: String get() = if (amount.isEmpty()) "${formatMoney(100, Currency.Usd)} = ${formatMoney(rate)}" else formatMoney(pesos)
    val cta: String get() = if (amount.isEmpty()) "Transfer" else "Transfer ${formatMoney(pesos)}"
}

sealed interface BetweenAccountsAction {
    data class AmountChanged(val text: String) : BetweenAccountsAction

    data object Transfer : BetweenAccountsAction

    data object Cancel : BetweenAccountsAction
}

fun BetweenAccountsState.reduce(action: BetweenAccountsAction): BetweenAccountsState = when (action) {
    is BetweenAccountsAction.AmountChanged -> copy(amount = sanitizeAmount(action.text, maxLength = 7))
    BetweenAccountsAction.Transfer, BetweenAccountsAction.Cancel -> this
}

class BetweenAccountsViewModel(private val repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(BetweenAccountsState(repository.snapshot.accounts.ownBalance, repository.snapshot.accounts.inversionesCashUsd))
    val state: StateFlow<BetweenAccountsState> = combine(local, repository.state) { state, snapshot ->
        state.copy(ownBalance = snapshot.accounts.ownBalance, inversionesCash = snapshot.accounts.inversionesCashUsd)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, local.value)

    fun onAction(action: BetweenAccountsAction) {
        local.value = local.value.reduce(action)
    }

    /** Transfer: moves the money and returns what TransferResult shows, or null when it isn't payable. */
    fun transfer(): TransferResultRoute? {
        val accounts = repository.snapshot.accounts
        val state = local.value.copy(ownBalance = accounts.ownBalance)
        if (!state.canTransfer) return null
        val receipt = repository.transferToInversiones(state.usdCents)
        return TransferResultRoute(
            recipient = receipt.recipient,
            amount = receipt.amount,
            usd = true,
            commission = 0,
            balanceBefore = receipt.balanceBefore,
            balanceAfter = receipt.balanceAfter,
            cashBefore = accounts.inversionesCashUsd,
        )
    }
}

/** transfer/between: Plata Crédito → Plata Inversiones, in dollars. */
@Composable
fun BetweenAccountsScreen(onTransferred: (TransferResultRoute) -> Unit, onCancel: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { BetweenAccountsViewModel(repository) }
    val state by viewModel.state.collectAsState()
    BetweenAccountsContent(state, { action ->
        when (action) {
            BetweenAccountsAction.Transfer -> viewModel.transfer()?.let(onTransferred)
            BetweenAccountsAction.Cancel -> onCancel()
            else -> viewModel.onAction(action)
        }
    }, autoFocus = true)
}

// 6e1c2ab5: the first card 22 under the sheet bar; the wordmark is PlataAppTile's
private val FormTop = 22.dp
private val WordmarkWidth = 32.dp
private val WordmarkHeight = 4.8.dp

@Composable
fun BetweenAccountsContent(
    state: BetweenAccountsState,
    onAction: (BetweenAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val spacing = PlataTheme.spacing
    val amountFocus = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { amountFocus.requestFocus() }
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier.padding(top = PlataTheme.sizes.navSheet + FormTop, start = spacing.screenMargin, end = spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.s20),
        ) {
            AccountCard(formatMoney(state.ownBalance, showCents = true), "From Plata Crédito", Modifier.fillMaxWidth())
            AccountCard(formatMoney(state.inversionesCash, Currency.Usd), "To Plata Inversiones", Modifier.fillMaxWidth()) { PlataInversionesTile() }
            AmountField(
                state.amount,
                { onAction(BetweenAccountsAction.AmountChanged(it)) },
                Modifier.fillMaxWidth().testTag("amount"),
                conversion = state.conversion,
                note = if (state.amount.isEmpty()) null else "No commission",
                focusRequester = amountFocus,
            )
        }
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            title = "Between accounts",
            leading = { TextButton("Cancel", { onAction(BetweenAccountsAction.Cancel) }) },
        )
        Button(
            state.cta,
            { onAction(BetweenAccountsAction.Transfer) },
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = PlataSafeArea.current.bottom + spacing.s20),
        )
    }
}

@Composable
private fun PlataInversionesTile() {
    IconTile(fill = PlataTheme.gradients.plataInversiones) {
        Image(
            rememberVectorPainter(PlataIcons.PlataWordmark),
            contentDescription = "Plata Inversiones",
            modifier = Modifier.size(WordmarkWidth, WordmarkHeight),
            colorFilter = ColorFilter.tint(PlataTheme.colors.textOnDark),
        )
    }
}
