package tech.bashga.plata.dev

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.dev.dials.DialOption
import tech.bashga.plata.dev.dials.DialPanel
import tech.bashga.plata.dev.dials.DialRoot
import tech.bashga.plata.dev.dials.DialSpring
import tech.bashga.plata.dev.dials.DialStore
import tech.bashga.plata.dev.dials.SliderDial
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The DialKit port over a dot grid like DialKit's demo; frames go to build/dials/. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = "w393dp-h852dp-xxhdpi")
class DialKitTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var panel: DialPanel
    private lateinit var effectRadius: SliderDial
    private var appTaps = 0

    @Before
    fun demoPanel() {
        DialStore.panels.clear()
        DialStore.fabCenter = null
        panel = DialPanel("Dot Grid")
        panel.folder("grid").apply {
            slider("columns", 16f, 4f..40f, 1f)
            slider("rows", 30f, 4f..60f, 1f)
            slider("dotSize", 4f, 1f..10f, 0.1f, "pt")
        }
        panel.folder("interaction").apply {
            effectRadius = slider("effectRadius", 140f, 20f..300f, 1f, "pt")
            slider("touchScale", 0.5f, 0f..1f, 0.01f, "x")
            select("responseCurve", listOf(DialOption("easeOut"), DialOption("linear"), DialOption("spring")))
        }
        panel.folder("pulse").apply {
            slider("opacityMin", 0.3f, 0f..1f, 0.01f)
            slider("opacityMax", 1f, 0f..1f, 0.01f)
            spring("settle", DialSpring.Time(0.35f, 0.24f))
        }
        panel.folder("haptics", collapsed = true).apply { toggle("enabled", true) }
        panel.action("replay") {}
    }

    private fun show(open: Boolean) = rule.setContent {
        PlataTheme {
            ProvideSafeArea(PlataSafeArea.IPhone) {
                DialRoot(defaultOpen = open) {
                    Canvas(Modifier.fillMaxSize().background(Color(0xFF1A1420)).clickable { appTaps++ }) {
                        val gap = size.width / 17
                        for (x in 1..16) for (y in 1..40) drawCircle(Color.White.copy(alpha = 0.8f), 2.5f, Offset(x * gap, y * gap))
                    }
                }
            }
        }
    }

    @Test
    fun closedShowsTheButton() {
        show(open = false)
        rule.onRoot().captureRoboImage("build/dials/closed.png")
    }

    @Test
    fun opensFromTheButtonAndCloses() {
        show(open = false)
        rule.mainClock.autoAdvance = false
        rule.onRoot().performTouchInput { click(Offset(width - 36f * density, height - 36f * density - 34f * density)) }
        listOf(60L, 120L, 400L).forEach { ms ->
            rule.mainClock.advanceTimeBy(ms)
            rule.onRoot().captureRoboImage("build/dials/opening-$ms.png")
        }
    }

    @Test
    fun buttonKeepsItsPlaceThroughOpenAndClose() {
        show(open = false)
        val fab = Offset(393f - 36f, 852f - 36f - 34f)
        rule.onRoot().performTouchInput {
            down(fab * density)
            moveBy(Offset(-150f, -300f) * density, delayMillis = 100)
            moveBy(Offset(-50f, -100f) * density, delayMillis = 100)
            up()
        }
        rule.waitForIdle()
        val moved = DialStore.fabCenter
        assertTrue(moved != null)
        rule.onRoot().performTouchInput { click(moved) }
        rule.waitForIdle()
        rule.onRoot().performTouchInput { click(Offset(width / 2f, 100f * density)) }
        rule.waitForIdle()
        assertEquals(moved, DialStore.fabCenter)
        rule.onRoot().captureRoboImage("build/dials/moved-after-close.png")
    }

    @Test
    fun twoFingerTapOpensWithoutTappingTheApp() {
        show(open = false)
        rule.onRoot().performTouchInput {
            down(0, Offset(width * 0.4f, height * 0.4f))
            down(1, Offset(width * 0.6f, height * 0.4f))
            up(0)
            up(1)
        }
        rule.waitForIdle()
        assertEquals(0, appTaps)
        rule.onNodeWithText("Version 1").assertExists()
        rule.onRoot().performTouchInput { click(Offset(width / 2f, height * 0.2f)) }
        rule.waitForIdle()
        rule.onRoot().performTouchInput { click(Offset(width / 2f, height * 0.2f)) }
        rule.waitForIdle()
        assertEquals(1, appTaps)
    }

    @Test
    fun drawer() {
        show(open = true)
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/dials/open.png")
    }

    @Test
    fun slidingMovesByTravelAndSnaps() {
        show(open = true)
        rule.waitForIdle()
        val row = rule.onNodeWithText("Effect Radius")
        val before = effectRadius.value
        row.performTouchInput {
            down(center)
            moveBy(Offset(40f * density, 0f))
            moveBy(Offset(40f * density, 0f))
        }
        rule.onRoot().captureRoboImage("build/dials/sliding.png")
        row.performTouchInput { up() }
        assertTrue(effectRadius.value > before)
        assertEquals(effectRadius.value, kotlin.math.round(effectRadius.value))
    }

    @Test
    fun versionsKeepTheirOwnValues() {
        show(open = true)
        rule.waitForIdle()
        effectRadius.set(200f)
        panel.savePreset()
        effectRadius.set(250f)
        panel.clearActivePreset()
        assertEquals(200f, effectRadius.value)
        panel.loadPreset(panel.presets.single().id)
        assertEquals(250f, effectRadius.value)
        assertEquals("Version 2", panel.activePresetName)
        rule.onNodeWithText("Version 2").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/dials/versions-menu.png")
    }

    @Test
    fun copiesValuesAsJson() {
        val text = panel.copyInstructionText()
        assertTrue("\"effectRadius\": 140" in text, text)
        assertTrue("\"duration\": 0.35" in text, text)
        assertTrue(text.startsWith("Update the DialKit configuration for \"Dot Grid\""), text)
    }
}
