package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class PasscodeDotState { Filled, Empty, Success, Error }

enum class PasscodeDotTone { Default, OnGradient }

@Composable
fun PasscodeDot(state: PasscodeDotState, modifier: Modifier = Modifier, tone: PasscodeDotTone = PasscodeDotTone.Default) {
    val colors = PlataTheme.colors
    val onGradient = tone == PasscodeDotTone.OnGradient
    val color = when (state) {
        PasscodeDotState.Filled -> if (onGradient) colors.onGradientInk else colors.accent
        PasscodeDotState.Empty -> if (onGradient) colors.onGradientInk.copy(alpha = 0.2f) else colors.controlOff
        PasscodeDotState.Success -> colors.success
        PasscodeDotState.Error -> colors.danger
    }
    Box(modifier.size(16.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(if (state == PasscodeDotState.Empty) 12.dp else 16.dp).background(color, CircleShape))
    }
}

@Preview
@Composable
internal fun PasscodeDotFilledPreview() = PlataTheme { PasscodeDot(PasscodeDotState.Filled) }

@Preview
@Composable
internal fun PasscodeDotEmptyPreview() = PlataTheme { PasscodeDot(PasscodeDotState.Empty) }

@Preview
@Composable
internal fun PasscodeDotSuccessPreview() = PlataTheme { PasscodeDot(PasscodeDotState.Success) }

@Preview
@Composable
internal fun PasscodeDotErrorPreview() = PlataTheme { PasscodeDot(PasscodeDotState.Error) }

@Preview
@Composable
internal fun PasscodeDotEmptyOnGradientPreview() = PlataTheme {
    PasscodeDot(PasscodeDotState.Empty, tone = PasscodeDotTone.OnGradient)
}
