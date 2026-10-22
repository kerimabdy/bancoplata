package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.navigation.PlaceholderScreen
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.StaticLayer
import tech.bashga.plata.navigation.StaticLayers
import tech.bashga.plata.designsystem.components.PageSheetDimmedPreview
import tech.bashga.plata.designsystem.components.PageSheetStackedPreview

class PageSheetScreenshots : ScreenshotTest() {
    // the top 120pt of the screen, compared with crop.sh 73a046d0 0 0 393 120
    @Test fun stacked() = component("page-sheet/stacked", margin = 0.dp) {
        SpecimenFrame(393.dp, 120.dp, 0.dp, 0.dp) { PageSheetStackedPreview() }
    }

    // compared with crop.sh 3b3eaed7 0 0 393 120
    @Test fun dimmed() = component("page-sheet/dimmed", margin = 0.dp) {
        SpecimenFrame(393.dp, 120.dp, 0.dp, 0.dp) { PageSheetDimmedPreview() }
    }

    // §4: the nav host's presentation of a live presenter (a placeholder page) under a Stacked and a Dimmed sheet
    @Test fun liveStacked() = screen("page-sheet-live-stacked") {
        StaticLayers(listOf(StaticLayer(Presentation.Push) { PlaceholderScreen("Presenter", "base") }, StaticLayer(Presentation.Sheet) {}))
    }

    @Test fun liveDimmed() = screen("page-sheet-live-dimmed") {
        StaticLayers(
            listOf(
                StaticLayer(Presentation.Cover) { PlaceholderScreen("Presenter", "cover") },
                StaticLayer(Presentation.Sheet) {},
            ),
        )
    }

    // a sheet over a sheet: the lower sheet becomes the card, the base is hidden (eb4cdf05's light edge)
    @Test fun liveSheetOverSheet() = screen("page-sheet-live-sheet-over-sheet") {
        StaticLayers(
            listOf(
                StaticLayer(Presentation.Push) { PlaceholderScreen("Base", "base") },
                StaticLayer(Presentation.Sheet) { PlaceholderScreen("Lower sheet", "sheet") },
                StaticLayer(Presentation.Sheet) {},
            ),
        )
    }
}
