package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class SectionHeaderSize { Title, Small }

/** Gradient paints the title's last word ("Improved cashback"); Full paints the whole title ("Portfolio analysis", gradients.ai). */
enum class SectionHeaderEmphasis { Plain, Gradient, Full }

/**
 * [emphasisBrush] defaults to gradients.brand for Gradient and gradients.ai for Full; the Plata+ cards give each word its
 * own (gradients.wordViolet, wordOrange, wordBlue, wordCoral). [trailing] (a "See all" TextButton, an amount) is centred
 * on the title line and may be taller than it without growing the header.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    size: SectionHeaderSize = SectionHeaderSize.Title,
    emphasis: SectionHeaderEmphasis = SectionHeaderEmphasis.Plain,
    emphasisBrush: Brush? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val style = if (size == SectionHeaderSize.Title) PlataTheme.typography.title3 else PlataTheme.typography.body
    val lineBox = with(LocalDensity.current) { style.lineHeight.toDp() }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(PlataTheme.spacing.s8))
        }
        val titleText: @Composable () -> Unit = {
            when (emphasis) {
                SectionHeaderEmphasis.Plain -> PlataText(title, style, colors.textPrimary)
                SectionHeaderEmphasis.Full -> GradientText(title, style, emphasisBrush ?: PlataTheme.gradients.ai)
                SectionHeaderEmphasis.Gradient -> {
                    val split = title.lastIndexOf(' ')
                    if (split > 0) PlataText(title.substring(0, split + 1), style, colors.textPrimary, maxLines = 1)
                    GradientText(title.substring(split + 1), style, emphasisBrush ?: PlataTheme.gradients.brand, maxLines = 1)
                }
            }
        }
        if (trailing == null) {
            Row { titleText() }
        } else {
            // top-aligned so a two-line title keeps the trailing slot on its first line (baff34c6)
            Row(Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                Row(Modifier.weight(1f)) { titleText() }
                Row(
                    Modifier.height(lineBox).wrapContentHeight(unbounded = true),
                    horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8),
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailing,
                )
            }
        }
    }
}

@Preview
@Composable
internal fun SectionHeaderTitlePreview() = PlataTheme {
    SectionHeader("Popular stocks", Modifier.fillMaxWidth(), trailing = {
        TextButton("See all", {}, weight = TextButtonWeight.Semibold, size = TextButtonSize.Subhead)
    })
}

@Preview
@Composable
internal fun SectionHeaderSmallPreview() = PlataTheme { SectionHeader("App icons", size = SectionHeaderSize.Small) }

@Preview
@Composable
internal fun SectionHeaderGradientPreview() = PlataTheme {
    SectionHeader("Improved cashback", emphasis = SectionHeaderEmphasis.Gradient)
}

@Preview
@Composable
internal fun SectionHeaderGradientWordsPreview() = PlataTheme {
    val gradients = PlataTheme.gradients
    Column(verticalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s16)) {
        SectionHeader("Improved cashback", emphasis = SectionHeaderEmphasis.Gradient)
        SectionHeader("No charges", emphasis = SectionHeaderEmphasis.Gradient, emphasisBrush = gradients.wordViolet)
        SectionHeader("Better rewards", emphasis = SectionHeaderEmphasis.Gradient, emphasisBrush = gradients.wordOrange)
        SectionHeader("Superior support", emphasis = SectionHeaderEmphasis.Gradient, emphasisBrush = gradients.wordBlue)
        SectionHeader("App customization", emphasis = SectionHeaderEmphasis.Gradient, emphasisBrush = gradients.wordCoral)
    }
}

@Preview
@Composable
internal fun SectionHeaderFullPreview() = PlataTheme {
    SectionHeader(
        "Portfolio analysis",
        emphasis = SectionHeaderEmphasis.Full,
        leading = { Icon(PlataIcons.AiBadge, size = PlataTheme.sizes.iconMedium) },
    )
}

@Preview
@Composable
internal fun SectionHeaderLeadingIconPreview() = PlataTheme {
    SectionHeader(
        "MSI partners",
        Modifier.fillMaxWidth(),
        leading = { Icon(PlataIcons.Msi, size = PlataTheme.sizes.iconMedium, brush = PlataTheme.gradients.glyph) },
        trailing = { TextButton("Show all", {}, weight = TextButtonWeight.Semibold, size = TextButtonSize.Subhead) },
    )
}
