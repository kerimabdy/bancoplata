package tech.bashga.plata.designsystem

import androidx.activity.ComponentActivity
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import tech.bashga.plata.designsystem.components.CodeField
import tech.bashga.plata.designsystem.theme.PlataTheme
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class CodeFieldTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun otpFieldIsAnOneTimeCodeNotAPassword() {
        rule.setContent { PlataTheme { CodeField("", {}, Modifier.testTag("code")) } }
        val config = rule.onNodeWithTag("code").fetchSemanticsNode().config
        assertEquals(ContentType.SmsOtpCode, config[SemanticsProperties.ContentType])
        assertEquals(false, config.contains(SemanticsProperties.Password))
    }
}
