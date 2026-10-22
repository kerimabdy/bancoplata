package tech.bashga.plata.flows

import org.junit.Test
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.Stage
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.navigation.graph.CardDetailsRoute
import tech.bashga.plata.navigation.graph.DeliveryDetailsRoute
import tech.bashga.plata.navigation.graph.HomeRoute
import tech.bashga.plata.navigation.graph.ProductTourRoute
import tech.bashga.plata.navigation.graph.ScheduleDeliveryIntroRoute
import tech.bashga.plata.navigation.graph.WelcomeCardRoute
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * SCREEN-MAP §5.1 from ScheduleDeliveryIntro on: schedule, the ambassador event on the open details sheet, CardDelivered
 * on close, then the Activation modal through to Home. Home's own controls (Full info, the feedback dialog's ✕) are
 * applied the way they navigate; their screens are the home area's tests.
 */
class DeliveryOnboardingFlowTest : DeliveryFlowTest() {
    @Test
    fun fromSchedulingToTheFirstDayHome() {
        launch(AppSnapshot(), ScheduleDeliveryIntroRoute)

        tap("Schedule now")
        type("query", "Av Rio")
        tap("Av. Río Mixcoac 25, Actipan")
        tap("Continue")
        tap("Select the closest slot")
        tap("Set up delivery")
        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        assertIs<Stage.CardComing>(repository.snapshot.stage)

        // Home › Card delivery › Full info
        navigate(DeliveryDetailsRoute)
        waitFor("Ambassador to be assigned")
        assertEquals(10_000L, (repository.snapshot.stage as Stage.CardComing).ambassadorDueAt)
        advanceBackend(9_999)
        waitFor("Ambassador to be assigned")
        advanceBackend(1)
        waitFor("Ambassador assigned")
        assertEquals(Ambassador.Assigned("Carlos"), (repository.snapshot.stage as Stage.CardComing).ambassador)

        tap("Close")
        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        assertEquals(Stage.Live, repository.snapshot.stage)
        assertEquals(Activation.FeedbackPending, repository.snapshot.activation)

        // the feedback dialog's ✕
        rule.runOnUiThread { repository.startActivation() }
        navigate(WelcomeCardRoute())
        waitFor("Welcome to\nPlata, Sofía")
        assertEquals(Activation.InProgress, repository.snapshot.activation)

        tap("Explore Plata")
        waitFor("Reward $30")
        tap("Thank you!")
        tap("Reward $30")
        rule.waitUntil(5_000) { isOnTop(ProductTourRoute::class) }
        val reward = repository.snapshot.ledger.single()
        assertEquals(EntryKind.Reward, reward.kind)
        assertEquals(-30_00L, reward.amount)
        assertTrue(reward.pending)
        assertEquals(3_500_00L, repository.snapshot.creditAvailable)

        waitFor("View your PIN in\ncard details")
        tap("Show PIN")
        rule.waitUntil(5_000) { isOnTop(CardDetailsRoute::class) }
        back()
        rule.waitUntil(5_000) { isOnTop(ProductTourRoute::class) }

        tap("Next")
        waitFor("Go digital!")
        tap("Next")
        waitFor("Commission-free\npayments")
        back()
        waitFor("Go digital!")
        tap("Next")
        tap("Let’s Plata!")

        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        val done = repository.snapshot
        assertEquals(Activation.Done, done.activation)
        assertEquals(Avatar.Generated, done.avatar)
        assertTrue(done.avatarIsNew)
    }

    @Test
    fun closeOnTheTourAlsoFinishesAndBackFromTheFirstPageReturnsToTheReward() {
        launch(AppSnapshot(stage = Stage.Live, activation = Activation.InProgress), tech.bashga.plata.navigation.HomeTab)
        navigate(WelcomeCardRoute())
        tap("Explore Plata")
        tap("Love it!")
        tap("Reward $50")
        waitFor("View your PIN in\ncard details")
        back()
        waitFor("Reward $50")
        tap("Reward $50")
        waitFor("View your PIN in\ncard details")
        tap("Close")
        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        assertEquals(Activation.Done, repository.snapshot.activation)
        assertEquals(listOf(-50_00L), repository.snapshot.ledger.map { it.amount })
    }
}
