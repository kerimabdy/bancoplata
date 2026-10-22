package tech.bashga.plata.flows

import org.junit.Test
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.Stage
import tech.bashga.plata.navigation.HomeTab
import tech.bashga.plata.navigation.graph.FeedbackRoute
import tech.bashga.plata.navigation.graph.WelcomeCardRoute
import kotlin.test.assertEquals

/** SCREEN-MAP §5.6: Home's feedback dialog 👍 → Feedback (Dimmed) → Send → WelcomeCard with the thank-you toast. */
class SendingDeliveryFeedbackFlowTest : DeliveryFlowTest() {
    private val delivered = AppSnapshot(stage = Stage.Live, activation = Activation.FeedbackPending, avatar = Avatar.Placeholder)

    @Test
    fun sendingOpensActivationWithTheToast() {
        launch(delivered, HomeTab)
        // the dialog's 👍
        navigate(FeedbackRoute(liked = true))
        waitFor("Great! Tell us what\nyou liked")

        tap("Clear introduction of the product")
        type("comment", "Everything was so easy!")
        waitFor("Everything was so easy!")
        tap("Send feedback")

        rule.waitUntil(5_000) { isOnTop(WelcomeCardRoute::class) }
        waitFor("Thanks for your feedback")
        assertEquals(Activation.InProgress, repository.snapshot.activation)
        rule.mainClock.advanceTimeBy(4_000)
        waitGone("Thanks for your feedback")
        waitFor("Explore Plata")
    }

    @Test
    fun closeAndSwipeDownBothMoveActivationToInProgressWithoutTheToast() {
        launch(delivered, HomeTab)
        navigate(FeedbackRoute(liked = true))
        tap("Close")
        rule.waitUntil(5_000) { isOnTop(WelcomeCardRoute::class) }
        assertEquals(Activation.InProgress, repository.snapshot.activation)
        waitFor("Explore Plata")
        rule.waitForIdle()
        assertEquals(0, rule.onAllNodes(androidx.compose.ui.test.hasText("Thanks for your feedback")).fetchSemanticsNodes().size)
    }

    @Test
    fun swipingTheSheetDownCountsAsClose() {
        launch(delivered, HomeTab)
        navigate(FeedbackRoute(liked = true))
        waitFor("Send feedback")
        back()
        rule.waitUntil(5_000) { isOnTop(WelcomeCardRoute::class) }
        assertEquals(Activation.InProgress, repository.snapshot.activation)
    }
}
