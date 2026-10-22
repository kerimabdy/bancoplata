package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.StockTilePreview

class StockTileScreenshots : ScreenshotTest() {
    @Test fun default() = component("stock-tile/default", background = { it.surfaceCard }) { StockTilePreview() }
}
