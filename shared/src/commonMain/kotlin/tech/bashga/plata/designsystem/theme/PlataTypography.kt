package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Centred, untrimmed: every line box is exactly lineHeight tall like a Figma/iOS text box, so measured paddings hold.
private val IosLineBox = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun plataStyle(size: TextUnit, weight: FontWeight, lineHeight: TextUnit, tracking: TextUnit) = TextStyle(
    fontFamily = FontFamily.Default,
    fontSize = size,
    fontWeight = weight,
    lineHeight = lineHeight,
    letterSpacing = tracking,
    lineHeightStyle = IosLineBox,
)

/** Type ramp from ds/foundations.md (`type/title3` → [title3]). [display] is set in uppercase by the caller. */
@Immutable
data class PlataTypography(
    val amountDisplay: TextStyle = plataStyle(36.sp, FontWeight.Bold, 43.sp, 0.37.sp),
    val largeTitle: TextStyle = plataStyle(34.sp, FontWeight.Bold, 41.sp, 0.40.sp),
    // lines sit 36 apart on every PromoHeadline (025e71c5, d7d78e0b, adeddd37); Figma's 34 makes two lines 4pt short
    val display: TextStyle = plataStyle(30.sp, FontWeight.ExtraBold, 36.sp, 0.40.sp),
    /** MediaHeader carousel title, 24 heavy (uppercase the string yourself; 816019bf caps 17). */
    val displaySmall: TextStyle = plataStyle(24.sp, FontWeight.ExtraBold, 29.sp, 0.07.sp),
    val title1: TextStyle = plataStyle(28.sp, FontWeight.Bold, 34.sp, 0.38.sp),
    val title2: TextStyle = plataStyle(24.sp, FontWeight.Bold, 29.sp, 0.07.sp),
    val title3: TextStyle = plataStyle(20.sp, FontWeight.Bold, 25.sp, (-0.45).sp),
    val value: TextStyle = plataStyle(20.sp, FontWeight.SemiBold, 25.sp, (-0.45).sp),
    val headline: TextStyle = plataStyle(17.sp, FontWeight.SemiBold, 22.sp, (-0.43).sp),
    val body: TextStyle = plataStyle(17.sp, FontWeight.Normal, 22.sp, (-0.43).sp),
    val bodyCompact: TextStyle = plataStyle(17.sp, FontWeight.Normal, 20.sp, (-0.43).sp),
    val bodyRelaxed: TextStyle = plataStyle(17.sp, FontWeight.Normal, 24.sp, (-0.43).sp),
    /** Legal copy on the account-opening Acknowledgment (80971162): 17 regular on a 20.33 pitch, a hair looser than [bodyCompact]. */
    val bodyLegal: TextStyle = plataStyle(17.sp, FontWeight.Normal, 20.33.sp, (-0.43).sp),
    val subhead: TextStyle = plataStyle(15.sp, FontWeight.Normal, 20.sp, (-0.23).sp),
    val subheadTight: TextStyle = plataStyle(15.sp, FontWeight.Normal, 18.sp, (-0.23).sp),
    val subheadTightStrong: TextStyle = plataStyle(15.sp, FontWeight.SemiBold, 18.sp, (-0.23).sp),
    val subheadStrong: TextStyle = plataStyle(15.sp, FontWeight.SemiBold, 20.sp, (-0.23).sp),
    val footnote: TextStyle = plataStyle(13.sp, FontWeight.Normal, 18.sp, (-0.08).sp),
    /** Two-line neutral Toast (73569385): 13 regular on a 16 pitch. */
    val footnoteTight: TextStyle = plataStyle(13.sp, FontWeight.Normal, 16.sp, (-0.08).sp),
    /** Figma `type/footnote-strong`: Badge count. */
    val footnoteStrong: TextStyle = plataStyle(13.sp, FontWeight.SemiBold, 18.sp, (-0.08).sp),
    /** Figma `type/keypad`: KeypadKey digit. */
    val keypad: TextStyle = plataStyle(34.sp, FontWeight.Normal, 41.sp, 0.40.sp),
    val caption: TextStyle = plataStyle(11.sp, FontWeight.Normal, 13.sp, 0.06.sp),
    val tab: TextStyle = plataStyle(10.sp, FontWeight.Medium, 12.sp, 0.12.sp),
) {
    val all: List<Pair<String, TextStyle>>
        get() = listOf(
            "amountDisplay" to amountDisplay, "largeTitle" to largeTitle, "display" to display, "displaySmall" to displaySmall,
            "title1" to title1, "title2" to title2, "title3" to title3, "value" to value,
            "headline" to headline, "body" to body, "bodyCompact" to bodyCompact, "bodyRelaxed" to bodyRelaxed,
            "subhead" to subhead, "subheadTight" to subheadTight, "subheadTightStrong" to subheadTightStrong,
            "subheadStrong" to subheadStrong, "footnote" to footnote, "footnoteTight" to footnoteTight, "footnoteStrong" to footnoteStrong, "keypad" to keypad, "caption" to caption, "tab" to tab,
        )
}
