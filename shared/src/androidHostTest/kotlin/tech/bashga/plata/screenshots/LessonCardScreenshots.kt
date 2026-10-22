package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.LessonCardCompactPreview
import tech.bashga.plata.designsystem.components.LessonCardLargePreview

class LessonCardScreenshots : ScreenshotTest() {
    @Test fun compact() = component("lesson-card/size=compact", margin = 0.dp) {
        SpecimenFrame(361.dp, 92.dp, 4.dp, 3.dp) { Box(Modifier.width(353.dp)) { LessonCardCompactPreview() } }
    }

    // the large card sits in a pager on 5d3ebc67 and is 349 wide there
    @Test fun large() = component("lesson-card/size=large", margin = 0.dp) {
        SpecimenFrame(361.dp, 183.dp, 4.dp, 3.7.dp) { Box(Modifier.width(349.dp)) { LessonCardLargePreview() } }
    }
}
