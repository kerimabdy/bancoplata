package tech.bashga.plata.dev

import androidx.compose.runtime.Composable
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
@Composable
actual fun isDebugBuild(): Boolean = Platform.isDebugBinary
