package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class TabBarItemKind { Icon, Value, IconValue }

/**
 * One 46-tall tab: a 28 glyph ([TabBarItemKind.Icon]), a 17pt semibold value such as "$200" (Value), or a 20 flame plus
 * value "1,500" (IconValue) over a 10pt label. Width comes from the TabBar (393 / 5). [badge] rings a red count onto the
 * glyph's top-right corner (9f1aa9fc).
 */
@Composable
fun TabBarItem(
    label: String,
    modifier: Modifier = Modifier,
    kind: TabBarItemKind = TabBarItemKind.Icon,
    active: Boolean = false,
    icon: ImageVector? = null,
    value: String? = null,
    badge: String? = null,
    onClick: () -> Unit = {},
) {
    val colors = PlataTheme.colors
    val sizes = PlataTheme.sizes
    val color = if (active) colors.brandPrimary else colors.textSecondary
    Column(
        modifier.fillMaxWidth().height(46.dp).pressable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(sizes.iconLarge).wrapContentWidth(unbounded = true), contentAlignment = Alignment.Center) {
            when (kind) {
                TabBarItemKind.Icon -> Box(Modifier.size(sizes.iconLarge)) {
                    if (icon != null) Icon(icon, size = sizes.iconLarge, tint = color)
                    if (badge != null) {
                        Badge(badge, Modifier.offset(20.dp, (-2).dp), size = BadgeSize.Small, outline = colors.surfaceBackground)
                    }
                }
                TabBarItemKind.Value -> PlataText(value.orEmpty(), PlataTheme.typography.headline, color, maxLines = 1)
                TabBarItemKind.IconValue -> Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon ?: PlataIcons.Flame, size = sizes.iconSmall, tint = color)
                    PlataText(value.orEmpty(), PlataTheme.typography.headline, color, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        PlataText(label, PlataTheme.typography.tab, color, maxLines = 1)
    }
}

@Preview
@Composable
internal fun TabBarItemActiveIconPreview() = PlataTheme {
    TabBarItem("Home", active = true, icon = PlataIcons.House)
}

@Preview
@Composable
internal fun TabBarItemInactiveIconPreview() = PlataTheme {
    TabBarItem("Payments", icon = PlataIcons.DoubleChevron)
}

@Preview
@Composable
internal fun TabBarItemValuePreview() = PlataTheme {
    TabBarItem("Invite", kind = TabBarItemKind.Value, value = "$200")
}

@Preview
@Composable
internal fun TabBarItemIconValuePreview() = PlataTheme {
    TabBarItem("Invite", kind = TabBarItemKind.IconValue, value = "1,500")
}

@Preview
@Composable
internal fun TabBarItemBadgePreview() = PlataTheme {
    TabBarItem("Home", active = true, icon = PlataIcons.House, badge = "2")
}
