package tech.bashga.plata.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDestinationDsl
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.Navigator
import androidx.navigation.get
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * How a destination appears. [Push] slides in over the current layer (the base screen, or inside the sheet or cover
 * on top). [Sheet] presents an iOS page sheet; [Cover] a full-screen modal. Both start a new layer that later pushes
 * stay in, until the layer is dismissed.
 */
enum class Presentation { Push, Sheet, Cover }

/**
 * The one navigator behind every Plata destination, so a single back stack holds pushes, sheets and covers and
 * [PlataNavHost] can draw presenters under their sheets.
 */
@Navigator.Name("Plata")
class PlataNavigator : Navigator<PlataNavigator.Destination>() {

    internal val backStack get() = state.backStack

    internal val transitionsInProgress get() = state.transitionsInProgress

    override fun navigate(entries: List<NavBackStackEntry>, navOptions: NavOptions?, navigatorExtras: Extras?) {
        entries.forEach { state.push(it) }
    }

    override fun createDestination(): Destination = Destination(this, Presentation.Push, null, false, false) {}

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
        // the entry under the popped ones is held in STARTED while transitioning; release it so it can resume
        val popIndex = state.transitionsInProgress.value.indexOf(popUpTo)
        state.transitionsInProgress.value.forEachIndexed { index, entry -> if (index > popIndex) onTransitionComplete(entry) }
    }

    internal fun onTransitionComplete(entry: NavBackStackEntry) = state.markTransitionComplete(entry)

    /** [backdrop] null picks the presenter's rule: Dimmed over a cover, Stacked otherwise. */
    @NavDestination.ClassType(Composable::class)
    class Destination(
        navigator: PlataNavigator,
        val presentation: Presentation,
        val backdrop: PageSheetBackdrop?,
        val hidesTabBar: Boolean,
        val expands: Boolean,
        internal val content: @Composable (NavBackStackEntry) -> Unit,
    ) : NavDestination(navigator)
}

@NavDestinationDsl
class PlataDestinationBuilder(
    private val plataNavigator: PlataNavigator,
    route: KClass<*>,
    typeMap: Map<KType, NavType<*>>,
    private val presentation: Presentation,
    private val backdrop: PageSheetBackdrop?,
    private val hidesTabBar: Boolean,
    private val expands: Boolean,
    private val content: @Composable (NavBackStackEntry) -> Unit,
) : NavDestinationBuilder<PlataNavigator.Destination>(plataNavigator, route, typeMap) {
    override fun instantiateDestination() = PlataNavigator.Destination(plataNavigator, presentation, backdrop, hidesTabBar, expands, content)
}

/**
 * Registers the destination for route class [T]. Read the route's arguments with `entry.toRoute<T>()`. An [expands]
 * push doesn't slide: the page grows out of the card it shares elements with (`sharedTransitionBounds`) and pops back in.
 *
 * ```
 * screen<CardDetailsRoute>(Presentation.Sheet) { entry -> CardDetailsScreen(entry.toRoute(), …) }
 * ```
 */
inline fun <reified T : Any> NavGraphBuilder.screen(
    presentation: Presentation = Presentation.Push,
    backdrop: PageSheetBackdrop? = null,
    hidesTabBar: Boolean = false,
    expands: Boolean = false,
    typeMap: Map<KType, NavType<*>> = emptyMap(),
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) {
    destination(PlataDestinationBuilder(provider[PlataNavigator::class], T::class, typeMap, presentation, backdrop, hidesTabBar, expands, content))
}

internal val NavBackStackEntry.plataDestination: PlataNavigator.Destination
    get() = destination as PlataNavigator.Destination
