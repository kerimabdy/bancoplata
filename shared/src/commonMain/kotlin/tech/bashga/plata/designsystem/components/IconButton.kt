package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.ChevronLeftHeavy
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlusOutlineDuotone
import tech.bashga.plata.designsystem.icons.QuestionMark
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class IconButtonStyle { Glass, Grey, Subtle, White, Tinted }

enum class IconButtonSize { S20, S24, S32, S40, S44, S48 }

/** The grey S20 disc measures 22 on 84e28520 and e7edfe4c; S24 is the LessonCard close (5ab1e0c9); the tinted square on 537a4b5a is S48. */
val IconButtonSize.box: Dp
    @Composable get() = when (this) {
        IconButtonSize.S20 -> 22.dp
        IconButtonSize.S24 -> PlataTheme.sizes.iconMedium
        IconButtonSize.S32 -> PlataTheme.sizes.buttonXSmall
        IconButtonSize.S40 -> PlataTheme.sizes.buttonSmall
        IconButtonSize.S44 -> PlataTheme.sizes.nav
        IconButtonSize.S48 -> PlataTheme.sizes.buttonMedium
    }

val IconButtonSize.glyph: Dp
    @Composable get() = when (this) {
        IconButtonSize.S20 -> 12.dp
        IconButtonSize.S24 -> PlataTheme.sizes.iconXSmall
        IconButtonSize.S32 -> PlataTheme.sizes.iconSmall
        IconButtonSize.S40 -> PlataTheme.sizes.iconMedium
        IconButtonSize.S44, IconButtonSize.S48 -> 26.dp
    }

/** [onCard]: grey on a white sheet fills with surface/background, not surface/control (6f8f3145). */
@Composable
fun IconButton(
    glyph: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IconButtonStyle = IconButtonStyle.Grey,
    size: IconButtonSize = IconButtonSize.S32,
    onCard: Boolean = false,
    glyphSize: Dp = size.glyph,
    contentDescription: String? = null,
) {
    val colors = PlataTheme.colors
    val shape = if (style == IconButtonStyle.Tinted) ContinuousRoundedShape(PlataTheme.radii.md) else CircleShape
    val fill = when (style) {
        IconButtonStyle.Glass -> colors.glassLight
        IconButtonStyle.Grey -> if (onCard) colors.surfaceBackground else colors.surfaceControl
        IconButtonStyle.Subtle, IconButtonStyle.Tinted -> colors.surfaceBackground
        IconButtonStyle.White -> colors.surfaceCard
    }
    val gradient = style == IconButtonStyle.White || style == IconButtonStyle.Tinted
    Box(
        modifier
            .size(size.box)
            .background(fill, shape)
            .pressable(onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            glyph,
            size = glyphSize,
            tint = when (style) {
                IconButtonStyle.Glass -> colors.textOnDark
                IconButtonStyle.Grey -> colors.textSecondary
                else -> colors.textPrimary
            },
            brush = if (gradient) PlataTheme.gradients.glyph else null,
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
internal fun IconButtonGlass32Preview() = PlataTheme {
    IconButton(PlataIcons.ChevronLeftHeavy, {}, style = IconButtonStyle.Glass, size = IconButtonSize.S32)
}

@Preview
@Composable
internal fun IconButtonGrey32Preview() = PlataTheme {
    IconButton(PlataIcons.CloseX, {}, style = IconButtonStyle.Grey, size = IconButtonSize.S32, onCard = true, glyphSize = 17.dp)
}

@Preview
@Composable
internal fun IconButtonGrey20Preview() = PlataTheme {
    IconButton(PlataIcons.QuestionMark, {}, style = IconButtonStyle.Grey, size = IconButtonSize.S20)
}

@Preview
@Composable
internal fun IconButtonSubtle40Preview() = PlataTheme {
    IconButton(PlataIcons.Document, {}, style = IconButtonStyle.Subtle, size = IconButtonSize.S40)
}

@Preview
@Composable
internal fun IconButtonWhite40Preview() = PlataTheme {
    IconButton(PlataIcons.Pie, {}, style = IconButtonStyle.White, size = IconButtonSize.S40)
}

@Preview
@Composable
internal fun IconButtonTinted48Preview() = PlataTheme {
    IconButton(PlataIcons.PlusOutlineDuotone, {}, style = IconButtonStyle.Tinted, size = IconButtonSize.S48)
}
