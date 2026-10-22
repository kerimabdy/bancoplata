package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.InMemoryKeyValueStore
import tech.bashga.plata.data.Stage
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.data.spent
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataApp
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * SCREEN-MAP §5 flows that cross areas, on one running [PlataApp] from Splash: first run (5.3) → delivery (5.5) →
 * onboarding (5.1) → Home; log out (5.33) → log in (5.4); Home → Payments (5.34, 5.29) → Home's balances.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class CrossAreaFlowsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val backend = StandardTestDispatcher()
    private var clock = 0L
    private lateinit var repository: AppRepository

    private fun launch(snapshot: AppSnapshot? = null) {
        repository = AppRepository(InMemoryKeyValueStore(), CoroutineScope(backend), { clock }, snapshot)
        rule.setContent { PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(repository) } } }
        rule.waitForIdle()
    }

    private fun advanceBackend(millis: Long) {
        clock += millis
        backend.scheduler.advanceTimeBy(millis)
        backend.scheduler.runCurrent()
        rule.waitForIdle()
    }

    private fun waitFor(matcher: SemanticsMatcher) = rule.waitUntilAtLeastOneExists(matcher, timeoutMillis = 5_000)

    private fun waitFor(text: String) = waitFor(hasText(text, substring = true))

    private fun waitGone(text: String) = rule.waitUntilDoesNotExist(hasText(text, substring = true), timeoutMillis = 5_000)

    private fun tap(text: String) = tap(hasText(text) and hasClickAction())

    private fun tapDescribed(description: String) = tap(hasContentDescription(description) and hasClickAction())

    private fun tap(matcher: SemanticsMatcher) {
        waitFor(matcher)
        val node = rule.onAllNodes(matcher).onFirst()
        // tab bars, nav bars and dialogs have no scroll parent
        try {
            node.performScrollTo()
        } catch (_: AssertionError) {
        }
        node.performClick()
        rule.waitForIdle()
    }

    private fun type(tag: String, text: String) {
        val field = (hasTestTag(tag) or hasAnyAncestor(hasTestTag(tag))) and hasSetTextAction()
        waitFor(field)
        rule.onNode(field, useUnmergedTree = true).performTextInput(text)
        rule.waitForIdle()
    }

    private fun unlock() {
        waitFor("Enter passcode")
        rule.tapDigits("123456")
        waitGone("Enter passcode")
    }

    @Test
    fun firstRunThroughDeliveryAndActivationToTheFirstDayHome() {
        launch()

        // §5.3
        waitFor("phone number")
        type("phone", "5512345678")
        tap("Continue")
        waitFor("Confirmation")
        rule.onNodeWithTag("otp").performTextInput("1234")
        waitFor("Scan QR")
        rule.onNodeWithTag("viewfinder").performTouchInput { longClick() }
        waitFor("Create passcode")
        rule.tapDigits("123456")
        waitFor("Confirm passcode")
        rule.tapDigits("123456")
        waitFor("Enable Face ID?")
        tap("Not now")

        // §5.5: stage=NotScheduled, so the root is ScheduleDeliveryIntro
        tap("Schedule now")
        type("query", "Av Rio")
        tap("Av. Río Mixcoac 25, Actipan")
        tap("Continue")
        tap("Select the closest slot")
        tap("Set up delivery")
        waitFor("Delivery successfully arranged!")
        assertIs<Stage.CardComing>(repository.snapshot.stage)
        tap("Done")
        waitGone("Delivery successfully arranged!")

        // §5.1 steps 5–7
        waitFor("Your card is coming")
        tap("Full info")
        waitFor("Ambassador to be assigned")
        advanceBackend(10_000)
        waitFor("Ambassador assigned")
        assertEquals(Ambassador.Assigned("Carlos"), (repository.snapshot.stage as Stage.CardComing).ambassador)
        tap("Close")
        waitFor("How was the meeting with")
        assertEquals(Stage.Live, repository.snapshot.stage)

        // §5.1 steps 8–15
        tapDescribed("Close")
        waitFor("Welcome to\nPlata, Sofía")
        tap("Explore Plata")
        tap("Thank you!")
        tap("Reward $30")
        waitFor("View your PIN in\ncard details")
        tap("Next")
        waitFor("Go digital!")
        tap("Next")
        waitFor("Commission-free\npayments")
        tap("Let’s Plata!")

        waitFor("Available in Plata Crédito")
        waitFor("$3,500")
        val done = repository.snapshot
        assertEquals(Activation.Done, done.activation)
        assertEquals(Avatar.Generated, done.avatar)
        assertTrue(done.session.loggedIn)
        assertTrue(done.ledger.single { it.kind == EntryKind.Reward }.pending)

        // §5.14 step 3: a relaunch settles the $30 reward
        rule.runOnUiThread { repository.coldLaunch() }
        waitFor("$3,470")
        assertEquals(3_470_00L, repository.snapshot.creditAvailable)
    }

    @Test
    fun loggingOutFromProfileThenLoggingBackInReturnsToHome() {
        launch(returningUser.copy(avatar = Avatar.Generated))
        unlock()

        // §5.33
        waitFor("Available in Plata Crédito")
        tap(hasText("Sofía") and hasClickAction())
        waitFor("Personal details")
        tap("Log out")
        waitFor("Are you sure you want to")
        rule.onAllNodesWithText("Log out").onLast().performClick()
        waitFor("phone number")
        assertFalse(repository.snapshot.session.loggedIn)

        // §5.4
        waitFor("5512345678")
        tap("Continue")
        waitFor("Confirmation")
        rule.onNodeWithTag("otp").performTextInput("2468")
        waitFor("Enable Face ID?")
        tap("Not now")
        waitFor("Available in Plata Crédito")
        val session = repository.snapshot.session
        assertTrue(session.loggedIn)
        assertEquals("123456", session.passcode)
    }

    @Test
    fun aTransferFromPaymentsShowsUpOnHome() {
        launch(returningUser)
        unlock()

        // §5.34 → §5.29
        waitFor("Available in Plata Crédito")
        waitFor("$3,500")
        tap("Payments")
        waitFor("Favorites, bills, providers")
        tap("Transfer")
        tap("By CLABE")
        type("number", "014180655012345678")
        waitFor("Santander")
        type("name", "Garcia")
        tap("Continue")
        waitFor("$97.57")
        type("amount", "50")
        tap("Transfer")
        waitFor("–$50")
        tap("Done")
        waitGone("–$50")

        val snapshot = repository.snapshot
        assertEquals(47_57, snapshot.accounts.ownBalance)
        tap("Home")
        waitFor("Available in Plata Crédito")
        waitFor(formatPesos(snapshot.creditAvailable))
        assertTrue(snapshot.spent() > 0)
    }

    private fun formatPesos(cents: Long): String {
        val pesos = cents / 100
        val grouped = pesos.toString().reversed().chunked(3).joinToString(",").reversed()
        val rest = cents % 100
        return if (rest == 0L) "$$grouped" else "$$grouped.${rest.toString().padStart(2, '0')}"
    }
}
