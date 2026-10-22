package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlataWordmark
import tech.bashga.plata.designsystem.theme.PlataTheme
import kotlin.math.ceil
import kotlin.math.max

enum class ChatDirection { Incoming, Outgoing }

private val BubbleMaxWidth = 300.dp
private val BubblePaddingV = 10.dp
private val ParagraphGap = 14.dp
private val ComposerPaddingV = 13.dp
private val FieldPaddingH = 11.dp
private val WordmarkWidth = 0.75f
private val TimeLift = 1.dp
private val Hairline = 0.5.dp
private val SenderInset = 10.dp

/** The orange PLATA disc: 56 in the header, 40 beside the bubbles. */
@Composable
fun ChatAvatar(modifier: Modifier = Modifier, size: Dp = PlataTheme.sizes.leadingTile) {
    Box(modifier.size(size).background(PlataTheme.gradients.appIcon, CircleShape), contentAlignment = Alignment.Center) {
        val wordmark = PlataIcons.PlataWordmark
        Image(
            rememberVectorPainter(wordmark),
            contentDescription = "Plata",
            modifier = Modifier.size(size * WordmarkWidth, size * WordmarkWidth * (wordmark.viewportHeight / wordmark.viewportWidth)),
            colorFilter = ColorFilter.tint(PlataTheme.colors.textOnDark),
        )
    }
}

/** 56 avatar, then 20 to its right the 20 bold [title] directly over the 17 [subtitle], centred on the avatar (fb63cde9). */
@Composable
fun ChatHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    avatar: @Composable () -> Unit = { ChatAvatar(size = AvatarSize.Medium.diameter) },
) {
    val colors = PlataTheme.colors
    Row(
        modifier.padding(horizontal = PlataTheme.spacing.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        avatar()
        Column {
            PlataText(title, PlataTheme.typography.title3, colors.textPrimary, maxLines = 1)
            PlataText(subtitle, PlataTheme.typography.body, colors.textPrimary, maxLines = 1)
        }
    }
}

/** Centred 15 semibold secondary date between message groups. */
@Composable
fun ChatDateSeparator(text: String, modifier: Modifier = Modifier) {
    PlataText(
        text,
        PlataTheme.typography.subheadStrong,
        PlataTheme.colors.textSecondary,
        modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

/** The sender's name above an incoming group, 10 in from the bubble edge (its ink starts 2 left of the bubble text). */
@Composable
fun ChatSenderLabel(name: String, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    PlataText(
        name,
        PlataTheme.typography.subheadStrong,
        PlataTheme.colors.textSecondary,
        modifier.padding(start = spacing.screenMargin + PlataTheme.sizes.leadingTile + spacing.s8 + SenderInset),
        maxLines = 1,
    )
}

/**
 * Message bubble, radius 20, padding 12/10, up to 300 wide. [paragraphs] are 17/20 and 14 apart (the source's blank
 * lines); links are spans in the AnnotatedString. [time] (13) sits at the bottom right on the last line when it fits
 * 8 after the text, and on its own line below otherwise. Incoming is white on the page, outgoing is accent with white text.
 */
@Composable
fun ChatBubble(
    paragraphs: List<AnnotatedString>,
    modifier: Modifier = Modifier,
    direction: ChatDirection = ChatDirection.Incoming,
    time: String? = null,
) {
    val colors = PlataTheme.colors
    val outgoing = direction == ChatDirection.Outgoing
    val ink = if (outgoing) colors.textOnDark else colors.textPrimary
    val bodyStyle = PlataTheme.typography.bodyCompact
    val measurer = rememberTextMeasurer()
    val timeGap = PlataTheme.spacing.s8
    Layout(
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(ParagraphGap)) {
                paragraphs.forEach { PlataText(it, bodyStyle, ink) }
            }
            if (time != null) {
                PlataText(
                    time,
                    PlataTheme.typography.footnote,
                    if (outgoing) colors.textOnDark.copy(alpha = 0.7f) else colors.textSecondary,
                    maxLines = 1,
                )
            }
        },
        modifier = modifier
            .widthIn(max = BubbleMaxWidth)
            .background(if (outgoing) colors.accent else colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.xl))
            .padding(horizontal = PlataTheme.spacing.s12, vertical = BubblePaddingV),
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val text = measurables[0].measure(loose)
        val stamp = measurables.getOrNull(1)?.measure(loose)
            ?: return@Layout layout(text.width, text.height) { text.place(0, 0) }
        val lastLineEnd = paragraphs.lastOrNull()?.let { last ->
            val layout = measurer.measure(last, bodyStyle, constraints = Constraints(maxWidth = constraints.maxWidth))
            ceil(layout.getLineRight(layout.lineCount - 1)).toInt()
        } ?: 0
        val inlineWidth = lastLineEnd + timeGap.roundToPx() + stamp.width
        val inline = inlineWidth <= constraints.maxWidth
        val width = max(text.width, if (inline) inlineWidth else stamp.width).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = if (inline) text.height else text.height + stamp.height
        layout(width, height) {
            text.place(0, 0)
            stamp.place(width - stamp.width, if (inline) text.height - stamp.height + TimeLift.roundToPx() else text.height)
        }
    }
}

