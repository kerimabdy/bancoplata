package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

// Plata+ benefit row marks (835216e3, 5b0f3d7c), traced on the 24 grid from the 32pt source glyphs (step 5).

private const val W = 1.6f

private val thumbsUpOutline by lazy {
    plataGlyph("thumbs-up-outline") {
        stroke("M 7.4 10.6 H 5.3 C 4.2 10.6 3.5 11.4 3.5 12.5 V 18.6 C 3.5 19.6 4.3 20.4 5.4 20.4 H 7.8", W)
        stroke(
            "M 7.8 20.4 C 6.9 19.5 6.6 18 6.6 15.6 C 6.6 13.3 7.2 11.6 8.6 10.3 L 13.3 3.9 C 13.9 3 15.6 3.2 15.6 4.6 " +
                "C 15.6 6 14.6 7.8 14.6 9.2 C 14.6 10.1 15.1 10.4 16 10.4 H 17.9 C 18.8 10.4 19.3 11.1 19.3 11.9 " +
                "C 19.3 12.6 19 12.9 19 13.3 C 19 13.7 19.3 14 19.3 14.6 C 19.3 15.2 18.9 15.5 18.9 16 " +
                "C 18.9 16.4 19.1 16.8 19.1 17.4 C 19.1 19.2 18.3 20.4 16.8 20.4 H 7.8",
            W,
        )
    }
}

/** "No annual fee". */
val PlataIcons.ThumbsUpOutline: ImageVector get() = thumbsUpOutline

private val gauge by lazy {
    plataGlyph("gauge") {
        stroke("M 3.9 12 A 8.1 8.1 0 1 0 20.1 12 A 8.1 8.1 0 1 0 3.9 12 Z", W)
        listOf(7.5f to 10.1f, 9.3f to 8f, 11.9f to 7.4f, 14.5f to 8f, 16.4f to 10.1f).forEach { (x, y) ->
            fill("M ${x - 0.9f} $y A 0.9 0.9 0 1 0 ${x + 0.9f} $y A 0.9 0.9 0 1 0 ${x - 0.9f} $y Z")
        }
        stroke("M 9.3 12.1 L 12 16.2", W)
        fill("M 10.6 16.2 A 1.4 1.4 0 1 0 13.4 16.2 A 1.4 1.4 0 1 0 10.6 16.2 Z")
    }
}

/** "Free interbank transfers". */
val PlataIcons.Gauge: ImageVector get() = gauge

private val dollarDown by lazy {
    plataGlyph("dollar-down") {
        stroke("M 14 4.2 H 7.6 C 5.3 4.2 3.6 6.6 3.6 11.6 C 3.6 16.6 5.3 18.9 7.6 18.9 H 11.6", W)
        stroke(
            "M 12.6 9.3 C 12.4 8.6 11.9 8.3 11.2 8.3 C 10.3 8.3 9.7 8.8 9.7 9.6 C 9.7 10.6 10.5 10.9 11.3 11.2 " +
                "C 12.2 11.5 12.8 12 12.8 12.9 C 12.8 13.8 12.1 14.3 11.2 14.3 C 10.4 14.3 9.8 13.9 9.6 13.3 " +
                "M 11.2 7.2 V 8.3 M 11.2 14.3 V 15.4",
            W,
        )
        stroke("M 17 3.8 V 20.4 M 13.8 17.2 L 17 20.4 L 20.2 17.2", W)
    }
}

/** "Lower investment commission". */
val PlataIcons.DollarDown: ImageVector get() = dollarDown

private val people by lazy {
    plataGlyph("people") {
        stroke("M 4 8.6 A 2.4 2.4 0 1 0 8.8 8.6 A 2.4 2.4 0 1 0 4 8.6 Z", W)
        stroke("M 13.6 7.8 A 3 3 0 1 0 19.6 7.8 A 3 3 0 1 0 13.6 7.8 Z", W)
        stroke("M 8.2 19.3 H 1.7 C 1.4 16.3 3.6 14 6.5 14 C 7.6 14 8.4 14.3 9.1 14.7", W)
        stroke("M 10 19.3 C 10.3 16.2 13 13.8 16.7 13.8 C 20.4 13.8 23.1 16.2 23.3 19.3 Z", W)
    }
}

