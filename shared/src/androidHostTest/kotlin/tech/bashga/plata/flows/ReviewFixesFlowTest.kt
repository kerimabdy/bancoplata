package tech.bashga.plata.flows

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.LinkAnnotation
import org.junit.Test
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.HomeTiles
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.Stage
import tech.bashga.plata.navigation.HomeTab
import tech.bashga.plata.navigation.graph.DeliveryDetailsRoute
import tech.bashga.plata.navigation.graph.FeedbackRoute
import tech.bashga.plata.navigation.graph.HomeRoute
import tech.bashga.plata.navigation.graph.ProductTourRoute
import tech.bashga.plata.navigation.graph.RewardAmbassadorRoute
import tech.bashga.plata.navigation.graph.WelcomeCardRoute
import tech.bashga.plata.screens.home.HomeLocal
import tech.bashga.plata.screens.home.HomeOverlay
import tech.bashga.plata.screens.home.cashbackSetupState
import tech.bashga.plata.screens.home.homeState
import tech.bashga.plata.screens.support.SUPPORT_PHONE
import tech.bashga.plata.screens.support.SUPPORT_TEL_URI
import tech.bashga.plata.screens.support.withPhoneLink
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ReviewFixesFlowTest : DeliveryFlowTest() {
    private val activating = AppSnapshot(stage = Stage.Live, activation = Activation.InProgress, avatar = Avatar.Placeholder)

    @Test
    fun rewardingAgainAfterBackReplacesThePendingEntry() {
        launch(activating, HomeTab)
        navigate(WelcomeCardRoute())
        tap("Explore Plata")
        tap("Reward $30")
        rule.waitUntil(5_000) { isOnTop(ProductTourRoute::class) }
        back()
        rule.waitUntil(5_000) { isOnTop(RewardAmbassadorRoute::class) }
        tap("Love it!")
        tap("Reward $50")
        rule.waitUntil(5_000) { isOnTop(ProductTourRoute::class) }
        val reward = repository.snapshot.ledger.single { it.kind == EntryKind.Reward }
        assertEquals(-50_00L, reward.amount)
        assertEquals(1, repository.snapshot.ledger.size)
    }

    @Test
    fun deliveryArrangedIsConsumedInTheRepository() {
        val scheduled = AppSnapshot(stage = Stage.CardComing("3 Jun"), deliveryArrangedPending = true)
        assertEquals(HomeOverlay.DeliveryArranged, homeState(scheduled, HomeLocal()).overlay)
        launch(scheduled, HomeTab)
        waitFor("Delivery successfully arranged!")
        tap("Done")
        waitGone("Delivery successfully arranged!")
        assertFalse(repository.snapshot.deliveryArrangedPending)
        // a rebuilt HomeViewModel derives the overlay from the snapshot alone
        assertNull(homeState(repository.snapshot, HomeLocal()).overlay)
    }

    @Test
    fun closingDeliveryTimeWhileReschedulingStillDelivers() {
        val assigned = CheckCardPackageDeliveryFlowTest.cardComing.copy(stage = Stage.CardComing("3 Jun", Ambassador.Assigned("Carlos"), 0))
        launch(assigned, HomeTab)
        navigate(DeliveryDetailsRoute)
        waitFor("Ambassador assigned")
        rule.onAllNodesWithText("Edit")[1].performClick()
        tap("Continue")
        tap("Plans changed")
        tap("Continue")
        waitFor("Delivery time")
        tap("Close")
        rule.waitUntil(5_000) { isOnTop(HomeRoute::class) }
        assertEquals(Stage.Live, repository.snapshot.stage)
        assertEquals(Activation.FeedbackPending, repository.snapshot.activation)
    }

    @Test
    fun homeCreditStatusFollowsTheMonth() {
        launch(AppSnapshot(stage = Stage.Live, activation = Activation.Done, month = Month.July), HomeTab)
        waitFor("Paid")
        assertEquals(0, rule.onAllNodes(hasText("Nothing due")).fetchSemanticsNodes().size)
        rule.runOnUiThread { repository.setMonth(Month.June) }
        waitFor("Nothing due")
    }

    @Test
    fun thumbsDownOpensTheWhatWentWrongVariant() {
        launch(AppSnapshot(stage = Stage.Live, activation = Activation.FeedbackPending), HomeTab)
        navigate(FeedbackRoute(liked = false))
        waitFor("Sorry! Tell us what\nwent wrong")
        tap("Slow process")
        tap("Send feedback")
        rule.waitUntil(5_000) { isOnTop(WelcomeCardRoute::class) }
        assertEquals(Activation.InProgress, repository.snapshot.activation)
    }

    @Test
    fun cashbackSetupStartsFromTheSavedCategories() {
        val saved = setOf(CashbackCategory.AllPurchases, CashbackCategory.Airline)
        assertEquals(saved, cashbackSetupState(AppSnapshot(homeTiles = HomeTiles(cashback = saved))).selected)
    }

    @Test
    fun supportPhoneIsATelLink() {
        val text = "Call us at $SUPPORT_PHONE anytime".withPhoneLink(Color.Red)
        val link = text.getLinkAnnotations(0, text.length).single()
        assertEquals(SUPPORT_TEL_URI, (link.item as LinkAnnotation.Url).url)
        assertEquals(SUPPORT_PHONE, text.text.substring(link.start, link.end))
    }
}
