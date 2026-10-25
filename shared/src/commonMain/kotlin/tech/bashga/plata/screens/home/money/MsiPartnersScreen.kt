package tech.bashga.plata.screens.home.money

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_dialog_terminal
import plata.shared.generated.resources.raster_logo_clip
import plata.shared.generated.resources.raster_logo_fiserv
import plata.shared.generated.resources.raster_logo_mercadopago
import plata.shared.generated.resources.raster_media_msi_partners
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.Chip
import tech.bashga.plata.designsystem.components.Dialog
import tech.bashga.plata.designsystem.components.DialogMedia
import tech.bashga.plata.designsystem.components.DialogPlacement
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.InsetRow
import tech.bashga.plata.designsystem.components.InsetRowTrailing
import tech.bashga.plata.designsystem.components.MediaHeader
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.PartnerCard
import tech.bashga.plata.designsystem.components.PartnerCardTrailing
import tech.bashga.plata.designsystem.components.SectionHeader
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProgressiveBlurImage
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.Beauty
import tech.bashga.plata.designsystem.icons.CarRental
import tech.bashga.plata.designsystem.icons.Globe
import tech.bashga.plata.designsystem.icons.MsiBadge
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.TShirt
import tech.bashga.plata.designsystem.icons.Volleyball
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay

sealed interface MsiOverlay {
    data object PartnersIntro : MsiOverlay

    data class Terminal(val provider: TerminalProvider) : MsiOverlay
}

/** [category] filters the store list; null shows every store. */
@Immutable
data class MsiPartnersState(
    val category: MsiCategory? = null,
    val overlay: MsiOverlay? = null,
    val hero: List<MsiPartner> = MsiPartners.hero,
    val allStores: List<MsiPartner> = MsiPartners.stores,
) {
    val stores: List<MsiPartner> get() = if (category == null) allStores else allStores.filter { it.category == category }
}

sealed interface MsiPartnersAction {
    data class SelectCategory(val category: MsiCategory) : MsiPartnersAction

    data class OpenTerminal(val provider: TerminalProvider) : MsiPartnersAction

    data object CloseOverlay : MsiPartnersAction

    data class OpenPartner(val id: String) : MsiPartnersAction

    data object Back : MsiPartnersAction
}

fun MsiPartnersState.reduce(action: MsiPartnersAction): MsiPartnersState = when (action) {
    is MsiPartnersAction.SelectCategory -> copy(category = if (category == action.category) null else action.category)
    is MsiPartnersAction.OpenTerminal -> copy(overlay = MsiOverlay.Terminal(action.provider))
    MsiPartnersAction.CloseOverlay -> copy(overlay = null)
    is MsiPartnersAction.OpenPartner, MsiPartnersAction.Back -> this
}

class MsiPartnersViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(
        MsiPartnersState(overlay = if (repository.snapshot.msiPartnersIntroSeen) null else MsiOverlay.PartnersIntro),
    )
    val state: StateFlow<MsiPartnersState> = _state.asStateFlow()

    fun onAction(action: MsiPartnersAction) {
        if (action == MsiPartnersAction.CloseOverlay && _state.value.overlay == MsiOverlay.PartnersIntro) repository.seeMsiPartnersIntro()
        _state.value = _state.value.reduce(action)
    }
}

/** home/msi/partners: the featured carousel, terminals and stores with MSI; the intro dialog on the first visit. */
@Composable
fun MsiPartnersScreen(onBack: () -> Unit, onPartner: (String) -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { MsiPartnersViewModel(repository) }
    val state by viewModel.state.collectAsState()
    MsiPartnersContent(state, { action ->
        when (action) {
            MsiPartnersAction.Back -> onBack()
            is MsiPartnersAction.OpenPartner -> onPartner(action.id)
            else -> viewModel.onAction(action)
        }
    })
}

// 9e2d2593: section titles 19.5 under the photo and 40 under the chips; chips 20 under their title, 12 apart
private val FirstSectionTop = 19.5.dp
private val SectionTop = 40.dp
private val ChipGap = 12.dp
private val HeroHeight = 420.dp
private val FeedBottom = 37.dp

// the bar row starts 5 above the safe-area top (iOS draws it from the status bar's bottom)
private val StatusBarOverlap = 5.dp

private val TerminalProvider.logo: DrawableResource
    get() = when (this) {
        TerminalProvider.Clip -> Res.drawable.raster_logo_clip
        TerminalProvider.MercadoPago -> Res.drawable.raster_logo_mercadopago
        TerminalProvider.Fiserv -> Res.drawable.raster_logo_fiserv
    }

private val storeChips = listOf(
    MsiCategory.SportingGoods to PlataIcons.Volleyball,
    MsiCategory.Beauty to PlataIcons.Beauty,
    MsiCategory.Airline to PlataIcons.Plane,
    MsiCategory.Travel to PlataIcons.Globe,
    MsiCategory.Clothing to PlataIcons.TShirt,
    MsiCategory.Automotive to PlataIcons.CarRental,
)

