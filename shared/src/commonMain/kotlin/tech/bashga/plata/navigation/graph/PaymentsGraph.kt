package tech.bashga.plata.navigation.graph

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.TransferReceipt
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.PaymentsTab
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screens.payments.BetweenAccountsScreen
import tech.bashga.plata.screens.payments.FavoritesScreen
import tech.bashga.plata.screens.payments.PaymentsScreen
import tech.bashga.plata.screens.payments.TransferAmountScreen
import tech.bashga.plata.screens.payments.TransferMethod
import tech.bashga.plata.screens.payments.TransferPurpose
import tech.bashga.plata.screens.payments.TransferRecipient
import tech.bashga.plata.screens.payments.TransferRecipientScreen
import tech.bashga.plata.screens.payments.TransferResultScreen
import tech.bashga.plata.screens.payments.digits
import tech.bashga.plata.screens.payments.outcome

@Serializable
data object PaymentsRoute

@Serializable
data object FavoritesRoute

/** [purpose]: "Send" or "AddFavorite"; [method]: "Clabe", "Card" or "Phone". */
@Serializable
data class TransferRecipientRoute(val purpose: String = "Send", val method: String = "Clabe")

/** [number] is the CLABE (18 digits) or card number (16 digits). */
@Serializable
data class TransferAmountRoute(val recipient: String, val number: String)

@Serializable
data object BetweenAccountsRoute

/**
 * A [TransferReceipt], flattened: amounts in cents of the currency, balances `ownBalance` in MXN cents. [account] is the
 * line under the name ("Santander •• 4821"), [bank] a `MethodCardBank` name or empty, [cashBefore] the Inversiones cash
 * before a between-accounts move (USD cents).
 */
@Serializable
data class TransferResultRoute(
    val recipient: String,
    val amount: Long,
    val usd: Boolean,
    val commission: Long,
    val balanceBefore: Long,
    val balanceAfter: Long,
    val account: String = "",
    val bank: String = "",
    val cashBefore: Long = 0,
)

/** The result key [FavoritesRoute] and [TransferRecipientRoute] hand back to Payments when a favorite was added. */
const val FAVORITE_ADDED = "favoriteAdded"

fun NavGraphBuilder.paymentsGraph(nav: AppNavigator, repository: AppRepository) {
    navigation<PaymentsTab>(startDestination = PaymentsRoute) {
        screen<PaymentsRoute> { entry ->
            val added by entry.savedStateHandle.getStateFlow<String?>(FAVORITE_ADDED, null).collectAsState()
            PaymentsScreen(
                addedFavoriteId = added,
                onAddedFavoriteShown = { entry.savedStateHandle[FAVORITE_ADDED] = null },
                onTransfer = { method -> nav.navigate(TransferRecipientRoute(TransferPurpose.Send.name, method.name)) },
                onEditFavorites = { nav.navigate(FavoritesRoute) },
            )
        }
    }
    screen<FavoritesRoute>(Presentation.Sheet) {
        FavoritesScreen(
            onClose = nav::back,
            onAddTransfer = {
                nav.back()
                nav.navigate(TransferRecipientRoute(TransferPurpose.AddFavorite.name, TransferMethod.Card.name))
            },
        )
    }
    screen<TransferRecipientRoute>(Presentation.Sheet) { entry ->
        val route = entry.toRoute<TransferRecipientRoute>()
        TransferRecipientScreen(
            purpose = TransferPurpose.valueOf(route.purpose),
            method = TransferMethod.valueOf(route.method),
            onContinue = { recipient -> nav.navigate(TransferAmountRoute(recipient.name, recipient.digits)) },
            onFavoriteAdded = { id -> nav.dismiss(result = FAVORITE_ADDED to id) },
            onCancel = { nav.dismiss() },
        )
    }
    screen<TransferAmountRoute> { entry ->
        val route = entry.toRoute<TransferAmountRoute>()
        val method = if (route.number.length == TransferMethod.Card.digits) TransferMethod.Card else TransferMethod.Clabe
        TransferAmountScreen(
            recipient = TransferRecipient(route.recipient, route.number, method),
            onTransferred = { result ->
                nav.dismiss()
                nav.navigate(result)
            },
            onBack = nav::back,
            onCancel = { nav.dismiss() },
        )
    }
    screen<BetweenAccountsRoute>(Presentation.Sheet) {
        BetweenAccountsScreen(
            onTransferred = { result ->
                nav.dismiss()
                nav.navigate(result)
            },
            onCancel = { nav.dismiss() },
        )
    }
    // the result replaces the transfer sheet full screen, over the tab root that started it (372e6fd9, 705c4f5e)
    screen<TransferResultRoute>(Presentation.Cover) { entry ->
        TransferResultScreen(entry.toRoute<TransferResultRoute>().outcome, onDone = { nav.dismiss() })
    }
}
