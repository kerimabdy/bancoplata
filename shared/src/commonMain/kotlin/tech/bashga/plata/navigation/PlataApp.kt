package tech.bashga.plata.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.dev.devPanel
import tech.bashga.plata.navigation.graph.SplashRoute
import tech.bashga.plata.navigation.graph.authGraph
import tech.bashga.plata.navigation.graph.deliveryGraph
import tech.bashga.plata.navigation.graph.homeGraph
import tech.bashga.plata.navigation.graph.investGraph
import tech.bashga.plata.navigation.graph.paymentsGraph
import tech.bashga.plata.navigation.graph.supportGraph

/** Every destination of the app, one registration call per area (SCREEN-MAP §3). */
fun NavGraphBuilder.plataGraph(nav: AppNavigator, repository: AppRepository) {
    authGraph(nav, repository)
    deliveryGraph(nav, repository)
    homeGraph(nav, repository)
    paymentsGraph(nav, repository)
    investGraph(nav, repository)
    supportGraph(nav, repository)
    devPanel(nav, repository)
}

/**
 * The running app over [repository]: the root graph from [startDestination] (Splash), the tab shell and the
 * presented layers. Dev › Reset recreates the graph, which relaunches at Splash.
 */
@Composable
fun PlataApp(repository: AppRepository, modifier: Modifier = Modifier, startDestination: Any = SplashRoute) {
    val launch by repository.launches.collectAsState()
    CompositionLocalProvider(LocalAppRepository provides repository) {
        LifecycleStartEffect(repository) {
            repository.onForeground()
            onStopOrDispose {}
        }
        key(launch) {
            val navigator = remember { PlataNavigator() }
            val controller = rememberNavController(navigator)
            val nav = remember(controller) { AppNavigator(controller, navigator, repository) }
            val graph = remember(controller) { controller.createGraph(startDestination) { plataGraph(nav, repository) } }
            CompositionLocalProvider(LocalAppNavigator provides nav) {
                PlataNavHost(controller, navigator, graph, modifier, baseFrame = { top, content -> MainShell(top, nav, content) })
            }
        }
    }
}
