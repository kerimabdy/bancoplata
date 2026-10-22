package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.ImageTileMinimumPreview
import tech.bashga.plata.designsystem.components.ImageTilePercentPreview

class ImageTileScreenshots : ScreenshotTest() {
    @Test fun minimum() = component("image-tile/footer=minimum", background = { it.surfaceCard }) { ImageTileMinimumPreview() }

    @Test fun percent() = component("image-tile/footer=percent", background = { it.surfaceCard }) { ImageTilePercentPreview() }
}
