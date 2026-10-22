package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

@Composable
fun Footnote(
    text: String,
    modifier: Modifier = Modifier,
    link: String? = null,
    onLinkClick: () -> Unit = {},
) {
    val annotated = buildAnnotatedString {
        append(text)
        if (link != null) {
            append(" ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "footnote-link",
                    styles = TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                ) { onLinkClick() },
            ) { append(link) }
        }
    }
    PlataText(annotated, PlataTheme.typography.footnote, PlataTheme.colors.textTertiary, modifier, textAlign = TextAlign.Center)
}

@Preview
@Composable
internal fun FootnotePreview() = PlataTheme { Footnote("By continuing you agree to our", link = "Privacy notice") }

@Preview
@Composable
internal fun FootnotePlainPreview() = PlataTheme { Footnote("Services offered by VestFi") }
