package tech.bashga.plata.screens.delivery

import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Delivery
import tech.bashga.plata.data.DeliveryDay
import tech.bashga.plata.data.DeliverySlot
import tech.bashga.plata.data.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryStateTest {
    // viewModelScope runs on Main, which unit tests don't have
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun geocoderMatchesEveryWordAndIgnoresAccents() {
        val results = geocode("Av Rio")
        assertEquals(5, results.size)
        assertTrue(results.all { it.address.street.startsWith("Av. Río") })
        assertEquals(listOf("Calle Durango 263, Roma Norte"), geocode("durango roma n").map { it.address.line })
        assertTrue(geocode("  ").isEmpty())
    }

    @Test
    fun emptySearchListsExampleAddresses() {
        assertEquals(examplePlaces, DeliveryAddressState().results)
        assertEquals(5, examplePlaces.distinct().size)
        assertEquals(geocode("durango"), DeliveryAddressState(query = "durango").results)
    }

    @Test
    fun addressSearchPickThenCancelReturnsToTheMap() {
        val picked = geocode("Av Rio").first().address
        val searching = DeliveryAddressState().reduce(DeliveryAddressAction.Query("Av Rio"))
        assertEquals(DeliveryAddressMode.Search, searching.mode)
        val map = searching.reduce(DeliveryAddressAction.Pick(picked))
        assertEquals(DeliveryAddressState(DeliveryAddressMode.Map, "Av Rio", picked), map)
        val again = map.reduce(DeliveryAddressAction.EditAddress)
        assertEquals(DeliveryAddressMode.Search, again.mode)
        assertEquals(map, again.reduce(DeliveryAddressAction.Cancel))
        assertEquals(DeliveryAddressState(), DeliveryAddressState().reduce(DeliveryAddressAction.Cancel))
    }

    @Test
    fun addressContinueSavesAndCancelWithNothingLeaves() {
        val repository = AppRepository.inMemory()
        val viewModel = DeliveryAddressViewModel(repository, editing = false)
        assertFalse(viewModel.onAction(DeliveryAddressAction.Cancel))
        val picked = geocode("Av Rio").first().address
        assertTrue(viewModel.onAction(DeliveryAddressAction.Pick(picked)))
        assertTrue(viewModel.onAction(DeliveryAddressAction.Details("Depto 4B ")))
        assertFalse(viewModel.onAction(DeliveryAddressAction.Continue))
        assertEquals(Delivery(picked, "Depto 4B"), repository.snapshot.delivery)
        val editing = DeliveryAddressViewModel(repository, editing = true)
        assertEquals(DeliveryAddressState(DeliveryAddressMode.Map, address = picked, details = "Depto 4B"), editing.state.value)
    }

    @Test
    fun aFullyBookedDayOffersTheClosestSlot() {
        val today = DeliveryTimeState(slots = AppSnapshot().deliverySlots)
        assertTrue(today.daySlots.isEmpty())
        assertEquals(today, today.reduce(DeliveryTimeAction.Slot(DeliverySlot.Morning)))
        val closest = today.reduce(DeliveryTimeAction.SelectClosest)
        assertEquals(DeliveryDay.Closest, closest.day)
        assertEquals(DeliverySlot.Morning, closest.slot)
        val midday = closest.reduce(DeliveryTimeAction.Slot(DeliverySlot.Midday))
        assertEquals(DeliverySlot.Midday, midday.slot)
        assertNull(midday.reduce(DeliveryTimeAction.Day(DeliveryDay.Other)).slot)
    }

    @Test
    fun settingUpTheDeliveryMovesTheStageToCardComing() {
        val repository = AppRepository.inMemory()
        val viewModel = DeliveryTimeViewModel(repository)
        assertFalse(viewModel.setUp())
        viewModel.onAction(DeliveryTimeAction.SelectClosest)
        assertTrue(viewModel.setUp())
        assertEquals(Stage.CardComing("3 Jun"), repository.snapshot.stage)
        assertEquals(DeliverySlot.Morning, repository.snapshot.delivery.slot)
    }

    @Test
    fun detailsShowTheSavedDeliveryAndTheAmbassador() {
        val picked = geocode("durango roma norte").first().address
        val snapshot = AppSnapshot(
            stage = Stage.CardComing("3 Jun", Ambassador.Assigned("Carlos")),
            delivery = Delivery(picked, "", DeliveryDay.Closest, DeliverySlot.Morning),
        )
        val state = snapshot.toDeliveryDetailsState()
        assertTrue(state.assigned)
        assertEquals("Calle Durango 263, Roma Norte", state.address)
        assertEquals("Cuauhtémoc, 06700 CDMX", state.addressLine2)
        assertEquals("Tuesday, Jun 3, 9 a.m. - 12 p.m.", state.time)
    }

    @Test
    fun rescheduleNeedsAReasonOrSomeOtherText() {
        val none = RescheduleReasonState()
        assertFalse(none.canContinue)
        assertTrue(none.reduce(RescheduleReasonAction.Pick(RescheduleReason.PlansChanged)).canContinue)
        val other = none.reduce(RescheduleReasonAction.OtherText(" "))
        assertEquals(RescheduleReason.Other, other.reason)
        assertFalse(other.canContinue)
        assertTrue(other.reduce(RescheduleReasonAction.OtherText("Travelling")).canContinue)
    }

    @Test
    fun feedbackRowsToggle() {
        val liked = FeedbackState().reduce(FeedbackAction.Toggle(FeedbackAspect.ClearIntro))
        assertEquals(setOf(FeedbackAspect.ClearIntro), liked.liked)
        assertEquals(FeedbackState(), liked.reduce(FeedbackAction.Toggle(FeedbackAspect.ClearIntro)))
        assertEquals("Everything was so easy!", liked.reduce(FeedbackAction.Comment("Everything was so easy!")).comment)
    }

    @Test
    fun rewardCtaFollowsTheSelection() {
        assertEquals("Reward $30", RewardAmbassadorState().cta)
        assertEquals("Reward $50", RewardAmbassadorState(Reward.LoveIt).cta)
        assertEquals("Reward $100", RewardAmbassadorState(Reward.Custom()).cta)
    }

    @Test
    fun tourPagesStepAndStopAtTheEnds() {
        val pin = ProductTourState()
        assertEquals(pin, pin.reduce(ProductTourAction.Back))
        val digital = pin.reduce(ProductTourAction.Next)
        assertEquals(TourPage.GoDigital, digital.page)
        val free = digital.reduce(ProductTourAction.Next)
        assertEquals(TourPage.CommissionFree, free.page)
        assertEquals(free, free.reduce(ProductTourAction.Next))
        assertEquals(digital, free.reduce(ProductTourAction.Back))
    }
}
