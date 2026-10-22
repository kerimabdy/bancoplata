package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ChatComposerPreview
import tech.bashga.plata.designsystem.components.ChatHeaderPreview
import tech.bashga.plata.designsystem.components.ChatIncomingPreview
import tech.bashga.plata.designsystem.components.ChatOutgoingFullLinePreview
import tech.bashga.plata.designsystem.components.ChatOutgoingPreview
import tech.bashga.plata.designsystem.components.ChatThreadPreview

// references are crops of fb63cde9 (the support chat), noted per test
class ChatScreenshots : ScreenshotTest() {
    // crop.sh fb63cde9 0 60 393 70; the 56 avatar starts at y 67
    @Test fun header() = component("chat/header", margin = 0.dp) {
        SpecimenFrame(393.dp, 70.dp, 0.dp, 7.dp) { ChatHeaderPreview() }
    }

    // crop.sh fb63cde9 0 60 393 460: header, date, sender, the first bubble, the second date
    @Test fun thread() = component("chat/thread", margin = 0.dp) {
        SpecimenFrame(393.dp, 460.dp, 0.dp, 7.dp) { ChatThreadPreview() }
    }

    // crop.sh fb63cde9 0 235 393 240; the bubble starts at y 238.7
    @Test fun incoming() = component("chat/incoming", margin = 0.dp) {
        SpecimenFrame(393.dp, 240.dp, 0.dp, 3.7.dp) { ChatIncomingPreview() }
    }

    @Test fun outgoing() = component("chat/outgoing") { ChatOutgoingPreview() }

    // a last line too full for the time, which wraps below it
    @Test fun outgoingFullLine() = component("chat/outgoing-full-line") { ChatOutgoingFullLinePreview() }

    // crop.sh fb63cde9 0 700 393 70; the bar starts at y 703.7
    @Test fun composer() = component("chat/composer", margin = 0.dp) {
        SpecimenFrame(393.dp, 70.dp, 0.dp, 3.7.dp) { ChatComposerPreview() }
    }
}
