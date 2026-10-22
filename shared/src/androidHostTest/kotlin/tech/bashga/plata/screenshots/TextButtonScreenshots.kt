package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.TextButtonAccentBodyPreview
import tech.bashga.plata.designsystem.components.TextButtonAccentSemiboldSubheadPreview
import tech.bashga.plata.designsystem.components.TextButtonOnDarkBodyPreview
import tech.bashga.plata.designsystem.components.TextButtonPrimaryBodyPreview
import tech.bashga.plata.designsystem.components.TextButtonPrimarySemiboldBodyPreview
import tech.bashga.plata.designsystem.components.TextButtonSecondarySubheadPreview

class TextButtonScreenshots : ScreenshotTest() {
    // specimens crop the label; the 44pt hit area extends above and below it
    @Test fun accentBody() = component("text-button/tone=accent,weight=regular,size=body", margin = 0.dp) {
        SpecimenFrame(52.dp, 26.dp, 4.dp, -9.dp) { TextButtonAccentBodyPreview() }
    }

    @Test fun accentSemiboldSubhead() = component("text-button/tone=accent,weight=semibold,size=subhead", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(62.dp, 26.dp, 10.5.dp, -9.dp) { TextButtonAccentSemiboldSubheadPreview() }
    }

    @Test fun primaryBody() = component("text-button/tone=primary,weight=regular,size=body", margin = 0.dp) {
        SpecimenFrame(202.dp, 30.dp, 4.dp, -7.dp) { TextButtonPrimaryBodyPreview() }
    }

    @Test fun primarySemiboldBody() = component("text-button/tone=primary,weight=semibold,size=body", margin = 0.dp) {
        SpecimenFrame(142.dp, 26.dp, 4.dp, -9.dp) { TextButtonPrimarySemiboldBodyPreview() }
    }

    @Test fun secondarySubhead() = component("text-button/tone=secondary,weight=regular,size=subhead", margin = 0.dp) {
        SpecimenFrame(96.dp, 28.dp, 4.dp, -8.dp) { TextButtonSecondarySubheadPreview() }
    }

    @Test fun onDarkBody() = component("text-button/tone=onDark,weight=regular,size=body", background = { it.surfaceSheetDark }, margin = 0.dp) {
        SpecimenFrame(52.dp, 26.dp, 4.dp, -9.dp) { TextButtonOnDarkBodyPreview() }
    }
}
