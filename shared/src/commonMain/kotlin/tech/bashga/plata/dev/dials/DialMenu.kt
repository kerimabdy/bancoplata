package tech.bashga.plata.dev.dials

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/** A plain-text clipboard entry; each platform builds its own. */
internal expect fun dialClipEntry(text: String): ClipEntry

internal sealed interface DialMenuItem {
    class Choice(val label: String, val checked: Boolean = false, val destructive: Boolean = false, val onClick: () -> Unit) : DialMenuItem

    data object Divider : DialMenuItem
}

private val MenuShape = ContinuousRoundedShape(13.dp)
private val MenuDestructive = Color(0xFFFF453A)

/**
 * SwiftUI's `Menu` as DialKit shows it: a dark rounded list under (or, without room, over) its anchor, a checkmark on
 * the picked choice. Tapping a choice or anywhere outside closes it.
 */
@Composable
internal fun DialMenu(items: List<DialMenuItem>, onDismiss: () -> Unit) {
    Popup(popupPositionProvider = BelowAnchor, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Column(
            Modifier
                .widthIn(min = 220.dp, max = 280.dp)
                .dropShadow(MenuShape, Shadow(radius = 32.dp, color = DialTheme.shadow, offset = DpOffset(0.dp, 8.dp)))
                .clip(MenuShape)
                .background(MenuBackground)
                .border(1.dp, DialTheme.border, MenuShape),
        ) {
            items.forEach { item ->
                when (item) {
                    DialMenuItem.Divider -> Box(Modifier.fillMaxWidth().height(8.dp).background(Color.Black.copy(alpha = 0.25f)))
                    is DialMenuItem.Choice -> Row(
                        Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .pressable {
                                onDismiss()
                                item.onClick()
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(Modifier.width(16.dp)) { if (item.checked) DialIcon(DialGlyph.Check, 16.dp, Color.White, strokeWidth = 2.4f) }
                        PlataText(item.label, PlataTheme.typography.body, if (item.destructive) MenuDestructive else Color.White, maxLines = 1)
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private val MenuBackground = Color(0xFF2C2C2E)

private object BelowAnchor : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val gap = 6
        val x = anchorBounds.left.coerceAtMost(windowSize.width - popupContentSize.width - gap).coerceAtLeast(gap)
        val below = anchorBounds.bottom + gap
        val y = if (below + popupContentSize.height <= windowSize.height) below else (anchorBounds.top - gap - popupContentSize.height).coerceAtLeast(gap)
        return IntOffset(x, y)
    }
}
