package tech.bashga.plata.flows

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.Month

/** SCREEN-MAP §5.7, Completing a lesson: Investments › Lesson 2 → Intro → two pages → Complete → Continue later. */
class InvestCompletingALessonFlowTest : InvestFlowTest() {
    @Test
    fun completesHowToBuyStocks() {
        // ⚠ Dev › Month=July, account opened
        launchInvestTab(AppRepository.inMemory(liveSnapshot(Month.July, InvestAccount.Opened)))

        scrollTo("Learn investment")
        // performScrollTo only moves the lesson row, not the feed around it
        rule.onNodeWithTag("investments-scroll").performTouchInput { swipeUp() }
        tap("Lesson 2")
        waitFor("How to buy stocks?")
        tap("Get started")
        waitFor("First, what is a stock?")
        tap("Next")
        waitFor("market order", substring = true)
        pressBack()
        waitFor("First, what is a stock?")
        tap("Next")
        tap("Next")
        waitFor("Lesson complete!")
        tap("Continue later")
        waitGone("Lesson complete!")
        waitFor("Learn investment")
    }
}
