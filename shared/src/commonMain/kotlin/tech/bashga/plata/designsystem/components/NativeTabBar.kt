package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** True where the system tab bar replaces [TabBar]: iOS 26 and later, for its Liquid Glass. */
internal expect val hasNativeTabBar: Boolean

/**
 * The system tab bar with [TabBar]'s items. It draws above all Compose content, so it fades out by [covered] (0–1)
 * while a sheet or overlay is over it. [modifier] sizes it: the row plus the bottom safe area.
 */
@Composable
internal expect fun NativeTabBar(
    active: TabBarTab,
    inviteValue: String,
    inviteKind: TabBarItemKind,
    homeBadge: String?,
    supportBadge: String?,
    covered: () -> Float,
    onSelect: (TabBarTab) -> Unit,
    modifier: Modifier = Modifier,
)
