package tech.bashga.plata.flows

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Session
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataApp
import kotlin.test.assertEquals

/** A logged-in user with the card live and activation done: the shell's tabs are reachable directly. */
val LiveSnapshot = AppSnapshot(
    session = Session("5512345678", "123456", deviceLinked = true, loggedIn = true),
    stage = Stage.Live,
    activation = Activation.Done,
)

/** Drives the payments screens of a running [PlataApp] the way a user does. */
@OptIn(ExperimentalTestApi::class)
class PaymentsDriver(private val rule: ComposeContentTestRule) {
    fun launch(repository: AppRepository, startDestination: Any) = rule.setContent {
        PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(repository, startDestination = startDestination) } }
    }

    fun waitFor(text: String) = rule.waitUntilAtLeastOneExists(hasText(text), timeoutMillis = 5_000)

    fun waitFor(matcher: SemanticsMatcher) = rule.waitUntilAtLeastOneExists(matcher, timeoutMillis = 5_000)

    fun left(text: String): Float = rule.onNode(hasText(text)).fetchSemanticsNode().boundsInRoot.left

    fun waitGone(text: String) = rule.waitUntilDoesNotExist(hasText(text), timeoutMillis = 5_000)

    /** Taps the clickable node showing [text] (a title with the same text is not clickable). */
    fun tap(text: String) {
        waitFor(hasText(text) and hasClickAction())
        rule.onNode(hasText(text) and hasClickAction()).performClick()
    }

    /** Taps the clickable node with [description] (an icon button). */
    fun tapDescribed(description: String) {
        val matcher = hasContentDescription(description) and hasClickAction()
        waitFor(matcher)
        rule.onNode(matcher).performClick()
    }

    /** Types into the text field tagged [tag] (the tag sits on the field's frame). */
    fun type(tag: String, text: String) {
        val field = (hasTestTag(tag) or hasAnyAncestor(hasTestTag(tag))) and hasSetTextAction()
        waitFor(field)
        rule.onNode(field, useUnmergedTree = true).performTextInput(text)
        rule.waitForIdle()
    }

    /** Payments › Transfer › By CLABE, then a Santander CLABE, "Garcia" and Continue (§5.29 steps 2–7). */
    fun enterSantanderRecipient(withReference: Boolean) {
        tap("Transfer")
        tap("By CLABE")
        type("number", "014180655012345678")
        waitFor("Santander")
        type("name", "Garcia")
        if (withReference) {
            tap("Additional fields")
            tap("Reference number")
            tap("Done")
            type("reference", "01")
        }
        tap("Continue")
        waitFor("From Plata Crédito")
    }
}

/** The nodes showing [texts] sit left to right in that order (the Favorites grid). */
fun PaymentsDriver.assertLeftToRight(vararg texts: String) {
    texts.forEach { waitFor(it) }
    val lefts = texts.map { left(it) }
    assertEquals(lefts.sorted(), lefts, "${texts.toList()} at $lefts")
}
