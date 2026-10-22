package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class TabBarTab { Home, Payments, Invest, Support, Invite }

// the material starts at y 769: items (46) sit 3 below its top edge, then the home-indicator band
private val TabBarItemTop = 3.dp

/**
 * The five-tab bar: Home, Payments, Invest, Support and the Invite tab, which shows [inviteValue] as text
 * ([TabBarItemKind.Value], "$200") or behind a flame ([TabBarItemKind.IconValue], "1,500"). [homeBadge] puts a red count
 * on Home (9f1aa9fc), [supportBadge] on Support (b7b04c52). The bar pads the bottom safe area; the source's backdrop blur has no Compose equivalent, so the
 * material is surface/tabbar at 90% only. The home indicator is the system's and is not drawn.
 */
@Composable
fun TabBar(
    active: TabBarTab,
    modifier: Modifier = Modifier,
    inviteValue: String = "$200",
    inviteKind: TabBarItemKind = TabBarItemKind.Value,
    homeBadge: String? = null,
    supportBadge: String? = null,
    onSelect: (TabBarTab) -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(PlataTheme.colors.surfaceTabbar)
            .padding(top = TabBarItemTop),
    ) {
        Row(Modifier.fillMaxWidth()) {
            val item = Modifier.weight(1f)
            TabBarItem("Home", item, active = active == TabBarTab.Home, icon = PlataIcons.House, badge = homeBadge, onClick = { onSelect(TabBarTab.Home) })
            TabBarItem("Payments", item, active = active == TabBarTab.Payments, icon = PlataIcons.DoubleChevron, onClick = { onSelect(TabBarTab.Payments) })
            TabBarItem("Invest", item, active = active == TabBarTab.Invest, icon = PlataIcons.Bars, onClick = { onSelect(TabBarTab.Invest) })
            TabBarItem("Support", item, active = active == TabBarTab.Support, icon = PlataIcons.Chat, badge = supportBadge, onClick = { onSelect(TabBarTab.Support) })
            TabBarItem(
                "Invite",
                item,
                kind = inviteKind,
                active = active == TabBarTab.Invite,
                icon = if (inviteKind == TabBarItemKind.IconValue) PlataIcons.Flame else null,
                value = inviteValue,
                onClick = { onSelect(TabBarTab.Invite) },
            )
        }
        Spacer(Modifier.height(PlataSafeArea.current.bottom))
    }
}

@Preview
@Composable
internal fun TabBarHomePreview() = PlataTheme { Box(Modifier.width(393.dp)) { TabBar(TabBarTab.Home) } }

@Preview
@Composable
internal fun TabBarPaymentsPreview() = PlataTheme { Box(Modifier.width(393.dp)) { TabBar(TabBarTab.Payments) } }

@Preview
@Composable
internal fun TabBarInvestPreview() = PlataTheme { Box(Modifier.width(393.dp)) { TabBar(TabBarTab.Invest) } }

@Preview
@Composable
internal fun TabBarSupportPreview() = PlataTheme {
    Box(Modifier.width(393.dp)) { TabBar(TabBarTab.Support, inviteValue = "1,500", inviteKind = TabBarItemKind.IconValue) }
}

@Preview
@Composable
internal fun TabBarBadgePreview() = PlataTheme { Box(Modifier.width(393.dp)) { TabBar(TabBarTab.Home, homeBadge = "2") } }
