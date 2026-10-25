package tech.bashga.plata.screens.home.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.CardId
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.data.spent
import tech.bashga.plata.designsystem.components.ActionTile
import tech.bashga.plata.designsystem.components.BalanceHeader
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.CardThumb
import tech.bashga.plata.designsystem.components.CardThumbKind
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonSize
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.SpendOrb
import tech.bashga.plata.designsystem.components.SpendOrbState
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.components.SummaryCardTrailingAlignment
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonWeight
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.components.TextFieldSurface
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.BillPay
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.TopUp
import tech.bashga.plata.designsystem.icons.Transfer
import tech.bashga.plata.designsystem.theme.PlataTheme

/** Cents throughout; [month] picks the "spent in" label and the [status] line. */
@Immutable
data class PlataCreditoState(
    val available: Long,
    val totalBalance: Long,
    val spent: Long,
    val month: Month,
    val status: CreditStatus = month.creditStatus,
    val clabe: String = "646180 1570 0000 4821",
) {
    val statusLine: String
        get() = when (status) {
            is CreditStatus.NothingDue -> "No payments due right now.\nStatement expected on ${status.nextStatement}"
            is CreditStatus.Paid -> "Everything is paid.\nStatement expected on ${status.nextStatement}"
        }
}

fun AppSnapshot.toPlataCreditoState() = PlataCreditoState(creditAvailable, creditTotalBalance, spent(), month)

sealed interface PlataCreditoAction {
    data object Back : PlataCreditoAction

    data object CreditPaymentDetails : PlataCreditoAction

    data object Transactions : PlataCreditoAction

    data class Card(val card: CardId) : PlataCreditoAction
}

class PlataCreditoViewModel(repository: AppRepository) : ViewModel() {
    val state: StateFlow<PlataCreditoState> = repository.state
        .map { it.toPlataCreditoState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.snapshot.toPlataCreditoState())
}

/** home/credito: the credit line from the ledger. */
@Composable
fun PlataCreditoScreen(onBack: () -> Unit, onCreditPayment: () -> Unit, onTransactions: () -> Unit, onCard: (CardId) -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PlataCreditoViewModel(repository) }
    val state by viewModel.state.collectAsState()
    PlataCreditoContent(state, { action ->
        when (action) {
            PlataCreditoAction.Back -> onBack()
            PlataCreditoAction.CreditPaymentDetails -> onCreditPayment()
            PlataCreditoAction.Transactions -> onTransactions()
            is PlataCreditoAction.Card -> onCard(action.card)
        }
    })
}

// 5e61179c: header and cards row sit 40 in, cards 38 under the sublink and 40 above the tiles; the two-tone tile glyphs are 32
private val HeaderInset = 40.dp
private val TileGlyph = 32.dp
private val CardsTop = 38.dp

// the bar row starts 5 above the safe-area top (iOS draws it from the status bar's bottom)
private val StatusBarOverlap = 5.dp

@Composable
fun PlataCreditoContent(state: PlataCreditoState, onAction: (PlataCreditoAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = safeArea.top - StatusBarOverlap + PlataTheme.sizes.nav, bottom = safeArea.bottom + spacing.s20),
        ) {
            Spacer(Modifier.height(spacing.s20))
            BalanceHeader(
                formatMoney(state.available),
                Modifier.fillMaxWidth().padding(horizontal = HeaderInset),
                caption = "Available in Plata Crédito",
                sublink = "${formatMoney(state.totalBalance)} Total balance",
                onSublinkClick = {},
                trailing = { Button("Withdraw", {}, style = ButtonStyle.WhiteAccent, size = ButtonSize.XSmall, icon = PlataIcons.Msi) },
            )
            Spacer(Modifier.height(CardsTop))
            Row(Modifier.padding(horizontal = HeaderInset), horizontalArrangement = Arrangement.spacedBy(spacing.s12)) {
                CardThumb(CardThumbKind.Physical, onClick = { onAction(PlataCreditoAction.Card(CardId.Physical)) })
                CardThumb(CardThumbKind.Digital, onClick = { onAction(PlataCreditoAction.Card(CardId.Digital)) })
                CardThumb(CardThumbKind.Add, onClick = {})
            }
            Spacer(Modifier.height(spacing.s40))
            Column(
                Modifier.padding(horizontal = spacing.screenMargin),
                verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.s20)) {
                    ActionTile("Top-up", PlataIcons.TopUp, Modifier.weight(1f), iconSize = TileGlyph, onClick = {})
                    ActionTile("Bill pay", PlataIcons.BillPay, Modifier.weight(1f), iconSize = TileGlyph, onClick = {})
                    ActionTile("Transfer", PlataIcons.Transfer, Modifier.weight(1f), iconSize = TileGlyph, onClick = {})
                }
                SummaryCard(
                    "Transactions",
                    subtitle = if (state.spent > 0) "${formatMoney(state.spent)} Spent in ${state.month.label}" else "Nothing spent in ${state.month.label}",
                    onClick = { onAction(PlataCreditoAction.Transactions) },
                ) {
                    SpendOrb(if (state.spent > 0) SpendOrbState.Active else SpendOrbState.Empty)
                }
                SummaryCard(
                    "Credit payment",
                    subtitle = state.statusLine,
                    trailingAlignment = SummaryCardTrailingAlignment.Top,
                ) {
                    TextButton("Details", { onAction(PlataCreditoAction.CreditPaymentDetails) }, weight = TextButtonWeight.Semibold)
                }
                SummaryCard(
                    "Top-up with CLABE",
                    trailingAlignment = SummaryCardTrailingAlignment.Top,
                    body = {
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s12), verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                state.clabe,
                                {},
                                Modifier.weight(1f),
                                surface = TextFieldSurface.Grey,
                                readOnly = true,
                                trailing = { Icon(PlataIcons.Copy, size = PlataTheme.sizes.iconMedium, tint = PlataTheme.colors.textSecondary) },
                            )
                            IconButton(PlataIcons.ArrowUp, {}, style = IconButtonStyle.Subtle, size = IconButtonSize.S48, contentDescription = "Share")
                        }
                    },
                ) {
                    TextButton("Details", {}, weight = TextButtonWeight.Semibold)
                }
            }
        }
        NavigationBar(onBack = { onAction(PlataCreditoAction.Back) }, collapsed = rememberCollapsed(scroll), includeStatusBar = true)
    }
}
