package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_clip
import plata.shared.generated.resources.raster_logo_vanguard
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class ChipStyle { Icon, Logo, Text }

/** [White] on the grey page, [Grey] (surface/background) on white cards. */
enum class ChipTone { White, Grey }

/**
 * Capsule filter or option, 40 high, label 15 semibold. Text chips centre their label in whatever width the caller
 * gives them (the 170-wide time slots on 24f9ddbc); icon and logo chips hug. Selected fills accent with a white label.
 */
@Composable
fun Chip(
    label: String,
    modifier: Modifier = Modifier,
    style: ChipStyle = ChipStyle.Text,
    tone: ChipTone = ChipTone.White,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val fill = when {
        selected -> colors.accent
        tone == ChipTone.Grey -> colors.surfaceBackground
        else -> colors.surfaceCard
    }
    // icon chips pad 10 with a 7 gap (816019bf); logo chips inset the 32 logo by 4 all round (9e2d2593, 3d1d2b9b)
    val (padding, gap) = when (style) {
        ChipStyle.Icon -> PaddingValues(horizontal = 10.dp) to 7.dp
        ChipStyle.Logo -> PaddingValues(start = spacing.s4, end = spacing.s12) to spacing.s8
        ChipStyle.Text -> PaddingValues(horizontal = spacing.s16) to spacing.s8
    }
    Row(
        modifier
            .height(PlataTheme.sizes.buttonSmall)
            .background(fill, ContinuousRoundedShape(PlataTheme.radii.full))
            .then(if (onClick != null) Modifier.pressable(role = Role.Tab, onClick = onClick) else Modifier)
            .padding(padding),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        PlataText(label, PlataTheme.typography.subheadStrong, if (selected) colors.textOnDark else colors.textPrimary, maxLines = 1)
    }
}

@Composable
fun Chip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.White,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) = Chip(label, modifier, ChipStyle.Icon, tone, selected, onClick) {
    Icon(icon, size = PlataTheme.sizes.iconSmall, tint = if (selected) PlataTheme.colors.textOnDark else PlataTheme.colors.textPrimary)
}

@Composable
fun Chip(
    label: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.White,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) = Chip(label, modifier, ChipStyle.Logo, tone, selected, onClick) { BrandLogo(logo, BrandLogoSize.S32) }

@Preview
@Composable
internal fun ChipIconWhitePreview() = PlataTheme { Chip("Airline tickets", PlataIcons.Plane) }

@Preview
@Composable
internal fun ChipLogoWhitePreview() = PlataTheme { Chip("Clip", painterResource(Res.drawable.raster_logo_clip)) }

@Preview
@Composable
internal fun ChipLogoGreyPreview() = PlataTheme {
    Chip("VGK", painterResource(Res.drawable.raster_logo_vanguard), tone = ChipTone.Grey)
}

@Preview
@Composable
internal fun ChipTextWhitePreview() = PlataTheme { Chip("12 p.m. – 3 p.m.", Modifier.width(170.dp)) }

@Preview
@Composable
internal fun ChipTextSelectedPreview() = PlataTheme { Chip("9 a.m. – 12 p.m.", Modifier.width(170.dp), selected = true) }
