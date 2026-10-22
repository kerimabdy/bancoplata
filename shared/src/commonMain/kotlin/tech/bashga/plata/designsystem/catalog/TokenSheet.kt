package tech.bashga.plata.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.theme.PlataColors
import tech.bashga.plata.designsystem.theme.PlataGradients
import tech.bashga.plata.designsystem.theme.PlataShadow
import tech.bashga.plata.designsystem.theme.PlataTheme

private fun PlataColors.named(): List<Pair<String, Color>> = listOf(
    "brandPrimary" to brandPrimary, "accent" to accent, "accentTint" to accentTint, "accentTrack" to accentTrack,
    "progressTrackFull" to progressTrackFull, "glyphTint" to glyphTint,
    "walletPocket" to walletPocket, "walletBlue" to walletBlue, "walletYellow" to walletYellow,
    "walletGreen" to walletGreen, "walletCoral" to walletCoral,
    "textPrimary" to textPrimary, "textPrimaryTitle" to textPrimaryTitle, "textSecondary" to textSecondary,
    "textTertiary" to textTertiary, "textOnDark" to textOnDark, "textSuccess" to textSuccess,
    "textNegative" to textNegative, "textDestructive" to textDestructive,
    "surfaceBackground" to surfaceBackground, "surfaceCard" to surfaceCard, "surfaceInset" to surfaceInset,
    "surfaceField" to surfaceField, "surfaceSearch" to surfaceSearch, "surfaceSecondaryButton" to surfaceSecondaryButton,
    "surfaceDark" to surfaceDark, "surfaceDarkCard" to surfaceDarkCard, "surfaceBlack" to surfaceBlack,
    "surfaceSheetDark" to surfaceSheetDark, "surfaceToast" to surfaceToast, "surfaceTabbar" to surfaceTabbar, "surfaceNavMaterial" to surfaceNavMaterial,
    "surfaceControl" to surfaceControl, "surfaceLogo" to surfaceLogo, "glassLight" to glassLight, "glassDark" to glassDark, "glassMaterial" to glassMaterial,
    "borderSubtle" to borderSubtle,
    "scrim" to scrim,
    "success" to success, "successTint" to successTint, "danger" to danger, "dangerSystem" to dangerSystem, "dangerTint" to dangerTint,
    "bankRed" to bankRed, "controlOff" to controlOff, "controlTrackOff" to controlTrackOff, "onGradientInk" to onGradientInk,
    "keypadOnGradientInk" to keypadOnGradientInk, "keypadSystem" to keypadSystem,
    "cardEmbossViolet" to cardEmbossViolet, "cardEmbossTeal" to cardEmbossTeal, "cardEmbossSilver" to cardEmbossSilver,
    "orbBlue" to orbBlue, "orbViolet" to orbViolet, "orbRed" to orbRed,
    "dialStart" to dialStart, "dialQuarter" to dialQuarter, "dialMid" to dialMid, "dialEnd" to dialEnd, "dialTick" to dialTick,
    "surfaceSheetStacked" to surfaceSheetStacked, "sheetDim" to sheetDim, "sheetBackdrop" to sheetBackdrop, "cardLabelTeal" to cardLabelTeal,
)

private fun PlataGradients.named(): List<Pair<String, Brush>> = listOf(
    "brand" to brand, "promo" to promo, "progress" to progress, "ai" to ai, "glyph" to glyph,
    "aiBadge" to aiBadge, "progressTrackTinted" to progressTrackTinted,
    "avatarPlaceholder" to avatarPlaceholder, "notification" to notification, "msi" to msi, "violet" to violet,
    "iridescent" to iridescent, "silver" to silver,
    "categoryOrange" to categoryOrange, "categorySky" to categorySky, "categoryPink" to categoryPink, "categoryLilac" to categoryLilac, "categoryPlum" to categoryPlum, "categoryBlue" to categoryBlue, "categoryMagenta" to categoryMagenta, "categoryTeal" to categoryTeal, "categoryYellow" to categoryYellow, "appIcon" to appIcon,
    "plataPlus" to plataPlus,
)

