package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class PasscodeDotsState { Entry, Success, Error }

private const val ShakeMillis = 400
private val ShakeOffsets = listOf(-12f, 10f, -8f, 6f, -3f)

/**
 * A row of [count] passcode dots on a 36 pitch: 196 wide for six (1bad6d9f), 124 for four (b76b9dda).
 * [filled] dots show while entering; [PasscodeDotsState.Success] turns every dot green (c126aa45);
 * [PasscodeDotsState.Error] turns them red and shakes the row once (a mismatch or a wrong passcode).
 */
@Composable
fun PasscodeDots(
    filled: Int,
    modifier: Modifier = Modifier,
    count: Int = 6,
    state: PasscodeDotsState = PasscodeDotsState.Entry,
    tone: PasscodeDotTone = PasscodeDotTone.Default,
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == PasscodeDotsState.Error) {
            shake.animateTo(0f, keyframes {
                durationMillis = ShakeMillis
                ShakeOffsets.forEachIndexed { i, x -> x at ShakeMillis * (i + 1) / (ShakeOffsets.size + 1) }
            })
        }
    }
    Row(
        modifier.offset { IntOffset(shake.value.dp.roundToPx(), 0) },
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s20),
    ) {
        repeat(count) { index ->
            val dot = when {
                state == PasscodeDotsState.Success -> PasscodeDotState.Success
                state == PasscodeDotsState.Error -> PasscodeDotState.Error
                index < filled -> PasscodeDotState.Filled
                else -> PasscodeDotState.Empty
            }
            PasscodeDot(dot, tone = tone)
        }
    }
}

@Preview
@Composable
internal fun PasscodeDotsEntryPreview() = PlataTheme { PasscodeDots(filled = 5) }

@Preview
@Composable
internal fun PasscodeDotsSuccessPreview() = PlataTheme { PasscodeDots(filled = 6, state = PasscodeDotsState.Success) }

@Preview
@Composable
internal fun PasscodeDotsErrorPreview() = PlataTheme { PasscodeDots(filled = 0, state = PasscodeDotsState.Error) }

@Preview
@Composable
internal fun PasscodeDotsOnGradientPreview() = PlataTheme {
    PasscodeDots(filled = 0, count = 4, tone = PasscodeDotTone.OnGradient)
}
