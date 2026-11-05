package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.isDeviceFramed

// inside the device frame the app keeps its designed device radius; filling a phone browser, it meets the browser chrome square
@Composable
internal actual fun screenCornerRadius(): Dp? = if (isDeviceFramed.value) null else 0.dp
