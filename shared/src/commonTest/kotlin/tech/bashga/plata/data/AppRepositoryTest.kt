package tech.bashga.plata.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppRepositoryTest {
    private fun TestScope.repository(store: KeyValueStore = InMemoryKeyValueStore(), scope: CoroutineScope = backgroundScope) =
        AppRepository(store, scope, now = { testScheduler.currentTime })

    @Test
    fun missingOrUndecodableSnapshotIsFirstRun() = runTest {
        assertEquals(AppSnapshot(), repository().snapshot)
        val store = InMemoryKeyValueStore(mapOf(SNAPSHOT_KEY to "{not json"))
        assertEquals(AppSnapshot(), repository(store).snapshot)
        assertNull(store.get(SNAPSHOT_KEY))
    }

    @Test
    fun everyChangeIsPersistedAndReloaded() = runTest {
        val store = InMemoryKeyValueStore()
        repository(store).apply {
            confirmOtp("5512345678")
            setUpDelivery(DeliveryDay.Closest, DeliverySlot.Morning, "3 Jun")
            setMonth(Month.July)
        }
        val reloaded = repository(store).snapshot
        assertEquals("5512345678", reloaded.session.phone)
        assertEquals(Stage.CardComing("3 Jun"), reloaded.stage)
        assertEquals(Month.July, reloaded.month)
    }

    @Test
    fun unknownFieldsAndValuesDecodeToDefaults() = runTest {
        val store = InMemoryKeyValueStore(mapOf(SNAPSHOT_KEY to """{"supportUnread":3,"futureField":1,"month":"August"}"""))
        val snapshot = repository(store).snapshot
        assertEquals(3, snapshot.supportUnread)
        assertEquals(Month.June, snapshot.month)
    }

    @Test
    fun firstLoginGoesThroughQrAndPasscode() = runTest {
        val repo = repository()
        assertEquals(AuthStep.ScanCardQr, repo.confirmOtp("5512345678"))
        repo.qrScanned()
        assertEquals(AuthStep.CreatePasscode, repo.confirmOtp("5512345678"))
        repo.savePasscode("123456")
        assertEquals(AuthStep.EnableFaceId, repo.confirmOtp("5512345678"))
        repo.logIn(faceId = false)
        assertTrue(repo.snapshot.session.loggedIn)
        assertTrue(repo.checkPasscode("123456"))
        assertFalse(repo.checkPasscode("000000"))
    }

    @Test
    fun logOutKeepsPhonePasscodeAndDeviceLink() = runTest {
        val repo = repository()
        repo.confirmOtp("5512345678")
        repo.qrScanned()
        repo.savePasscode("123456")
        repo.logIn(faceId = true)
        repo.logOut()
        assertEquals(Session("5512345678", "123456", deviceLinked = true, loggedIn = false, faceId = true), repo.snapshot.session)
        assertEquals(AuthStep.EnableFaceId, repo.confirmOtp("5512345678"))
    }

    @Test
    fun ambassadorIsAssignedTenSecondsAfterTheFirstDetailsOpen() = runTest {
        val repo = repository()
        repo.setUpDelivery(DeliveryDay.Closest, DeliverySlot.Morning, "3 Jun")
        repo.openDeliveryDetails()
        assertEquals(Ambassador.Unassigned, (repo.snapshot.stage as Stage.CardComing).ambassador)
        assertFalse(repo.closeDeliveryDetails())
        repo.openDeliveryDetails()
        advanceTimeBy(AMBASSADOR_DELAY_MILLIS - 1)
        runCurrent()
        assertEquals(Ambassador.Unassigned, (repo.snapshot.stage as Stage.CardComing).ambassador)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(Ambassador.Assigned(ASSIGNED_AMBASSADOR), (repo.snapshot.stage as Stage.CardComing).ambassador)
    }

    @Test
    fun ambassadorDueTimeSurvivesARelaunch() = runTest {
        val store = InMemoryKeyValueStore()
        repository(store).apply {
            setUpDelivery(DeliveryDay.Closest, DeliverySlot.Morning, "3 Jun")
            openDeliveryDetails()
        }
        advanceTimeBy(4_000)
        val relaunched = repository(store).also { it.coldLaunch() }
        advanceTimeBy(5_999)
        runCurrent()
        assertEquals(Ambassador.Unassigned, (relaunched.snapshot.stage as Stage.CardComing).ambassador)
        advanceTimeBy(1)
        runCurrent()
        assertTrue((relaunched.snapshot.stage as Stage.CardComing).ambassador is Ambassador.Assigned)
    }

    @Test
    fun ambassadorDueWhileClosedIsAssignedOnForeground() = runTest {
        val store = InMemoryKeyValueStore()
        repository(store).apply {
            setUpDelivery(DeliveryDay.Closest, DeliverySlot.Morning, "3 Jun")
            openDeliveryDetails()
        }
        val relaunched = AppRepository(store, backgroundScope, now = { AMBASSADOR_DELAY_MILLIS + 1 })
        assertEquals(Ambassador.Unassigned, (relaunched.snapshot.stage as Stage.CardComing).ambassador)
        relaunched.onForeground()
        assertTrue((relaunched.snapshot.stage as Stage.CardComing).ambassador is Ambassador.Assigned)
    }

    @Test
    fun closingDetailsWhileAssignedDeliversTheCard() = runTest {
        val repo = repository()
        repo.setUpDelivery(DeliveryDay.Closest, DeliverySlot.Morning, "3 Jun")
        repo.openDeliveryDetails()
        advanceTimeBy(AMBASSADOR_DELAY_MILLIS)
        runCurrent()
        assertTrue(repo.closeDeliveryDetails())
        assertEquals(Stage.Live, repo.snapshot.stage)
        assertEquals(Activation.FeedbackPending, repo.snapshot.activation)
        assertEquals(Avatar.Placeholder, repo.snapshot.avatar)
    }

    @Test
    fun activationRunsFeedbackThenModalThenDone() = runTest {
        val repo = repository()
        repo.startActivation()
        assertEquals(Activation.InProgress, repo.snapshot.activation)
        repo.finishActivation()
        assertEquals(Activation.Done, repo.snapshot.activation)
        assertEquals(Avatar.Generated, repo.snapshot.avatar)
        assertTrue(repo.snapshot.avatarIsNew)
        repo.clearNewAvatar()
        assertFalse(repo.snapshot.avatarIsNew)
    }

    @Test
    fun rewardIsPendingUntilTheNextColdLaunch() = runTest {
        val store = InMemoryKeyValueStore()
        val repo = repository(store)
        repo.rewardAmbassador(30_00)
        assertEquals(3_500_00, repo.snapshot.creditAvailable)
        assertEquals(0, repo.snapshot.spent())
        val relaunched = repository(store).also { it.coldLaunch() }
        assertEquals(3_470_00, relaunched.snapshot.creditAvailable)
        assertEquals(30_00, relaunched.snapshot.spent())
    }

    @Test
    fun juneCheck() = runTest {
        val store = InMemoryKeyValueStore()
        repository(store).rewardAmbassador(30_00)
        val repo = repository(store).also { it.coldLaunch() }
        repo.seedPurchases()
        repo.seedPurchases()
        assertEquals(70_00, repo.snapshot.spent(Month.June))
        assertEquals(40_00, repo.snapshot.received(Month.June))
        assertEquals(3_470_00, repo.snapshot.creditAvailable)
        assertEquals("$3,470", formatMoney(repo.snapshot.creditAvailable))
    }

    @Test
    fun julySeedCountsTowardJulyOnly() = runTest {
        val repo = repository()
        repo.setMonth(Month.July)
        repo.seedPurchases()
        assertEquals(1_832_40, repo.snapshot.spent(Month.July))
        assertEquals(0, repo.snapshot.spent(Month.June))
    }

    @Test
    fun ownBalanceChain() = runTest {
        val repo = repository()
        val between = repo.transferToInversiones(1_00)
        assertEquals(TransferReceipt("Plata Inversiones", 1_00, Currency.Usd, 0, 97_57, 78_81), between)
        assertEquals(1_00, repo.snapshot.accounts.inversionesCashUsd)
        val external = repo.transfer("Santander", 70_00, commission = 230_84)
        assertEquals(8_81, external.balanceAfter)
        assertEquals(8_81, repo.snapshot.accounts.ownBalance)
        assertEquals(3_500_00 - 230_84, repo.snapshot.creditAvailable)
    }

    @Test
    fun dnutQuoteRefreshesOnceAfterTheBuy() = runTest {
        val repo = repository()
        assertFalse(repo.buyDnut())
        repo.transferToInversiones(1_00)
        assertTrue(repo.buyDnut())
        assertEquals(0, repo.snapshot.accounts.inversionesCashUsd)
        assertEquals(1_00, repo.snapshot.portfolioValue)
        repo.refreshQuotes()
        repo.refreshQuotes()
        assertEquals(1_01, repo.snapshot.portfolioValue)
        repo.transferToInversiones(1_00)
        assertEquals(2_01, repo.snapshot.portfolioValue)
        assertEquals(2_00, repo.snapshot.portfolioCost)
    }

    @Test
    fun homeBadgeCountsBadgedTiles() = runTest {
        val repo = repository()
        assertEquals(2, repo.snapshot.homeBadge)
        repo.saveCashback(setOf(CashbackCategory.AllPurchases))
        repo.visitMsi()
        assertEquals(0, repo.snapshot.homeBadge)
    }

    @Test
    fun favoritesArePrependedAndReordered() = runTest {
        val repo = repository()
        val card = Favorite("plata-card", FavoriteKind.PlataCard, "Ana López")
        repo.addFavorite(card)
        assertEquals(listOf(card, Favorite.MyMobile), repo.snapshot.favorites)
        repo.moveFavorite(0, 1)
        assertEquals(listOf(Favorite.MyMobile, card), repo.snapshot.favorites)
        repo.removeFavorite(card.id)
        assertEquals(listOf(Favorite.MyMobile), repo.snapshot.favorites)
    }

    @Test
    fun resetForgetsEverything() = runTest {
        val store = InMemoryKeyValueStore()
        val repo = repository(store)
        repo.addSupportMessage()
        repo.reset()
        assertEquals(AppSnapshot(), repo.snapshot)
        assertNull(store.get(SNAPSHOT_KEY))
        assertEquals(1, repo.launches.value)
    }

    @Test
    fun moneyFormatting() {
        assertEquals("$3,457.97", formatMoney(3_457_97))
        assertEquals("−$70", formatMoney(-70_00))
        assertEquals("USD 2.01", formatMoney(2_01, Currency.Usd))
        assertEquals("USD 0.00", formatMoney(0, Currency.Usd, showCents = true))
    }
}
