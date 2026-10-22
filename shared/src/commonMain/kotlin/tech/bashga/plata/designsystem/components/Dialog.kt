package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_dialog_delivered
import plata.shared.generated.resources.raster_dialog_heart
import plata.shared.generated.resources.raster_dialog_success
import plata.shared.generated.resources.raster_dialog_terminal
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.CalendarGrid
import tech.bashga.plata.designsystem.icons.CloseXBold
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [Inset]: artwork centred under a close button (6f8f3145). [Bleed]: a photo across the top edge, no close (764b45eb). */
enum class DialogMedia { Inset, Bleed }

/** [Secondary]: the thin grey cross (6f8f3145, b95e6964). [Primary]: the heavier ink cross (ec4a6b30). */
enum class DialogCloseTone { Secondary, Primary }

// 6f8f3145: artwork top 66 below the dialog top, title 31 under the artwork; 764b45eb: title 26 under the photo
private val InsetMediaTop = 66.dp
private val InsetMediaGap = 31.dp
private val BleedMediaGap = 26.dp

// the close disc sits 24 from the top and 20 from the right edge with a 15 cross (6f8f3145); 20–21 from the top on ec4a6b30, b95e6964
private val CloseTop = 24.dp
private val CloseGlyph = 15.dp
// body → Button 48 (6f8f3145), extras → Button 40 (764b45eb)
private val ActionsGapAfterBody = 48.dp

// dialog bottom sits 4 into the home-indicator inset (6f8f3145: bottom at y 821.7 of 852)
private val DialogBottomOverlap = 4.dp

/**
 * Centred alert card, r40 (iOS continuous), white, 40 side and bottom padding: [media] slot, 20 bold [title],
 * 17/20 [body] [textGap] below (20; 12 on ec4a6b30), [extras] (InsetRow value on 764b45eb, thumbs pair on b95e6964)
 * [extrasGap] below, then [actions] [actionsGap] below (48 after the body, 40 after extras): the primary Button and
 * optionally a [DialogSecondaryAction] 12 under it (ec4a6b30).
 * With no [actions] the dialog ends [bottomPadding] under the last block (the no-actions variant: 52 under the thumbs on b95e6964).
 * Title line breaks are the caller's ("We opened your⏎investing account!" fits on one line at 297).
 * [onClose] shows the 32 close button (Inset only; [closeTone] Primary for the ink cross on ec4a6b30).
 * Place it with [DialogPlacement] inside a [ModalScrim].
 */
@Composable
fun Dialog(
    title: AnnotatedString?,
    modifier: Modifier = Modifier,
    body: AnnotatedString? = null,
    media: DialogMedia = DialogMedia.Inset,
    onClose: (() -> Unit)? = null,
    mediaContent: (@Composable () -> Unit)? = null,
    contentTop: Dp = if (mediaContent != null && media == DialogMedia.Inset) InsetMediaTop else PlataTheme.spacing.s40,
    mediaGap: Dp = if (media == DialogMedia.Bleed) BleedMediaGap else InsetMediaGap,
    textGap: Dp = PlataTheme.spacing.s20,
    extrasGap: Dp = PlataTheme.spacing.s40,
    actionsGap: Dp = Dp.Unspecified,
    bottomPadding: Dp = PlataTheme.spacing.s40,
    closeTop: Dp = CloseTop,
    closeTone: DialogCloseTone = DialogCloseTone.Secondary,
    extras: (@Composable ColumnScope.() -> Unit)? = null,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val shape = ContinuousRoundedShape(PlataTheme.radii.sheet)
    val side = PlataTheme.spacing.s40
    Box(modifier.fillMaxWidth().clip(shape).background(colors.surfaceCard, shape)) {
        Column(Modifier.fillMaxWidth().padding(bottom = bottomPadding), horizontalAlignment = Alignment.CenterHorizontally) {
            if (mediaContent != null && media == DialogMedia.Bleed) {
                Box(Modifier.fillMaxWidth()) { mediaContent() }
            } else {
                Spacer(Modifier.height(contentTop))
                if (mediaContent != null) mediaContent()
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = side), horizontalAlignment = Alignment.CenterHorizontally) {
                if (mediaContent != null && (title != null || body != null)) Spacer(Modifier.height(mediaGap))
                if (title != null) PlataText(title, typography.title3, colors.textPrimary, textAlign = TextAlign.Center)
                if (body != null) {
                    if (title != null) Spacer(Modifier.height(textGap))
                    PlataText(body, typography.bodyCompact, colors.textPrimary, textAlign = TextAlign.Center)
                }
                if (extras != null) {
                    Spacer(Modifier.height(extrasGap))
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, content = extras)
                }
                if (actions != null) {
                    val gap = if (actionsGap != Dp.Unspecified) actionsGap else if (extras != null) PlataTheme.spacing.s40 else ActionsGapAfterBody
                    Spacer(Modifier.height(gap))
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = actions,
                    )
                }
            }
        }
        if (onClose != null && media == DialogMedia.Inset) {
            val primary = closeTone == DialogCloseTone.Primary
            IconButton(
                if (primary) PlataIcons.CloseXBold else PlataIcons.CloseX,
                onClose,
                Modifier.align(Alignment.TopEnd).padding(top = closeTop, end = PlataTheme.spacing.s20),
                style = if (primary) IconButtonStyle.Subtle else IconButtonStyle.Grey,
                onCard = true,
                glyphSize = if (primary) PlataTheme.sizes.iconSmall else CloseGlyph,
                contentDescription = "Close",
            )
        }
    }
}

