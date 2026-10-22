package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import tech.bashga.plata.designsystem.icons.ChevronBack
import tech.bashga.plata.designsystem.icons.ChevronDisclosure
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class ChevronDirection { Right, Left }

@Composable
fun Chevron(
    direction: ChevronDirection,
    modifier: Modifier = Modifier,
    tint: Color = when (direction) {
        // the source disclosure ink peaks at #60–#71, darker than text/tertiary
        ChevronDirection.Right -> PlataTheme.colors.textSecondary
        ChevronDirection.Left -> PlataTheme.colors.accent
    },
) {
    val glyph = when (direction) {
        ChevronDirection.Right -> PlataIcons.ChevronDisclosure
        ChevronDirection.Left -> PlataIcons.ChevronBack
    }
    Icon(glyph, modifier, size = PlataTheme.sizes.iconMedium, tint = tint)
}

@Preview
@Composable
internal fun ChevronRightPreview() = PlataTheme { Chevron(ChevronDirection.Right) }

@Preview
@Composable
internal fun ChevronLeftPreview() = PlataTheme { Chevron(ChevronDirection.Left) }
