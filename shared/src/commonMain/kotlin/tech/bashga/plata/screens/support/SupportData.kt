package tech.bashga.plata.screens.support

import androidx.compose.runtime.Immutable
import tech.bashga.plata.data.AppSnapshot

const val SUPPORT_PHONE = "+52 55 9990 8880"

const val SUPPORT_TEL_URI = "tel:+525599908880"

/** One incoming message group; [paragraphs] keep the source's line breaks, an empty one is its blank line. */
@Immutable
data class SupportMessage(val date: String, val sender: String, val time: String, val paragraphs: List<String>)

private val seedThread = listOf(
    SupportMessage(
        date = "31 March 2023",
        sender = "Plata Support Team",
        time = "02:00",
        paragraphs = listOf(
            "Hi!",
            "We care about you. That's why we\nare available 24/7 in this chat and\nvia phone number at\n$SUPPORT_PHONE, to help you if\nyou need us.",
            "Please, let us know what we can do\nfor you, we're always happy to\nassist you!",
        ),
    ),
    SupportMessage(
        date = "29 June",
        sender = "Plata",
        time = "10:15",
        paragraphs = listOf(
            "Hi, Sofía. 👋 These are 5 reasons\nwhy you should invest",
            "",
            "✅ Grow your money: When you\ninvest, your money can increase its\nvalue. It's like planting a seed that\ngrows into a tree.",
            "✅ Beat inflation: Investing helps your savings keep up with rising prices.",
        ),
    ),
)

/** The support thread; the fake backend has no message store, so it is the seeded conversation. */
val AppSnapshot.supportThread: List<SupportMessage>
    get() = seedThread
