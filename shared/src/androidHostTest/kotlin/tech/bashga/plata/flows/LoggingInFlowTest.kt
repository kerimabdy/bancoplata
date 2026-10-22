package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SCREEN-MAP §5.4, Logging in: Lock › Log out (§5.33's end, from the lock screen) → PhoneNumber, prefilled → Otp →
 * EnableFaceId (the device is linked, so QR and passcode are skipped) → Not now → the tab shell.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class LoggingInFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun aLinkedDeviceLogsBackInWithoutQrOrPasscode() {
        val repository = AppRepository.inMemory(returningUser)
        rule.launch(repository)

        rule.waitFor("Enter passcode")
        rule.waitFor(hasContentDescription("Log out"))
        rule.onNodeWithContentDescription("Log out").performClick()
        rule.waitFor("phone number")
        assertFalse(repository.snapshot.session.loggedIn)
        rule.onNodeWithText("5512345678").assertExists()

        rule.onNodeWithText("Continue").performClick()
        rule.waitFor("Confirmation")
        rule.onNodeWithText("Resend in 4 sec").assertExists()
        rule.onNodeWithTag("otp").performTextInput("2468")

        rule.waitFor("Enable Face ID?")
        rule.onNodeWithText("Not now").performClick()
        rule.waitFor("Payments")
        rule.waitUntilGone("Enable Face ID?")

        val session = repository.snapshot.session
        assertTrue(session.loggedIn)
        assertFalse(session.faceId)
        assertEquals("123456", session.passcode)
    }

    @Test
    fun theOtpSheetCancelsBackToThePhoneNumber() {
        val repository = AppRepository.inMemory(returningUser.copy(session = returningUser.session.copy(loggedIn = false)))
        rule.launch(repository)

        rule.waitFor("phone number")
        rule.onNodeWithText("Continue").performClick()
        rule.waitFor("Confirmation")
        rule.onNodeWithText("Cancel").performClick()
        rule.waitUntilGone("Confirmation")
        rule.onNodeWithText("Continue").assertExists()
    }
}
