package tech.bashga.plata.navigation.graph

import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AuthStep
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screens.auth.EnableFaceIdScreen
import tech.bashga.plata.screens.auth.LockScreen
import tech.bashga.plata.screens.auth.PasscodeScreen
import tech.bashga.plata.screens.auth.PhoneNumberScreen
import tech.bashga.plata.screens.auth.ScanCardQrScreen

@Serializable
data object LockRoute

@Serializable
data object PhoneNumberRoute

@Serializable
data object ScanCardQrRoute

@Serializable
data object PasscodeRoute

@Serializable
data object EnableFaceIdRoute

/** Where a launch lands, behind the system splash. */
fun AppRepository.launchRoute(): Any = if (snapshot.session.loggedIn) LockRoute else PhoneNumberRoute

private fun AppNavigator.afterOtp(step: AuthStep) = navigate(
    when (step) {
        AuthStep.ScanCardQr -> ScanCardQrRoute
        AuthStep.CreatePasscode -> PasscodeRoute
        AuthStep.EnableFaceId -> EnableFaceIdRoute
    },
)

fun NavGraphBuilder.authGraph(nav: AppNavigator, repository: AppRepository) {
    screen<LockRoute> {
        LockScreen(
            onUnlocked = nav::goToStageRoot,
            onLoggedOut = { nav.replaceRoot(PhoneNumberRoute) },
        )
    }
    screen<PhoneNumberRoute> {
        PhoneNumberScreen(
            onOtpConfirmed = nav::afterOtp,
            onPrivacyNotice = {},
        )
    }
    screen<ScanCardQrRoute> {
        ScanCardQrScreen(onScanned = { nav.navigate(PasscodeRoute) }, onClose = nav::back)
    }
    screen<PasscodeRoute> {
        PasscodeScreen(onPasscodeSet = { nav.navigate(EnableFaceIdRoute) })
    }
    screen<EnableFaceIdRoute> {
        EnableFaceIdScreen(onLoggedIn = nav::goToStageRoot)
    }
}
