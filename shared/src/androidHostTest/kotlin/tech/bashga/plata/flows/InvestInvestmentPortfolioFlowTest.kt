package tech.bashga.plata.flows

import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.dev.DevActions
import kotlin.test.assertEquals

/** SCREEN-MAP §5.21, Investment portfolio: the card opens Portfolio, whose first open moves DNUT 1.00 → 1.01. */
class InvestInvestmentPortfolioFlowTest : InvestFlowTest() {
    @Test
    fun theQuoteTicksOnceOnTheFirstOpen() {
        val repository = AppRepository.inMemory(liveSnapshot(Month.July, InvestAccount.Opened, listOf("VGK", "NVDA")))
        launchInvestTab(repository)
        waitFor("Investment portfolio")

        // ⚠ move USD 1 in, Dev › Buy DNUT, move USD 1 in again (flow 31)
        repository.transferToInversiones(100)
        DevActions(repository).buyDnut()
        repository.transferToInversiones(100)
        assertEquals(100, repository.snapshot.invest.holdings.single().marketValue)

        tap("Investment portfolio")
        waitFor("Your stocks")
        waitFor("USD 2.01")
        assertEquals(101, repository.snapshot.invest.holdings.single().marketValue)
        waitFor("0.31 DNUT")

        scrollTo("Investment limit")
        waitFor("USD 1,269.00 left in July")
        waitFor("3 in July")

        pressBack()
        waitFor("Recents")
        tap("Investment portfolio")
        waitFor("USD 2.01")
        assertEquals(101, repository.snapshot.invest.holdings.single().marketValue)
    }
}
