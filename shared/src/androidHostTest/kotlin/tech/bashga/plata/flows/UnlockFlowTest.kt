package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertTrue

/** SCREEN-MAP §5.1 steps 1–3: Splash → Lock → the stored passcode opens the stage root; a wrong one keeps the lock. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class UnlockFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theStoredPasscodeUnlocks() {
        val repository = AppRepository.inMemory(returningUser)
        rule.launch(repository)

        rule.waitFor("Enter passcode")
        rule.tapDigits("654321")
        rule.waitFor("Enter passcode")
        rule.tapDigits("123456")
        rule.waitUntilGone("Enter passcode")
        rule.waitFor("Payments")
        assertTrue(repository.snapshot.session.loggedIn)
    }
}
