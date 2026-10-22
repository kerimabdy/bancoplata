package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ListRowBenefitPreview
import tech.bashga.plata.designsystem.components.ListRowCheckPreview
import tech.bashga.plata.designsystem.components.ListRowCrownPreview
import tech.bashga.plata.designsystem.components.ListRowDoubleDefaultPreview
import tech.bashga.plata.designsystem.components.ListRowDoubleGreyPreview
import tech.bashga.plata.designsystem.components.ListRowEditingPreview
import tech.bashga.plata.designsystem.components.ListRowSingleDefaultPreview
import tech.bashga.plata.designsystem.components.ListRowSingleDisabledPreview

class ListRowScreenshots : ScreenshotTest() {
    @Test fun singleDefault() = component("list-row/size=single,state=default,surface=plain", background = { it.surfaceCard }, margin = 0.dp) {
        ListRowSingleDefaultPreview()
    }

    @Test fun doubleDefault() = component("list-row/size=double,state=default,surface=plain", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(360.7.dp, 68.dp, 4.dp, 0.dp) { ListRowDoubleDefaultPreview() }
    }

    @Test fun singleDisabled() = component("list-row/size=single,state=disabled,surface=plain", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 56.dp, (-16).dp, 0.dp) { ListRowSingleDisabledPreview() }
    }

    @Test fun doubleGrey() = component("list-row/size=double,state=default,surface=grey", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 78.dp, 3.5.dp, 4.3.dp) { ListRowDoubleGreyPreview() }
    }

    // source crops: edit mode 2be9ff97 @ 0,220,393,80; benefits 787363a9; affiliation 00e6fc01
    @Test fun editing() = component("list-row/editing", margin = 0.dp) {
        SpecimenFrame(393.dp, 80.dp, 0.dp, 7.dp) { ListRowEditingPreview() }
    }

    // 787363a9 @ 16,568,361,92 and @ 16,503,361,76: the card edge is 4 in
    @Test fun benefit() = component("list-row/benefit", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(361.dp, 92.dp, 4.dp, 5.4.dp) { ListRowBenefitPreview() }
    }

    @Test fun crown() = component("list-row/crown", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(361.dp, 76.dp, 4.dp, 2.9.dp) { ListRowCrownPreview() }
    }

    @Test fun check() = component("list-row/check") { ListRowCheckPreview() }
}
