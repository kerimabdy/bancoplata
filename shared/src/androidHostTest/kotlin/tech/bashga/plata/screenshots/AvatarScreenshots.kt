package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.AvatarPhotoSmallPreview
import tech.bashga.plata.designsystem.components.AvatarPhotoMediumPreview
import tech.bashga.plata.designsystem.components.AvatarPhotoLargePreview
import tech.bashga.plata.designsystem.components.AvatarPlaceholderMediumPreview
import tech.bashga.plata.designsystem.components.AvatarPlaceholderLargePreview

class AvatarScreenshots : ScreenshotTest() {
    @Test fun photo32() = component("avatar/kind=photo,size=32", margin = 0.dp) {
        SpecimenFrame(40.dp, 40.dp, 2.dp, 2.dp) { AvatarPhotoSmallPreview() }
    }

    @Test fun photo56() = component("avatar/kind=photo,size=56") { AvatarPhotoMediumPreview() }

    @Test fun photo96() = component("avatar/kind=photo,size=96", margin = 0.dp) {
        SpecimenFrame(106.dp, 106.dp, 5.dp, 5.dp) { AvatarPhotoLargePreview() }
    }

    @Test fun placeholder56() = component("avatar/kind=placeholder,size=56") { AvatarPlaceholderMediumPreview() }

    @Test fun placeholder96() = component("avatar/kind=placeholder,size=96", margin = 0.dp) {
        SpecimenFrame(106.dp, 106.dp, 5.dp, 5.dp) { AvatarPlaceholderLargePreview() }
    }
}
