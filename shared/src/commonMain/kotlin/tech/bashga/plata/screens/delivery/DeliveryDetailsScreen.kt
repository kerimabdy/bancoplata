package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_ambassador_assigned
import plata.shared.generated.resources.raster_badge_package
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.CardThumb
import tech.bashga.plata.designsystem.components.CardThumbKind
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonSize
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.InlineNotice
import tech.bashga.plata.designsystem.components.MapPreview
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.components.SummaryCardTrailingAlignment
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.CalendarGrid
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataBackHandler

/** [ambassador] updates in place when the AmbassadorAssigned event fires while the sheet is open. */
@Immutable
data class DeliveryDetailsState(
    val ambassador: Ambassador = Ambassador.Unassigned,
    val address: String = "",
    val addressLine2: String = "",
    val location: Pair<Double, Double>? = null,
    val details: String = "",
    val time: String = "",
) {
    val assigned: Boolean get() = ambassador is Ambassador.Assigned
}

sealed interface DeliveryDetailsAction {
    data object CheckPackage : DeliveryDetailsAction

    data object EditAddress : DeliveryDetailsAction

    data object EditTime : DeliveryDetailsAction

    data object AddToCalendar : DeliveryDetailsAction

    data object AddDetails : DeliveryDetailsAction

    data object Agreement : DeliveryDetailsAction

    data object Close : DeliveryDetailsAction
}

internal fun AppSnapshot.toDeliveryDetailsState(): DeliveryDetailsState {
    val address = delivery.address
    return DeliveryDetailsState(
        ambassador = (stage as? Stage.CardComing)?.ambassador ?: Ambassador.Unassigned,
        address = address?.line.orEmpty(),
        addressLine2 = address?.place?.line2.orEmpty(),
        location = address?.place?.let { it.latitude to it.longitude },
        details = delivery.details,
        time = deliveryWhen.orEmpty(),
    )
}

class DeliveryDetailsViewModel(private val repository: AppRepository) : ViewModel() {
    init {
        repository.openDeliveryDetails()
    }

    val state: StateFlow<DeliveryDetailsState> = repository.state.map { it.toDeliveryDetailsState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.snapshot.toDeliveryDetailsState())

    /** Close or swipe down; true when that delivered the card (event CardDelivered). */
    fun close(): Boolean = repository.closeDeliveryDetails()
}

/**
 * delivery/details. Opening it arms AmbassadorAssigned; closing it (Close, swipe down, back) once the ambassador is
 * assigned fires CardDelivered. Leaving it for a sheet on top doesn't count.
 */
@Composable
fun DeliveryDetailsScreen(
    onCheckPackage: () -> Unit,
    onEditAddress: () -> Unit,
    onEditTime: () -> Unit,
    onClose: (delivered: Boolean) -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { DeliveryDetailsViewModel(repository) }
    val state by viewModel.state.collectAsState()
    val close = { onClose(viewModel.close()) }
    PlataBackHandler(onBack = close)
    DeliveryDetailsContent(state, onAction = { action ->
        when (action) {
            DeliveryDetailsAction.CheckPackage -> onCheckPackage()
            DeliveryDetailsAction.EditAddress -> onEditAddress()
            DeliveryDetailsAction.EditTime -> onEditTime()
            DeliveryDetailsAction.Close -> close()
            DeliveryDetailsAction.AddToCalendar, DeliveryDetailsAction.AddDetails, DeliveryDetailsAction.Agreement -> Unit
        }
    })
}

// 1d655cd9: the 96 ambassador disc 56 under the sheet top, its title 18 below, the first card 21 under the title
private val SheetToAvatar = 56.dp
private val AvatarSize = 96.dp
private val AvatarGlyph = 60.dp
private val AvatarToTitle = 18.dp
private val TitleToCards = 21.dp

// the card row grows to the 52 thumb plus 20 above and below (91.7 on 1d655cd9), unlike the height-neutral SummaryCard slot
private val ThumbCardPadding = 20.dp
private val ThumbCardVertical = 22.dp
private val AddressToMap = 15.dp
private val AddressHeaderGap = 4.dp
private val DetailsHeaderGap = 1.dp
private val DateToButton = 18.dp

// 88790268 ends the list 95 above the screen bottom, which is also what lets the sheet scroll that far
private val ListBottom = 61.dp

// the avatar has gone under the bar once its bottom passes the bar's
private val CollapseAt = SheetToAvatar + AvatarSize - 56.dp

