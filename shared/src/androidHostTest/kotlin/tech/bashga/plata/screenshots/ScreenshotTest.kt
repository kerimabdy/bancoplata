package tech.bashga.plata.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataColors
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.RootReplaceTransition
import tech.bashga.plata.navigation.StaticLayer
import tech.bashga.plata.navigation.StaticLayers
import tech.bashga.plata.navigation.TabShell
import java.io.File

/** iPhone 15/16 logical size at @3x: a full-screen render is 1179×2556px, the source screens minus the watermark bar. */
const val PLATA_DEVICE = "w393dp-h852dp-xxhdpi"

private const val CAPTURE_TAG = "plata-capture"

/**
 * Base class for screenshot tests. Subclass it once per component or screen area, in its own file:
 *
 * ```
 * class ListRowScreenshots : ScreenshotTest() {
 *     @Test fun single() = component("list-row/size=single,state=default,surface=plain") { ListRowSinglePreview() }
 * }
 * ```
 *
 * Renders go to `cmp-app/screenshots/component/<name>.png` and `cmp-app/screenshots/screen/<name>.png`,
 * named so `tools/compare.sh` and `tools/compare-component.sh` can find their reference images.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
abstract class ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** A full 393×852 screen with the iPhone safe area (top 59, bottom 34). [id] is the source screen id. */
    fun screen(id: String, content: @Composable () -> Unit) {
        capture(listOf("screen/$id")) { ScreenFrame(content) }
    }

    /**
     * One reference state: `XContent(state)` composed inside the fake app context at 393×852 and the iPhone safe area,
     * written to `screenshots/screen/<id>.png` for each of [ids] (ids that show the same state share one test).
     *
     * - [presentation]: how the screen under test is presented; [backdrop] overrides the Stacked/Dimmed rule for a sheet.
     * - [under]: the presenters below it, bottom first ([StaticLayer]); the nav host's rules shrink or dim them.
     * - [shell]: the tab bar around the bottom layer, for tab screens and the screens pushed in a tab.
     * - [repository]: what `LocalAppRepository` provides (a fixture; the default is a first-run snapshot).
     * - [clockMillis]: pauses the clock and captures that many ms after the first frame (mid-animation references,
     *   the text caret); [replacing] then plays the nav host's root-replace transition from it to [content].
     * - [setup]: what a user does before the capture: scroll, focus a field, swipe a pager ([StateSetup]).
     */
    fun screenState(
        vararg ids: String,
        presentation: Presentation = Presentation.Push,
        backdrop: PageSheetBackdrop? = null,
        under: List<StaticLayer> = emptyList(),
        shell: TestShell? = null,
        repository: AppRepository = AppRepository.inMemory(),
        clockMillis: Long? = null,
        replacing: (@Composable () -> Unit)? = null,
        setup: StateSetup.() -> Unit = {},
        content: @Composable () -> Unit,
    ) {
        if (clockMillis != null) composeRule.mainClock.autoAdvance = false
        capture(ids.map { "screen/$it" }, prepare = {
            if (clockMillis != null) composeRule.mainClock.advanceTimeByFrame()
            StateSetup(composeRule).setup()
            if (clockMillis != null) composeRule.mainClock.advanceTimeBy(clockMillis)
        }) {
            ScreenFrame {
                CompositionLocalProvider(LocalAppRepository provides repository) {
                    val layers = under + StaticLayer(presentation, backdrop, content)
                    val stack: @Composable () -> Unit = {
                        StaticLayers(layers) { base ->
                            if (shell == null) base() else TabShell(shell.tab, shell.data, {}) { base() }
                        }
                    }
                    if (replacing != null) RootReplaceTransition(from = replacing, to = stack) else stack()
                }
            }
        }
    }

    @Composable
    private fun ScreenFrame(content: @Composable () -> Unit) {
        Box(Modifier.size(393.dp, 852.dp).testTag(CAPTURE_TAG)) {
            ProvideSafeArea(PlataSafeArea.IPhone) {
                Box(Modifier.fillMaxSize()) { content() }
            }
        }
    }

    /**
     * A component at its own size, inset by [margin] on [background] (e.g. `{ it.surfaceCard }`) to mirror the ~4pt margin of `ds/specimens` crops.
     * [name] is `<component-folder>/<variant>`, the same relative path as its specimen. [clockMillis] and [setup] work as
     * in [screenState] (a focused field's caret, a mid-animation frame).
     */
    fun component(
        name: String,
        background: (PlataColors) -> Color = { it.surfaceBackground },
        margin: Dp = 4.dp,
        clockMillis: Long? = null,
        setup: StateSetup.() -> Unit = {},
        content: @Composable () -> Unit,
    ) {
        if (clockMillis != null) composeRule.mainClock.autoAdvance = false
        capture(listOf("component/$name"), prepare = {
            if (clockMillis != null) composeRule.mainClock.advanceTimeByFrame()
            StateSetup(composeRule).setup()
            if (clockMillis != null) composeRule.mainClock.advanceTimeBy(clockMillis)
        }) {
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart)
                    .testTag(CAPTURE_TAG)
                    .background(background(PlataTheme.colors))
                    .padding(margin),
            ) { content() }
        }
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(relativePaths: List<String>, prepare: () -> Unit = {}, content: @Composable () -> Unit) {
        composeRule.setContent { PlataTheme { content() } }
        prepare()
        val files = relativePaths.map { File(screenshotsDir, "$it.png").apply { parentFile?.mkdirs() } }
        composeRule.onNodeWithTag(CAPTURE_TAG).captureRoboImage(
            filePath = files.first().absolutePath,
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record),
        )
        files.drop(1).forEach { files.first().copyTo(it, overwrite = true) }
    }

    private val screenshotsDir: File
        get() = File(System.getProperty("plata.screenshots.dir") ?: "../screenshots")
}