/** "Increased bonus". */
val PlataIcons.People: ImageVector get() = people

private val shieldCheck by lazy {
    plataGlyph("shield-check") {
        stroke("M 16 5.3 L 11.8 3.9 L 6.9 5.6 C 6.3 5.8 5.8 6.4 5.8 7.1 V 12.6 C 5.8 15.5 8.3 18.4 11.8 20.2 L 16 17.3", W)
        stroke("M 10.4 11.2 L 13.4 13.8 L 20 7.2", W)
    }
}

/** "Fraud compensation". */
val PlataIcons.ShieldCheck: ImageVector get() = shieldCheck

private val heartBolt by lazy {
    plataGlyph("heart-bolt") {
        stroke(
            "M 12 7.4 C 10.9 5.5 9.3 4.6 7.6 4.6 C 5.4 4.6 3.8 6.4 3.8 9 C 3.8 13 7.6 16.3 12 19.6 " +
                "C 16.4 16.3 20.2 13 20.2 9 C 20.2 6.4 18.6 4.6 16.4 4.6 C 14.7 4.6 13.1 5.5 12 7.4 Z",
            W,
        )
        fill("M 13.3 8.2 L 9.3 13.2 H 11.5 L 10.8 16.5 L 14.8 11.9 H 12.4 Z")
    }
}

/** "Purchase protection". */
val PlataIcons.HeartBolt: ImageVector get() = heartBolt

private val chatStar by lazy {
    plataGlyph("chat-star") {
        stroke(
            "M 8 16.6 H 4.5 C 3.4 16.6 2.6 15.8 2.6 14.7 V 5 C 2.6 3.9 3.4 3.1 4.5 3.1 H 19.5 C 20.6 3.1 21.4 3.9 21.4 5 " +
                "V 14.7 C 21.4 15.8 20.6 16.6 19.5 16.6 H 11.4 L 8 20 Z",
            W,
        )
        fill("M 11.90 6.50 L 12.84 9.11 L 15.61 9.19 L 13.42 10.89 L 14.19 13.56 L 11.90 12.00 L 9.61 13.56 L 10.38 10.89 L 8.19 9.19 L 10.96 9.11 Z")
    }
}

/** "SMS notifications". */
val PlataIcons.ChatStar: ImageVector get() = chatStar

private val paintBrush by lazy {
    plataGlyph("brush") {
        stroke("M 10.6 12 L 19.6 3.6 C 20.4 2.9 21.6 3.8 21.1 4.7 L 13.9 14.8 Z", W)
        stroke(
            "M 9.6 13.1 C 7.7 12.6 6 13.6 5.7 15.3 C 5.4 16.9 4.6 17.6 2.8 17.9 C 4 19.8 6 20.9 8.3 20.6 " +
                "C 10.6 20.3 12.2 18.4 11.9 16.2",
            W,
        )
    }
}

/** "Special app icons". */
val PlataIcons.PaintBrush: ImageVector get() = paintBrush

private val diamondPlus by lazy {
    plataGlyph("diamond-plus") {
        stroke(
            "M 10.6 3.6 C 11.4 2.8 12.6 2.8 13.4 3.6 L 20.4 10.6 C 21.2 11.4 21.2 12.6 20.4 13.4 L 13.4 20.4 " +
                "C 12.6 21.2 11.4 21.2 10.6 20.4 L 3.6 13.4 C 2.8 12.6 2.8 11.4 3.6 10.6 Z",
            W,
        )
        stroke("M 12 8.6 V 15.4 M 8.6 12 H 15.4", W)
    }
}

/** "Plata+ badge". */
val PlataIcons.DiamondPlus: ImageVector get() = diamondPlus

/** For the icon sheet. */
val PlataIcons.benefitGlyphs: List<ImageVector>
    get() = listOf(ThumbsUpOutline, Gauge, DollarDown, People, ShieldCheck, HeartBolt, ChatStar, PaintBrush, DiamondPlus)
