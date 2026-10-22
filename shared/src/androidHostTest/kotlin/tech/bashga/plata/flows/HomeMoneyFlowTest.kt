package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.Session
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataApp
import tech.bashga.plata.screenshots.PLATA_DEVICE

/** The Home B flows (SCREEN-MAP §5.15, 5.19, 5.23, 5.24, 5.27, 5.35): a live, logged-in user unlocks and taps from Home. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
abstract class HomeMoneyFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** Live, logged in with passcode 123456, activation done: Splash → Lock. */
    val live = AppSnapshot(
        session = Session("5512345678", PASSCODE, deviceLinked = true, loggedIn = true),
        stage = Stage.Live,
        activation = Activation.Done,
        avatar = Avatar.Generated,
    )

    fun launch(repository: AppRepository) = rule.setContent {
        PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(repository) } }
    }

    /** Launches at Splash, unlocks Lock with the passcode keypad and waits for Home. */
    fun launchAtHome(repository: AppRepository) {
        launch(repository)
        waitFor("1")
        PASSCODE.forEach { rule.onNodeWithText(it.toString()).performClick() }
        waitFor("Available in Plata Crédito")
    }

    fun waitFor(text: String, substring: Boolean = false) =
        rule.waitUntilAtLeastOneExists(hasText(text, substring = substring), timeoutMillis = 5_000)

    fun waitGone(text: String, substring: Boolean = false) =
        rule.waitUntilDoesNotExist(hasText(text, substring = substring), timeoutMillis = 5_000)

    fun tap(text: String, index: Int = 0, substring: Boolean = false) {
        rule.onAllNodesWithText(text, substring = substring)[index].performClick()
        rule.waitForIdle()
    }

    /** Runs the click action of the node holding [text], for cards whose centre lands on a nested button. */
    fun click(text: String) {
        node(text).performSemanticsAction(SemanticsActions.OnClick)
        rule.waitForIdle()
    }

    fun node(text: String, substring: Boolean = false): SemanticsNodeInteraction =
        rule.onAllNodesWithText(text, substring = substring).onFirst()

    /** Waits until [text] is on screen: a push slides in, so its nodes exist before they are displayed. */
    fun seen(text: String, substring: Boolean = false) = rule.waitUntil(5_000) {
        runCatching { node(text, substring).assertIsDisplayed() }.isSuccess
    }

    fun scrollTo(text: String, substring: Boolean = false) {
        node(text, substring).performScrollTo()
        rule.waitForIdle()
    }

    fun back() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    companion object {
        const val PASSCODE = "123456"
    }
}
