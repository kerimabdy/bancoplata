package tech.bashga.plata.flows

import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTouchInput
import org.junit.Test
import tech.bashga.plata.data.Address
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Delivery
import tech.bashga.plata.data.DeliveryDay
import tech.bashga.plata.data.DeliverySlot
import tech.bashga.plata.data.Stage
import tech.bashga.plata.navigation.HomeTab
import tech.bashga.plata.navigation.graph.DeliveryDetailsRoute
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** SCREEN-MAP §5.9: DeliveryDetails before AmbassadorAssigned → the unboxing stories, tapped through to the end. */
class CheckCardPackageDeliveryFlowTest : DeliveryFlowTest() {
    @Test
    fun packageStoriesPlayToTheEndAndClosingThemNeverDeliversTheCard() {
        launch(cardComing, HomeTab)
        navigate(DeliveryDetailsRoute)
        waitFor("Ambassador to be assigned")

        tap("Check card package upon delivery")
        waitFor("Secure packaging", substring = true)
        // three steps, plus one for a tap the sheet swallows while it is still sliding in
        repeat(4) {
            if (rule.onAllNodes(hasText("Contact support")).fetchSemanticsNodes().isEmpty()) {
                rule.onNode(hasTestTag("stories-captions"), useUnmergedTree = true).performTouchInput { click(center.copy(x = width * 0.8f)) }
                rule.waitForIdle()
            }
        }
        waitFor("Contact support")

        advanceBackend(10_000)
        assertIs<Ambassador.Assigned>((repository.snapshot.stage as Stage.CardComing).ambassador)
        tap("Close")
        waitFor("Ambassador assigned")
        assertIs<Stage.CardComing>(repository.snapshot.stage)
    }

    companion object {
        val cardComing = AppSnapshot(
            stage = Stage.CardComing("3 Jun"),
            delivery = Delivery(Address("Calle Durango 263", "Roma Norte"), "", DeliveryDay.Closest, DeliverySlot.Morning),
        )
    }
}