@Composable
fun Dialog(
    title: String?,
    modifier: Modifier = Modifier,
    body: String? = null,
    media: DialogMedia = DialogMedia.Inset,
    onClose: (() -> Unit)? = null,
    mediaContent: (@Composable () -> Unit)? = null,
    contentTop: Dp = if (mediaContent != null && media == DialogMedia.Inset) InsetMediaTop else PlataTheme.spacing.s40,
    mediaGap: Dp = if (media == DialogMedia.Bleed) BleedMediaGap else InsetMediaGap,
    textGap: Dp = PlataTheme.spacing.s20,
    extrasGap: Dp = PlataTheme.spacing.s40,
    actionsGap: Dp = Dp.Unspecified,
    bottomPadding: Dp = PlataTheme.spacing.s40,
    closeTop: Dp = CloseTop,
    closeTone: DialogCloseTone = DialogCloseTone.Secondary,
    extras: (@Composable ColumnScope.() -> Unit)? = null,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
) = Dialog(
    title?.let(::AnnotatedString), modifier, body?.let(::AnnotatedString), media, onClose, mediaContent,
    contentTop, mediaGap, textGap, extrasGap, actionsGap, bottomPadding, closeTop, closeTone, extras, actions,
)

/** Scrim with the dialog 8 from the screen sides and its bottom just inside the home-indicator inset (6f8f3145, 764b45eb, b95e6964). */
@Composable
fun DialogPlacement(onDismiss: () -> Unit, modifier: Modifier = Modifier, dialog: @Composable () -> Unit) {
    val inset = PlataTheme.spacing.s8
    val bottom = (PlataSafeArea.current.bottom - DialogBottomOverlap).coerceAtLeast(inset)
    ModalScrim(onDismiss, modifier) { Box(Modifier.padding(start = inset, end = inset, bottom = bottom)) { dialog() } }
}

/** Plain secondary action under the dialog Button: 24 ink glyph, 8 gap, 17 regular text/primary, 48 high (ec4a6b30). */
@Composable
fun DialogSecondaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.height(PlataTheme.sizes.buttonMedium).pressable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, size = PlataTheme.sizes.iconMedium)
        PlataText(label, PlataTheme.typography.body, PlataTheme.colors.textPrimary, maxLines = 1)
    }
}

@Composable
private fun DialogPrimaryAction(label: String) {
    Button(label, {}, Modifier.fillMaxWidth())
}

@Preview
@Composable
internal fun DialogInsetPreview() = PlataTheme {
    Dialog(
        "We opened your\ninvesting account!",
        Modifier.width(377.dp),
        body = "Now you can buy your first stocks.\nGood luck and have fun!",
        onClose = {},
        mediaContent = { Image(painterResource(Res.drawable.raster_dialog_success), null, Modifier.size(82.dp, 87.dp)) },
        actions = { DialogPrimaryAction("Start investing") },
    )
}

@Preview
@Composable
internal fun DialogBleedPreview() = PlataTheme {
    val bold = SpanStyle(fontWeight = FontWeight.SemiBold)
    Dialog(
        AnnotatedString("MSI is available\non Clip terminals"),
        Modifier.width(377.dp),
        body = buildAnnotatedString {
            append("Purchase at ")
            withStyle(bold) { append("any stores") }
            append(" that has\nthe terminal")
        },
        media = DialogMedia.Bleed,
        mediaContent = {
            Image(
                painterResource(Res.drawable.raster_dialog_terminal),
                null,
                Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop,
            )
        },
        extras = { InsetRow("Minimum amount", "$100", Modifier.fillMaxWidth(), trailing = InsetRowTrailing.Value) },
        actions = { DialogPrimaryAction("Great") },
    )
}

@Preview
@Composable
internal fun DialogSecondaryActionPreview() = PlataTheme {
    Dialog(
        "Delivery successfully arranged!",
        Modifier.width(377.dp),
        body = "An ambassador will call you before the\ndelivery. Don’t forget your INE!",
        onClose = {},
        mediaContent = { Image(painterResource(Res.drawable.raster_dialog_delivered), null, Modifier.size(85.dp, 90.33.dp)) },
        contentTop = 62.dp,
        mediaGap = 30.dp,
        textGap = PlataTheme.spacing.s12,
        actionsGap = PlataTheme.spacing.s40,
        closeTop = 21.dp,
        closeTone = DialogCloseTone.Primary,
        actions = {
            DialogPrimaryAction("Done")
            DialogSecondaryAction("Add to calendar", {}, icon = PlataIcons.CalendarGrid)
        },
    )
}

@Preview
@Composable
internal fun DialogNoActionsPreview() = PlataTheme {
    // b95e6964 sets its blocks tighter than the other dialogs
    Dialog(
        "How was the meeting with\nAmbassador?",
        Modifier.width(377.dp),
        body = "Thumbs up if it went well",
        onClose = {},
        mediaContent = { Image(painterResource(Res.drawable.raster_dialog_heart), null, Modifier.size(82.67.dp, 83.33.dp)) },
        contentTop = 55.dp,
        mediaGap = 29.dp,
        textGap = PlataTheme.spacing.s4,
        extrasGap = 33.dp,
        bottomPadding = 52.dp,
        closeTop = PlataTheme.spacing.s20,
        extras = {
            Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s40)) {
                IconCircle(PlataIcons.ThumbsDown, IconCircleSize.S80, tint = PlataTheme.colors.textSecondary)
                IconCircle(PlataIcons.ThumbsUp, IconCircleSize.S80, tone = IconCircleTone.Success)
            }
        },
    )
}
