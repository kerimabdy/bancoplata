package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ButtonBlackLargePreview
import tech.bashga.plata.designsystem.components.ButtonDestructiveLargePreview
import tech.bashga.plata.designsystem.components.ButtonPrimaryLargeLoadingPreview
import tech.bashga.plata.designsystem.components.ButtonPrimaryLargePreview
import tech.bashga.plata.designsystem.components.ButtonPrimaryMediumPreview
import tech.bashga.plata.designsystem.components.ButtonPrimarySmallPreview
import tech.bashga.plata.designsystem.components.ButtonSecondaryLargePreview
import tech.bashga.plata.designsystem.components.ButtonSecondaryOnCardMediumPreview
import tech.bashga.plata.designsystem.components.ButtonSecondaryOnCardSmallPreview
import tech.bashga.plata.designsystem.components.ButtonSuccessMediumPreview
import tech.bashga.plata.designsystem.components.ButtonWhiteAccentXSmallPreview
import tech.bashga.plata.designsystem.components.ButtonWhiteLargePreview

class ButtonScreenshots : ScreenshotTest() {
    @Test fun primaryLarge() = component("button/style=primary,size=large,state=default") { ButtonPrimaryLargePreview() }

    // the spinner turns forever, so the clock is paused at a fixed frame instead of idling
    @Test fun primaryLargeLoading() = component("button/style=primary,size=large,state=loading", clockMillis = 0) { ButtonPrimaryLargeLoadingPreview() }

    @Test fun primaryMedium() = component("button/style=primary,size=medium,state=default", background = { it.surfaceCard }) {
        ButtonPrimaryMediumPreview()
    }

    @Test fun primarySmall() = component("button/style=primary,size=small,state=default", background = { it.surfaceCard }) {
        ButtonPrimarySmallPreview()
    }

    @Test fun secondaryLarge() = component("button/style=secondary,size=large,state=default") { ButtonSecondaryLargePreview() }

    @Test fun secondaryOnCardMedium() = component("button/style=secondaryOnCard,size=medium,state=default", background = { it.surfaceCard }) {
        ButtonSecondaryOnCardMediumPreview()
    }

    @Test fun secondaryOnCardSmall() = component("button/style=secondaryOnCard,size=small,state=default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(100.dp, 48.dp, 4.3.dp, 4.3.dp) { ButtonSecondaryOnCardSmallPreview() }
    }

    // over the purple-to-magenta promo backdrop of 083f8017
    @Test fun whiteLarge() = component("button/style=white,size=large,state=default", background = { Color(0xFF9E3470) }) {
        ButtonWhiteLargePreview()
    }

    @Test fun whiteAccentXSmall() = component("button/style=whiteAccent,size=xsmall,state=default", margin = 0.dp) {
        SpecimenFrame(112.7.dp, 42.dp, 4.7.dp, 5.7.dp) { ButtonWhiteAccentXSmallPreview() }
    }

    @Test fun blackLarge() = component("button/style=black,size=large,state=default") { ButtonBlackLargePreview() }

    @Test fun successMedium() = component("button/style=success,size=medium,state=default", background = { it.surfaceCard }) {
        ButtonSuccessMediumPreview()
    }

    @Test fun destructiveLarge() = component("button/style=destructive,size=large,state=default") { ButtonDestructiveLargePreview() }
}
