package tech.bashga.plata.screens.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_cashback_coin
import plata.shared.generated.resources.raster_lesson_complete_check
import plata.shared.generated.resources.raster_logo_booking
import plata.shared.generated.resources.raster_logo_justo
import plata.shared.generated.resources.raster_logo_nike
import plata.shared.generated.resources.raster_tile_booking
import plata.shared.generated.resources.raster_tile_nike
import plata.shared.generated.resources.raster_tile_temu
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CheckCircle
import tech.bashga.plata.designsystem.components.CheckCircleState
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonSize
import tech.bashga.plata.designsystem.components.IconTile
import tech.bashga.plata.designsystem.components.ImageTile
import tech.bashga.plata.designsystem.components.ImageTileFooter
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.QuestionMark
import tech.bashga.plata.designsystem.theme.PlataGradients
import tech.bashga.plata.designsystem.theme.PlataTheme

const val MAX_CASHBACK_CATEGORIES = 4

enum class CashbackStep { Intro, Select, Confirmed }

/** [month] is the repository's clock; the selection is for the month after it. */
@Immutable
data class CashbackSetupState(
    val step: CashbackStep = CashbackStep.Intro,
    val selected: Set<CashbackCategory> = emptySet(),
    val month: Month = Month.June,
) {
    val full: Boolean get() = selected.size >= MAX_CASHBACK_CATEGORIES
}

sealed interface CashbackSetupAction {
    data object StartSelecting : CashbackSetupAction

    data class Toggle(val category: CashbackCategory) : CashbackSetupAction

    data class Info(val category: CashbackCategory) : CashbackSetupAction

    data object Done : CashbackSetupAction

    /** Close (Intro, Confirmed), Cancel (Select) or Done on Confirmed: the sheet goes. */
    data object Dismiss : CashbackSetupAction

    data object ShowAllPartners : CashbackSetupAction

    data class OpenPartner(val name: String) : CashbackSetupAction
}

fun CashbackSetupState.reduce(action: CashbackSetupAction): CashbackSetupState = when (action) {
    CashbackSetupAction.StartSelecting -> copy(step = CashbackStep.Select)
    is CashbackSetupAction.Toggle -> when {
        action.category in selected -> copy(selected = selected - action.category)
        full -> this
        else -> copy(selected = selected + action.category)
    }
    CashbackSetupAction.Done -> if (full) copy(step = CashbackStep.Confirmed) else this
    else -> this
}

/** Reopening the sheet starts from what was saved, so Done without changes keeps it. */
fun cashbackSetupState(snapshot: AppSnapshot) = CashbackSetupState(selected = snapshot.homeTiles.cashback, month = snapshot.month)

class CashbackSetupViewModel(private val repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(cashbackSetupState(repository.snapshot))
    val state: StateFlow<CashbackSetupState> = combine(repository.state, local) { snapshot, state -> state.copy(month = snapshot.month) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, local.value.copy(month = repository.snapshot.month))

    fun onAction(action: CashbackSetupAction) {
        val next = local.value.reduce(action)
        if (next.step == CashbackStep.Confirmed && local.value.step != CashbackStep.Confirmed) repository.saveCashback(next.selected)
        local.value = next
    }
}

/** cashback: Intro → Select 4 → Confirmed in one sheet; Partners pushes inside it. */
@Composable
fun CashbackSetupScreen(onDismiss: () -> Unit, onPartners: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { CashbackSetupViewModel(repository) }
    val state by viewModel.state.collectAsState()
    CashbackSetupContent(state, onAction = { action ->
        viewModel.onAction(action)
        when (action) {
            CashbackSetupAction.Dismiss -> onDismiss()
            CashbackSetupAction.ShowAllPartners -> onPartners()
            else -> Unit
        }
    })
}

@Composable
fun CashbackSetupContent(state: CashbackSetupState, onAction: (CashbackSetupAction) -> Unit, modifier: Modifier = Modifier) {
    when (state.step) {
        CashbackStep.Intro -> IntroStep(onAction, modifier)
        CashbackStep.Select -> SelectStep(state, onAction, modifier)
        CashbackStep.Confirmed -> ConfirmedStep(onAction, modifier)
    }
}

// 2d2d03c9: the coin art is 300×280 and starts 131 below the sheet top; the headline 66 under it
private val CoinTop = 77.dp
private val CoinHeadlineGap = 66.dp

