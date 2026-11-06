package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
import kotlin.test.assertTrue

/** SCREEN-MAP §5.3, Creating a passcode, on the real screens: PhoneNumber ⟨Otp⟩ → Passcode → EnableFaceId. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class FirstRunFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun firstRunLinksTheDeviceAndCreatesAPasscode() {
        val repository = AppRepository.inMemory()
        rule.launch(repository)

        rule.waitFor("phone number")
        rule.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("phone"))).performTextInput("5512345678")
        rule.onNodeWithText("Continue").performClick()
        rule.waitFor("Confirmation")
        rule.onNodeWithText("+52 55 1234 5678", substring = true).assertExists()
        rule.onNodeWithTag("otp").performTextInput("1234")

        rule.waitFor("Create passcode")
        assertTrue(repository.snapshot.session.deviceLinked)
        rule.tapDigits("12345")
        rule.tapDigits("6")
        rule.waitFor("Confirm passcode")
        rule.tapDigits("123456")
        rule.waitFor("Passcode was set!")
        rule.waitFor("Enable Face ID?")

        val session = repository.snapshot.session
        assertEquals("5512345678", session.phone)
        assertEquals("123456", session.passcode)

        rule.onNodeWithText("Enable").performClick()
        rule.waitUntilGone("Enable Face ID?")
        assertTrue(repository.snapshot.session.loggedIn)
        assertTrue(repository.snapshot.session.faceId)
    }
}
