package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals

/** SCREEN-MAP §5.22, Support: the Support tab opens the chat and clears the unread badge; a typed message sends. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class SupportFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theSupportTabOpensTheChat() {
        val repository = AppRepository.inMemory(returningUser.copy(month = Month.July, supportUnread = 4))
        rule.launch(repository)

        rule.waitFor("Enter passcode")
        rule.tapDigits("123456")
        rule.waitFor("Payments")
        assertEquals(4, repository.snapshot.supportUnread)

        rule.onNodeWithText("Support").performClick()
        rule.waitFor("We’re here 24/7")
        rule.waitFor("Plata Support Team")
        rule.waitForIdle()
        assertEquals(0, repository.snapshot.supportUnread)

        val composer = rule.onNode(hasSetTextAction() and (hasTestTag("composer") or hasAnyAncestor(hasTestTag("composer"))))
        composer.performTextInput("Where is my card?")
        composer.performImeAction()
        rule.waitFor("Where is my card?")
        rule.onNodeWithText("Message").assertExists()
    }
}
