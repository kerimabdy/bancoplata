package tech.bashga.plata.motion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.designsystem.components.BottomSheet
import tech.bashga.plata.designsystem.components.ModalScrim
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay
import tech.bashga.plata.navigation.OverlayHost
import tech.bashga.plata.navigation.SheetOverlay
import tech.bashga.plata.screenshots.PLATA_DEVICE

/** Captures frames of a page sheet's drag release and a bottom sheet's entrance into build/motion/. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class OverlayMotionTest {
    @get:Rule
    val rule = createComposeRule()

    private var sheet by mutableStateOf<Unit?>(Unit)
    private var bottom by mutableStateOf<Unit?>(null)

    private fun host() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            PlataTheme {
                ProvideSafeArea(PlataSafeArea.IPhone) {
                    OverlayHost(Modifier.size(393.dp, 852.dp).testTag("host")) {
                        Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground))
                        Overlay(bottom, onDismiss = { bottom = null }) {
                            ModalScrim({ bottom = null }) { BottomSheet(title = "Transfer") { Box(Modifier.size(200.dp)) } }
                        }
                        SheetOverlay(sheet, onDismiss = { sheet = null }) {
                            Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceCard).testTag("sheet"))
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1_000)
    }

    private fun capture(name: String, times: List<Long>) {
        var at = 0L
        times.forEach { t ->
            rule.mainClock.advanceTimeBy(t - at)
            at = t
            rule.onNodeWithTag("host").captureRoboImage("build/motion/$name-${t.toString().padStart(4, '0')}.png")
        }
    }

    private fun drag(distance: Float) = rule.onNodeWithTag("sheet").performTouchInput {
        down(center)
        repeat(20) { moveBy(Offset(0f, distance / 20), delayMillis = 16) }
        up()
    }

    @Test fun sheetDragCancel() {
        host()
        drag(600f)
        capture("sheet-drag-cancel", (0L..500L step 50L).toList())
    }

    @Test fun sheetDragDismiss() {
        host()
        drag(1_500f)
        capture("sheet-drag-dismiss", (0L..700L step 50L).toList())
        assertNull(sheet)
    }

    @Test fun sheetRubberBand() {
        host()
        drag(-600f)
        capture("sheet-rubber-band", (0L..300L step 50L).toList())
    }

    @Test fun bottomSheetPresent() {
        sheet = null
        host()
        bottom = Unit
        capture("bottom-present", (0L..400L step 32L).toList())
    }
}
