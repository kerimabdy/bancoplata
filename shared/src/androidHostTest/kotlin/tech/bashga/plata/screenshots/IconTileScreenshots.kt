package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.IconTileGlyphPreview
import tech.bashga.plata.designsystem.components.IconTileLogoPreview

class IconTileScreenshots : ScreenshotTest() {
    @Test fun logo() = component("icon-tile/content=logo", background = { it.surfaceCard }) { IconTileLogoPreview() }

    @Test fun glyph() = component("icon-tile/content=glyph") { IconTileGlyphPreview() }
}
