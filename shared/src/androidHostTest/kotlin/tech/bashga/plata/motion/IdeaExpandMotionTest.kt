package tech.bashga.plata.motion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.flows.InvestFlowTest

/** Captures the idea card growing into its page and closing back into it, by back and by pulling down, into build/motion/. */
class IdeaExpandMotionTest : InvestFlowTest() {
    @Test
    fun expandsAndCollapses() {
        launchInvestTab(AppRepository.inMemory(liveSnapshot(Month.June)))
        scrollTo("Investment ideas")
        scrollTo("Vanguard FTSE Europe ETF")
        rule.mainClock.autoAdvance = false
        rule.onAllNodes(hasText("Vanguard FTSE Europe ETF")).onFirst().performClick()
        capture("open")
        pressBack()
        capture("close")
    }

    @Test
    fun pullsDownToClose() {
        openIdea()
        val scroll = rule.onNodeWithTag("idea-scroll")
        val step = 20f * rule.density.density
        scroll.performTouchInput { down(center) }
        (1..3).forEach { i ->
            scroll.performTouchInput { moveBy(Offset(0f, step)) }
            rule.onRoot().captureRoboImage("build/motion/idea-pull-down-$i.png")
        }
        // back up: the page grows back, nothing closes
        (1..3).forEach { i ->
            scroll.performTouchInput { moveBy(Offset(0f, -step)) }
            rule.onRoot().captureRoboImage("build/motion/idea-pull-up-$i.png")
        }
        // all the way: it lets go into the card under the finger, and leaves the tree as it flies
        scroll.performTouchInput {
            repeat(5) { moveBy(Offset(0f, step)) }
            up()
        }
        capture("pull-close")
    }

    @Test
    fun closesAfterACancelledPull() {
        openIdea()
        val scroll = rule.onNodeWithTag("idea-scroll")
        scroll.performTouchInput { down(center); moveBy(Offset(0f, 40f * rule.density.density)); up() }
        rule.mainClock.advanceTimeBy(1000)
        pressBack()
        capture("cancelled-pull-close")
    }

    @Test
    fun closesWhilePulled() {
        openIdea()
        val scroll = rule.onNodeWithTag("idea-scroll")
        scroll.performTouchInput { down(center); moveBy(Offset(0f, 40f * rule.density.density)) }
        pressBack()
        capture("held-close")
    }

    @Test
    fun flingIntoTheTopSwingsAndStaysOpen() {
        openIdea()
        rule.mainClock.autoAdvance = true
        val scroll = rule.onNodeWithTag("idea-scroll")
        repeat(2) { scroll.performTouchInput { swipeUp(startY = bottom * 0.85f, endY = top + height * 0.15f, durationMillis = 400) } }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        // the finger lifts well before the top: only the fling's momentum runs into it
        scroll.performTouchInput { swipeDown(startY = top + height * 0.3f, endY = top + height * 0.55f, durationMillis = 40) }
        capture("swing")
        rule.mainClock.advanceTimeBy(2000)
        scroll.assertExists()
    }

    private fun openIdea() {
        launchInvestTab(AppRepository.inMemory(liveSnapshot(Month.June)))
        scrollTo("Investment ideas")
        scrollTo("Vanguard FTSE Europe ETF")
        rule.onAllNodes(hasText("Vanguard FTSE Europe ETF")).onFirst().performClick()
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
    }

    private fun capture(name: String) {
        var at = 0L
        (0L..700L step 50L).forEach { t ->
            rule.mainClock.advanceTimeBy(t - at)
            at = t
            rule.onRoot().captureRoboImage("build/motion/idea-$name-${t.toString().padStart(4, '0')}.png")
        }
    }
}
