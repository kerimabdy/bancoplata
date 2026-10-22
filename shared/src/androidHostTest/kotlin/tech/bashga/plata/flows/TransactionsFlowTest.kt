package tech.bashga.plata.flows

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.HomeTiles
import tech.bashga.plata.data.Month
import tech.bashga.plata.dev.DevActions

/** SCREEN-MAP §5.35 Transactions: Home (June, after the reward settled and the seed) → Transactions → July. */
class TransactionsFlowTest : HomeMoneyFlowTest() {
    @Test
    fun transactions() {
        val cashback = setOf(CashbackCategory.AllPurchases, CashbackCategory.Airline, CashbackCategory.Entertainment, CashbackCategory.Restaurants)
        val repository = AppRepository.inMemory(live.copy(homeTiles = HomeTiles(cashback = cashback, msiVisited = true)))
        val dev = DevActions(repository)
        repository.rewardAmbassador(30_00)
        repository.coldLaunch()
        dev.seedPurchases()
        launchAtHome(repository)
        waitFor("$3,470")

        tap("Transactions")
        waitFor("Search for operations")
        seen("June")
        seen("$70")
        seen("$40")
        seen("Reversal from Amazon Prime")

        // ⚠ Dev › Month=July, then what flows 29 and 31, Dev › Buy DNUT and the July seed write; Dev › Support message ×4
        dev.setMonth(Month.July)
        repository.transferToInversiones(1_00)
        dev.buyDnut()
        repository.transfer("Ana López", 70_00, 230_84)
        dev.seedPurchases()
        repeat(4) { dev.supportMessage() }

        waitFor("July")
        waitGone("Reversal from Amazon Prime")
        seen("4")
        scrollTo("Amazon")
        seen("1 July")
        seen("+$18.32")
        seen("Krispy Kreme")

        rule.onNodeWithText("Search for operations").performTextInput("krispy")
        waitGone("Amazon")
        seen("Krispy Kreme")
    }
}
