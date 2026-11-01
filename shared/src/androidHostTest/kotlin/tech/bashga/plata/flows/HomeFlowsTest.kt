package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppIcon
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.CardId
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.HomeTiles
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.Session
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.dev.DevActions
import tech.bashga.plata.navigation.PlataApp
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The Home A flows of SCREEN-MAP §5 (8, 10, 12, 13, 14, 18, 28, 32, 33), driven through the running graph: a live,
 * logged-in user unlocks Lock with the keypad and taps from the FirstDay Home.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class HomeFlowsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** FirstDay right after Activation: avatar generated and new, no cashback, MSI not visited, nothing settled. */
    private val firstDay = AppSnapshot(
        session = Session("5512345678", PASSCODE, deviceLinked = true, loggedIn = true),
        stage = Stage.Live,
        activation = Activation.Done,
        avatar = Avatar.Generated,
        avatarIsNew = true,
    )

    private fun launchAtHome(snapshot: AppSnapshot = firstDay): AppRepository {
        val repository = AppRepository.inMemory(snapshot)
        rule.setContent { PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(repository) } } }
        waitFor("1")
        PASSCODE.forEach { rule.onNodeWithText(it.toString()).performClick() }
        return repository
    }

    private fun waitFor(text: String, substring: Boolean = false) =
        rule.waitUntilAtLeastOneExists(hasText(text, substring = substring), timeoutMillis = 5_000)

    private fun waitGone(text: String, substring: Boolean = false) =
        rule.waitUntilDoesNotExist(hasText(text, substring = substring), timeoutMillis = 5_000)

    private fun node(text: String, substring: Boolean = false): SemanticsNodeInteraction = rule.onAllNodesWithText(text, substring = substring).onFirst()

    private fun tap(text: String, substring: Boolean = false) {
        node(text, substring).scrollIntoView().performClick()
        rule.waitForIdle()
    }

    // tab bars, nav bars and dialogs have no scroll parent
    private fun SemanticsNodeInteraction.scrollIntoView(): SemanticsNodeInteraction =
        try {
            performScrollTo()
        } catch (_: AssertionError) {
            this
        }

    private fun back() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    /** Home › avatar: Profile opens on PictureIntro while the avatar is new; Got it clears it. */
    private fun openProfile(repository: AppRepository) {
        waitFor("Available in Plata Crédito")
        tap(CUSTOMER)
        if (repository.snapshot.avatarIsNew) {
            waitFor("here is your", substring = true)
            tap("Got it")
            waitGone("here is your", substring = true)
        }
        waitFor("Personal details")
    }

    @Test
    fun homeFirstDayThenActive() {
        // §5.14: FirstDay, scroll to the banner, then Active after the tiles, a relaunch and Dev › Seed purchases
        val repository = launchAtHome()
        waitFor("Nothing spent in June")
        node("None to defer").assertExists()
        node("Bring a friend and", substring = true).performScrollTo()
        rule.runOnUiThread {
            repository.update {
                it.copy(homeTiles = HomeTiles(setOf(CashbackCategory.AllPurchases, CashbackCategory.Airline, CashbackCategory.Entertainment, CashbackCategory.Restaurants), msiVisited = true), avatarIsNew = false)
            }
            repository.rewardAmbassador(30_00)
            repository.coldLaunch()
            DevActions(repository).seedPurchases()
        }
        waitFor("$70 Spent in June")
        waitFor("$3,470")
        waitFor("109 partners")
    }

    @Test
    fun cashback() {
        // §5.10: Choose cashback › Select 4 › Done › Show all › swipe and scroll the partners
        val repository = launchAtHome()
        waitFor("Choose")
        tap("Choose")
        waitFor("Get up to 15%", substring = true)
        tap("Select 4 categories")
        waitFor("for July", substring = true)
        listOf("1% All purchases", "5% Airline tickets", "5% Home and DIY", "5% Mobile, TV and Internet").forEach(::tap)
        tap("Done")
        waitFor("Time to use your card", substring = true)
        assertEquals(setOf(CashbackCategory.AllPurchases, CashbackCategory.Airline, CashbackCategory.HomeDiy, CashbackCategory.Mobile), repository.snapshot.homeTiles.cashback)
        tap("Show all")
        waitFor("Adidas")
        node("Farfetch").performScrollTo()
        back()
        waitFor("Time to use your card", substring = true)
        tap("Done")
        waitGone("Time to use your card", substring = true)
        waitFor("Cashback earned")
    }

    @Test
    fun referAFriend() {
        // §5.12: the Invite tab in June, then Dev › Month=July and reopen
        val repository = launchAtHome()
        waitFor("Invite")
        tap("Invite")
        waitFor("Invitation link")
        back()
        waitGone("Invitation link")
        rule.runOnUiThread { DevActions(repository).setMonth(Month.July) }
        tap("Invite")
        waitFor("15 days left")
        node("Not with Plata").performScrollTo()
        node("How it works").performScrollTo()
    }

    @Test
    fun profilePictureThenScroll() {
        // §5.18: the avatar opens Profile with PictureIntro; Got it clears avatarIsNew; scroll to the end
        val repository = launchAtHome()
        openProfile(repository)
        assertFalse(repository.snapshot.avatarIsNew)
        node("App version").performScrollTo()
        node("1.53.0(417)").assertExists()
    }

    @Test
    fun viewMembership() {
        // §5.8: Profile › Plata+ › scroll
        val repository = launchAtHome()
        openProfile(repository)
        tap("Plata+")
        waitFor("PLATA+ IS YOURS", substring = true)
        node("No annual fee").performScrollTo()
        node("Terms and conditions").performScrollTo()
        tap("Close")
        waitGone("Terms and conditions")
    }

    @Test
    fun changeAppIcon() {
        // §5.13: scroll the icon row, tap Black, OK
        val repository = launchAtHome()
        openProfile(repository)
        node("App version").performScrollTo()
        tap("Black")
        waitFor("You have changed the", substring = true)
        assertEquals(AppIcon.Black, repository.snapshot.settings.appIcon)
        tap("OK")
        waitGone("You have changed the", substring = true)
    }

    @Test
    fun personalDetails() {
        // §5.28: Personal details row; the chat link goes to Support
        val repository = launchAtHome()
        openProfile(repository)
        tap("Personal details")
        waitFor("To update your details", substring = true)
        tap("Close")
        waitGone("To update your details", substring = true)
        tap("Personal details")
        tap("To update your details", substring = true)
        waitGone("Personal details")
    }

    @Test
    fun devPanelFromAppVersion() {
        val repository = launchAtHome()
        openProfile(repository)
        rule.onNodeWithTag("app-version", useUnmergedTree = true).performScrollTo().performTouchInput { longClick() }
        waitFor("Dev panel")
    }

    @Test
    fun logOut() {
        // §5.33: Dev › Month=July, Dev › Support message ×4, Log out, confirm: PhoneNumber with the session kept
        val repository = launchAtHome()
        openProfile(repository)
        rule.runOnUiThread {
            DevActions(repository).setMonth(Month.July)
            repeat(4) { DevActions(repository).supportMessage() }
        }
        node("1.54.0(424)").performScrollTo()
        tap("Log out")
        waitFor("Are you sure you want to", substring = true)
        tap("Cancel")
        waitGone("Are you sure you want to", substring = true)
        tap("Log out")
        waitFor("Are you sure you want to", substring = true)
        rule.onAllNodesWithText("Log out").onLast().performClick()
        waitGone("App version")
        val session = repository.snapshot.session
        assertFalse(session.loggedIn)
        assertEquals("5512345678", session.phone)
        assertEquals(PASSCODE, session.passcode)
        assertTrue(session.deviceLinked)
    }

    @Test
    fun freezeACard() {
        // §5.32: Activation › ProductTour(ViewPin) › Show PIN (CardDetails, Dimmed) › Freeze › Freeze
        val repository = launchAtHome(firstDay.copy(activation = Activation.InProgress, avatarIsNew = false, avatar = Avatar.Placeholder))
        waitFor("Explore Plata")
        tap("Explore Plata")
        waitFor("Thank you!")
        tap("Thank you!")
        tap("Reward $30")
        waitFor("Show PIN")
        tap("Show PIN")
        waitFor("Swipe or tap to see details")
        tap("Freeze")
        waitFor("Freeze card")
        rule.onAllNodesWithText("Freeze").onLast().performClick()
        waitFor("Unfreeze")
        assertTrue(CardId.Physical in repository.snapshot.cards.frozen)
        waitFor("Charges can’t be made to your card")
    }

    private companion object {
        const val PASSCODE = "123456"
        const val CUSTOMER = "Sofía"
    }
}
