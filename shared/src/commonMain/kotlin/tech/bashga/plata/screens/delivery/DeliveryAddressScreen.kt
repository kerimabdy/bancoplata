package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.bashga.plata.data.Address
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.BottomSheet
import tech.bashga.plata.designsystem.components.BottomSheetAlign
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconSize
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.PlataMap
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.components.TextFieldClearButton
import tech.bashga.plata.designsystem.components.TextFieldSurface
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataBackHandler

enum class DeliveryAddressMode { Search, Map }

@Immutable
data class DeliveryAddressState(
    val mode: DeliveryAddressMode = DeliveryAddressMode.Search,
    val query: String = "",
    val address: Address? = null,
    val details: String = "",
) {
    internal val results: List<Place> get() = geocode(query)
}

sealed interface DeliveryAddressAction {
    data class Query(val text: String) : DeliveryAddressAction

    data class Pick(val address: Address) : DeliveryAddressAction

    data object Cancel : DeliveryAddressAction

    data object EditAddress : DeliveryAddressAction

    data class Details(val text: String) : DeliveryAddressAction

    data object Continue : DeliveryAddressAction
}

/** Cancel with nothing chosen and Continue leave the screen; the ViewModel handles those before reducing. */
fun DeliveryAddressState.reduce(action: DeliveryAddressAction): DeliveryAddressState = when (action) {
    is DeliveryAddressAction.Query -> copy(query = action.text)
    is DeliveryAddressAction.Pick -> copy(mode = DeliveryAddressMode.Map, address = action.address)
    DeliveryAddressAction.Cancel -> if (address != null) copy(mode = DeliveryAddressMode.Map) else this
    DeliveryAddressAction.EditAddress -> copy(mode = DeliveryAddressMode.Search)
    is DeliveryAddressAction.Details -> copy(details = action.text)
    DeliveryAddressAction.Continue -> this
}

class DeliveryAddressViewModel(private val repository: AppRepository, editing: Boolean) : ViewModel() {
    private val _state = MutableStateFlow(
        repository.snapshot.delivery.let { saved ->
            if (editing && saved.address != null) {
                DeliveryAddressState(DeliveryAddressMode.Map, address = saved.address, details = saved.details)
            } else {
                DeliveryAddressState()
            }
        },
    )
    val state: StateFlow<DeliveryAddressState> = _state.asStateFlow()

    /** False when the action leaves the screen: Cancel with nothing chosen, or Continue (which saves first). */
    fun onAction(action: DeliveryAddressAction): Boolean {
        val current = _state.value
        when (action) {
            DeliveryAddressAction.Cancel -> if (current.address == null) return false
            DeliveryAddressAction.Continue -> {
                val address = current.address ?: return true
                repository.saveDeliveryAddress(address, current.details.trim())
                return false
            }
            else -> Unit
        }
        _state.value = current.reduce(action)
        return true
    }
}

/**
 * delivery/address: search for the address, then confirm it on the map with optional details. [editing] starts on
 * the map with the saved address (DeliveryDetails › Address › Edit).
 */
@Composable
fun DeliveryAddressScreen(editing: Boolean, onContinue: () -> Unit, onCancel: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { DeliveryAddressViewModel(repository, editing) }
    val state by viewModel.state.collectAsState()
    val onAction: (DeliveryAddressAction) -> Unit = { action ->
        if (!viewModel.onAction(action)) {
            if (action == DeliveryAddressAction.Continue) onContinue() else onCancel()
        }
    }
    PlataBackHandler(enabled = state.mode == DeliveryAddressMode.Search && state.address != null) {
        onAction(DeliveryAddressAction.Cancel)
    }
    DeliveryAddressContent(state, onAction)
}

@Composable
fun DeliveryAddressContent(state: DeliveryAddressState, onAction: (DeliveryAddressAction) -> Unit, modifier: Modifier = Modifier) {
    when (state.mode) {
        DeliveryAddressMode.Search -> DeliveryAddressSearch(state, onAction, modifier)
        DeliveryAddressMode.Map -> DeliveryAddressMap(state, onAction, modifier)
    }
}

