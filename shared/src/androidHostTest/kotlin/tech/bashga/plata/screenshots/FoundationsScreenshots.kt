package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import org.robolectric.annotation.Config
import tech.bashga.plata.designsystem.catalog.Glyph
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.designsystem.catalog.ColorTokenSheetPreview
import tech.bashga.plata.designsystem.catalog.DimensionTokenSheetPreview
import tech.bashga.plata.designsystem.catalog.IconSheetPreview
import tech.bashga.plata.designsystem.catalog.TypeTokenSheetPreview

// the sheets are taller than a phone screen; component() captures only what fits in the window
@Config(sdk = [36], qualifiers = "w393dp-h2600dp-xxhdpi")
class FoundationsScreenshots : ScreenshotTest() {
    @Test fun colors() = component("foundations/colors", margin = 0.dp) { ColorTokenSheetPreview() }

    @Test fun type() = component("foundations/type", margin = 0.dp) { TypeTokenSheetPreview() }

    @Test fun dimensions() = component("foundations/dimensions", margin = 0.dp) { DimensionTokenSheetPreview() }

    @Test fun icons() = component("foundations/icons", margin = 0.dp) { IconSheetPreview() }

    // glyph fidelity against the Icon specimens; the Icon component's own tests use the icon/ names
    @Test fun glyphPercent20() = component("foundations/glyph-percent-20", background = { it.surfaceCard }, margin = 3.dp) {
        Glyph(PlataIcons.Percent, PlataTheme.colors.textPrimary, PlataTheme.sizes.iconSmall)
    }

    @Test fun glyphAppearance24() = component("foundations/glyph-appearance-24", background = { it.surfaceCard }, margin = 2.dp) {
        Glyph(PlataIcons.Appearance, PlataTheme.colors.textPrimary, PlataTheme.sizes.iconMedium)
    }

    @Test fun glyphDoubleChevron28() = component("foundations/glyph-double-chevron-28", margin = 3.dp) {
        Glyph(PlataIcons.DoubleChevron, PlataTheme.colors.textSecondary, PlataTheme.sizes.iconLarge)
    }

    @Test fun glyphNotification24() = component("foundations/glyph-notification-24", background = { it.surfaceCard }, margin = 2.dp) {
        Glyph(PlataIcons.Notification, PlataTheme.colors.textPrimary, PlataTheme.sizes.iconMedium, PlataTheme.gradients.notification)
    }

    @Test fun gradientAiTitle() = component("foundations/gradient-ai-title", background = { it.surfaceCard }, margin = 2.dp) {
        GradientText("Portfolio analysis", PlataTheme.typography.title3, PlataTheme.gradients.ai)
    }
}
