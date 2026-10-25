package tech.bashga.plata.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_adidas
import plata.shared.generated.resources.raster_logo_alibaba
import plata.shared.generated.resources.raster_logo_aliexpress
import plata.shared.generated.resources.raster_logo_booking
import plata.shared.generated.resources.raster_logo_checkmybody
import plata.shared.generated.resources.raster_logo_coppel
import plata.shared.generated.resources.raster_logo_emma
import plata.shared.generated.resources.raster_logo_europcar
import plata.shared.generated.resources.raster_logo_expedia
import plata.shared.generated.resources.raster_logo_farfetch
import plata.shared.generated.resources.raster_logo_nike
import plata.shared.generated.resources.raster_media_booking
import plata.shared.generated.resources.raster_media_partners
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Chip
import tech.bashga.plata.designsystem.components.MediaHeader
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.PartnerCard
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProgressiveBlurImage
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.Beauty
import tech.bashga.plata.designsystem.icons.CarRental
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** The chip filters; [matches] is the partner category each one shows. */
enum class PartnerFilter(val label: String, val glyph: ImageVector, val matches: String) {
    AirlineTickets("Airline tickets", PlataIcons.Plane, "Travel"),
    Beauty("Beauty", PlataIcons.Beauty, "Beauty"),
    CarRental("Car rental", PlataIcons.CarRental, "Car rental"),
    Restaurants("Restaurants", PlataIcons.ForkKnife, "Restaurants"),
}

@Immutable
data class PartnersState(val category: PartnerFilter? = null)

sealed interface PartnersAction {
    data object Back : PartnersAction

    data class ToggleCategory(val category: PartnerFilter) : PartnersAction

    data class OpenPartner(val name: String) : PartnersAction
}

fun PartnersState.reduce(action: PartnersAction): PartnersState = when (action) {
    is PartnersAction.ToggleCategory -> copy(category = if (category == action.category) null else action.category)
    else -> this
}

class PartnersViewModel : ViewModel() {
    private val _state = MutableStateFlow(PartnersState())
    val state: StateFlow<PartnersState> = _state.asStateFlow()

    fun onAction(action: PartnersAction) = _state.update { it.reduce(action) }
}

/** cashback/partners, pushed inside the CashbackSetup sheet. */
@Composable
fun PartnersScreen(onBack: () -> Unit) {
    val viewModel = viewModel { PartnersViewModel() }
    val state by viewModel.state.collectAsState()
    PartnersContent(state, onAction = { action ->
        viewModel.onAction(action)
        if (action == PartnersAction.Back) onBack()
    })
}

private class Partner(val name: String, val category: String, val rate: String, val logo: DrawableResource)

private class FeaturedPartner(val title: String, val partner: Partner, val background: DrawableResource)

private val booking = Partner("Booking", "Travel", "4%", Res.drawable.raster_logo_booking)

// the source shows the first two of nine pages; the rest repeat them
private val featured = listOf(
    FeaturedPartner("Unleash your potential", Partner("Nike", "Sporting goods", "10%", Res.drawable.raster_logo_nike), Res.drawable.raster_media_partners),
    FeaturedPartner("Book anything", booking, Res.drawable.raster_media_booking),
)

private const val FeaturedPages = 9

private val partners = listOf(
    Partner("Adidas", "Sporting goods", "3%", Res.drawable.raster_logo_adidas),
    Partner("Alibaba", "General purchases", "8%", Res.drawable.raster_logo_alibaba),
    Partner("AliExpress", "General purchases", "10%", Res.drawable.raster_logo_aliexpress),
    booking,
    Partner("Check My Body Health", "Sporting goods", "9.5%", Res.drawable.raster_logo_checkmybody),
    Partner("Coppel", "General purchases", "2%", Res.drawable.raster_logo_coppel),
    Partner("Emma", "Home and DIY", "3%", Res.drawable.raster_logo_emma),
    Partner("Europcar", "Car rental", "5%", Res.drawable.raster_logo_europcar),
    Partner("Expedia", "Travel", "7%", Res.drawable.raster_logo_expedia),
    Partner("Farfetch", "Clothing", "1%", Res.drawable.raster_logo_farfetch),
)

// the bar takes the "Partners" title once the 420 hero is under it (917b0cb2)
private val HeroCollapse = 366.dp

@Composable
fun PartnersContent(state: PartnersState, onAction: (PartnersAction) -> Unit, modifier: Modifier = Modifier) {
    val pager = rememberPagerState { FeaturedPages }
    val scroll = rememberScrollState()
    val back = { onAction(PartnersAction.Back) }
    PartnerDirectoryContent(
        scroll,
        title = "Partners",
        collapseAfter = HeroCollapse,
        onBack = back,
        modifier = modifier,
        header = {
            MediaHeader(
                pager,
                title = { featured[it % featured.size].title },
                modifier = Modifier.fillMaxWidth().testTag("partners-hero"),
                navTitle = "Partners",
                onBack = back,
                featured = { page ->
                    val partner = featured[page % featured.size].partner
                    PartnerCard(partner.name, partner.category, partner.rate, Modifier.fillMaxWidth(), onClick = { onAction(PartnersAction.OpenPartner(partner.name)) }) {
                        BrandLogo(painterResource(partner.logo), BrandLogoSize.S40)
                    }
                },
            ) { page ->
                ProgressiveBlurImage(painterResource(featured[page % featured.size].background), Modifier.fillMaxSize())
            }
        },
        chips = {
            items(PartnerFilter.entries) { filter ->
                Chip(filter.label, filter.glyph, selected = state.category == filter, onClick = { onAction(PartnersAction.ToggleCategory(filter)) })
            }
        },
    ) {
        partners.filter { state.category == null || it.category == state.category.matches }.forEach { partner ->
            PartnerCard(
                partner.name,
                partner.category,
                partner.rate,
                Modifier.fillMaxWidth().testTag("partner-${partner.name.lowercase()}"),
                onClick = { onAction(PartnersAction.OpenPartner(partner.name)) },
            ) { BrandLogo(painterResource(partner.logo), BrandLogoSize.S40) }
        }
    }
}

/**
 * A partner directory page (Partners, and the shape MsiPartners shares): a [header] that scrolls away, a row of
 * [chips], the [items] 20 apart, and a material bar with [title] once [collapseAfter] has scrolled under it.
 */
@Composable
fun PartnerDirectoryContent(
    scroll: ScrollState,
    title: String,
    collapseAfter: Dp,
    onBack: () -> Unit,
    header: @Composable () -> Unit,
    chips: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
    items: @Composable ColumnScope.() -> Unit,
) {
    val spacing = PlataTheme.spacing
    val collapsed = rememberCollapsed(scroll, collapseAfter)
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("directory-scroll").backdropSource().verticalScroll(scroll)) {
            header()
            Spacer(Modifier.height(spacing.s20))
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.screenMargin),
                horizontalArrangement = Arrangement.spacedBy(spacing.s12),
                content = chips,
            )
            Spacer(Modifier.height(spacing.s20))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap), content = items)
            Spacer(Modifier.height(spacing.s40 + PlataSafeArea.current.bottom))
        }
        if (collapsed) NavigationBar(style = NavigationBarStyle.Sheet, title = title, onBack = onBack, material = true)
    }
}
