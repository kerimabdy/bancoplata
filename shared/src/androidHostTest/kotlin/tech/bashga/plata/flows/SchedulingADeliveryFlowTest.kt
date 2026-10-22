package tech.bashga.plata.flows

import org.junit.Test
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.DeliveryDay
import tech.bashga.plata.data.DeliverySlot
import tech.bashga.plata.data.Stage
import tech.bashga.plata.navigation.graph.HomeRoute
import tech.bashga.plata.navigation.graph.ScheduleDeliveryIntroRoute
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** SCREEN-MAP §5.5: ScheduleDeliveryIntro → DeliveryAddress (Search → Map) → DeliveryTime → Home(CardComing). */
class SchedulingADeliveryFlowTest : DeliveryFlowTest() {
    @Test
    fun schedulingMovesTheStageToCardComing() {
        launch(AppSnapshot(), ScheduleDeliveryIntroRoute)

        tap("Schedule now")
        waitFor("Delivery address")
        type("query", "Av Rio")
        tap("Av. Río Mixcoac 25, Actipan")
        waitFor("Details (optional)")
        type("details", "Depto 4B")
        tap("Continue")

        waitFor("Delivery time")
        waitFor("The closest time slot available is", substring = true)
        tap("Select the closest slot")
        tap("Set up delivery")

        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        val snapshot = repository.snapshot
        assertEquals(Stage.CardComing("3 Jun"), snapshot.stage)
        assertTrue(snapshot.deliveryArrangedPending)
        assertEquals("Av. Río Mixcoac 25", snapshot.delivery.address?.street)
        assertEquals("Depto 4B", snapshot.delivery.details)
        assertEquals(DeliveryDay.Closest, snapshot.delivery.day)
        assertEquals(DeliverySlot.Morning, snapshot.delivery.slot)
        assertTrue(nav.controller.currentBackStack.value.none { it.destination.route?.contains("Delivery") == true })
    }

    @Test
    fun cancelWithNothingChosenClosesTheSheetAndCloseDismissesAll() {
        launch(AppSnapshot(), ScheduleDeliveryIntroRoute)

        tap("Schedule now")
        tap("Cancel")
        rule.waitUntil(5_000) { isOnTop(ScheduleDeliveryIntroRoute::class) }

        tap("Schedule now")
        type("query", "durango roma norte")
        tap("Calle Durango 263, Roma Norte")
        tap("Continue")
        waitFor("Delivery time")
        tap("Close")
        rule.waitUntil(5_000) { isOnTop(ScheduleDeliveryIntroRoute::class) }
        assertEquals(Stage.NotScheduled, repository.snapshot.stage)
        assertEquals("Calle Durango 263", repository.snapshot.delivery.address?.street)
    }
}
