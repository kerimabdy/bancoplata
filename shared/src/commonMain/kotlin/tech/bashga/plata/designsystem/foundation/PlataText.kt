package tech.bashga.plata.designsystem.foundation

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isSpecified

@Composable
fun PlataText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(color = color, textAlign = textAlign),
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun PlataText(
    text: AnnotatedString,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(
        text = remember(text) { text.withNewlinesInMetricRuns() },
        modifier = modifier,
        style = style.merge(color = color, textAlign = textAlign),
        maxLines = maxLines,
        overflow = overflow,
    )
}

// a `\n` alone in its own run lends Android's line-height span taller fallback metrics (36 → 39.33 for two lines)
internal fun AnnotatedString.withNewlinesInMetricRuns(): AnnotatedString {
    val adjusted = spanStyles.map { range ->
        if (!range.item.affectsMetrics || range.start >= range.end) return@map range
        val start = if (text[range.start] == '\n') range.start + 1 else range.start
        val end = if (range.end < text.length && text[range.end] == '\n') range.end + 1 else range.end
        if (start == range.start && end == range.end) range else AnnotatedString.Range(range.item, start, end)
    }
    if (adjusted.zip(spanStyles).all { (a, b) -> a === b }) return this
    val source = this
    return buildAnnotatedString {
        append(source.text)
        adjusted.filter { it.start < it.end }.forEach { addStyle(it.item, it.start, it.end) }
        source.paragraphStyles.forEach { addStyle(it.item, it.start, it.end) }
        source.getStringAnnotations(0, source.length).forEach { addStringAnnotation(it.tag, it.item, it.start, it.end) }
        source.getLinkAnnotations(0, source.length).forEach { link ->
            when (val item = link.item) {
                is LinkAnnotation.Url -> addLink(item, link.start, link.end)
                is LinkAnnotation.Clickable -> addLink(item, link.start, link.end)
            }
        }
    }
}

private val SpanStyle.affectsMetrics: Boolean
    get() = fontSize.isSpecified || fontWeight != null || fontStyle != null || fontSynthesis != null ||
        fontFamily != null || fontFeatureSettings != null || letterSpacing.isSpecified || baselineShift != null ||
        textGeometricTransform != null || localeList != null

/**
 * Text filled with [brush] across the text itself (all lines), e.g. `PlataTheme.gradients.brand`.
 * A `SpanStyle(brush = …)` is sized to the whole paragraph, so for one gradient word inside a sentence
 * set the word as its own [GradientText] in a Row/FlowRow, or give the brush explicit start/end offsets.
 */
@Composable
fun GradientText(
    text: String,
    style: TextStyle,
    brush: Brush,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        // the paragraph is as wide as its constraints, and the brush spans the paragraph, so hug the text first
        modifier = modifier.width(IntrinsicSize.Max),
        style = style.merge(TextStyle(brush = brush, textAlign = textAlign)),
        maxLines = maxLines,
    )
}
