package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class InlineNoticeTone { Warning, Info }

/**
 * Warning: danger-tint banner, red 24 glyph, 15pt ink text, optional chevron (1d655cd9).
 * Info: accent-tint box, radius 14, 15/18 accent text, no icon (d4c491d6). [onClick] makes the notice tappable.
 */
@Composable
fun InlineNotice(
    message: String,
    modifier: Modifier = Modifier,
    tone: InlineNoticeTone = InlineNoticeTone.Warning,
    icon: ImageVector? = if (tone == InlineNoticeTone.Warning) PlataIcons.Warning else null,
    showChevron: Boolean = tone == InlineNoticeTone.Warning,
    onClick: (() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val warning = tone == InlineNoticeTone.Warning
    Row(
        modifier
            .defaultMinSize(minHeight = PlataTheme.sizes.rowSingle)
            .background(
                if (warning) colors.dangerTint else colors.accentTint,
                ContinuousRoundedShape(if (warning) PlataTheme.radii.lg else 14.dp),
            )
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .padding(start = spacing.s20, end = if (showChevron) spacing.s16 else spacing.s20)
            .padding(vertical = if (warning) spacing.s16 else 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, size = PlataTheme.sizes.iconMedium, tint = if (warning) colors.danger else colors.accent)
            Spacer(Modifier.width(spacing.s16))
        }
        PlataText(
            message,
            if (warning) PlataTheme.typography.subhead else PlataTheme.typography.subheadTight,
            if (warning) colors.textPrimary else colors.accent,
            Modifier.weight(1f),
        )
        if (showChevron) Chevron(ChevronDirection.Right)
    }
}

@Preview
@Composable
internal fun InlineNoticeWarningPreview() = PlataTheme {
    InlineNotice("Check card package upon delivery", Modifier.fillMaxWidth(), onClick = {})
}

@Preview
@Composable
internal fun InlineNoticeInfoPreview() = PlataTheme {
    InlineNotice("To update your details send us\na message in chat", Modifier.fillMaxWidth(), tone = InlineNoticeTone.Info)
}
