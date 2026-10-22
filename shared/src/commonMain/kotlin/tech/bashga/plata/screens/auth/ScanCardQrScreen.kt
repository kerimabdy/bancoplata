package tech.bashga.plata.screens.auth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_scanner_camera
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@Immutable
data class ScanCardQrState(val torch: Boolean = false)

sealed interface ScanCardQrAction {
    data object ToggleTorch : ScanCardQrAction

    /** Any QR code read, or a long press on the viewfinder. */
    data object QrScanned : ScanCardQrAction

    data object Close : ScanCardQrAction
}

fun ScanCardQrState.reduce(action: ScanCardQrAction): ScanCardQrState = when (action) {
    ScanCardQrAction.ToggleTorch -> copy(torch = !torch)
    ScanCardQrAction.QrScanned, ScanCardQrAction.Close -> this
}

class ScanCardQrViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(ScanCardQrState())
    val state: StateFlow<ScanCardQrState> = _state.asStateFlow()

    private val _scans = Channel<Unit>(Channel.CONFLATED)
    val scans: Flow<Unit> = _scans.receiveAsFlow()

    fun onAction(action: ScanCardQrAction) {
        if (action == ScanCardQrAction.QrScanned) {
            repository.qrScanned()
            _scans.trySend(Unit)
        }
        _state.value = _state.value.reduce(action)
    }
}

/** auth/scan-qr: links the device; [onScanned] after the QR is read. */
@Composable
fun ScanCardQrScreen(onScanned: () -> Unit, onClose: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { ScanCardQrViewModel(repository) }
    val state by viewModel.state.collectAsState()
    val scanned by rememberUpdatedState(onScanned)
    LaunchedEffect(viewModel) { viewModel.scans.collect { scanned() } }
    ScanCardQrContent(state, { action ->
        if (action == ScanCardQrAction.Close) onClose() else viewModel.onAction(action)
    })
}

// 408f4810: the glass buttons centre at y 87, the two title lines start at y 206
private val ButtonsTop = 12.dp
private val ButtonsToTitle = 103.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScanCardQrContent(state: ScanCardQrState, onAction: (ScanCardQrAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    Box(modifier.fillMaxSize()) {
        // no camera feed yet: the raster stands in for it, and a long press stands in for reading a QR code
        Image(
            painterResource(Res.drawable.raster_scanner_camera),
            null,
            Modifier
                .fillMaxSize()
                .testTag("viewfinder")
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onLongClickLabel = "Scan",
                    onLongClick = { onAction(ScanCardQrAction.QrScanned) },
                    onClick = {},
                ),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(PlataSafeArea.current.top + ButtonsTop))
            Row(Modifier.fillMaxWidth().padding(horizontal = spacing.s20)) {
                IconButton(
                    PlataIcons.Lightning,
                    { onAction(ScanCardQrAction.ToggleTorch) },
                    style = if (state.torch) IconButtonStyle.White else IconButtonStyle.Glass,
                    contentDescription = if (state.torch) "Flashlight on" else "Flashlight",
                )
                Spacer(Modifier.weight(1f))
                IconButton(PlataIcons.CloseX, { onAction(ScanCardQrAction.Close) }, style = IconButtonStyle.Glass, contentDescription = "Close")
            }
            Spacer(Modifier.height(ButtonsToTitle))
            PlataText(
                "Scan QR\nplaced on the back of your card",
                PlataTheme.typography.value,
                PlataTheme.colors.textOnDark,
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}