@Composable
fun ChatBubble(text: String, modifier: Modifier = Modifier, direction: ChatDirection = ChatDirection.Incoming, time: String? = null) =
    ChatBubble(text.split("\n\n").map(::AnnotatedString), modifier, direction, time)

/**
 * One message line in the thread, 20 from the screen edges. Incoming bubbles sit 8 right of a 40 [avatar] column,
 * bottom-aligned (pass `null` to keep the column empty for earlier bubbles of a group); outgoing bubbles hug the right.
 */
@Composable
fun ChatMessage(
    direction: ChatDirection,
    modifier: Modifier = Modifier,
    avatar: (@Composable () -> Unit)? = null,
    bubble: @Composable () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = PlataTheme.spacing.screenMargin),
        horizontalArrangement = if (direction == ChatDirection.Outgoing) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (direction == ChatDirection.Incoming) {
            Box(Modifier.size(PlataTheme.sizes.leadingTile)) { avatar?.invoke() }
            Spacer(Modifier.width(PlataTheme.spacing.s8))
        }
        bubble()
    }
}

/**
 * The bar above the tab bar: a white 40 plus disc and a white capsule field (hairline edges), 8 apart, 13 above and below, on the tab-bar
 * material. [onAttach] is the plus; the field shows [placeholder] until something is typed. [onSend] is the keyboard's Send key.
 */
@Composable
fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Message",
    onAttach: () -> Unit = {},
    onSend: (() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val textStyle = PlataTheme.typography.body
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceTabbar)
            .padding(horizontal = PlataTheme.spacing.screenMargin, vertical = ComposerPaddingV),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            PlataIcons.Plus,
            onAttach,
            Modifier.border(Hairline, colors.borderSubtle, CircleShape),
            style = IconButtonStyle.White, size = IconButtonSize.S40, contentDescription = "Attach")
        BasicTextField(
            value,
            onValueChange,
            Modifier.weight(1f).height(PlataTheme.sizes.buttonSmall),
            textStyle = textStyle.merge(color = colors.textPrimary),
            singleLine = true,
            keyboardOptions = if (onSend != null) KeyboardOptions(imeAction = ImeAction.Send) else KeyboardOptions.Default,
            keyboardActions = KeyboardActions(onSend = { onSend?.invoke() }),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { field ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(PlataTheme.sizes.buttonSmall)
                        .background(colors.surfaceCard, CircleShape)
                        .border(Hairline, colors.borderSubtle, CircleShape)
                        .padding(horizontal = FieldPaddingH),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) PlataText(placeholder, textStyle, colors.controlOff, maxLines = 1)
                    field()
                }
            },
        )
    }
}

private val sampleWelcome: List<AnnotatedString>
    @Composable get() {
        val accent = PlataTheme.colors.accent
        return listOf(
            AnnotatedString("Hi!"),
            buildAnnotatedString {
                append("We care about you. That's why we are available 24/7 in this chat and via phone number at ")
                withStyle(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)) { append("+52 55 9990 8880") }
                append(", to help you if you need us.")
            },
            AnnotatedString("Please, let us know what we can do for you, we're always happy to assist you!"),
        )
    }

@Preview
@Composable
internal fun ChatHeaderPreview() = PlataTheme { ChatHeader("Support", "We’re here 24/7", Modifier.width(393.dp)) }

@Preview
@Composable
internal fun ChatDateSeparatorPreview() = PlataTheme { ChatDateSeparator("31 March 2023", Modifier.width(393.dp)) }

@Preview
@Composable
internal fun ChatSenderLabelPreview() = PlataTheme { ChatSenderLabel("Plata Support Team") }

@Preview
@Composable
internal fun ChatIncomingPreview() = PlataTheme {
    ChatMessage(ChatDirection.Incoming, Modifier.width(393.dp), avatar = { ChatAvatar() }) {
        ChatBubble(sampleWelcome, time = "02:00")
    }
}

@Preview
@Composable
internal fun ChatOutgoingPreview() = PlataTheme {
    ChatMessage(ChatDirection.Outgoing, Modifier.width(393.dp)) {
        ChatBubble("How do I set a spending limit?", direction = ChatDirection.Outgoing, time = "02:04")
    }
}

@Preview
@Composable
internal fun ChatOutgoingFullLinePreview() = PlataTheme {
    ChatMessage(ChatDirection.Outgoing, Modifier.width(393.dp)) {
        ChatBubble("Can I raise my limit before the trip next", direction = ChatDirection.Outgoing, time = "02:05")
    }
}

@Preview
@Composable
internal fun ChatThreadPreview() = PlataTheme {
    Column(Modifier.width(393.dp)) {
        ChatHeader("Support", "We’re here 24/7")
        Spacer(Modifier.height(50.dp))
        ChatDateSeparator("31 March 2023")
        Spacer(Modifier.height(PlataTheme.spacing.s20))
        ChatSenderLabel("Plata Support Team")
        Spacer(Modifier.height(6.dp))
        ChatMessage(ChatDirection.Incoming, avatar = { ChatAvatar() }) { ChatBubble(sampleWelcome, time = "02:00") }
        Spacer(Modifier.height(22.dp))
        ChatDateSeparator("29 June")
    }
}

@Preview
@Composable
internal fun ChatComposerPreview() = PlataTheme { ChatComposer("", {}, Modifier.width(393.dp)) }
