package tech.bashga.plata.screens.home.money

import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Currency
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerAccount
import tech.bashga.plata.data.LedgerEntry
import tech.bashga.plata.data.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeMoneyStateTest {
    private fun entry(day: Int, title: String, kind: EntryKind, amount: Long, account: LedgerAccount = LedgerAccount.CreditLine, currency: Currency = Currency.Mxn) =
        LedgerEntry(Month.June, day, title, kind, account, amount, currency)

    @Test
    fun juneSeedGivesTheReferenceTotalsAndRows() {
        val ledger = listOf(
            entry(14, "Ambassador reward", EntryKind.Reward, -30_00),
            entry(14, "OXXO", EntryKind.Purchase, -20_00),
            entry(14, "Carlos Ruiz", EntryKind.TransferIn, 20_00),
            entry(14, "Amazon Prime", EntryKind.Purchase, -20_00),
            entry(14, "Reversal from Amazon Prime", EntryKind.Refund, 20_00),
        )
        val state = TransactionsState(Month.June, ledger)
        assertEquals(70_00, state.spent)
        assertEquals(40_00, state.received)
        assertEquals(0, state.cashback)
        val today = state.days.single()
        assertEquals("Today", today.label)
        assertEquals(listOf("Reversal from Amazon Prime", "Amazon Prime", "Carlos Ruiz", "OXXO", "Ambassador reward"), today.rows.map { it.title })
        assertEquals("+$20", today.rows[0].amount)
        assertEquals("−$0.20", today.rows[0].secondary)
        assertEquals("+$0.20", today.rows[1].secondary)
        assertNull(today.rows[2].secondary)
    }

    @Test
    fun betweenAccountsShowsTheDollarsBought() {
        val row = entry(3, "Crédito → Inversiones", EntryKind.BetweenAccounts, -18_95, LedgerAccount.OwnBalance).row
        assertEquals("USD 1.01", row.amount)
        assertEquals("USD 1", entry(3, "Crédito → Inversiones", EntryKind.BetweenAccounts, -18_76, LedgerAccount.OwnBalance).row.amount)
        assertEquals("with VAT", entry(4, "Commission for operation", EntryKind.Commission, -230_84).row.secondary)
    }

    @Test
    fun searchFiltersByTitleOrCategory() {
        val ledger = listOf(entry(14, "Amazon Prime", EntryKind.Purchase, -20_00), entry(14, "Carlos Ruiz", EntryKind.TransferIn, 20_00))
        assertEquals(listOf("Carlos Ruiz"), TransactionsState(Month.June, ledger, query = "transfers").days.single().rows.map { it.title })
        assertEquals(emptyList(), TransactionsState(Month.June, ledger, query = "nothing").days)
    }

    @Test
    fun creditStatusFollowsTheMonth() {
        assertEquals(CreditStatus.NothingDue("Jul 8"), Month.June.creditStatus)
        assertEquals("Aug 6", Month.July.creditStatus.nextStatement)
        val snapshot = AppSnapshot(ledger = listOf(entry(2, "Liverpool", EntryKind.Purchase, -2_170_00), entry(7, "Payment", EntryKind.TransferIn, 2_127_97)))
        assertEquals(3_457_97, snapshot.toPlataCreditoState().available)
        assertEquals(42_03, snapshot.creditTotalBalance)
    }

    @Test
    fun accordionsToggle() {
        val state = CreditPaymentState(Month.July.creditStatus)
        assertEquals(setOf(CreditSection.BillingPeriod), state.expanded)
        val opened = state.reduce(CreditPaymentAction.Toggle(CreditSection.WhenToPay))
        assertEquals(setOf(CreditSection.BillingPeriod, CreditSection.WhenToPay), opened.expanded)
        assertEquals(setOf(CreditSection.WhenToPay), opened.reduce(CreditPaymentAction.Toggle(CreditSection.BillingPeriod)).expanded)
    }

    @Test
    fun msiPartnersFiltersAndOverlays() {
        val state = MsiPartnersState()
        val beauty = state.reduce(MsiPartnersAction.SelectCategory(MsiCategory.Beauty))
        assertEquals(listOf("Alfaparf"), beauty.stores.map { it.name })
        assertNull(beauty.reduce(MsiPartnersAction.SelectCategory(MsiCategory.Beauty)).category)
        val terminal = state.reduce(MsiPartnersAction.OpenTerminal(TerminalProvider.Clip))
        assertEquals(MsiOverlay.Terminal(TerminalProvider.Clip), terminal.overlay)
        assertNull(terminal.reduce(MsiPartnersAction.CloseOverlay).overlay)
        assertEquals(10, state.hero.size)
    }

    @Test
    fun partnersIntroShowsUntilClosed() {
        val repository = AppRepository.inMemory()
        val first = MsiPartnersViewModel(repository)
        assertEquals(MsiOverlay.PartnersIntro, first.state.value.overlay)
        first.onAction(MsiPartnersAction.CloseOverlay)
        assertNull(first.state.value.overlay)
        assertNull(MsiPartnersViewModel(repository).state.value.overlay)
    }
}