@Composable
private fun IntroStep(onAction: (CashbackSetupAction) -> Unit, modifier: Modifier) {
    val spacing = PlataTheme.spacing
    Column(
        modifier
            .fillMaxSize()
            .background(PlataTheme.colors.surfaceCard)
            .padding(bottom = PlataSafeArea.current.bottom + spacing.s20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavigationBar(style = NavigationBarStyle.Sheet, leading = { TextButton("Close", { onAction(CashbackSetupAction.Dismiss) }) })
        Spacer(Modifier.height(CoinTop))
        Image(painterResource(Res.drawable.raster_cashback_coin), null, Modifier.size(300.dp, 280.dp))
        Spacer(Modifier.height(CoinHeadlineGap))
        HeadlineBlock(
            "Get up to 15%\ncashback",
            Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth(),
            body = "Select 4 categories each month\nto receive cashback with every purchase",
            size = HeadlineBlockSize.Medium,
            align = HeadlineBlockAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        Button("Select 4 categories", { onAction(CashbackSetupAction.StartSelecting) }, Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth())
    }
}

private class CategoryRow(val category: CashbackCategory, val title: String, val fill: (PlataGradients) -> Brush)

private val categoryRows = listOf(
    CategoryRow(CashbackCategory.AllPurchases, "1% All purchases") { it.categoryOrange },
    CategoryRow(CashbackCategory.Airline, "5% Airline tickets") { it.categorySky },
    CategoryRow(CashbackCategory.ChildrensStores, "5% Children's stores") { it.categoryPink },
    CategoryRow(CashbackCategory.Clothing, "5% Clothing") { it.categoryLilac },
    CategoryRow(CashbackCategory.HomeDiy, "5% Home and DIY") { it.categoryPlum },
    CategoryRow(CashbackCategory.Mobile, "5% Mobile, TV and Internet") { it.categoryBlue },
    CategoryRow(CashbackCategory.Restaurants, "5% Restaurants") { it.categoryMagenta },
    CategoryRow(CashbackCategory.SportingGoods, "5% Sporting goods") { it.categoryTeal },
    CategoryRow(CashbackCategory.Taxi, "5% Taxi") { it.categoryYellow },
    CategoryRow(CashbackCategory.Justo, "15% Jüsto") { it.categoryOrange },
)

// the title's line box starts 21 under the sheet bar, the list 28 under the title
private val SelectTitleTop = 21.dp
private val SelectListTop = 28.dp

@Composable
private fun SelectStep(state: CashbackSetupState, onAction: (CashbackSetupAction) -> Unit, modifier: Modifier) {
    val spacing = PlataTheme.spacing
    val gradients = PlataTheme.gradients
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            NavigationBar(style = NavigationBarStyle.Sheet, leading = { TextButton("Cancel", { onAction(CashbackSetupAction.Dismiss) }) })
            Spacer(Modifier.height(SelectTitleTop))
            HeadlineBlock("Select 4 categories\nfor ${state.month.next}", Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth())
            Spacer(Modifier.height(SelectListTop))
            categoryRows.forEach { row ->
                val checked = row.category in state.selected
                ListRow(
                    row.title,
                    Modifier.fillMaxWidth(),
                    enabled = checked || !state.full,
                    onClick = { onAction(CashbackSetupAction.Toggle(row.category)) },
                    leading = {
                        val glyph = row.category.glyph
                        if (glyph != null) {
                            IconTile(glyph, fill = row.fill(gradients))
                        } else {
                            IconTile(painterResource(Res.drawable.raster_logo_justo), contentDescription = "Jüsto")
                        }
                    },
                    trailing = {
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s16), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(PlataIcons.QuestionMark, { onAction(CashbackSetupAction.Info(row.category)) }, size = IconButtonSize.S20, contentDescription = "About")
                            CheckCircle(if (checked) CheckCircleState.Checked else CheckCircleState.Unchecked)
                        }
                    },
                )
            }
            Spacer(Modifier.height(PlataSafeArea.current.bottom + spacing.s40 * 2))
        }
        if (state.full) {
            Button(
                "Done",
                { onAction(CashbackSetupAction.Done) },
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = PlataSafeArea.current.bottom + spacing.s20)
                    .fillMaxWidth(),
            )
        }
    }
}

// b68fc03a: the check (the lesson-complete art at 84) sits 69 under the sheet bar; tiles 12 apart
private val CheckTop = 66.dp
private val CheckGap = 25.dp
private val TileGap = 12.dp
// the raster's disc fills only 70 of its 84 box; the source disc is 81 across
private const val CheckArtScale = 1.16f

@Composable
private fun ConfirmedStep(onAction: (CashbackSetupAction) -> Unit, modifier: Modifier) {
    val spacing = PlataTheme.spacing
    val dismiss = { onAction(CashbackSetupAction.Dismiss) }
    Column(
        modifier
            .fillMaxSize()
            .background(PlataTheme.colors.surfaceBackground)
            .padding(bottom = PlataSafeArea.current.bottom + spacing.s20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavigationBar(style = NavigationBarStyle.Sheet, leading = { TextButton("Close", dismiss) })
        Spacer(Modifier.height(CheckTop))
        HeadlineBlock(
            "Great choice!\nTime to use your card",
            Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth(),
            body = "You’ll get your cashback at the\nend of the month",
            size = HeadlineBlockSize.Medium,
            align = HeadlineBlockAlign.Center,
            gap = spacing.s4,
            mediaGap = CheckGap,
            media = { Image(painterResource(Res.drawable.raster_lesson_complete_check), null, Modifier.size(84.dp, 88.dp).scale(CheckArtScale)) },
        )
        Spacer(Modifier.weight(1f))
        SectionCard(
            Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth(),
            title = "Get more cashback",
            headerTrailing = { SectionCardAction("Show all", { onAction(CashbackSetupAction.ShowAllPartners) }, Modifier.padding(end = spacing.cardPadding)) },
            contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, bottom = spacing.cardPadding),
        ) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                ImageTile(
                    "Nike",
                    "10%",
                    painterResource(Res.drawable.raster_tile_nike),
                    footer = ImageTileFooter.Percent,
                    logo = { BrandLogo(painterResource(Res.drawable.raster_logo_nike), BrandLogoSize.S40) },
                    onClick = { onAction(CashbackSetupAction.OpenPartner("Nike")) },
                )
                ImageTile(
                    "Booking",
                    "4%",
                    painterResource(Res.drawable.raster_tile_booking),
                    footer = ImageTileFooter.Percent,
                    logo = { BrandLogo(painterResource(Res.drawable.raster_logo_booking), BrandLogoSize.S40) },
                    onClick = { onAction(CashbackSetupAction.OpenPartner("Booking")) },
                )
                ImageTile(
                    "Temu",
                    "30%",
                    painterResource(Res.drawable.raster_tile_temu),
                    footer = ImageTileFooter.Percent,
                    onClick = { onAction(CashbackSetupAction.OpenPartner("Temu")) },
                )
            }
        }
        Spacer(Modifier.height(spacing.s20))
        Button("Done", dismiss, Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth())
    }
}
