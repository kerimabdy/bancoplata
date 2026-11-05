package tech.bashga.plata.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

// the web build previews the iPhone app
actual val platformBackStyle: BackStyle = BackStyle.Ios

@Composable
internal actual fun rememberReduceMotion(): Boolean =
    remember { window.matchMedia("(prefers-reduced-motion: reduce)").matches }
