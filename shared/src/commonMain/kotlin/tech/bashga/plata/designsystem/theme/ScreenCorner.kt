package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** The radius of the display's own corners where the platform reports it; null leaves [PlataRadii.device] as designed. */
@Composable
internal expect fun screenCornerRadius(): Dp?
