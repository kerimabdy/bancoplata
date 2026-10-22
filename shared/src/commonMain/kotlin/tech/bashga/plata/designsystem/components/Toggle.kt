package tech.bashga.plata.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.theme.PlataTheme

private val TrackWidth = 51.dp
private val TrackHeight = 31.dp
private val KnobSize = 27.dp
private val KnobInset = 2.dp

/** Only state=off appears in the source; on uses the success green like UISwitch. */
@Composable
fun Toggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = PlataTheme.colors
    val knobX by animateDpAsState(if (checked) TrackWidth - KnobSize - KnobInset * 2 else 0.dp)
    Box(
        modifier
            .size(TrackWidth, TrackHeight)
            .background(if (checked) colors.success else colors.controlTrackOff, CircleShape)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                } else {
                    Modifier
                },
            )
            .padding(KnobInset),
    ) {
        Box(
            Modifier
                .offset(x = knobX)
                .size(KnobSize)
                .plataShadow(PlataTheme.shadows.switchKnob, CircleShape)
                .background(colors.surfaceCard, CircleShape),
        )
    }
}

@Preview
@Composable
internal fun ToggleOffPreview() = PlataTheme { Toggle(checked = false, onCheckedChange = {}) }

@Preview
@Composable
internal fun ToggleOnPreview() = PlataTheme { Toggle(checked = true, onCheckedChange = {}) }
