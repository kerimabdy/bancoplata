package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_lesson_stocks_etfs
import plata.shared.generated.resources.raster_lesson_why_investing
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class LessonCardSize { Compact, Large }

/**
 * White card, radius 20, padding 20, 20pt title over a 13pt read time. Compact (5ab1e0c9, b658c9da) hugs its text (86 for
 * one title line) with the [thumbnail] centred on the right and an optional grey close disc 12 in from the corner.
 * Large (5d3ebc67) is 175 tall with the [action] (a small Button) pinned 20 above the bottom and the thumbnail bleeding off
 * the bottom-right corner. Titles break where the source breaks them: pass its "\n" ("How to build a\nstrong portfolio").
 * [contentPadding] is the text column's padding; the two-line card on b658c9da pads 17.5 top and bottom (106 high).
 */
@Composable
fun LessonCard(
    title: String,
    readTime: String,
    modifier: Modifier = Modifier,
    size: LessonCardSize = LessonCardSize.Compact,
    showClose: Boolean = size == LessonCardSize.Compact,
    onClose: () -> Unit = {},
    onClick: () -> Unit = {},
    thumbnail: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(PlataTheme.spacing.cardPadding),
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val compact = size == LessonCardSize.Compact
    Box(
        modifier
            .clip(ContinuousRoundedShape(PlataTheme.radii.xl))
            .background(colors.surfaceCard)
            .pressable(onClick = onClick)
            .then(if (compact) Modifier else Modifier.height(175.dp)),
    ) {
        Row(if (compact) Modifier else Modifier.fillMaxHeight()) {
            Column(Modifier.weight(1f).padding(contentPadding).then(if (compact) Modifier else Modifier.fillMaxHeight())) {
                PlataText(title, PlataTheme.typography.title3, colors.textPrimary)
                Spacer(Modifier.height(3.dp))
                PlataText(readTime, PlataTheme.typography.footnote, colors.textSecondary, maxLines = 1)
                if (!compact && action != null) {
                    Spacer(Modifier.weight(1f))
                    action()
                }
            }
            if (thumbnail != null) {
                Box(
                    if (compact) Modifier.align(Alignment.CenterVertically).padding(end = spacing.s20) else Modifier.align(Alignment.Bottom),
                ) { thumbnail() }
            }
        }
        if (showClose) {
            IconButton(
                PlataIcons.CloseX,
                onClose,
                Modifier.align(Alignment.TopEnd).padding(spacing.s12),
                style = IconButtonStyle.Grey,
                size = IconButtonSize.S24,
                contentDescription = "Close",
            )
        }
    }
}

@Preview
@Composable
internal fun LessonCardCompactPreview() = PlataTheme {
    LessonCard(
        "Stocks vs ETFs",
        "3-minute read",
        Modifier.fillMaxWidth(),
        thumbnail = { Image(painterResource(Res.drawable.raster_lesson_stocks_etfs), null, Modifier.size(64.dp, 80.dp)) },
    )
}

@Preview
@Composable
internal fun LessonCardLargePreview() = PlataTheme {
    LessonCard(
        "Why investing matters",
        "3-minute read",
        Modifier.fillMaxWidth(),
        size = LessonCardSize.Large,
        thumbnail = { Image(painterResource(Res.drawable.raster_lesson_why_investing), null, Modifier.size(139.dp, 175.dp)) },
        action = { Button("Lesson 2", {}, size = ButtonSize.Small, icon = PlataIcons.Play) },
    )
}
