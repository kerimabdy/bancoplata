package tech.bashga.plata.flows

import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import org.junit.Test
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.BackStyle
import tech.bashga.plata.navigation.PlataApp
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Month
import kotlin.test.assertTrue

/** An iOS back swipe over a page pushed from Home moves that page with the finger, wherever Compose's input layer sits. */
class BackSwipeFollowsFingerTest : HomeMoneyFlowTest() {
    @Test
    fun msiFollowsTheFingerUnderAPopup() {
        rule.setContent { PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(AppRepository.inMemory(live.copy(month = Month.July)), backStyle = BackStyle.Ios) } } }
        waitFor("1")
        PASSCODE.forEach { rule.onNodeWithText(it.toString()).performClick() }
        waitFor("Available in Plata Crédito")
        tap("MSI")
        waitFor("Nothing to defer")
        rule.waitForIdle()
        val rest = node("Nothing to defer").fetchSemanticsNode().boundsInRoot
        val width = rule.onRoot().fetchSemanticsNode().size.width.toFloat()

        val input = DirectNavigationEventInput()
        rule.runOnUiThread { rule.activity.navigationEventDispatcher.addInput(input) }
        // as Compose's UIKit input reports it under a Popup layer 1,000 px across: progress carries that offset, touchX doesn't
        val layerX = 1_000f
        rule.runOnUiThread { input.backStarted(NavigationEvent(touchX = 0f, touchY = 1200f, progress = 0f, swipeEdge = NavigationEvent.EDGE_LEFT, frameTimeMillis = 1_000)) }
        rule.waitForIdle()
        rule.runOnUiThread { input.backProgressed(NavigationEvent(touchX = 200f, touchY = 1200f, progress = (200f + layerX) / width, swipeEdge = NavigationEvent.EDGE_LEFT, frameTimeMillis = 1_000)) }
        rule.waitForIdle()
        val moved = node("Nothing to defer").fetchSemanticsNode().boundsInRoot.left - rest.left

        assertTrue(moved in 150f..250f, "page moved $moved px for a 200 px swipe")
    }
}
