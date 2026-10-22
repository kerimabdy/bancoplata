package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.ApplePayMark
import tech.bashga.plata.designsystem.icons.Mastercard
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class CardThumbKind { Physical, Digital, Add }

/** [number] stands in for the source's redacted card number; 9f1aa9fc shows "••••" on the digital card. */
@Composable
fun CardThumb(
    kind: CardThumbKind,
    modifier: Modifier = Modifier,
    number: String = "••••",
    onClick: (() -> Unit)? = null,
    badge: (@Composable BoxScope.() -> Unit)? = defaultBadge(kind),
) {
    val colors = PlataTheme.colors
    val shape = ContinuousRoundedShape(PlataTheme.radii.sm)
    val face = when (kind) {
        CardThumbKind.Physical -> PlataTheme.gradients.silver
        CardThumbKind.Digital -> PlataTheme.gradients.iridescent
        CardThumbKind.Add -> null
    }
    Box(
        modifier
            .size(76.dp, 52.dp)
            .plataShadow(PlataTheme.shadows.thumb, shape)
            .clip(shape)
            .then(if (face != null) Modifier.background(face) else Modifier.background(colors.surfaceBackground))
            .border(1.dp, colors.borderSubtle, shape)
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier),
    ) {
        if (kind == CardThumbKind.Add) {
            Icon(PlataIcons.Plus, Modifier.align(Alignment.Center), size = PlataTheme.sizes.iconSmall, tint = colors.textSecondary)
            return@Box
        }
        if (badge != null) badge()
        Row(
            Modifier.align(Alignment.BottomStart).padding(start = 7.dp, end = 7.dp, bottom = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlataText(number, PlataTheme.typography.caption, colors.textPrimary, Modifier.size(41.dp, 11.dp), maxLines = 1)
            Icon(PlataIcons.Mastercard, size = PlataTheme.sizes.iconMedium)
        }
    }
}

private fun defaultBadge(kind: CardThumbKind): (@Composable BoxScope.() -> Unit)? = when (kind) {
    CardThumbKind.Physical -> {
        { Icon(PlataIcons.ApplePayMark, Modifier.offset(7.dp, 5.3.dp), size = PlataTheme.sizes.iconMedium) }
    }
    CardThumbKind.Digital -> {
        { Icon(PlataIcons.Cloud, Modifier.align(Alignment.TopEnd).offset((-6.4).dp, 0.8.dp), size = PlataTheme.sizes.iconSmall, tint = PlataTheme.colors.textPrimary) }
    }
    CardThumbKind.Add -> null
}

@Preview
@Composable
internal fun CardThumbPhysicalPreview() = PlataTheme { CardThumb(CardThumbKind.Physical) }

@Preview
@Composable
internal fun CardThumbDigitalPreview() = PlataTheme { CardThumb(CardThumbKind.Digital) }

@Preview
@Composable
internal fun CardThumbAddPreview() = PlataTheme { CardThumb(CardThumbKind.Add) }
