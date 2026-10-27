package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal actual val hasNativeTabBar: Boolean = false

@Composable
internal actual fun NativeTabBar(
    active: TabBarTab,
    inviteValue: String,
    inviteKind: TabBarItemKind,
    homeBadge: String?,
    supportBadge: String?,
    covered: () -> Float,
    onSelect: (TabBarTab) -> Unit,
    modifier: Modifier,
) = TabBar(active, modifier, inviteValue, inviteKind, homeBadge, supportBadge, onSelect)