private fun Color.hex(): String {
    val argb = toArgb().toLong() and 0xFFFFFFFFL
    val rgb = (argb and 0xFFFFFFL).toString(16).uppercase().padStart(6, '0')
    val alpha = (argb shr 24).toInt()
    return if (alpha == 0xFF) "#$rgb" else "#$rgb @${(alpha * 100 + 127) / 255}%"
}

@Composable
private fun SheetTitle(text: String) {
    PlataText(
        text,
        PlataTheme.typography.title3,
        PlataTheme.colors.textPrimary,
        Modifier.padding(top = PlataTheme.spacing.s24, bottom = PlataTheme.spacing.s8),
    )
}

@Composable
private fun SheetColumn(content: @Composable () -> Unit) {
    Column(
        Modifier
            .width(393.dp)
            .background(PlataTheme.colors.surfaceBackground)
            .padding(horizontal = PlataTheme.spacing.screenMargin)
            .padding(bottom = PlataTheme.spacing.s24),
    ) { content() }
}

@Composable
fun ColorTokenSheet() {
    val colors = PlataTheme.colors
    SheetColumn {
        SheetTitle("Colors")
        colors.named().chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth().padding(vertical = PlataTheme.spacing.s4)) {
                pair.forEach { (name, color) ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(ContinuousRoundedShape(PlataTheme.radii.xs))
                                .background(color)
                                .border(0.5.dp, colors.controlOff, ContinuousRoundedShape(PlataTheme.radii.xs)),
                        )
                        Spacer(Modifier.width(PlataTheme.spacing.s8))
                        Column {
                            PlataText(name, PlataTheme.typography.caption, colors.textPrimary)
                            PlataText(color.hex(), PlataTheme.typography.caption, colors.textSecondary)
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        SheetTitle("Gradients")
        PlataTheme.gradients.named().forEach { (name, brush) ->
            Row(Modifier.padding(vertical = PlataTheme.spacing.s4), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(160.dp, 28.dp).clip(ContinuousRoundedShape(PlataTheme.radii.xs)).background(brush))
                Spacer(Modifier.width(PlataTheme.spacing.s12))
                PlataText(name, PlataTheme.typography.footnote, colors.textPrimary)
            }
        }
        GradientText("Cashback", PlataTheme.typography.title3, PlataTheme.gradients.brand)
        GradientText("Portfolio analysis", PlataTheme.typography.title3, PlataTheme.gradients.ai)
    }
}

private fun TextStyle.describe(): String {
    val weight = when (fontWeight) {
        FontWeight.Normal -> "regular"
        FontWeight.Medium -> "medium"
        FontWeight.SemiBold -> "semibold"
        FontWeight.Bold -> "bold"
        FontWeight.ExtraBold -> "heavy"
        else -> fontWeight.toString()
    }
    return "${fontSize.value.toInt()}/${lineHeight.value.toInt()} $weight ${letterSpacing.value}"
}

@Composable
fun TypeTokenSheet() {
    val colors = PlataTheme.colors
    SheetColumn {
        SheetTitle("Type")
        PlataTheme.typography.all.forEach { (name, style) ->
            Column(Modifier.padding(vertical = PlataTheme.spacing.s4)) {
                PlataText("$name · ${style.describe()}", PlataTheme.typography.caption, colors.textSecondary)
                PlataText(
                    if (name == "display") "PLATA+ IS YOURS" else "Transactions $3,457.97",
                    style,
                    colors.textPrimary,
                    // shows the line box so line heights can be checked against foundations.md
                    Modifier.background(colors.accentTint),
                )
            }
        }
        SheetTitle("Wrapped body (bodyRelaxed vs body)")
        PlataText(
            "Check card package upon delivery. Services offered by VestFi.",
            PlataTheme.typography.bodyRelaxed,
            colors.textPrimary,
            Modifier.background(colors.accentTint),
        )
        Spacer(Modifier.height(PlataTheme.spacing.s8))
        PlataText(
            "Check card package upon delivery. Services offered by VestFi.",
            PlataTheme.typography.body,
            colors.textPrimary,
            Modifier.background(colors.accentTint),
        )
    }
}

