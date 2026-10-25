package tech.bashga.plata.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.CardId
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.OverlayHost
import tech.bashga.plata.screenshots.PLATA_DEVICE
import tech.bashga.plata.screens.home.CardDetailsContent
import tech.bashga.plata.screens.home.CardDetailsOverlay
import tech.bashga.plata.screens.home.CardDetailsState

/** Captures mid-animation frames of the card sheet's transitions into build/motion/ for eyeballing the motion. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class CardDetailsMotionTest {
    @get:Rule
    val rule = createComposeRule()

    private fun frames(name: String, from: CardDetailsState, to: CardDetailsState, times: List<Long>, tag: String = "motion") {
        var state by mutableStateOf(from)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            PlataTheme {
                ProvideSafeArea(PlataSafeArea.IPhone) {
                    OverlayHost(Modifier.size(393.dp, 852.dp).testTag("host")) {
                        Box(Modifier.size(393.dp, 560.dp).testTag("motion")) { CardDetailsContent(state, {}) }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1_000)
        state = to
        var at = 0L
        times.forEach { t ->
            rule.mainClock.advanceTimeBy(t - at)
            at = t
            rule.onNodeWithTag(tag).captureRoboImage("build/motion/$name-${t.toString().padStart(4, '0')}.png")
        }
    }

    private val times = (0L..2_000L step 100L).toList()

    @Test fun freeze() = frames("freeze", CardDetailsState(CardId.Physical), CardDetailsState(CardId.Physical, frozen = true), times.take(8))

    @Test fun reissue() = frames("reissue", CardDetailsState(CardId.Physical, number = "9512"), CardDetailsState(CardId.Physical, number = "9984"), times)

    @Test fun discard() = frames("discard", CardDetailsState(CardId.Physical, number = "9984"), CardDetailsState(CardId.Physical, number = "9984", cancelled = true), times.take(11))

    @Test fun reissueDialog() = frames(
        "reissue-dialog",
        CardDetailsState(CardId.Physical),
        CardDetailsState(CardId.Physical, overlay = CardDetailsOverlay.ReissueConfirm),
        (0L..400L step 50L).toList(),
        tag = "host",
    )
}
