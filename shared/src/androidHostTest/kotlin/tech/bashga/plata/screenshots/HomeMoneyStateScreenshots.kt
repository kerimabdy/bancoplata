package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerAccount
import tech.bashga.plata.data.LedgerEntry
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.Currency
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.screens.home.money.CreditPaymentContent
import tech.bashga.plata.screens.home.money.CreditPaymentState
import tech.bashga.plata.screens.home.money.MsiHomeContent
import tech.bashga.plata.screens.home.money.MsiHomeState
import tech.bashga.plata.screens.home.money.MsiOverlay
import tech.bashga.plata.screens.home.money.MsiPartnerDetailContent
import tech.bashga.plata.screens.home.money.MsiPartnerDetailState
import tech.bashga.plata.screens.home.money.MsiPartners
import tech.bashga.plata.screens.home.money.MsiPartnersContent
import tech.bashga.plata.screens.home.money.MsiPartnersState
import tech.bashga.plata.screens.home.money.PlataCreditoContent
import tech.bashga.plata.screens.home.money.PlataCreditoState
import tech.bashga.plata.screens.home.money.TerminalProvider
import tech.bashga.plata.screens.home.money.TransactionsContent
import tech.bashga.plata.screens.home.money.TransactionsState
import tech.bashga.plata.screens.home.money.creditStatus

/** SCREEN-MAP §3.3, Transactions to MsiPartners: one test per reference id, named after it. */
class HomeMoneyStateScreenshots : ScreenshotTest() {
    private val june = TestShell(TabBarTab.Home, Month.June)
    private val july = TestShell(TabBarTab.Home, Month.July)

    @Test fun `894b1b01`() = screenState("894b1b01", shell = june) {
        TransactionsContent(TransactionsState(Month.June, JuneLedger), onAction = {})
    }

    @Test fun `d58c1486`() = screenState(
        "d58c1486",
        shell = july.copy(supportBadge = 4),
        setup = { scrollToAnchor("day-4", top = 115.dp, scrollableTag = "transactions") },
    ) {
        TransactionsContent(TransactionsState(Month.July, JulyLedger), onAction = {})
    }

    @Test fun `5e61179c`() = screenState("5e61179c", shell = july) {
        PlataCreditoContent(PlataCreditoState(available = 3_457_97, totalBalance = 42_03, spent = 2_170_00, month = Month.July), onAction = {})
    }

    @Test fun `22bf11f1`() = screenState("22bf11f1", shell = june) {
        CreditPaymentContent(CreditPaymentState(Month.June.creditStatus), onAction = {})
    }

    @Test fun `d2e81313`() = screenState("d2e81313", shell = july) {
        CreditPaymentContent(CreditPaymentState(Month.July.creditStatus), onAction = {})
    }

    @Test fun `8bda0dcf`() = screenState(
        "8bda0dcf",
        shell = july,
        setup = { scrollToAnchor("how-it-works", top = 125.67.dp, scrollableTag = "credit-payment") },
    ) {
        CreditPaymentContent(CreditPaymentState(Month.July.creditStatus), onAction = {})
    }

    @Test fun `d7d78e0b`() = screenState("d7d78e0b", shell = july) {
        MsiHomeContent(MsiHomeState(), onAction = {})
    }

    @Test fun `b50f7634`() = screenState(
        "b50f7634",
        shell = july,
        setup = { scrollToAnchor("defer-to-msi", top = 159.67.dp, scrollableTag = "msi-home") },
    ) {
        MsiHomeContent(MsiHomeState(), onAction = {})
    }

    @Test fun `b78c58b4`() = screenState("b78c58b4", shell = july) {
        MsiPartnerDetailContent(MsiPartnerDetailState(MsiPartners.MercadoLibre), onAction = {})
    }

    @Test fun `9e2d2593`() = screenState("9e2d2593", shell = july) {
        MsiPartnersContent(MsiPartnersState(), onAction = {})
    }

    @Test fun `d55a4d32`() = screenState("d55a4d32", shell = july) {
        MsiPartnersContent(MsiPartnersState(overlay = MsiOverlay.PartnersIntro), onAction = {})
    }

    @Test fun `764b45eb`() = screenState("764b45eb", shell = july) {
        MsiPartnersContent(MsiPartnersState(overlay = MsiOverlay.Terminal(TerminalProvider.Clip)), onAction = {})
    }

    @Test fun `9356b352`() = screenState(
        "9356b352",
        shell = july,
        setup = { scrollToAnchor("stores-with-msi", top = 110.67.dp, scrollableTag = "msi-partners") },
    ) {
        MsiPartnersContent(MsiPartnersState(), onAction = {})
    }

    private companion object {
        fun entry(month: Month, day: Int, title: String, kind: EntryKind, account: LedgerAccount, amount: Long, currency: Currency = Currency.Mxn) =
            LedgerEntry(month, day, title, kind, account, amount, currency)

        /** §2.1 fixture: the settled ambassador reward and the June seed, oldest first. */
        val JuneLedger = listOf(
            entry(Month.June, 14, "Ambassador reward", EntryKind.Reward, LedgerAccount.CreditLine, -30_00),
            entry(Month.June, 14, "OXXO", EntryKind.Purchase, LedgerAccount.CreditLine, -20_00),
            entry(Month.June, 14, "Carlos Ruiz", EntryKind.TransferIn, LedgerAccount.CreditLine, 20_00),
            entry(Month.June, 14, "Amazon Prime", EntryKind.Purchase, LedgerAccount.CreditLine, -20_00),
            entry(Month.June, 14, "Reversal from Amazon Prime", EntryKind.Refund, LedgerAccount.CreditLine, 20_00),
        )

        /** §2.1 fixture: what flows 29 and 31, Dev › Buy DNUT and the July seed write, on the reference's days. */
        val JulyLedger = listOf(
            entry(Month.July, 1, "Amazon", EntryKind.Purchase, LedgerAccount.CreditLine, -1_832_40),
            entry(Month.July, 3, "Crédito → Inversiones", EntryKind.BetweenAccounts, LedgerAccount.OwnBalance, -18_95),
            entry(Month.July, 3, "Krispy Kreme", EntryKind.BuyShares, LedgerAccount.Inversiones, -1_01, Currency.Usd),
            entry(Month.July, 3, "Crédito → Inversiones", EntryKind.BetweenAccounts, LedgerAccount.OwnBalance, -18_76),
            entry(Month.July, 4, "Ana López", EntryKind.TransferOut, LedgerAccount.OwnBalance, -70_00),
            entry(Month.July, 4, "Commission for operation", EntryKind.Commission, LedgerAccount.CreditLine, -230_84),
        )
    }
}
