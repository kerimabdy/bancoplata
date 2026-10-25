package tech.bashga.plata.screens.home.money

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.painterResource
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.ImageTile
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.PromoHeadline
import tech.bashga.plata.designsystem.components.PromoHeadlineAlign
import tech.bashga.plata.designsystem.components.PromoHeadlineTone
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.SectionHeaderEmphasis
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.components.SummaryCardTrailingAlignment
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.MsiBadge
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@Immutable
data class MsiHomeState(
    val partnerCount: Int = 110,
    val featured: List<MsiPartner> = MsiPartners.featured,
)

sealed interface MsiHomeAction {
    data class OpenPartner(val id: String) : MsiHomeAction

    data object ShowAllPartners : MsiHomeAction

    data object Back : MsiHomeAction
}

/** Opening the hub is what clears the MSI tile's badge on Home. */
class MsiHomeViewModel(repository: AppRepository) : ViewModel() {
    val state = MsiHomeState()

    init {
        if (!repository.snapshot.homeTiles.msiVisited) repository.visitMsi()
    }
}

/** home/msi: the meses-sin-intereses hub. */
@Composable
fun MsiHomeScreen(onBack: () -> Unit, onPartner: (String) -> Unit, onAllPartners: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { MsiHomeViewModel(repository) }
    MsiHomeContent(viewModel.state, { action ->
        when (action) {
            is MsiHomeAction.OpenPartner -> onPartner(action.id)
            MsiHomeAction.ShowAllPartners -> onAllPartners()
            MsiHomeAction.Back -> onBack()
        }
    })
}

// d7d78e0b: the headline 19.2 under the bar, the first card 40 under the headline; tiles 12 apart, 20 in from the card
private val HeadlineTop = 19.2.dp

// the bar row starts 5 above the safe-area top (iOS draws it from the status bar's bottom)
private val StatusBarOverlap = 5.dp
private val CardsTop = 40.dp
private val TileGap = 12.dp
private val NavBadge = 36.dp
private val SubtitleGap = 3.dp

// the one-line summary cards are 85 high: 17 under the subtitle instead of 20
private val SummaryBottom = 17.dp
// b50f7634 scrolls 70.5 at most: the feed ends 42 above the tab bar
private val FeedBottom = 42.dp

@Composable
fun MsiHomeContent(state: MsiHomeState, onAction: (MsiHomeAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val safeArea = PlataSafeArea.current
    val scroll = rememberScrollState()
    val summaryPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = SummaryBottom)
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("msi-home")
                .backdropSource()
                .verticalScroll(scroll)
                .padding(top = safeArea.top - StatusBarOverlap + PlataTheme.sizes.nav + HeadlineTop, bottom = safeArea.bottom + FeedBottom),
        ) {
            PromoHeadline("Meses", Modifier.fillMaxWidth(), line2 = "sin intereses", align = PromoHeadlineAlign.Center, tone = PromoHeadlineTone.Plain)
            Spacer(Modifier.height(CardsTop))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                SummaryCard(
                    "Defer to MSI",
                    Modifier.testTag("defer-to-msi"),
                    subtitle = "Nothing to defer",
                    subtitleStyle = typography.bodyCompact,
                    trailingAlignment = SummaryCardTrailingAlignment.Top,
                    contentPadding = summaryPadding,
                    onClick = {},
                ) { SectionCardAction("Show all", {}) }
                SectionCard(
                    title = "${state.partnerCount} MSI Partners",
                    headerTrailing = { SectionCardAction("Show all", { onAction(MsiHomeAction.ShowAllPartners) }) },
                ) {
                    Row(
                        Modifier.bleedEnd(spacing.cardPadding).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(TileGap),
                    ) {
                        state.featured.forEach { partner ->
                            val tile = partner.tile ?: return@forEach
                            ImageTile(
                                partner.name,
                                "Min ${partner.minimumText}",
                                painterResource(tile),
                                logo = { BrandLogo(painterResource(partner.logo), BrandLogoSize.S40) },
                                onClick = { onAction(MsiHomeAction.OpenPartner(partner.id)) },
                            )
                        }
                    }
                }
                SectionCard(title = "MSI to Cashback", titleEmphasis = SectionHeaderEmphasis.Gradient, headerGap = SubtitleGap, contentPadding = summaryPadding) {
                    PlataText("No active partner MSI", typography.bodyCompact, colors.textPrimary)
                }
                SummaryCard("Active MSI", subtitle = "No active MSI", subtitleStyle = typography.bodyCompact, contentPadding = summaryPadding)
            }
        }
        NavigationBar(
            onBack = { onAction(MsiHomeAction.Back) },
            center = { Icon(PlataIcons.MsiBadge, size = NavBadge) },
            collapsed = rememberCollapsed(scroll),
            includeStatusBar = true,
        )
    }
}

/** Lets a row run past the card's end padding to its edge, where the card's shape clips it (d7d78e0b). */
private fun Modifier.bleedEnd(amount: Dp) = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra, minWidth = constraints.minWidth + extra))
    layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
}
