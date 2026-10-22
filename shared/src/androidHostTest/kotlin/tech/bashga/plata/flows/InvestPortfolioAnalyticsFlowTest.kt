package tech.bashga.plata.flows

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.dev.DevActions

/** SCREEN-MAP §5.26, Portfolio analytics: Portfolio (USD 2.01) › allocation → PortfolioAllocation at cost (USD 2.00). */
class InvestPortfolioAnalyticsFlowTest : InvestFlowTest() {
    private fun investor(): AppRepository {
        val repository = AppRepository.inMemory(liveSnapshot(Month.July, InvestAccount.Opened, listOf("VGK", "NVDA")))
        repository.transferToInversiones(100)
        repository.buyDnut()
        repository.transferToInversiones(100)
        return repository
    }

    @Test
    fun theAllocationIsAtCost() {
        val repository = investor()
        launchInvestTab(repository)
        tap("Investment portfolio")
        waitFor("USD 2.01")

        // ⚠ Dev › Support message ×1
        DevActions(repository).supportMessage()
        rule.onNode(hasContentDescription("Allocation")).performClick()
        waitFor("Moderate profile")
        waitFor("USD 2.00")
        waitFor("1")
        scrollTo("Invest in Fixed income ETFs")
        waitFor("Sell USD 0.21", substring = true)
        scrollTo("Portfolio analysis")
        waitFor("998.00 left")
        pressBack()
        waitFor("USD 2.01")
    }

    @Test
    fun cashPlusOffersTheTransferIntoThePortfolio() {
        launchInvestTab(investor())
        tap("Investment portfolio")
        waitFor("Your stocks")
        rule.onNode(hasContentDescription("Add cash")).performClick()
        waitFor("By CLABE")
        tap("To my Plata account")
        waitGone("By CLABE")
        waitGone("Your stocks")
    }
}
