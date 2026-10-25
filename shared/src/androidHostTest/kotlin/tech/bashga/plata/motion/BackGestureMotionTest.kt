package tech.bashga.plata.motion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.BackStyle
import tech.bashga.plata.navigation.PlataNavHost
import tech.bashga.plata.navigation.PlataNavigator
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screenshots.PLATA_DEVICE

@Serializable internal object MotionList

@Serializable internal object MotionDetail

/** Captures frames of an interactive back swipe over a push, in both back styles, into build/motion/ for eyeballing. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class BackGestureMotionTest {
    @get:Rule
    val rule = createComposeRule()

    private fun swipe(name: String, style: BackStyle, edge: Int) {
        lateinit var controller: NavHostController
        lateinit var input: DirectNavigationEventInput
        rule.mainClock.autoAdvance = false
        rule.setContent {
            PlataTheme {
                ProvideSafeArea(PlataSafeArea.IPhone) {
                    val navigator = remember { PlataNavigator() }
                    controller = rememberNavController(navigator)
                    val graph = remember(controller) {
                        controller.createGraph(MotionList) {
                            screen<MotionList> { Page("Accounts", PlataTheme.colors.surfaceCard) }
                            screen<MotionDetail> { Page("Details", PlataTheme.colors.accent) }
                        }
                    }
                    val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
                    input = remember { DirectNavigationEventInput() }
                    DisposableEffect(dispatcher) {
                        dispatcher.addInput(input)
                        onDispose { dispatcher.removeInput(input) }
                    }
                    Box(Modifier.size(393.dp, 852.dp).testTag("motion")) {
                        PlataNavHost(controller, navigator, graph, style = style)
                    }
                }
            }
        }
        rule.runOnUiThread { controller.navigate(MotionDetail) }
        rule.mainClock.advanceTimeBy(1_000)
        fun capture(label: String) = rule.onNodeWithTag("motion").captureRoboImage("build/motion/$name-$label.png")

        val touchX = if (edge == NavigationEvent.EDGE_LEFT) 0f else 1179f
        rule.runOnUiThread { input.backStarted(NavigationEvent(touchX = touchX, touchY = 1200f, progress = 0f, swipeEdge = edge, frameTimeMillis = 1_000)) }
        listOf(0.25f, 0.5f, 0.75f).forEachIndexed { index, progress ->
            val frame = 1_000L + 100L * (index + 1)
            rule.runOnUiThread { input.backProgressed(NavigationEvent(touchX = touchX, touchY = 1200f, progress = progress, swipeEdge = edge, frameTimeMillis = frame)) }
            rule.mainClock.advanceTimeBy(16)
            capture("p${(progress * 100).toInt()}")
        }
        rule.runOnUiThread { input.backCompleted() }
        var at = 0L
        listOf(16L, 100L, 200L, 400L).forEach { t ->
            rule.mainClock.advanceTimeBy(t - at)
            at = t
            capture("release-${t.toString().padStart(4, '0')}")
        }
    }

    @Test fun iosSwipe() = swipe("back-ios", BackStyle.Ios, NavigationEvent.EDGE_LEFT)

    @Test fun androidPredictiveBack() = swipe("back-android", BackStyle.Android, NavigationEvent.EDGE_LEFT)
}

@Composable
private fun Page(title: String, tint: Color) {
    Column(
        Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground).padding(PlataTheme.spacing.s16).padding(top = 59.dp),
        verticalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12),
    ) {
        BasicText(title, style = PlataTheme.typography.title1)
        repeat(6) { Box(Modifier.fillMaxWidth().height(72.dp).background(tint, RoundedCornerShape(PlataTheme.radii.md))) }
    }
}
