package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ChoiceCardCenteredPreview
import tech.bashga.plata.designsystem.components.ChoiceCardRowListPreview
import tech.bashga.plata.designsystem.components.ChoiceCardRowPreview
import tech.bashga.plata.designsystem.components.ChoiceCardRowSelectedPreview
import tech.bashga.plata.designsystem.components.ChoiceCardTilePreview
import tech.bashga.plata.designsystem.components.ChoiceCardTileSelectedPreview

class ChoiceCardScreenshots : ScreenshotTest() {
    @Test fun tileDefault() = component("choice-card/layout=tile,state=default", margin = 0.dp) {
        SpecimenFrame(116.dp, 78.dp, 4.dp, 4.3.dp) { ChoiceCardTilePreview() }
    }

    @Test fun tileSelected() = component("choice-card/layout=tile,state=selected", margin = 0.dp) {
        SpecimenFrame(116.dp, 78.dp, 4.dp, 4.3.dp) { ChoiceCardTileSelectedPreview() }
    }

    @Test fun rowDefault() = component("choice-card/layout=row,state=default", margin = 0.dp) {
        SpecimenFrame(361.dp, 80.dp, 4.dp, 3.7.dp) { Box(Modifier.width(353.dp)) { ChoiceCardRowPreview() } }
    }

    @Test fun rowSelected() = component("choice-card/layout=row,state=selected", margin = 0.dp) {
        SpecimenFrame(361.dp, 80.dp, 4.dp, 3.5.dp) { Box(Modifier.width(353.dp)) { ChoiceCardRowSelectedPreview() } }
    }

    @Test fun centeredDefault() = component("choice-card/layout=centered,state=default") {
        Box(Modifier.width(353.dp)) { ChoiceCardCenteredPreview() }
    }

    @Test fun rowList() = component("choice-card/row-list") { Box(Modifier.width(353.dp)) { ChoiceCardRowListPreview() } }
}
