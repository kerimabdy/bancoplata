package tech.bashga.plata.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.CardId
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.dev.DevPanelRoute
import tech.bashga.plata.dev.isDebugBuild
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.HomeTab
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screens.home.CardDetailsScreen
import tech.bashga.plata.screens.home.CashbackSetupScreen
import tech.bashga.plata.screens.home.HomeScreen
import tech.bashga.plata.screens.home.PartnersScreen
import tech.bashga.plata.screens.home.PlataPlusScreen
import tech.bashga.plata.screens.home.ProfileScreen
import tech.bashga.plata.screens.home.ReferFriendScreen

/** The pre-shell root while the card is coming, then the Home tab root. */
@Serializable
data object HomeRoute

/** [cardId] is a [CardId] name. */
@Serializable
data class CardDetailsRoute(val cardId: String)

@Serializable
data object ProfileRoute

@Serializable
data object PlataPlusRoute

@Serializable
data object CashbackSetupRoute

@Serializable
data object PartnersRoute

@Serializable
data object ReferFriendRoute

@Serializable
data object TransactionsRoute

@Serializable
data object PlataCreditoRoute

@Serializable
data object CreditPaymentRoute

@Serializable
data object MsiHomeRoute

@Serializable
data class MsiPartnerDetailRoute(val partnerId: String)

@Serializable
data object MsiPartnersRoute

fun NavGraphBuilder.homeGraph(nav: AppNavigator, repository: AppRepository) {
    navigation<HomeTab>(startDestination = HomeRoute) {
        screen<HomeRoute> {
            HomeScreen(
                onProfile = { nav.navigate(ProfileRoute) },
                onCashback = { nav.navigate(CashbackSetupRoute) },
                onMsi = { nav.navigate(MsiHomeRoute) },
                onTransactions = { nav.navigate(TransactionsRoute) },
                onCredit = { nav.navigate(PlataCreditoRoute) },
                onCard = { nav.navigate(CardDetailsRoute(it.name)) },
                onPaymentDates = { nav.navigate(CreditPaymentRoute) },
                onReferFriend = { nav.navigate(ReferFriendRoute) },
                onDeliveryDetails = { nav.navigate(DeliveryDetailsRoute) },
                onFeedback = { liked -> nav.navigate(FeedbackRoute(liked)) },
                onActivation = { nav.navigate(WelcomeCardRoute()) },
            )
        }
        screen<ProfileRoute> {
            ProfileScreen(
                onBack = nav::back,
                onPlataPlus = { nav.navigate(PlataPlusRoute) },
                onSupportChat = { nav.switchTab(TabBarTab.Support) },
                onLoggedOut = { nav.replaceRoot(PhoneNumberRoute) },
                devPanelEnabled = isDebugBuild(),
                onDevPanel = { nav.navigate(DevPanelRoute) },
            )
        }
        screen<TransactionsRoute> {
            tech.bashga.plata.screens.home.money.TransactionsScreen(onBack = nav::back)
        }
        screen<PlataCreditoRoute> {
            tech.bashga.plata.screens.home.money.PlataCreditoScreen(nav::back, { nav.navigate(CreditPaymentRoute) }, { nav.navigate(TransactionsRoute) }, { nav.navigate(CardDetailsRoute(it.name)) })
        }
        screen<CreditPaymentRoute> {
            tech.bashga.plata.screens.home.money.CreditPaymentScreen(onBack = nav::back)
        }
        screen<MsiHomeRoute> {
            tech.bashga.plata.screens.home.money.MsiHomeScreen(nav::back, { nav.navigate(MsiPartnerDetailRoute(it)) }, { nav.navigate(MsiPartnersRoute) })
        }
        screen<MsiPartnerDetailRoute> { entry ->
            tech.bashga.plata.screens.home.money.MsiPartnerDetailScreen(entry.toRoute<MsiPartnerDetailRoute>().partnerId, onBack = nav::back)
        }
        screen<MsiPartnersRoute> {
            tech.bashga.plata.screens.home.money.MsiPartnersScreen(nav::back, { nav.navigate(MsiPartnerDetailRoute(it)) })
        }
    }
    screen<CardDetailsRoute>(Presentation.Sheet) { entry ->
        CardDetailsScreen(CardId.valueOf(entry.toRoute<CardDetailsRoute>().cardId), onTransactions = { nav.navigate(TransactionsRoute) })
    }
    screen<PlataPlusRoute>(Presentation.Sheet) {
        PlataPlusScreen(onClose = nav::back)
    }
    screen<CashbackSetupRoute>(Presentation.Sheet) {
        CashbackSetupScreen(onDismiss = { nav.dismiss() }, onPartners = { nav.navigate(PartnersRoute) })
    }
    screen<PartnersRoute> {
        PartnersScreen(onBack = nav::back)
    }
    screen<ReferFriendRoute>(Presentation.Sheet) {
        ReferFriendScreen(onClose = nav::back)
    }
}