@Composable
fun MsiPartnersContent(state: MsiPartnersState, onAction: (MsiPartnersAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val safeArea = PlataSafeArea.current
    val scroll = rememberScrollState()
    val pager = rememberPagerState { state.hero.size }
    val chipRow = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = spacing.screenMargin)
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("msi-partners").backdropSource().verticalScroll(scroll).padding(bottom = safeArea.bottom + FeedBottom)) {
            MediaHeader(
                pager,
                title = { "The best is yet to come" },
                navTitle = "MSI partners",
                onBack = { onAction(MsiPartnersAction.Back) },
                height = HeroHeight,
                featured = { page ->
                    val partner = state.hero[page]
                    PartnerCard(
                        partner.name,
                        partner.category.label,
                        partner.minimumText,
                        Modifier.fillMaxWidth(),
                        trailing = PartnerCardTrailing.Minimum,
                        onClick = { onAction(MsiPartnersAction.OpenPartner(partner.id)) },
                    ) { BrandLogo(painterResource(partner.logo), BrandLogoSize.S40) }
                },
            ) {
                ProgressiveBlurImage(painterResource(Res.drawable.raster_media_msi_partners), Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(FirstSectionTop))
            SectionHeader("Terminals with MSI", Modifier.padding(horizontal = spacing.screenMargin))
            Spacer(Modifier.height(spacing.s20))
            Row(chipRow, horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
                TerminalProvider.entries.forEach { provider ->
                    Chip(provider.label, painterResource(provider.logo), onClick = { onAction(MsiPartnersAction.OpenTerminal(provider)) })
                }
            }
            Spacer(Modifier.height(SectionTop))
            SectionHeader("Stores with MSI", Modifier.padding(horizontal = spacing.screenMargin).testTag("stores-with-msi"))
            Spacer(Modifier.height(spacing.s20))
            Row(chipRow, horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
                storeChips.forEach { (category, icon) ->
                    Chip(category.label, icon, selected = state.category == category, onClick = { onAction(MsiPartnersAction.SelectCategory(category)) })
                }
            }
            Spacer(Modifier.height(spacing.s20))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                state.stores.forEach { store ->
                    PartnerCard(
                        store.name,
                        store.category.label,
                        store.minimumText,
                        Modifier.fillMaxWidth(),
                        trailing = PartnerCardTrailing.Minimum,
                        onClick = { onAction(MsiPartnersAction.OpenPartner(store.id)) },
                    ) { BrandLogo(painterResource(store.logo), BrandLogoSize.S40) }
                }
            }
        }
        val barBottom = safeArea.top - StatusBarOverlap + PlataTheme.sizes.nav
        if (rememberCollapsed(scroll, threshold = HeroHeight - barBottom)) {
            NavigationBar(title = "MSI partners", onBack = { onAction(MsiPartnersAction.Back) }, collapsed = true, includeStatusBar = true)
        }
    }
    MsiOverlays(state.overlay, onClose = { onAction(MsiPartnersAction.CloseOverlay) })
}

// d55a4d32: the 96 MSI badge 62 under the dialog top, the title 27.5 under it; both dialogs end 2.5 above DialogPlacement's bottom
private val BadgeSize = 96.dp
private val BadgeTop = 62.dp
private val BadgeGap = 27.5.dp
private val DialogLift = (-2.5).dp
private val TerminalPhotoHeight = 300.dp

@Composable
private fun MsiOverlays(overlay: MsiOverlay?, onClose: () -> Unit) {
    Overlay(overlay as? MsiOverlay.PartnersIntro, onDismiss = onClose) {
        DialogPlacement(onDismiss = onClose) {
            Dialog(
                "MSI partners",
                Modifier.offset(y = DialogLift),
                body = "Make purchases with MSI with your\nPlata Card in our partner stores",
                onClose = onClose,
                mediaContent = { Icon(PlataIcons.MsiBadge, size = BadgeSize) },
                contentTop = BadgeTop,
                mediaGap = BadgeGap,
                actions = { Button("Start exploring", onClose, Modifier.fillMaxWidth()) },
            )
        }
    }
    Overlay(overlay as? MsiOverlay.Terminal, onDismiss = onClose) { terminal ->
        DialogPlacement(onDismiss = onClose) {
            Dialog(
                AnnotatedString("MSI is available\non ${terminal.provider.label} terminals"),
                Modifier.offset(y = DialogLift),
                body = buildAnnotatedString {
                    append("Purchase at ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("any stores") }
                    append(" that has\nthe terminal")
                },
                media = DialogMedia.Bleed,
                mediaContent = {
                    Image(
                        painterResource(Res.drawable.raster_dialog_terminal),
                        null,
                        Modifier.fillMaxWidth().height(TerminalPhotoHeight),
                        contentScale = ContentScale.Crop,
                    )
                },
                extras = {
                    InsetRow("Minimum amount", formatMoney(terminal.provider.minimum), Modifier.fillMaxWidth(), trailing = InsetRowTrailing.Value)
                },
                actions = { Button("Great", onClose, Modifier.fillMaxWidth()) },
            )
        }
    }
}
