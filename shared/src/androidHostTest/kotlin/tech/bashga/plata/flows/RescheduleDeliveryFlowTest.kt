package tech.bashga.plata.flows

import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Test
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.Stage
import tech.bashga.plata.navigation.HomeTab
import tech.bashga.plata.navigation.graph.DeliveryDetailsRoute
import tech.bashga.plata.navigation.graph.HomeRoute
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** SCREEN-MAP §5.30: DeliveryDetails (Assigned) → RescheduleReason → DeliveryTime → back to DeliveryDetails; Close delivers. */
class RescheduleDeliveryFlowTest : DeliveryFlowTest() {
    @Test
    fun reschedulingReturnsToDetailsAndClosingThenDeliversTheCard() {
        val assigned = CheckCardPackageDeliveryFlowTest.cardComing.copy(stage = Stage.CardComing("3 Jun", Ambassador.Assigned("Carlos"), 0))
        launch(assigned, HomeTab)
        navigate(DeliveryDetailsRoute)
        waitFor("Ambassador assigned")

        rule.onAllNodesWithText("Edit")[1].performClick()
        waitFor("Why do you want to", substring = true)
        tap("Continue")
        waitFor("Why do you want to", substring = true)
        tap("Plans changed")
        tap("Continue")

        waitFor("Delivery time")
        tap("Select the closest slot")
        tap("12 p.m. – 3 p.m.")
        tap("Set up delivery")
        rule.waitUntil(5_000) { isOnTop(DeliveryDetailsRoute::class) }
        waitFor("Tuesday, Jun 3, 12 p.m. - 3 p.m.")
        assertIs<Stage.CardComing>(repository.snapshot.stage)

        tap("Close")
        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        assertEquals(Stage.Live, repository.snapshot.stage)
        assertEquals(Activation.FeedbackPending, repository.snapshot.activation)
        assertEquals(Avatar.Placeholder, repository.snapshot.avatar)
    }

    @Test
    fun swipingTheDetailsDownWhileAssignedAlsoDelivers() {
        val assigned = CheckCardPackageDeliveryFlowTest.cardComing.copy(stage = Stage.CardComing("3 Jun", Ambassador.Assigned("Carlos"), 0))
        launch(assigned, HomeTab)
        navigate(DeliveryDetailsRoute)
        waitFor("Ambassador assigned")
        back()
        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        assertEquals(Stage.Live, repository.snapshot.stage)
    }
}
