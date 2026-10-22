package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
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
import tech.bashga.plata.data.Invest
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.Session
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.InvestTab
import tech.bashga.plata.navigation.PlataApp
import tech.bashga.plata.screenshots.PLATA_DEVICE

/**
 * The invest flows (SCREEN-MAP §5) drive the real graph from the Invest tab of a live account: the stage the flow
 * starts at is a seeded snapshot, so the tests don't depend on the auth screens in front of it.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
abstract class InvestFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    protected fun liveSnapshot(month: Month, account: InvestAccount = InvestAccount.NotOpened, recents: List<String> = emptyList()) = AppSnapshot(
        session = Session("5512345678", "123456", deviceLinked = true, loggedIn = true),
        stage = Stage.Live,
        activation = Activation.Done,
        month = month,
        invest = Invest(account, recents = recents),
    )

    protected fun launchInvestTab(repository: AppRepository) = rule.setContent {
        PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(repository, startDestination = InvestTab) } }
    }

    protected fun waitFor(matcher: SemanticsMatcher) = rule.waitUntilAtLeastOneExists(matcher, timeoutMillis = 5_000)

    protected fun waitFor(text: String, substring: Boolean = false) = waitFor(hasText(text, substring = substring))

    protected fun waitGone(text: String, substring: Boolean = false) =
        rule.waitUntilDoesNotExist(hasText(text, substring = substring), timeoutMillis = 5_000)

    protected fun tap(text: String, substring: Boolean = false) {
        waitFor(text, substring)
        click(hasText(text, substring = substring))
    }

    protected fun tapIn(text: String, ancestorTag: String) {
        val node = hasText(text) and hasAnyAncestor(hasTestTag(ancestorTag))
        waitFor(node)
        click(node)
    }

    private fun click(matcher: SemanticsMatcher) {
        val node = rule.onAllNodes(matcher).onFirst()
        if (rule.onAllNodes(matcher and hasAnyAncestor(hasScrollAction())).fetchSemanticsNodes().isNotEmpty()) node.performScrollTo()
        node.performClick()
    }

    protected fun scrollTo(text: String, substring: Boolean = false) {
        waitFor(text, substring)
        val matcher = hasText(text, substring = substring)
        if (rule.onAllNodes(matcher and hasAnyAncestor(hasScrollAction())).fetchSemanticsNodes().isNotEmpty()) rule.onAllNodes(matcher).onFirst().performScrollTo()
    }

    protected fun pressBack() = rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
}
