package tech.bashga.plata.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.InvestTab
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screens.invest.InvestLessons
import tech.bashga.plata.screens.invest.InvestOnboardingScreen
import tech.bashga.plata.screens.invest.InvestmentIdeaScreen
import tech.bashga.plata.screens.invest.InvestmentsScreen
import tech.bashga.plata.screens.invest.LessonScreen
import tech.bashga.plata.screens.invest.PortfolioAllocationScreen
import tech.bashga.plata.screens.invest.PortfolioScreen
import tech.bashga.plata.screens.invest.StockDetailScreen
import tech.bashga.plata.screens.invest.StockListKind
import tech.bashga.plata.screens.invest.StockListScreen
import tech.bashga.plata.screens.invest.stockListKind

@Serializable
data object InvestmentsRoute

@Serializable
data object InvestOnboardingRoute

/** [list]: "Popular", "Crypto", "Commodities", "ShortTermBonds" or "All". */
@Serializable
data class StockListRoute(val list: String = "Popular", val searching: Boolean = false)

@Serializable
data class StockDetailRoute(val ticker: String)

@Serializable
data object PortfolioRoute

@Serializable
data object PortfolioAllocationRoute

@Serializable
data class InvestmentIdeaRoute(val ideaId: String)

@Serializable
data class LessonRoute(val lessonId: String)

fun NavGraphBuilder.investGraph(nav: AppNavigator, @Suppress("UNUSED_PARAMETER") repository: AppRepository) {
    navigation<InvestTab>(startDestination = InvestmentsRoute) {
        screen<InvestmentsRoute> {
            InvestmentsScreen(
                onSearch = { nav.navigate(StockListRoute(StockListKind.All.name, searching = true)) },
                onOpenAccount = { nav.navigate(InvestOnboardingRoute) },
                onPortfolio = { nav.navigate(PortfolioRoute) },
                onStock = { nav.navigate(StockDetailRoute(it)) },
                onSeeAll = { nav.navigate(StockListRoute(it.name)) },
                onIdea = { nav.navigate(InvestmentIdeaRoute(it)) },
                onLesson = { nav.navigate(LessonRoute(it)) },
            )
        }
        screen<StockListRoute> { entry ->
            val route = entry.toRoute<StockListRoute>()
            StockListScreen(
                stockListKind(route.list),
                route.searching,
                onStock = { nav.navigate(StockDetailRoute(it)) },
                onBack = nav::back,
            )
        }
        screen<StockDetailRoute>(hidesTabBar = true) { entry ->
            StockDetailScreen(
                entry.toRoute<StockDetailRoute>().ticker,
                onLesson = { nav.navigate(LessonRoute(InvestLessons.UNDERSTANDING_THE_CHART)) },
                onBack = nav::back,
            )
        }
        screen<PortfolioRoute> {
            PortfolioScreen(
                onAllocation = { nav.navigate(PortfolioAllocationRoute) },
                onTransferIn = { nav.navigate(BetweenAccountsRoute) },
                onStock = { nav.navigate(StockDetailRoute(it)) },
                onLesson = { nav.navigate(LessonRoute(InvestLessons.BUILD_A_PORTFOLIO)) },
                onBack = nav::back,
            )
        }
        screen<PortfolioAllocationRoute> {
            PortfolioAllocationScreen(onStock = { nav.navigate(StockDetailRoute(it)) }, onBack = nav::back)
        }
        screen<InvestmentIdeaRoute>(hidesTabBar = true) { entry ->
            InvestmentIdeaScreen(
                entry.toRoute<InvestmentIdeaRoute>().ideaId,
                onFund = { nav.navigate(StockDetailRoute(it)) },
                onBack = nav::back,
            )
        }
    }
    screen<InvestOnboardingRoute>(Presentation.Sheet) {
        InvestOnboardingScreen(onDismiss = { nav.dismiss() })
    }
    screen<LessonRoute>(Presentation.Cover) { entry ->
        LessonScreen(
            entry.toRoute<LessonRoute>().lessonId,
            onClose = { nav.dismiss() },
            onNextLesson = {
                nav.back()
                nav.navigate(LessonRoute(it))
            },
        )
    }
}
