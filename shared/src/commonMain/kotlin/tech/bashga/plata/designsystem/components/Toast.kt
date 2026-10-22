package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class ToastTone { Success, Neutral }

/**
 * Success is a hugging green pill with one 17pt line; neutral is a grey card the caller sizes (377 wide on 73569385)
 * with up to two 13pt lines beside a 32 glyph.
 */
@Composable
fun Toast(
    message: String,
    modifier: Modifier = Modifier,
    tone: ToastTone = ToastTone.Success,
    icon: ImageVector = if (tone == ToastTone.Success) PlataIcons.Check else PlataIcons.Shield,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    when (tone) {
        ToastTone.Success -> Row(
            modifier
                .height(50.dp)
                .background(colors.success, ContinuousRoundedShape(PlataTheme.radii.full))
                // the check sits 18 in and the label 46 in on 5b9474e2
                .padding(PaddingValues(start = 18.dp, end = spacing.s20)),
            horizontalArrangement = Arrangement.spacedBy(spacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, size = PlataTheme.sizes.iconSmall, tint = colors.textOnDark)
            PlataText(message, PlataTheme.typography.body, colors.textOnDark, maxLines = 1)
        }
        ToastTone.Neutral -> Row(
            modifier
                // opaque: 73569385 reads #5C5D63 across the card, the 95% Figma fill comes out lighter over the page
                .background(colors.surfaceToast, ContinuousRoundedShape(PlataTheme.radii.xl))
                .padding(horizontal = spacing.s12, vertical = 12.5.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, size = 32.dp, tint = colors.textOnDark)
            PlataText(message, PlataTheme.typography.footnoteTight, colors.textOnDark, Modifier.weight(1f), maxLines = 2)
        }
    }
}

/**
 * Shows [message] as a [Toast] while it is non-null, fading in and out, and calls [onDismiss] [durationMillis] after it
 * appears (the FeedbackThanks and VestFiInfo toasts). Position it with [modifier]. A toast that is showing on the
 * first frame appears without animating, and a paused test clock keeps it on screen.
 */
@Composable
fun ToastHost(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ToastTone = ToastTone.Success,
    icon: ImageVector = if (tone == ToastTone.Success) PlataIcons.Check else PlataIcons.Shield,
    durationMillis: Long = 3_000,
) {
    val visible = remember { MutableTransitionState(message != null) }
    visible.targetState = message != null
    var shown by remember { mutableStateOf(message) }
    if (message != null) shown = message
    val dismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(message) {
        if (message != null) {
            delay(durationMillis)
            dismiss()
        }
    }
    AnimatedVisibility(visible, modifier, enter = fadeIn(), exit = fadeOut()) {
        shown?.let { Toast(it, tone = tone, icon = icon) }
    }
}

@Preview
@Composable
internal fun ToastHostPreview() = PlataTheme { ToastHost("Thanks for your feedback", onDismiss = {}) }

@Preview
@Composable
internal fun ToastSuccessPreview() = PlataTheme { Toast("Thanks for your feedback") }

@Preview
@Composable
internal fun ToastNeutralPreview() = PlataTheme {
    Toast(
        "Powered by VestFi, Asesor en Inversiones Independiente, S.A.P.I. de C.V.",
        Modifier.width(377.dp),
        tone = ToastTone.Neutral,
    )
}
