package tech.bashga.plata.navigation.graph

import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.CardId
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screens.delivery.DeliveryAddressScreen
import tech.bashga.plata.screens.delivery.DeliveryDetailsScreen
import tech.bashga.plata.screens.delivery.DeliveryTimeScreen
import tech.bashga.plata.screens.delivery.FeedbackScreen
import tech.bashga.plata.screens.delivery.ProductTourScreen
import tech.bashga.plata.screens.delivery.RescheduleReasonScreen
import tech.bashga.plata.screens.delivery.RewardAmbassadorScreen
import tech.bashga.plata.screens.delivery.ScheduleDeliveryIntroScreen
import tech.bashga.plata.screens.delivery.UnboxingStoriesScreen
import tech.bashga.plata.screens.delivery.WelcomeCardScreen
import tech.bashga.plata.screens.support.SUPPORT_TEL_URI

@Serializable
data object ScheduleDeliveryIntroRoute

/** [editing]: opened from DeliveryDetails › Address › Edit, so it starts on the map and returns there. */
@Serializable
data class DeliveryAddressRoute(val editing: Boolean = false)

/** [rescheduling]: presented by RescheduleReason, so Set up delivery returns to DeliveryDetails. */
@Serializable
data class DeliveryTimeRoute(val rescheduling: Boolean = false)

@Serializable
data object DeliveryDetailsRoute

@Serializable
data object CardPackagingRoute

@Serializable
data object RescheduleReasonRoute

/** [liked]: 👍 (true) or 👎 on the feedback dialog. */
@Serializable
data class FeedbackRoute(val liked: Boolean = true)

/** The root of the Activation modal; [feedbackThanks] shows the toast after Send feedback. */
@Serializable
data class WelcomeCardRoute(val feedbackThanks: Boolean = false)

@Serializable
data object RewardAmbassadorRoute

@Serializable
data object ProductTourRoute

fun NavGraphBuilder.deliveryGraph(nav: AppNavigator, repository: AppRepository) {
    screen<ScheduleDeliveryIntroRoute> {
        ScheduleDeliveryIntroScreen(onScheduleNow = { nav.navigate(DeliveryAddressRoute()) })
    }
    screen<DeliveryAddressRoute>(Presentation.Sheet) { entry ->
        val route = entry.toRoute<DeliveryAddressRoute>()
        DeliveryAddressScreen(
            editing = route.editing,
            onContinue = { if (route.editing) nav.back() else nav.navigate(DeliveryTimeRoute()) },
            onCancel = { nav.back() },
        )
    }
    screen<DeliveryTimeRoute>(Presentation.Sheet) { entry ->
        val route = entry.toRoute<DeliveryTimeRoute>()
        DeliveryTimeScreen(
            onSetUp = {
                if (route.rescheduling) {
                    nav.controller.popBackStack<DeliveryDetailsRoute>(inclusive = false)
                } else {
                    nav.replaceRoot(HomeRoute)
                }
            },
            onBack = { nav.back() },
            onClose = {
                // dismissing all also closes the DeliveryDetails underneath, which may deliver the card
                if (route.rescheduling) repository.closeDeliveryDetails()
                nav.dismissAll()
            },
        )
    }
    screen<DeliveryDetailsRoute>(Presentation.Sheet) {
        DeliveryDetailsScreen(
            onCheckPackage = { nav.navigate(CardPackagingRoute) },
            onEditAddress = { nav.navigate(DeliveryAddressRoute(editing = true)) },
            onEditTime = { nav.navigate(RescheduleReasonRoute) },
            // Home is already the root and reads the stage, so a delivered card needs no root replace
            onClose = { nav.back() },
        )
    }
    screen<CardPackagingRoute>(Presentation.Sheet) {
        val uriHandler = LocalUriHandler.current
        UnboxingStoriesScreen(
            onCallSupport = { uriHandler.openUri(SUPPORT_TEL_URI) },
            onContactSupport = { nav.navigate(SupportCoverRoute) },
            onClose = { nav.back() },
        )
    }
    screen<RescheduleReasonRoute>(Presentation.Sheet) {
        RescheduleReasonScreen(onContinue = { nav.navigate(DeliveryTimeRoute(rescheduling = true)) }, onClose = { nav.back() })
    }
    screen<FeedbackRoute>(Presentation.Sheet, backdrop = PageSheetBackdrop.Dimmed) { entry ->
        FeedbackScreen(
            liked = entry.toRoute<FeedbackRoute>().liked,
            onSent = {
                nav.back()
                nav.navigate(WelcomeCardRoute(feedbackThanks = true))
            },
            onClosed = {
                nav.back()
                nav.navigate(WelcomeCardRoute())
            },
        )
    }
    screen<WelcomeCardRoute>(Presentation.Cover) { entry ->
        WelcomeCardScreen(
            feedbackThanks = entry.toRoute<WelcomeCardRoute>().feedbackThanks,
            onExplore = { nav.navigate(RewardAmbassadorRoute) },
            onContactSupport = {
                nav.dismiss()
                nav.switchTab(TabBarTab.Support)
            },
        )
    }
    screen<RewardAmbassadorRoute> {
        RewardAmbassadorScreen(onRewarded = { nav.navigate(ProductTourRoute) }, onBack = { nav.back() })
    }
    screen<ProductTourRoute> {
        ProductTourScreen(
            onShowPin = { nav.navigate(CardDetailsRoute(CardId.Physical.name)) },
            onBack = { nav.back() },
            onFinished = { nav.dismiss() },
        )
    }
}