@Composable
fun DeliveryDetailsContent(state: DeliveryDetailsState, onAction: (DeliveryDetailsAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAt)
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(scroll).testTag("details-scroll"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(SheetToAvatar))
            AmbassadorAvatar(state.assigned, AvatarSize)
            Spacer(Modifier.height(AvatarToTitle))
            PlataText(
                if (state.assigned) "Ambassador assigned" else "Ambassador to be assigned",
                PlataTheme.typography.title3,
                colors.textPrimary,
            )
            Spacer(Modifier.height(TitleToCards))
            Column(
                Modifier.padding(horizontal = spacing.screenMargin),
                verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
            ) {
                DeliveryCards(state, onAction)
            }
            Spacer(Modifier.height(PlataSafeArea.current.bottom + ListBottom))
        }
        NavigationBar(
            // the source bar hides what scrolls under it; the material alone would let the cards show through
            if (collapsed) Modifier.background(colors.surfaceBackground) else Modifier,
            style = NavigationBarStyle.Sheet,
            leading = { TextButton("Close", { onAction(DeliveryDetailsAction.Close) }) },
            center = if (collapsed) {
                { AmbassadorAvatar(state.assigned, 38.dp) }
            } else {
                null
            },
            material = collapsed,
        )
    }
}

@Composable
private fun AmbassadorAvatar(assigned: Boolean, size: Dp) {
    val colors = PlataTheme.colors
    if (assigned && size < AvatarSize) {
        Image(painterResource(Res.drawable.raster_ambassador_assigned), "Ambassador assigned", Modifier.size(size))
        return
    }
    Box(Modifier.size(size).background(colors.surfaceCard, CircleShape), contentAlignment = Alignment.Center) {
        Icon(PlataIcons.Person, size = size * AvatarGlyph.value / AvatarSize.value, tint = colors.textSecondary)
    }
}

@Composable
private fun DeliveryCards(state: DeliveryDetailsState, onAction: (DeliveryDetailsAction) -> Unit) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    SummaryCard(
        "To be delivered",
        subtitle = "Plata Crédito",
        subtitleStyle = typography.bodyCompact,
        contentPadding = PaddingValues(horizontal = ThumbCardPadding, vertical = ThumbCardVertical),
    ) {
        CardThumb(
            CardThumbKind.Physical,
            badge = {
                Image(
                    painterResource(Res.drawable.raster_badge_package),
                    "Package",
                    Modifier.align(Alignment.TopEnd).offset((-8).dp, 3.33.dp).size(16.dp),
                )
            },
        )
    }
    InlineNotice("Check card package upon delivery", Modifier.fillMaxWidth(), onClick = { onAction(DeliveryDetailsAction.CheckPackage) })
    SummaryCard("Bring your INE", subtitle = "To verify your identity", subtitleStyle = typography.bodyCompact) {
        IconCircle(PlataIcons.Ine, IconCircleSize.S40, tint = colors.textPrimary)
    }
    SectionCard(
        Modifier.testTag("address-card"),
        title = "Address",
        headerTrailing = { SectionCardAction("Edit", { onAction(DeliveryDetailsAction.EditAddress) }) },
        headerGap = AddressHeaderGap,
    ) {
        PlataText(listOf(state.address, state.addressLine2).filter { it.isNotEmpty() }.joinToString("\n"), typography.bodyCompact, colors.textPrimary)
        state.location?.let { (latitude, longitude) ->
            Spacer(Modifier.height(AddressToMap))
            MapPreview(latitude, longitude)
        }
    }
    SummaryCard(
        "Date and time",
        subtitle = state.time,
        subtitleStyle = typography.bodyCompact,
        bodyGap = DateToButton,
        trailingAlignment = SummaryCardTrailingAlignment.Top,
        body = {
            Button(
                "Add to calendar",
                { onAction(DeliveryDetailsAction.AddToCalendar) },
                Modifier.fillMaxWidth(),
                style = ButtonStyle.SecondaryOnCard,
                icon = PlataIcons.CalendarGrid,
            )
        },
    ) { SectionCardAction("Edit", { onAction(DeliveryDetailsAction.EditTime) }) }
    SectionCard(
        title = "Details (optional)",
        headerTrailing = { SectionCardAction(if (state.details.isEmpty()) "Add" else "Edit", { onAction(DeliveryDetailsAction.AddDetails) }) },
        headerGap = DetailsHeaderGap,
    ) {
        if (state.details.isEmpty()) {
            PlataText("Add extra details to help us reach you", typography.bodyCompact, colors.textSecondary)
        } else {
            PlataText(state.details, typography.bodyCompact, colors.textPrimary)
        }
    }
    SummaryCard("Plata card agreement", subtitle = "To be signed upon card delivery", subtitleStyle = typography.bodyCompact) {
        IconButton(PlataIcons.Document, { onAction(DeliveryDetailsAction.Agreement) }, style = IconButtonStyle.Subtle, size = IconButtonSize.S40)
    }
}
