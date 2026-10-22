package tech.bashga.plata.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.navigation.graph.HomeRoute
import tech.bashga.plata.navigation.graph.ReferFriendRoute
import tech.bashga.plata.navigation.graph.ScheduleDeliveryIntroRoute
import tech.bashga.plata.navigation.graph.WelcomeCardRoute

/**
 * What graph files call to move around. A destination's [Presentation] decides how [navigate] shows it, so the same
 * call pushes a screen, presents a sheet or a cover.
 */
@Stable
class AppNavigator internal constructor(
    val controller: NavHostController,
    private val navigator: PlataNavigator,
    private val repository: AppRepository,
) {
    // tabs left since the last root replace, so their saved stacks are current; a replace starts every tab fresh
    private val restorableTabs = TabBarTab.entries.toMutableSet()

    fun navigate(route: Any, options: NavOptionsBuilder.() -> Unit = {}) = controller.navigate(route, options)

    /** Pops the top destination: back from a push, or dismissing a sheet or cover with nothing pushed inside. */
    fun back() {
        controller.popBackStack()
    }

    /** Dismisses the sheet or cover on top, with everything pushed inside it. [result] goes to the screen under it. */
    fun dismiss(result: Pair<String, String>? = null) {
        val stack = navigator.backStack.value
        val root = stack.lastOrNull { it.isModal } ?: return
        if (result != null) stack.getOrNull(stack.indexOf(root) - 1)?.savedStateHandle?.set(result.first, result.second)
        navigator.popBackStack(root, false)
    }

    /** Dismisses every sheet and cover, back to the base screen. */
    fun dismissAll() {
        val root = navigator.backStack.value.firstOrNull { it.isModal } ?: return
        navigator.popBackStack(root, false)
    }

    /** The tab bar: switches tabs keeping each tab's stack; the active tab pops to its root; Invite presents ReferFriend. */
    fun switchTab(tab: TabBarTab) {
        val graph = tab.graph ?: return navigate(ReferFriendRoute)
        val stack = navigator.backStack.value
        if (stack.lastOrNull()?.destination?.tab == tab) {
            val root = stack.first { it.destination.tab == tab }
            controller.popBackStack(root.destination.id, inclusive = false)
            return
        }
        stack.lastOrNull()?.destination?.tab?.let { restorableTabs += it }
        val restore = tab in restorableTabs
        controller.navigate(graph) {
            popUpTo<HomeRoute> { saveState = true }
            launchSingleTop = true
            restoreState = restore
        }
    }

    /** Replaces the whole back stack with [route]: the splash and auth hand-offs, log out, stage changes. */
    fun replaceRoot(route: Any) {
        restorableTabs.clear()
        controller.navigate(route) { popUpTo(controller.graph.id) { inclusive = true } }
    }

    /** The root for the current stage (SCREEN-MAP §2); an unfinished activation reopens its modal. */
    fun goToStageRoot() {
        val snapshot = repository.snapshot
        when (snapshot.stage) {
            Stage.NotScheduled -> replaceRoot(ScheduleDeliveryIntroRoute)
            is Stage.CardComing, Stage.Live -> replaceRoot(HomeRoute)
        }
        if (snapshot.stage == Stage.Live && snapshot.activation == Activation.InProgress) navigate(WelcomeCardRoute())
    }
}

private val NavBackStackEntry.isModal: Boolean
    get() = plataDestination.presentation != Presentation.Push

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> { error("No AppNavigator provided") }
