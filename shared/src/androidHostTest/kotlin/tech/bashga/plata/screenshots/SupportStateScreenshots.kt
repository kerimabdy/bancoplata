package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.screens.support.SupportChatContent
import tech.bashga.plata.screens.support.SupportChatState
import tech.bashga.plata.screens.support.supportThread

/** SCREEN-MAP §3.6: one test per reference id, named after it. */
class SupportStateScreenshots : ScreenshotTest() {
    @Test fun `fb63cde9`() = screenState("fb63cde9", shell = TestShell(TabBarTab.Support, Month.July)) {
        SupportChatContent(SupportChatState(thread = AppSnapshot().supportThread, draft = ""), onAction = {})
    }
}
