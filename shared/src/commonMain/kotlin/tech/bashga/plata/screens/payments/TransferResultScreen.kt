package tech.bashga.plata.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_plata_inversiones
import plata.shared.generated.resources.raster_logo_santander
import tech.bashga.plata.data.Currency
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.BalanceChange
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.MethodCardBank
import tech.bashga.plata.designsystem.components.TransferResult
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.graph.TransferResultRoute

/** Amounts in cents; [sourceBefore]/[sourceAfter] are the own balance (MXN) around the transfer. */
sealed interface TransferOutcome {
    val sourceBefore: Long
    val sourceAfter: Long

    @Immutable
    data class External(
        val recipient: String,
        val account: String,
        val bank: MethodCardBank?,
        val amount: Long,
        val commission: Long,
        override val sourceBefore: Long,
        override val sourceAfter: Long,
    ) : TransferOutcome

    /** [amount] USD cents into Plata Inversiones, whose cash goes [cashBefore] → [cashBefore] + [amount]. */
    @Immutable
    data class BetweenAccounts(
        val amount: Long,
        val cashBefore: Long,
        override val sourceBefore: Long,
        override val sourceAfter: Long,
    ) : TransferOutcome
}

@Immutable
data class TransferResultState(val result: TransferOutcome)

sealed interface TransferResultAction {
    /** (no ref) */
    data object Receipt : TransferResultAction

    data object Done : TransferResultAction
}

val TransferResultRoute.outcome: TransferOutcome
    get() = if (usd) {
        TransferOutcome.BetweenAccounts(amount, cashBefore, balanceBefore, balanceAfter)
    } else {
        TransferOutcome.External(recipient, account, MethodCardBank.entries.firstOrNull { it.name == bank }, amount, commission, balanceBefore, balanceAfter)
    }

/** transfer/result: nothing on it changes, so the state comes straight from the route. */
@Composable
fun TransferResultScreen(result: TransferOutcome, onDone: () -> Unit) {
    TransferResultContent(TransferResultState(result), { if (it == TransferResultAction.Done) onDone() })
}

// 372e6fd9: the bank logo's top sits 111 below the safe-area top; 705c4f5e: 153
private val ExternalTop = 111.dp
private val BetweenTop = 153.dp

@Composable
fun TransferResultContent(state: TransferResultState, onAction: (TransferResultAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    val source = BalanceChange(formatMoney(state.result.sourceBefore, showCents = true), formatMoney(state.result.sourceAfter, showCents = true))
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        when (val result = state.result) {
            is TransferOutcome.External -> TransferResult(
                name = result.recipient,
                amount = "–${formatMoney(result.amount)}",
                source = "From Plata Crédito",
                sourceChange = source,
                modifier = Modifier.fillMaxWidth().padding(top = safeArea.top + ExternalTop),
                account = result.account,
                caption = if (result.commission > 0) "Commission up to ${formatMoney(result.commission)}" else null,
                onReceipt = { onAction(TransferResultAction.Receipt) },
            ) {
                if (result.bank == MethodCardBank.Santander) {
                    BrandLogo(painterResource(Res.drawable.raster_logo_santander), BrandLogoSize.S96, showCheck = true)
                } else {
                    IconCircle(PlataIcons.Bank, IconCircleSize.S80)
                }
            }
            is TransferOutcome.BetweenAccounts -> TransferResult(
                name = "Plata Inversiones",
                amount = formatMoney(result.sourceBefore - result.sourceAfter),
                source = "From Plata Crédito",
                sourceChange = source,
                modifier = Modifier.fillMaxWidth().padding(top = safeArea.top + BetweenTop),
                nameChange = BalanceChange(formatMoney(result.cashBefore, Currency.Usd), formatMoney(result.cashBefore + result.amount, Currency.Usd)),
            ) { BrandLogo(painterResource(Res.drawable.raster_logo_plata_inversiones), BrandLogoSize.S96, showCheck = true) }
        }
        Button(
            "Done",
            { onAction(TransferResultAction.Done) },
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = safeArea.bottom + spacing.s20),
        )
    }
}
