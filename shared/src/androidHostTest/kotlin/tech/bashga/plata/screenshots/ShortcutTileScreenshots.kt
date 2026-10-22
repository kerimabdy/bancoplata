package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ShortcutTileChoosePreview
import tech.bashga.plata.designsystem.components.ShortcutTileCompactPreview
import tech.bashga.plata.designsystem.components.ShortcutTileMsiPreview
import tech.bashga.plata.designsystem.components.ShortcutTileRegularPreview
import tech.bashga.plata.designsystem.components.ShortcutTileTallPreview

class ShortcutTileScreenshots : ScreenshotTest() {
    @Test fun regular() = component("shortcut-tile/size=regular", margin = 0.dp) {
        SpecimenFrame(174.dp, 88.dp, 4.3.dp, 4.dp) { ShortcutTileRegularPreview() }
    }

    @Test fun tall() = component("shortcut-tile/size=tall", margin = 0.dp) {
        SpecimenFrame(178.dp, 164.dp, 4.3.dp, 4.dp) { ShortcutTileTallPreview() }
    }

    // the screen variants, framed like source crops 9f1aa9fc @ 200,312,178,88 / 16,312,178,88 and 894b1b01 @ 198,226,178,80
    @Test fun msi() = component("shortcut-tile/msi", margin = 0.dp) {
        SpecimenFrame(178.dp, 88.dp, 5.dp, 4.dp) { ShortcutTileMsiPreview() }
    }

    @Test fun choose() = component("shortcut-tile/choose", margin = 0.dp) {
        SpecimenFrame(178.dp, 88.dp, 4.3.dp, 4.dp) { ShortcutTileChoosePreview() }
    }

    @Test fun compact() = component("shortcut-tile/compact", margin = 0.dp) {
        SpecimenFrame(178.dp, 80.dp, 4.7.dp, 4.7.dp) { ShortcutTileCompactPreview() }
    }
}
