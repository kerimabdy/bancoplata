package tech.bashga.plata.navigation

import androidx.compose.runtime.Composable

// the desktop window previews the iPhone app
actual val platformBackStyle: BackStyle = BackStyle.Ios

@Composable
internal actual fun rememberReduceMotion(): Boolean = false
