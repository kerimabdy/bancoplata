package tech.bashga.plata.flows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.Month
import kotlin.test.assertEquals

/** SCREEN-MAP §5.2, Opening an account: VestFi toast → Affiliation → Acknowledgment ⟨Signature⟩ → Volume → RiskProfile ⟨AccountOpened⟩. */
class InvestOpeningAnAccountFlowTest : InvestFlowTest() {
    @Test
    fun opensAnInvestingAccount() {
        // ⚠ Dev › Month=July; recents from earlier visits to the VGK idea and NVDA
        val repository = AppRepository.inMemory(liveSnapshot(Month.July, recents = listOf("VGK", "NVDA")))
        launchInvestTab(repository)

        tap("By VestFi")
        waitFor("Powered by VestFi", substring = true)

        tap("Open an account")
        waitFor("Affiliation")
        tap("None of the below")
        tap("Continue")
        waitFor("Acknowledgment")

        tap("Sign agreements")
        waitFor("Your signature")
        rule.onNode(hasTestTag("signature-pad")).performTouchInput {
            swipe(Offset(width * 0.3f, height * 0.3f), Offset(width * 0.5f, height * 0.6f), durationMillis = 300)
        }
        tap("Done")
        waitFor("Investment volume")
        waitFor("~5%")

        rule.onNode(hasTestTag("volume-dial")).performTouchInput {
            // 70% round the dial from 12 o'clock is 252°: left of centre, a little below it
            val r = width / 2f * 0.86f
            swipe(Offset(centerX, centerY - r), Offset(centerX - 0.951f * r, centerY + 0.309f * r), durationMillis = 400)
        }
        waitFor("~70%")

        tap("Continue")
        waitFor("Moderate", substring = true)
        tap("I agree")
        waitFor("investing account!", substring = true)
        assertEquals(InvestAccount.Opened, repository.snapshot.invest.account)

        tap("Start investing")
        waitGone("investing account!", substring = true)
        waitFor("Investment portfolio")
        waitFor("Recents")
        assertEquals(listOf("VGK", "NVDA"), repository.snapshot.invest.recents)
    }

    @Test
    fun backWalksTheStepsAndCloseDismisses() {
        val repository = AppRepository.inMemory(liveSnapshot(Month.June))
        launchInvestTab(repository)

        tap("Open an account")
        tap("None of the below")
        tap("Continue")
        waitFor("Acknowledgment")
        pressBack()
        waitFor("Affiliation")
        tap("Close")
        waitGone("Affiliation")
        waitFor("Investing is easy")
        assertEquals(InvestAccount.NotOpened, repository.snapshot.invest.account)
    }
}
