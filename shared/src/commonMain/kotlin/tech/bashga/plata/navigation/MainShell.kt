package tech.bashga.plata.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.InviteOffer
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Stage
import tech.bashga.plata.data.homeBadge
import tech.bashga.plata.data.inviteOffer
import tech.bashga.plata.designsystem.components.TabBar
import tech.bashga.plata.designsystem.components.TabBarItemKind
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea

/** Nested graph routes, one per tab with a page of its own. Invite has none: it presents ReferFriend. */
@Serializable
data object HomeTab

@Serializable
data object PaymentsTab

@Serializable
data object InvestTab

@Serializable
data object SupportTab

internal val TabBarTab.graph: Any?
    get() = when (this) {
        TabBarTab.Home -> HomeTab
        TabBarTab.Payments -> PaymentsTab
        TabBarTab.Invest -> InvestTab
        TabBarTab.Support -> SupportTab
        TabBarTab.Invite -> null
    }

internal val NavDestination.tab: TabBarTab?
    get() = hierarchy.firstNotNullOfOrNull { node ->
        when {
            node.hasRoute(HomeTab::class) -> TabBarTab.Home
            node.hasRoute(PaymentsTab::class) -> TabBarTab.Payments
            node.hasRoute(InvestTab::class) -> TabBarTab.Invest
            node.hasRoute(SupportTab::class) -> TabBarTab.Support
            else -> null
        }
    }

// the bar's 3pt top padding plus its 46 items, above the home-indicator inset
private val TabBarRowHeight = 49.dp

/** The tab bar data the repository drives: the invite item follows the month, the badges count tiles and messages. */
data class TabBarData(val inviteValue: String, val inviteKind: TabBarItemKind, val homeBadge: String?, val supportBadge: String?)

val AppSnapshot.tabBarData: TabBarData
    get() = TabBarData(
        inviteValue = if (month.inviteOffer == InviteOffer.Standard) "$200" else "1,500",
        inviteKind = if (month.inviteOffer == InviteOffer.Standard) TabBarItemKind.Value else TabBarItemKind.IconValue,
        homeBadge = homeBadge.takeIf { it > 0 }?.toString(),
        supportBadge = supportUnread.takeIf { it > 0 }?.toString(),
    )

/**
 * Draws [content] full screen with the tab bar over its bottom edge when [active] is set. Content sees a safe-area
 * bottom that includes the bar, like iOS, so it pads its own scroll content and CTAs.
 */
@Composable
fun TabShell(active: TabBarTab?, data: TabBarData, onSelect: (TabBarTab) -> Unit, content: @Composable () -> Unit) {
    if (active == null) {
        content()
        return
    }
    val safeArea = PlataSafeArea.current
    Box(Modifier.fillMaxSize()) {
        ProvideSafeArea(safeArea.copy(bottom = safeArea.bottom + TabBarRowHeight)) { content() }
        TabBar(
            active,
            Modifier.align(Alignment.BottomCenter),
            inviteValue = data.inviteValue,
            inviteKind = data.inviteKind,
            homeBadge = data.homeBadge,
            supportBadge = data.supportBadge,
            onSelect = onSelect,
        )
    }
}

/** MainShell: the tab bar shows on tab destinations once the card is live, except on `hidesTabBar` routes. */
@Composable
internal fun MainShell(top: NavBackStackEntry, nav: AppNavigator, content: @Composable () -> Unit) {
    val snapshot by LocalAppRepository.current.state.collectAsState()
    val tab = top.destination.tab
    val visible = tab != null && snapshot.stage == Stage.Live && !top.plataDestination.hidesTabBar
    TabShell(if (visible) tab else null, snapshot.tabBarData, nav::switchTab, content)
}
