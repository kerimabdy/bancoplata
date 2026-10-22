package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.InMemoryKeyValueStore
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.LocalAppNavigator
import tech.bashga.plata.navigation.MainShell
import tech.bashga.plata.navigation.PlataNavHost
import tech.bashga.plata.navigation.PlataNavigator
import tech.bashga.plata.navigation.plataGraph
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.reflect.KClass

/**
 * Base for the delivery-area flow tests (SCREEN-MAP §5.1, 5.5, 5.6, 5.9, 5.30). It runs the real graph like
 * `PlataApp`, from a seeded snapshot and stage root, and keeps the [AppNavigator] so a flow can start where another
 * area's screen would have sent it (Home › Card delivery › Full info, the feedback dialog). The repository's timers
 * run on [backend] against the fake [clock], so a test fires AmbassadorAssigned by advancing them.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
abstract class DeliveryFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    protected val backend = StandardTestDispatcher()
    protected var clock = 0L
    protected lateinit var repository: AppRepository
    protected lateinit var nav: AppNavigator

    protected fun launch(snapshot: AppSnapshot, start: Any) {
        repository = AppRepository(InMemoryKeyValueStore(), CoroutineScope(backend), { clock }, snapshot)
        rule.setContent {
            PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { FlowApp(repository, start) { nav = it } } }
        }
        rule.waitForIdle()
    }

    /** What another area's control does: navigate as it would, on the UI thread. */
    protected fun navigate(route: Any) {
        rule.runOnUiThread { nav.navigate(route) }
        rule.waitForIdle()
    }

    protected fun advanceBackend(millis: Long) {
        clock += millis
        backend.scheduler.advanceTimeBy(millis)
        backend.scheduler.runCurrent()
        rule.waitForIdle()
    }

    protected fun waitFor(text: String, substring: Boolean = false) =
        rule.waitUntilAtLeastOneExists(hasText(text, substring = substring), timeoutMillis = 5_000)

    protected fun waitGone(text: String, substring: Boolean = false) =
        rule.waitUntilDoesNotExist(hasText(text, substring = substring), timeoutMillis = 5_000)

    protected fun tap(text: String, substring: Boolean = false) {
        waitFor(text, substring)
        rule.onNodeWithText(text, substring = substring).performClick()
        rule.waitForIdle()
    }

    protected fun type(fieldTag: String, text: String) {
        rule.onNode(hasSetTextAction() and (hasTestTag(fieldTag) or hasAnyAncestor(hasTestTag(fieldTag))), useUnmergedTree = true)
            .performTextInput(text)
        rule.waitForIdle()
    }

    /** System back: what the iOS edge swipe and a sheet's swipe down also send. */
    protected fun back() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    protected fun isOnTop(route: KClass<*>): Boolean = nav.controller.currentBackStackEntry?.destination?.hasRoute(route) == true
}

@Composable
private fun FlowApp(repository: AppRepository, start: Any, onNav: (AppNavigator) -> Unit) {
    CompositionLocalProvider(LocalAppRepository provides repository) {
        val navigator = remember { PlataNavigator() }
        val controller = rememberNavController(navigator)
        val nav = remember(controller) { AppNavigator(controller, navigator, repository).also(onNav) }
        val graph = remember(controller) { controller.createGraph(start) { plataGraph(nav, repository) } }
        CompositionLocalProvider(LocalAppNavigator provides nav) {
            PlataNavHost(controller, navigator, graph, baseFrame = { top, content -> MainShell(top, nav, content) })
        }
    }
}
