package tech.bashga.plata.flows

import androidx.compose.ui.test.hasText
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Month
import kotlin.test.assertEquals

/** SCREEN-MAP §5.17, Investment ideas: Investments › Investment ideas › Vanguard → the idea (VGK to recents) → scroll. */
class InvestInvestmentIdeasFlowTest : InvestFlowTest() {
    @Test
    fun opensTheVanguardIdea() {
        val repository = AppRepository.inMemory(liveSnapshot(Month.June))
        launchInvestTab(repository)

        scrollTo("Investment ideas")
        tap("Vanguard FTSE Europe ETF")
        waitFor("We think it's time to invest")
        assertEquals(listOf("VGK"), repository.snapshot.invest.recents)
        rule.onAllNodes(hasText("Payments")).fetchSemanticsNodes().let { assertEquals(0, it.size, "no tab bar on InvestmentIdea") }

        scrollTo("Research report", substring = true)
        tap("VGK")
        waitFor("Buy from USD 1")
        pressBack()
        waitFor("We think it's time to invest")
        pressBack()
        waitFor("Investment ideas")
        waitFor("Recents")
    }
}
