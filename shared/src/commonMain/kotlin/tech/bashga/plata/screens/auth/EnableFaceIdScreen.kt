package tech.bashga.plata.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_faceid_art
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonTone
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme

sealed interface EnableFaceIdAction {
    /** The system biometric prompt is not shown yet; Enable records the choice directly. */
    data object Enable : EnableFaceIdAction

    data object NotNow : EnableFaceIdAction
}

class EnableFaceIdViewModel(private val repository: AppRepository) : ViewModel() {
    fun onAction(action: EnableFaceIdAction) = repository.logIn(faceId = action == EnableFaceIdAction.Enable)
}

/** auth/face-id: either choice logs in, then [onLoggedIn] goes to the stage root. */
@Composable
fun EnableFaceIdScreen(onLoggedIn: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { EnableFaceIdViewModel(repository) }
    EnableFaceIdContent({ action ->
        viewModel.onAction(action)
        onLoggedIn()
    })
}

// 8d47c829: the white top band ends 11 under the safe area; art at y 367, Enable at 748, "Not now" runs under the home indicator
private val TopBand = 11.dp
private val BandToArt = 297.dp
private val ArtToTitle = 50.dp
private val BodyToButton = 166.dp
private val ButtonToSecondary = 14.dp

@Composable
fun EnableFaceIdContent(onAction: (EnableFaceIdAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    Column(
        modifier
            .fillMaxSize()
            .background(PlataTheme.colors.surfaceBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.fillMaxWidth().height(PlataSafeArea.current.top + TopBand).background(PlataTheme.colors.surfaceCard))
        Spacer(Modifier.height(BandToArt))
        HeadlineBlock(
            "Enable Face ID?",
            Modifier.fillMaxWidth().padding(horizontal = spacing.s40),
            body = "Turn on Face ID to log in\nwithout passcode",
            size = HeadlineBlockSize.Small,
            align = HeadlineBlockAlign.Center,
            mediaGap = ArtToTitle,
            media = { Image(painterResource(Res.drawable.raster_faceid_art), null, Modifier.size(82.dp, 81.dp)) },
        )
        Spacer(Modifier.height(BodyToButton))
        Button("Enable", { onAction(EnableFaceIdAction.Enable) }, Modifier.fillMaxWidth().padding(horizontal = spacing.s40))
        Spacer(Modifier.height(ButtonToSecondary))
        TextButton("Not now", { onAction(EnableFaceIdAction.NotNow) }, tone = TextButtonTone.Primary)
        Spacer(Modifier.height(PlataSafeArea.current.bottom))
    }
}
