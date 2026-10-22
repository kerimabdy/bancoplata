package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PromoChevron
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class PromoHeadlineAlign { Leading, Center }

/**
 * Split: line 1 ink, line 2 gradients.brand (025e71c5). Gradient: the whole headline in gradients.titleViolet (63be46ba).
 * Brand: solid brand orange (adeddd37; it samples #FF5D16, not the inventory's #FF4F00). Plain: ink (d7d78e0b).
 */
enum class PromoHeadlineTone { Split, Gradient, Brand, Plain }

/**
 * Heavy uppercase display headline, lines 36 apart. Leading adds the heavy accent chevron after the last line and the
 * whole block becomes tappable through [onClick]; Center takes an optional [eyebrow] above and [body] below, 8 from the
 * 36 line boxes (the inventory's 12 assumed Figma's 34 boxes; both put the caps where adeddd37 and 63be46ba have them).
 * The Plata+ welcome (787363a9) sets its body in text/primary 12 below: pass [bodyColor] and [bodyGap].
 */
@Composable
fun PromoHeadline(
    line1: String,
    modifier: Modifier = Modifier,
    line2: String? = null,
    align: PromoHeadlineAlign = PromoHeadlineAlign.Leading,
    tone: PromoHeadlineTone = PromoHeadlineTone.Split,
    eyebrow: String? = null,
    body: String? = null,
    bodyColor: Color = PlataTheme.colors.textSecondary,
    bodyGap: Dp = PlataTheme.spacing.s8,
    showChevron: Boolean = align == PromoHeadlineAlign.Leading,
    onClick: (() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val gradients = PlataTheme.gradients
    val typography = PlataTheme.typography
    val style = typography.display
    val center = align == PromoHeadlineAlign.Center
    val textAlign = if (center) TextAlign.Center else TextAlign.Start
    val lines = listOfNotNull(line1, line2).map { it.uppercase() }
    Column(
        modifier.then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier),
        horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        if (eyebrow != null) {
            PlataText(eyebrow, typography.bodyCompact, colors.textPrimary, textAlign = textAlign)
            Spacer(Modifier.height(PlataTheme.spacing.s8))
        }
        val last = lines.last()
        val leadLines = lines.dropLast(1)
        when (tone) {
            PromoHeadlineTone.Gradient -> {
                GradientText(lines.joinToString("\n"), style, gradients.titleViolet, textAlign = textAlign)
            }
            else -> {
                val solid = if (tone == PromoHeadlineTone.Brand) colors.brandPrimary else colors.textPrimary
                leadLines.forEach { PlataText(it, style, solid, textAlign = textAlign) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tone == PromoHeadlineTone.Split && leadLines.isNotEmpty()) {
                        GradientText(last, style, gradients.brand, textAlign = textAlign)
                    } else {
                        PlataText(last, style, solid, textAlign = textAlign)
                    }
                    if (showChevron) {
                        Spacer(Modifier.width(PlataTheme.spacing.s4))
                        Icon(PlataIcons.PromoChevron, size = PlataTheme.sizes.iconMedium, tint = colors.accent)
                    }
                }
            }
        }
        if (body != null) {
            Spacer(Modifier.height(bodyGap))
            PlataText(body, typography.bodyCompact, bodyColor, Modifier.fillMaxWidth(), textAlign = textAlign)
        }
    }
}

@Preview
@Composable
internal fun PromoHeadlineSplitPreview() = PlataTheme {
    PromoHeadline("Use your card", line2 = "to get cashback", onClick = {})
}

@Preview
@Composable
internal fun PromoHeadlinePlainPreview() = PlataTheme {
    PromoHeadline("Meses", Modifier.fillMaxWidth(), line2 = "sin intereses", align = PromoHeadlineAlign.Center, tone = PromoHeadlineTone.Plain)
}

@Preview
@Composable
internal fun PromoHeadlineGradientPreview() = PlataTheme {
    PromoHeadline(
        "Get $200",
        Modifier.fillMaxWidth(),
        align = PromoHeadlineAlign.Center,
        tone = PromoHeadlineTone.Gradient,
        eyebrow = "Bring a friend",
        body = "Your friend will get a card with $0\nannual fee",
    )
}

@Preview
@Composable
internal fun PromoHeadlineBrandPreview() = PlataTheme {
    PromoHeadline(
        "Get $3,000 for",
        Modifier.fillMaxWidth(),
        line2 = "3 friends",
        align = PromoHeadlineAlign.Center,
        tone = PromoHeadlineTone.Brand,
        eyebrow = "Bring a friend",
        body = "Get $500 for the first,\n$1,500 for two,\n$3,000 for three,\nand $500 more for each one after that",
    )
}

@Preview
@Composable
internal fun PromoHeadlinePrimaryBodyPreview() = PlataTheme {
    PromoHeadline(
        "Plata+ is yours",
        Modifier.fillMaxWidth(),
        align = PromoHeadlineAlign.Center,
        tone = PromoHeadlineTone.Plain,
        body = "$39/mo + VAT starting on your first credit card due date",
        bodyColor = PlataTheme.colors.textPrimary,
        bodyGap = PlataTheme.spacing.s12,
    )
}
