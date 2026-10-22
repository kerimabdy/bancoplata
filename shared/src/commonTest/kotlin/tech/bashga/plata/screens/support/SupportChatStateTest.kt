package tech.bashga.plata.screens.support

import kotlin.test.Test
import kotlin.test.assertEquals

class SupportChatStateTest {
    @Test
    fun sendMovesTheDraftIntoTheChat() {
        val typed = SupportChatState().reduce(SupportChatAction.TypeDraft("  Hello "))
        assertEquals("  Hello ", typed.draft)
        val sent = typed.reduce(SupportChatAction.Send)
        assertEquals(listOf("Hello"), sent.sent)
        assertEquals("", sent.draft)
        assertEquals(sent, sent.reduce(SupportChatAction.TypeDraft("   ")).reduce(SupportChatAction.Send).copy(draft = ""))
    }
}
