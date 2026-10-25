package tech.bashga.plata.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

actual val platformBackStyle: BackStyle = BackStyle.Ios

@Composable
internal actual fun rememberReduceMotion(): Boolean = remember { UIAccessibilityIsReduceMotionEnabled() }