@Composable
private fun DeliveryAddressSearch(state: DeliveryAddressState, onAction: (DeliveryAddressAction) -> Unit, modifier: Modifier) {
    val spacing = PlataTheme.spacing
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Column(modifier.fillMaxSize().background(PlataTheme.colors.surfaceCard)) {
        // this bar's title sits 2 lower than the other sheet bars
        NavigationBar(
            Modifier.padding(top = 2.dp),
            style = NavigationBarStyle.Sheet,
            title = "Delivery address",
            leading = { TextButton("Cancel", { onAction(DeliveryAddressAction.Cancel) }) },
        )
        TextField(
            state.query,
            { onAction(DeliveryAddressAction.Query(it)) },
            Modifier
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, top = spacing.s4)
                .fillMaxWidth()
                .testTag("query"),
            label = null,
            surface = TextFieldSurface.Grey,
            trailing = if (state.query.isNotEmpty()) ({ TextFieldClearButton({ onAction(DeliveryAddressAction.Query("")) }) }) else null,
            focusRequester = focus,
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = spacing.s20, start = spacing.screenMargin, bottom = PlataSafeArea.current.bottom),
            verticalArrangement = Arrangement.spacedBy(spacing.s4),
        ) {
            state.results.forEach { place ->
                ListRow(
                    place.address.line,
                    Modifier.fillMaxWidth(),
                    subtitle = place.city,
                    onClick = { onAction(DeliveryAddressAction.Pick(place.address)) },
                    subtitleStyle = PlataTheme.typography.subhead,
                    trailing = { Icon(PlataIcons.ArrowUpLeft, IconSize.Medium, tint = PlataTheme.colors.textSecondary) },
                )
            }
        }
    }
}

@Composable
private fun DeliveryAddressMap(state: DeliveryAddressState, onAction: (DeliveryAddressAction) -> Unit, modifier: Modifier) {
    var detailsFocused by remember { mutableStateOf(false) }
    val keyboard = keyboardHeight(detailsFocused)
    val safeArea = PlataSafeArea.current
    val density = LocalDensity.current
    var sheetHeight by remember { mutableStateOf(0.dp) }
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        state.address?.place?.let { place ->
            PlataMap(
                place.latitude,
                place.longitude,
                // tucked under the sheet's rounded top so the corners show map, not background
                Modifier.fillMaxSize().padding(bottom = (sheetHeight - PlataTheme.radii.sheet).coerceAtLeast(0.dp)),
            )
        }
        // above the keyboard the sheet's bottom inset is the keyboard, not the home indicator
        ProvideSafeArea(safeArea.copy(bottom = if (keyboard > 0.dp) keyboard else safeArea.bottom)) {
            BottomSheet(
                Modifier.align(Alignment.BottomCenter).onSizeChanged { sheetHeight = with(density) { it.height.toDp() } },
                title = "Delivery address",
                align = BottomSheetAlign.Center,
                contentSpacing = PlataTheme.spacing.s20,
                footer = { Button("Continue", { onAction(DeliveryAddressAction.Continue) }, Modifier.fillMaxWidth()) },
            ) {
                Box(Modifier.fillMaxWidth()) {
                    TextField(
                        state.address?.line.orEmpty(),
                        {},
                        Modifier.fillMaxWidth(),
                        placeholder = "Address",
                        surface = TextFieldSurface.Grey,
                        readOnly = true,
                        alwaysShowLabel = true,
                    )
                    // the read-only field would take the tap as a caret placement; this layer makes it "search again"
                    Box(
                        Modifier
                            .matchParentSize()
                            .testTag("address")
                            .pressable(onClickLabel = "Change address") { onAction(DeliveryAddressAction.EditAddress) },
                    )
                }
                TextField(
                    state.details,
                    { onAction(DeliveryAddressAction.Details(it)) },
                    Modifier
                        .fillMaxWidth()
                        .testTag("details")
                        .onFocusChanged { detailsFocused = it.isFocused },
                    placeholder = "Details (optional)",
                    surface = TextFieldSurface.Grey,
                    trailing = if (state.details.isNotEmpty()) ({ TextFieldClearButton({ onAction(DeliveryAddressAction.Details("")) }) }) else null,
                )
            }
        }
    }
}
