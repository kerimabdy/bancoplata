package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.LiveChartEmptyPreview
import tech.bashga.plata.designsystem.components.LiveChartFixedPreview
import tech.bashga.plata.designsystem.components.LiveChartLivePreview
import tech.bashga.plata.designsystem.components.LiveChartLoadingPreview

class LiveChartScreenshots : ScreenshotTest() {
    @Test fun live() = component("live-chart/state=live") { LiveChartLivePreview() }

    @Test fun fixed() = component("live-chart/state=fixed") { LiveChartFixedPreview() }

    // the loading line breathes forever, so the clock is paused at a fixed frame instead of idling
    @Test fun loading() = component("live-chart/state=loading", clockMillis = 0) { LiveChartLoadingPreview() }

    @Test fun empty() = component("live-chart/state=empty") { LiveChartEmptyPreview() }
}
