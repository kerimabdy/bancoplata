package tech.bashga.plata.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.SupportTab
import tech.bashga.plata.navigation.screen
import tech.bashga.plata.screens.support.SupportChatScreen

@Serializable
data object SupportChatRoute

/** SupportChat as a full-screen cover, for CardPackaging › Contact support before the tab bar exists. */
@Serializable
data object SupportCoverRoute

fun NavGraphBuilder.supportGraph(nav: AppNavigator, repository: AppRepository) {
    navigation<SupportTab>(startDestination = SupportChatRoute) {
        screen<SupportChatRoute> { SupportChatScreen() }
    }
    screen<SupportCoverRoute>(Presentation.Cover) { SupportChatScreen(onClose = nav::back) }
}
