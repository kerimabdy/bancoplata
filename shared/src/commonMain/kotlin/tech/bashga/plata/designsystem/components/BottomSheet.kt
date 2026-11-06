package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousTopRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.DoubleChevronDuotone
import tech.bashga.plata.designsystem.icons.PhoneHandset
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.swipeToDismiss

/** [Leading]: 20 bold title on the left (0b2e699a, 99ceac12). [Center]: 17 semibold title centred (78f0c567). */
enum class BottomSheetAlign { Leading, Center }

// the centred headline's line box starts 18 below the sheet top (78f0c567)
private val CenterTitleTop = 18.dp
// the bold leading title's line box ends 18 above the first row (0b2e699a)
private val LeadingTitleGap = 18.dp

/**
 * Modal sheet anchored to the screen bottom: white, top radius 40, 20 side padding, [title] then [content]
 * ([contentSpacing] apart: 4 between the 56 ListRows on 0b2e699a, 20 between fields on 78f0c567), then [footer]
 * (Button primary large) 20 below. The sheet ends at the screen edge: the bottom padding is the safe area plus 20
 * under a footer, or plus 40 under bare rows (0b2e699a). Put it in a [ModalScrim] aligned to the bottom.
 */
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    title: String? = null,
    align: BottomSheetAlign = BottomSheetAlign.Leading,
    titleTop: Dp = if (align == BottomSheetAlign.Leading) PlataTheme.spacing.s40 else CenterTitleTop,
    titleGap: Dp = if (align == BottomSheetAlign.Leading) LeadingTitleGap else PlataTheme.spacing.s20,
    contentSpacing: Dp = 0.dp,
    footer: (@Composable () -> Unit)? = null,
    bottomPadding: Dp = if (footer != null) PlataTheme.spacing.s20 else PlataTheme.spacing.s40,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceCard, ContinuousTopRoundedShape(PlataTheme.radii.sheet))
            .padding(horizontal = PlataTheme.spacing.s20)
            .padding(top = if (title != null) titleTop else PlataTheme.spacing.s20, bottom = bottomPadding + PlataSafeArea.current.bottom),
    ) {
        if (title != null) {
            if (align == BottomSheetAlign.Leading) {
                PlataText(title, typography.title3, colors.textPrimary)
            } else {
                PlataText(title, typography.headline, colors.textPrimary, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(titleGap))
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(contentSpacing), content = content)
        if (footer != null) {
            Spacer(Modifier.height(PlataTheme.spacing.s20))
            footer()
        }
    }
}

/**
 * The 50% black backdrop behind a BottomSheet or Dialog, filling its parent. Tapping it calls [onDismiss];
 * [content] is laid out in a Box ([contentAlignment] BottomCenter for sheets and dialogs), and bottom content can be
 * dragged down to dismiss it like a page sheet.
 */
@Composable
fun ModalScrim(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = LocalModalMotion.current?.takeIf { contentAlignment == Alignment.BottomCenter }
    // set while composing, so the host's layer already skips its fade on the first visible frame
    if (motion != null) motion.slides = true
    val progress = motion?.progress ?: { 1f }
    val back = motion?.back ?: { 0f }
    // let taps through while the scrim fades out, so the screen under it answers straight away
    val leaving = motion?.leaving?.invoke() == true
    // reopened while still fading out, the same scrim comes back: start it undragged
    val presentation = motion?.presentation?.invoke()
    var drag by remember(presentation) { mutableFloatStateOf(0f) }
    val bottom = contentAlignment == Alignment.BottomCenter
    Box(
        modifier
            .fillMaxSize()
            .then(if (leaving) Modifier else Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)),
        contentAlignment = contentAlignment,
    ) {
        Box(Modifier.matchParentSize().graphicsLayer { alpha = progress() * (1f - drag) }.background(PlataTheme.colors.scrim))
        // swallow taps on the sheet itself so they don't reach the scrim
        Box(
            Modifier
                .graphicsLayer {
                    translationY = (1f - progress()) * size.height
                    scaleX = 1f - BackGestureShrink * back()
                    scaleY = scaleX
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .then(if (bottom) Modifier.swipeToDismiss(onDismiss, { drag = it }, resetKey = presentation, dismissed = motion?.leaving) else Modifier)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            content = content,
        )
    }
}

/**
 * Provided by the overlay host so a bottom [ModalScrim] slides its content up instead of the whole layer fading, and
 * shrinks it toward the bottom edge with Android's predictive [back] gesture (0–1).
 */
class ModalMotion(
    val progress: () -> Float,
    val back: () -> Float = { 0f },
    val leaving: () -> Boolean = { false },
    val presentation: () -> Int = { 0 },
) {
    var slides = false
        internal set
}

/** How much a sheet shrinks at the end of a predictive back gesture. */
internal const val BackGestureShrink = 0.1f

val LocalModalMotion = staticCompositionLocalOf<ModalMotion?> { null }

@Preview
@Composable
internal fun BottomSheetLeadingPreview() = PlataTheme {
    BottomSheet(Modifier.width(393.dp), title = "Transfer", contentSpacing = PlataTheme.spacing.s4) {
        ListRow(
            "By CLABE",
            showChevron = true,
            onClick = {},
            horizontalPadding = 0.dp,
            leading = { IconCircle(PlataIcons.DoubleChevronDuotone, IconCircleSize.S40) },
        )
        ListRow(
            "By card number",
            showChevron = true,
            onClick = {},
            horizontalPadding = 0.dp,
            leading = { IconCircle(PlataIcons.Card, IconCircleSize.S40) },
        )
        ListRow(
            "By phone number to Plata user",
            showChevron = true,
            onClick = {},
            horizontalPadding = 0.dp,
            leading = { IconCircle(PlataIcons.PhoneHandset, IconCircleSize.S40) },
        )
    }
}

@Preview
@Composable
internal fun BottomSheetCenterPreview() = PlataTheme {
    BottomSheet(
        Modifier.width(393.dp),
        title = "Delivery address",
        align = BottomSheetAlign.Center,
        contentSpacing = PlataTheme.spacing.s20,
        footer = { Button("Continue", {}, Modifier.fillMaxWidth()) },
    ) {
        TextField("Calle Durango 263, Roma Norte", {}, Modifier.fillMaxWidth(), placeholder = "Address", surface = TextFieldSurface.Grey, alwaysShowLabel = true)
        TextField("", {}, Modifier.fillMaxWidth(), placeholder = "Details (optional)", surface = TextFieldSurface.Grey)
    }
}
