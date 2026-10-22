package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** The glass label reads 17 on 09c22ffa (Figma and the inventory say 15); the white pill pads 8 (adeddd37, b68fc03a). */
enum class PillTone { Glass, White }

/** [labelBrush] null sets a white pill's label in text/primary; the b68fc03a cashback pill uses `labelStyle = headline`. */
@Composable
fun Pill(
    label: String,
    modifier: Modifier = Modifier,
    tone: PillTone = PillTone.Glass,
    icon: ImageVector? = null,
    labelBrush: Brush? = PlataTheme.gradients.brand,
    labelStyle: TextStyle? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val glass = tone == PillTone.Glass
    val style = labelStyle ?: if (glass) typography.body else typography.subheadStrong
    Row(
        modifier
            .height(if (glass) 30.dp else 28.dp)
            .background(if (glass) colors.glassMaterial else colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.full))
            .padding(horizontal = if (glass) PlataTheme.spacing.s12 else PlataTheme.spacing.s8),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, size = PlataTheme.sizes.iconXSmall, tint = if (glass) colors.textOnDark else colors.brandPrimary)
        when {
            glass -> PlataText(label, style, colors.textOnDark, maxLines = 1)
            labelBrush != null -> GradientText(label, style, labelBrush, maxLines = 1)
            else -> PlataText(label, style, colors.textPrimary, maxLines = 1)
        }
    }
}

@Preview
@Composable
internal fun PillGlassPreview() = PlataTheme { Pill("Active", tone = PillTone.Glass) }

@Preview
@Composable
internal fun PillWhitePreview() = PlataTheme { Pill("10%", tone = PillTone.White, labelStyle = PlataTheme.typography.headline) }

@Preview
@Composable
internal fun PillWhiteIconPreview() = PlataTheme {
    Pill("15 days left", tone = PillTone.White, icon = PlataIcons.Flame, labelBrush = null)
}
