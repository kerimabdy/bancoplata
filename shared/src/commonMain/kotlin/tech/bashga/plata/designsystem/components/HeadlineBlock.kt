package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.designsystem.foundation.PlataText

enum class HeadlineBlockSize { Large, Medium, Small }

enum class HeadlineBlockAlign { Leading, Center }

/**
 * Title (large 34, medium 28, small 20 bold) over an optional body. The body is 17/20 on every source screen measured
 * (00e6fc01, 2d2d03c9, 8d47c829); long copy passes `bodyStyle = typography.bodyRelaxed` (17/24, 7ab581a2). Blank lines in
 * [body] split paragraphs, [paragraphSpacing] apart. Title→body [gap]: large 12, medium 15, small 20 as measured.
 * [media] (illustration, success mark) sits [mediaGap] above the title.
 */
@Composable
fun HeadlineBlock(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    size: HeadlineBlockSize = HeadlineBlockSize.Large,
    align: HeadlineBlockAlign = HeadlineBlockAlign.Leading,
    bodyStyle: TextStyle = PlataTheme.typography.bodyCompact,
    bodyColor: Color = PlataTheme.colors.textPrimary,
    gap: Dp = when (size) {
        HeadlineBlockSize.Large -> PlataTheme.spacing.s12
        HeadlineBlockSize.Medium -> 15.dp
        HeadlineBlockSize.Small -> PlataTheme.spacing.s20
    },
    paragraphSpacing: Dp = 10.dp,
    mediaGap: Dp = PlataTheme.spacing.s24,
    media: (@Composable () -> Unit)? = null,
) {
    val typography = PlataTheme.typography
    val center = align == HeadlineBlockAlign.Center
    val textAlign = if (center) TextAlign.Center else TextAlign.Start
    val titleStyle = when (size) {
        HeadlineBlockSize.Large -> typography.largeTitle
        HeadlineBlockSize.Medium -> typography.title1
        HeadlineBlockSize.Small -> typography.title3
    }
    Column(modifier, horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start) {
        if (media != null) {
            media()
            Spacer(Modifier.height(mediaGap))
        }
        PlataText(title, titleStyle, PlataTheme.colors.textPrimary, Modifier.fillMaxWidth(), textAlign = textAlign)
        if (body != null) {
            Column(
                Modifier.padding(top = gap),
                verticalArrangement = Arrangement.spacedBy(paragraphSpacing),
            ) {
                body.split("\n\n").forEach {
                    PlataText(it, bodyStyle, bodyColor, Modifier.fillMaxWidth(), textAlign = textAlign)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun HeadlineBlockLargePreview() = PlataTheme {
    HeadlineBlock("Affiliation", Modifier.fillMaxWidth(), body = "Please read and mark every option that applies to you")
}

@Preview
@Composable
internal fun HeadlineBlockMediumCenterPreview() = PlataTheme {
    HeadlineBlock(
        "Get up to 15%\ncashback",
        Modifier.fillMaxWidth(),
        body = "Select 4 categories each month\nto receive cashback with every purchase",
        size = HeadlineBlockSize.Medium,
        align = HeadlineBlockAlign.Center,
    )
}

@Preview
@Composable
internal fun HeadlineBlockSmallCenterPreview() = PlataTheme {
    HeadlineBlock(
        "Enable Face ID?",
        Modifier.fillMaxWidth(),
        body = "Turn on Face ID to log in\nwithout passcode",
        size = HeadlineBlockSize.Small,
        align = HeadlineBlockAlign.Center,
    )
}

@Preview
@Composable
internal fun HeadlineBlockLongCopyPreview() = PlataTheme {
    HeadlineBlock(
        "First, what is a stock?",
        Modifier.fillMaxWidth(),
        body = "Stocks are like tiny pieces of ownership in a company. When you own a stock, you own a bit of that company and its success can make your stock more valuable.\n\n" +
            "👉🏼 If you own a stock you can receive dividends (fraction of profits the company makes)\n\n" +
            "👉🏼 Or if the company does well, the price of the stock goes up and you can sell to earn the price difference.",
        size = HeadlineBlockSize.Medium,
        bodyStyle = PlataTheme.typography.bodyRelaxed,
        gap = PlataTheme.spacing.s20,
    )
}
