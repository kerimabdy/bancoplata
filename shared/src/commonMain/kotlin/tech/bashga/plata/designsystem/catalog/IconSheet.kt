package tech.bashga.plata.designsystem.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.brushTint
import tech.bashga.plata.designsystem.icons.ChevronBack
import tech.bashga.plata.designsystem.icons.ChevronDisclosure
import tech.bashga.plata.designsystem.icons.NotificationDot
import tech.bashga.plata.designsystem.icons.NotificationOutline
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.isMulticolour
import tech.bashga.plata.designsystem.icons.moleculeGlyphs
import tech.bashga.plata.designsystem.icons.organismGlyphs
import tech.bashga.plata.designsystem.icons.sourceGlyphs
import tech.bashga.plata.designsystem.theme.PlataTheme

@Composable
internal fun Glyph(vector: ImageVector, tint: Color, size: Dp, brush: Brush? = null) {
    val sized = Modifier.size(size * vector.defaultWidth.value / 24f, size * vector.defaultHeight.value / 24f)
    Image(
        painter = rememberVectorPainter(vector),
        contentDescription = null,
        modifier = if (brush != null && !vector.isMulticolour) sized.brushTint(brush) else sized,
        colorFilter = if (vector.isMulticolour || brush != null) null else ColorFilter.tint(tint),
    )
}

@Composable
fun IconSheet() {
    val colors = PlataTheme.colors
    val glyphs = PlataIcons.figmaGlyphs +
        listOf(
            "chevron-disclosure" to PlataIcons.ChevronDisclosure,
            "chevron-back" to PlataIcons.ChevronBack,
            "notification-outline" to PlataIcons.NotificationOutline,
            "notification-dot" to PlataIcons.NotificationDot,
        ) + PlataIcons.sourceGlyphs + PlataIcons.moleculeGlyphs + PlataIcons.organismGlyphs
    Column(
        Modifier
            .width(393.dp)
            .background(colors.surfaceBackground)
            .padding(horizontal = PlataTheme.spacing.s8, vertical = PlataTheme.spacing.s16),
    ) {
        glyphs.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { (name, vector) ->
                    Column(
                        Modifier.weight(1f).height(60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            Glyph(vector, colors.textPrimary, PlataTheme.sizes.iconMedium)
                        }
                        PlataText(name, PlataTheme.typography.caption, colors.textSecondary, textAlign = TextAlign.Center)
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(PlataTheme.spacing.s16))
        PlataText("size 20 / 24 / 28 · textPrimary, textSecondary, accent, brandPrimary", PlataTheme.typography.caption, colors.textSecondary)
        Spacer(Modifier.height(PlataTheme.spacing.s8))
        Spacer(Modifier.height(PlataTheme.spacing.s16))
        PlataText(
            "brushTint · glyph: link, document, square, msiBars · brand: percent · notification · outline + dot",
            PlataTheme.typography.caption,
            colors.textSecondary,
        )
        Spacer(Modifier.height(PlataTheme.spacing.s8))
        Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12)) {
            val medium = PlataTheme.sizes.iconMedium
            val gradients = PlataTheme.gradients
            listOf(PlataIcons.Link, PlataIcons.Document, PlataIcons.Square, PlataIcons.MsiBars).forEach {
                Glyph(it, colors.textPrimary, medium, gradients.glyph)
            }
            Glyph(PlataIcons.Percent, colors.textPrimary, medium, gradients.brand)
            Glyph(PlataIcons.Notification, colors.textPrimary, medium, gradients.notification)
            Box {
                Glyph(PlataIcons.NotificationOutline, colors.textPrimary, medium)
                Glyph(PlataIcons.NotificationDot, colors.textPrimary, medium)
            }
            Box {
                Glyph(PlataIcons.NotificationOutline, colors.accent, medium)
                Glyph(PlataIcons.NotificationDot, colors.textPrimary, medium)
            }
        }
        Spacer(Modifier.height(PlataTheme.spacing.s16))
        listOf(colors.textPrimary, colors.textSecondary, colors.accent, colors.brandPrimary).forEach { tint ->
            Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12), verticalAlignment = Alignment.CenterVertically) {
                listOf(PlataTheme.sizes.iconSmall, PlataTheme.sizes.iconMedium, PlataTheme.sizes.iconLarge).forEach { size ->
                    Glyph(PlataIcons.House, tint, size)
                    Glyph(PlataIcons.Bell, tint, size)
                    Glyph(PlataIcons.ClearX, tint, size)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun IconSheetPreview() = PlataTheme { IconSheet() }
