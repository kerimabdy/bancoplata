package tech.bashga.plata.flows

import androidx.compose.ui.test.hasText
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.dev.DevActions
import kotlin.test.assertEquals

/** SCREEN-MAP §5.20, Investments: the tab before and after the account, June and July, scrolled through the feed. */
class InvestInvestmentsFlowTest : InvestFlowTest() {
    @Test
    fun theFeedFollowsTheAccountAndTheMonth() {
        val repository = AppRepository.inMemory(liveSnapshot(Month.June))
        launchInvestTab(repository)

        waitFor("Investing is easy")
        assertEquals(0, rule.onAllNodes(hasText("Recents")).fetchSemanticsNodes().size)
        waitFor("$200")

        // ⚠ Dev › Month=July; account opened through flow 2 (recents VGK, NVDA)
        DevActions(repository).setMonth(Month.July)
        repository.openInvestAccount()
        repository.addRecent("NVDA")
        repository.addRecent("VGK")
        waitFor("Investment portfolio")
        waitFor("Recents")
        waitFor("1,500")
        waitFor("1.72%")

        scrollTo("Top movers")
        waitFor("WOLF")
        tap("Losers")
        waitFor("TSLA")
        tap("Gainers")
        waitFor("WOLF")

        // ⚠ Dev › Month=June
        DevActions(repository).setMonth(Month.June)
        scrollTo("Investment ideas")
        waitFor("$200")
        DevActions(repository).setMonth(Month.July)
        scrollTo("Crypto ETFs")
        scrollTo("Commodities ETFs")
        DevActions(repository).setMonth(Month.June)
        scrollTo("Short term bonds ETFs")
        scrollTo("All stocks")
    }
}