@Composable
private fun RadiusSample(name: String, radius: Dp, width: Dp, height: Dp = width) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width, height)
                .clip(ContinuousRoundedShape(radius))
                .background(PlataTheme.colors.surfaceCard),
        )
        PlataText("$name ${radius.value.toInt()}", PlataTheme.typography.caption, PlataTheme.colors.textSecondary)
    }
}

@Composable
private fun ShadowSample(name: String, shadow: PlataShadow) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val shape = ContinuousRoundedShape(PlataTheme.radii.full)
        Box(
            Modifier
                .padding(PlataTheme.spacing.s12)
                .size(56.dp)
                .plataShadow(shadow, shape)
                .background(PlataTheme.colors.surfaceCard, shape),
        )
        PlataText(name, PlataTheme.typography.caption, PlataTheme.colors.textSecondary)
    }
}

@Composable
private fun TokenBar(name: String, value: Dp) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(value, 12.dp).background(PlataTheme.colors.brandPrimary))
        Spacer(Modifier.width(PlataTheme.spacing.s8))
        PlataText("$name ${value.value.toInt()}", PlataTheme.typography.caption, PlataTheme.colors.textSecondary)
    }
}

@Composable
fun DimensionTokenSheet() {
    val radii = PlataTheme.radii
    val spacing = PlataTheme.spacing
    val sizes = PlataTheme.sizes
    SheetColumn {
        SheetTitle("Radii (continuous)")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("xs" to radii.xs, "sm" to radii.sm, "md" to radii.md, "lg" to radii.lg, "xl" to radii.xl, "full" to radii.full)
                .forEach { (name, radius) -> RadiusSample(name, radius, 48.dp) }
        }
        Spacer(Modifier.height(spacing.s12))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.s12)) {
            RadiusSample("xxl", radii.xxl, 160.dp, 96.dp)
            RadiusSample("sheet", radii.sheet, 160.dp, 96.dp)
        }
        SheetTitle("Spacing")
        listOf(
            "s4" to spacing.s4, "s8" to spacing.s8, "s12" to spacing.s12, "s16" to spacing.s16, "s20" to spacing.s20,
            "s24" to spacing.s24, "s32" to spacing.s32, "s40" to spacing.s40, "screenMargin" to spacing.screenMargin,
            "searchMargin" to spacing.searchMargin, "cardPadding" to spacing.cardPadding, "cardGap" to spacing.cardGap,
            "rowGap" to spacing.rowGap, "titleSubtitle" to spacing.titleSubtitle, "sectionGap" to spacing.sectionGap,
        ).forEach { (name, value) -> TokenBar(name, value) }
        SheetTitle("Sizes")
        listOf(
            "rowSingle" to sizes.rowSingle, "rowAsset" to sizes.rowAsset, "buttonLarge" to sizes.buttonLarge,
            "buttonMedium" to sizes.buttonMedium, "buttonSmall" to sizes.buttonSmall, "buttonXSmall" to sizes.buttonXSmall,
            "search" to sizes.search, "nav" to sizes.nav, "navSheet" to sizes.navSheet, "tabbar" to sizes.tabbar,
            "iconXSmall" to sizes.iconXSmall, "iconSmall" to sizes.iconSmall, "iconMedium" to sizes.iconMedium, "iconLarge" to sizes.iconLarge,
        ).forEach { (name, value) -> TokenBar(name, value) }
        SheetTitle("Shadows")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            ShadowSample("orb", PlataTheme.shadows.orb)
            ShadowSample("thumb", PlataTheme.shadows.thumb)
            ShadowSample("knob", PlataTheme.shadows.knob)
            ShadowSample("switchKnob", PlataTheme.shadows.switchKnob)
        }
    }
}

@Preview
@Composable
internal fun ColorTokenSheetPreview() = PlataTheme { ColorTokenSheet() }

@Preview
@Composable
internal fun TypeTokenSheetPreview() = PlataTheme { TypeTokenSheet() }

@Preview
@Composable
internal fun DimensionTokenSheetPreview() = PlataTheme { DimensionTokenSheet() }
