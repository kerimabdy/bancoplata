package tech.bashga.plata.dev

import androidx.compose.runtime.Composable

// the desktop build exists only to develop against
@Composable
actual fun isDebugBuild(): Boolean = true
