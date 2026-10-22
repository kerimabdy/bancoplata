package tech.bashga.plata.flows

import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerAccount
import tech.bashga.plata.data.LedgerEntry
import tech.bashga.plata.data.Month
import tech.bashga.plata.dev.DevActions

/** SCREEN-MAP §5.27 Credit payments: PlataCredito (July) → CreditPayment in June, then July, then How it works. */
class CreditPaymentsFlowTest : HomeMoneyFlowTest() {
    @Test
    fun creditPayments() {
        // ⚠ fixture: July spending of $2,170 with the statement paid down to a $42.03 balance
        val ledger = listOf(
            LedgerEntry(Month.July, 2, "Liverpool", EntryKind.Purchase, LedgerAccount.CreditLine, -2_170_00),
            LedgerEntry(Month.July, 7, "Credit payment", EntryKind.TransferIn, LedgerAccount.CreditLine, 2_127_97),
        )
        val repository = AppRepository.inMemory(live.copy(month = Month.July, ledger = ledger))
        launchAtHome(repository)

        click("Available in Plata Crédito")
        waitFor("Withdraw")
        seen("$3,457.97")
        waitFor("$42.03 Total balance")
        seen("$2,170 Spent in July")
        seen("Everything is paid.", substring = true)

        DevActions(repository).setMonth(Month.June)
        waitFor("No payments due right now.", substring = true)
        tap("Details")
        waitFor("About payment dates")
        seen("Statement expected on Jul 8.")

        DevActions(repository).setMonth(Month.July)
        waitFor("All paid for 24 Jun - 7 Jul")
        seen("Full monthly payment")

        scrollTo("How it works")
        scrollTo("When to pay")
        click("When to pay")
        waitFor("Pay by the payment date", substring = true)
        click("When to pay")
        waitGone("Pay by the payment date", substring = true)

        back()
        waitFor("$3,457.97")
    }
}
