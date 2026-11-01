package tech.bashga.plata.screenshots

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.junit.Test
import tech.bashga.plata.screens.auth.EnableFaceIdContent
import tech.bashga.plata.screens.auth.LockContent
import tech.bashga.plata.screens.auth.LockState
import tech.bashga.plata.screens.auth.PasscodeContent
import tech.bashga.plata.screens.auth.PasscodeMode
import tech.bashga.plata.screens.auth.PasscodeState
import tech.bashga.plata.screens.auth.PasscodeStatus
import tech.bashga.plata.screens.auth.PhoneNumberContent
import tech.bashga.plata.screens.auth.PhoneNumberState
import tech.bashga.plata.screens.auth.PhoneOverlay
import tech.bashga.plata.screens.auth.ScanCardQrContent
import tech.bashga.plata.screens.auth.ScanCardQrState
import tech.bashga.plata.screens.auth.SplashContent

/** SCREEN-MAP §3.1: one test per reference id, named after it. */
class AuthStateScreenshots : ScreenshotTest() {
    @Test fun `e61c2bc5`() = screenState("e61c2bc5") { SplashContent() }

    @Test fun `b76b9dda`() = screenState("b76b9dda") {
        LockContent(LockState(length = 4), onAction = {})
    }

    // the last frames of the root replace after Log out, the page all but zoomed in
    @Test fun `f8f29bdc`() = screenState("f8f29bdc", clockMillis = 300, replacing = {}) {
        PhoneNumberContent(PhoneNumberState(phone = "5512345678"), onAction = {})
    }

    // composed a frame late: OverlayHost misses overlays registered during its own first composition (frozen Overlays.kt)
    @Test fun `a0a0ef2b`() = screenState("a0a0ef2b") {
        var composed by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { composed = true }
        if (composed) PhoneNumberContent(PhoneNumberState(phone = "5512345678", overlay = PhoneOverlay.Otp()), onAction = {}, focusCode = false)
    }

    @Test fun `408f4810`() = screenState("408f4810") { ScanCardQrContent(ScanCardQrState(torch = false), onAction = {}) }

    @Test fun `1bad6d9f`() = screenState("1bad6d9f") {
        PasscodeContent(PasscodeState(mode = PasscodeMode.Create, entered = "12345"), onAction = {})
    }

    @Test fun `efe5d661`() = screenState("efe5d661") {
        PasscodeContent(PasscodeState(mode = PasscodeMode.Confirm, entered = "", first = "123456"), onAction = {})
    }

    @Test fun `c126aa45`() = screenState("c126aa45") {
        PasscodeContent(PasscodeState(PasscodeMode.Confirm, entered = "123456", first = "123456", status = PasscodeStatus.Success), onAction = {})
    }

    @Test fun `8d47c829`() = screenState("8d47c829") { EnableFaceIdContent(onAction = {}) }
}
