package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

// UIKit has no public API for the display's corner radius
@Composable
internal actual fun screenCornerRadius(): Dp? = null
