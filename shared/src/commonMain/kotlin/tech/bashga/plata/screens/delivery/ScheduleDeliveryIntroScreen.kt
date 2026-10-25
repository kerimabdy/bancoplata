package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_onboarding_delivery
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.OnboardingBullets
import tech.bashga.plata.designsystem.components.OnboardingHero
import tech.bashga.plata.designsystem.icons.PlataIcons

sealed interface ScheduleDeliveryIntroAction {
    data object ScheduleNow : ScheduleDeliveryIntroAction
}

/** onboarding/schedule: the stage root while no delivery is scheduled. It has no state of its own, so no ViewModel. */
@Composable
fun ScheduleDeliveryIntroScreen(onScheduleNow: () -> Unit) {
    ScheduleDeliveryIntroContent(onAction = { action ->
        when (action) {
            ScheduleDeliveryIntroAction.ScheduleNow -> onScheduleNow()
        }
    })
}

@Composable
fun ScheduleDeliveryIntroContent(onAction: (ScheduleDeliveryIntroAction) -> Unit, modifier: Modifier = Modifier) {
    OnboardingHero(
        "Schedule\nthe delivery",
        modifier,
        media = {
            Image(
                painterResource(Res.drawable.raster_onboarding_delivery),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
        },
        body = {
            OnboardingBullets(
                listOf(
                    PlataIcons.MapPin to "Choose a place for your\ncard delivery",
                    PlataIcons.CardWide to "You’ll get a card and cool gift",
                    PlataIcons.Ine to "You’ll need your INE to open\nan account",
                ),
            )
        },
        actions = { Button("Schedule now", { onAction(ScheduleDeliveryIntroAction.ScheduleNow) }, Modifier.fillMaxWidth()) },
    )
}
