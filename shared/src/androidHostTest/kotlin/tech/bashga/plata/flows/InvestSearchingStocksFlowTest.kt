package tech.bashga.plata.flows

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Month
import kotlin.test.assertEquals

/** SCREEN-MAP §5.25, Searching stocks: Popular stocks › See all → type "krisp" → Krispy Kreme (DNUT to recents). */
class InvestSearchingStocksFlowTest : InvestFlowTest() {
    private val field = hasSetTextAction() and androidx.compose.ui.test.hasAnyAncestor(hasTestTag("stock-search"))

    @Test
    fun findsKrispyKremeAsYouType() {
        val repository = AppRepository.inMemory(liveSnapshot(Month.June))
        launchInvestTab(repository)

        tapIn("See all", "popular-stocks")
        waitFor("Popular stocks")
        waitFor("Microsoft")

        waitFor(field)
        rule.onNode(field).performClick()
        waitFor("Cancel")
        rule.onNode(field).performTextInput("krisp")
        waitFor("Krispy Kreme")
        waitGone("Microsoft")

        tap("Cancel")
        waitFor("Microsoft")

        rule.onNode(field).performClick()
        rule.onNode(field).performTextInput("kr")
        waitGone("Microsoft")
        tap("Krispy Kreme")
        waitFor("Buy from USD 1")
        assertEquals(listOf("DNUT"), repository.snapshot.invest.recents)
        assertEquals(0, rule.onAllNodes(hasText("Payments")).fetchSemanticsNodes().size, "no tab bar on StockDetail")
    }

    @Test
    fun theTabSearchFieldOpensSearchMode() {
        launchInvestTab(AppRepository.inMemory(liveSnapshot(Month.June)))
        tap("Stocks or ETFs")
        waitFor("Cancel")
        waitFor(field)
        rule.onNode(field).performTextInput("nvid")
        waitFor("Nvidia")
    }
}
