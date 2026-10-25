package tech.bashga.plata.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_dialog_delivered
import plata.shared.generated.resources.raster_dialog_heart
import plata.shared.generated.resources.raster_promo_friend
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.CardId
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.InviteOffer
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.Stage
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.screens.home.money.creditStatus
import tech.bashga.plata.screens.home.money.homeLabel
import tech.bashga.plata.data.inviteOffer
import tech.bashga.plata.data.spent
import tech.bashga.plata.designsystem.components.Avatar as AvatarImage
import tech.bashga.plata.designsystem.components.AvatarSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CardThumb
import tech.bashga.plata.designsystem.components.CardThumbKind
import tech.bashga.plata.designsystem.components.CreditSummaryCard
import tech.bashga.plata.designsystem.components.Dialog
import tech.bashga.plata.designsystem.components.DialogCloseTone
import tech.bashga.plata.designsystem.components.DialogPlacement
import tech.bashga.plata.designsystem.components.DialogSecondaryAction
import tech.bashga.plata.designsystem.components.GreetingHeader
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.IconCircleTone
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.PromoCard
import tech.bashga.plata.designsystem.components.PromoCardLayout
import tech.bashga.plata.designsystem.components.PromoHeadline
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAction
import tech.bashga.plata.designsystem.components.ShortcutTile
import tech.bashga.plata.designsystem.components.ShortcutTileGlyphRow
import tech.bashga.plata.designsystem.components.ShortcutTitleTone
import tech.bashga.plata.designsystem.components.SpendOrb
import tech.bashga.plata.designsystem.components.SpendOrbState
import tech.bashga.plata.designsystem.components.SummaryCard
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.CalendarGrid
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.LocalIsTopEntry
import tech.bashga.plata.navigation.Overlay

sealed interface HomeStage {
    /** The pre-shell delivery feed: arriving on [date] at [address]. */
    data class CardComing(val date: String, val address: String) : HomeStage

    data object Live : HomeStage
}

enum class HomeOverlay { DeliveryArranged, AmbassadorFeedback }

/** One feed for every stage (SCREEN-MAP §3.3): FirstDay and Active are values of these fields, not modes. */
@Immutable
data class HomeState(
    val stage: HomeStage = HomeStage.Live,
    val avatar: Avatar = Avatar.Placeholder,
    val avatarIsNew: Boolean = false,
    val cashback: Set<CashbackCategory> = emptySet(),
    val msiVisited: Boolean = false,
    val available: Long = 3_500_00,
    val spent: Long = 0,
    val month: Month = Month.June,
    val overlay: HomeOverlay? = null,
)

sealed interface HomeAction {
    data object OpenProfile : HomeAction

    data object OpenCashback : HomeAction

    data object OpenMsi : HomeAction

    data object OpenTransactions : HomeAction

    data object OpenCredit : HomeAction

    data class OpenCard(val card: CardId) : HomeAction

    data object AddCard : HomeAction

    data object OpenPaymentDates : HomeAction

    data object OpenReferFriend : HomeAction

    data object OpenDeliveryDetails : HomeAction

    data object CloseDeliveryArranged : HomeAction

    data object AddToCalendar : HomeAction

    data class RateAmbassador(val liked: Boolean) : HomeAction

    data object CloseAmbassadorFeedback : HomeAction

    /** Home is the top entry again (a sheet over it closed). */
    data object Resumed : HomeAction
}

/** What only this screen knows: whether the feedback dialog stepped aside for its sheet. */
data class HomeLocal(val feedbackHidden: Boolean = false)

fun HomeLocal.reduce(action: HomeAction): HomeLocal = when (action) {
    is HomeAction.RateAmbassador -> copy(feedbackHidden = true)
    HomeAction.Resumed -> copy(feedbackHidden = false)
    else -> this
}

fun homeState(snapshot: AppSnapshot, local: HomeLocal): HomeState {
    val coming = snapshot.stage as? Stage.CardComing
    val address = snapshot.delivery.address
    val feedback = snapshot.stage == Stage.Live && snapshot.activation == Activation.FeedbackPending && !local.feedbackHidden
    return HomeState(
        stage = if (coming != null) HomeStage.CardComing(coming.date, address?.let { "${it.street}, ${it.area}" }.orEmpty()) else HomeStage.Live,
        avatar = snapshot.avatar,
        avatarIsNew = snapshot.avatarIsNew,
        cashback = snapshot.homeTiles.cashback,
        msiVisited = snapshot.homeTiles.msiVisited,
        available = snapshot.creditAvailable,
        spent = snapshot.spent(),
        month = snapshot.month,
        overlay = HomeOverlay.DeliveryArranged.takeIf { coming != null && snapshot.deliveryArrangedPending }
            ?: HomeOverlay.AmbassadorFeedback.takeIf { feedback },
    )
}

class HomeViewModel(private val repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(HomeLocal())
    val state: StateFlow<HomeState> = combine(repository.state, local, ::homeState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, homeState(repository.snapshot, local.value))

    fun onAction(action: HomeAction) {
        local.update { it.reduce(action) }
        if (action == HomeAction.CloseAmbassadorFeedback) repository.startActivation()
        if (action == HomeAction.CloseDeliveryArranged) repository.consumeDeliveryArranged()
    }
}

/**
 * home: the pre-shell root while the card is coming, then the Home tab root. DeliveryArranged shows once after scheduling;
 * the feedback dialog follows `activation`. The lambdas are the §3 destinations.
 */
@Composable
fun HomeScreen(
    onProfile: () -> Unit,
    onCashback: () -> Unit,
    onMsi: () -> Unit,
    onTransactions: () -> Unit,
    onCredit: () -> Unit,
    onCard: (CardId) -> Unit,
    onPaymentDates: () -> Unit,
    onReferFriend: () -> Unit,
    onDeliveryDetails: () -> Unit,
    onFeedback: (liked: Boolean) -> Unit,
    onActivation: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { HomeViewModel(repository) }
    val state by viewModel.state.collectAsState()
    val isTop = LocalIsTopEntry.current
    LaunchedEffect(isTop) { if (isTop) viewModel.onAction(HomeAction.Resumed) }
    HomeContent(state, onAction = { action ->
        viewModel.onAction(action)
        when (action) {
            HomeAction.OpenProfile -> onProfile()
            HomeAction.OpenCashback -> onCashback()
            HomeAction.OpenMsi -> onMsi()
            HomeAction.OpenTransactions -> onTransactions()
            HomeAction.OpenCredit -> onCredit()
            is HomeAction.OpenCard -> onCard(action.card)
            HomeAction.OpenPaymentDates -> onPaymentDates()
            HomeAction.OpenReferFriend -> onReferFriend()
            HomeAction.OpenDeliveryDetails -> onDeliveryDetails()
            is HomeAction.RateAmbassador -> onFeedback(action.liked)
            HomeAction.CloseAmbassadorFeedback -> onActivation()
            else -> Unit
        }
    })
}

// 9f1aa9fc: avatar at y 104, headline box at 208.5, shortcut tiles at 316.3, cards 20 apart
private val GreetingTop = 45.dp
private val GreetingToHeadline = 48.5.dp
private val HeadlineToTiles = 36.dp
private val ShortcutGap = 17.dp
// ec4a6b30: avatar at y 110, title box at 208.5, card at 314
private val ComingGreetingTop = 51.dp
private val ComingGreetingToTitle = 43.5.dp
private val ComingTitleToCard = 23.5.dp
// the small nav avatar takes over once the greeting is under the bar (f5db1665)
private val CollapseAfter = 40.dp
private val FeedBottom = 37.dp

@Composable
fun HomeContent(state: HomeState, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    val safeArea = PlataSafeArea.current
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("home-scroll").verticalScroll(scroll)) {
            when (val stage = state.stage) {
                is HomeStage.CardComing -> CardComingFeed(stage, onAction)
                HomeStage.Live -> LiveFeed(state, onAction)
            }
            Spacer(Modifier.height(safeArea.bottom + FeedBottom))
        }
        if (state.stage == HomeStage.Live) {
            TabBarBackdrop(Modifier.align(Alignment.BottomCenter))
        }
        if (state.stage == HomeStage.Live && collapsed) {
            NavigationBar(material = true, includeStatusBar = true, center = { AvatarImage(AvatarSize.Small, image = state.avatar.painter()) })
        }
    }
    Overlay(state.overlay?.takeIf { it == HomeOverlay.DeliveryArranged }, onDismiss = { onAction(HomeAction.CloseDeliveryArranged) }) {
        DeliveryArrangedDialog(onAction)
    }
    Overlay(state.overlay?.takeIf { it == HomeOverlay.AmbassadorFeedback }, onDismiss = { onAction(HomeAction.CloseAmbassadorFeedback) }) {
        AmbassadorFeedbackDialog(onAction)
    }
}

@Composable
private fun CardComingFeed(stage: HomeStage.CardComing, onAction: (HomeAction) -> Unit) {
    val spacing = PlataTheme.spacing
    Spacer(Modifier.height(PlataSafeArea.current.top + ComingGreetingTop))
    GreetingHeader("Hola, $CustomerName", "Welcome to Plata", Modifier.padding(horizontal = spacing.s40), showBadge = false)
    Spacer(Modifier.height(ComingGreetingToTitle))
    PlataText(
        "Your card is coming\n${stage.date}",
        PlataTheme.typography.largeTitle,
        PlataTheme.colors.textPrimary,
        Modifier.padding(horizontal = spacing.s40),
    )
    Spacer(Modifier.height(ComingTitleToCard))
    SectionCard(
        Modifier.padding(horizontal = spacing.screenMargin),
        title = "Card delivery",
        headerTrailing = { SectionCardAction("Full info", { onAction(HomeAction.OpenDeliveryDetails) }) },
    ) {
        ListRow("Delivery address", subtitle = stage.address, horizontalPadding = 0.dp)
    }
}

@Composable
private fun LiveFeed(state: HomeState, onAction: (HomeAction) -> Unit) {
    val spacing = PlataTheme.spacing
    Spacer(Modifier.height(PlataSafeArea.current.top + GreetingTop))
    GreetingHeader(
        CustomerName,
        "Welcome to Plata",
        Modifier.padding(horizontal = spacing.s40).pressable(onClickLabel = "Profile") { onAction(HomeAction.OpenProfile) },
        avatarImage = state.avatar.painter(),
        sparkle = state.avatarIsNew,
    )
    Spacer(Modifier.height(GreetingToHeadline))
    PromoHeadline("Use your card", Modifier.padding(horizontal = spacing.s40), line2 = "to get cashback", onClick = { onAction(HomeAction.OpenCashback) })
    Spacer(Modifier.height(HeadlineToTiles))
    Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ShortcutGap)) {
            val cashback = { onAction(HomeAction.OpenCashback) }
            if (state.cashback.isEmpty()) {
                ShortcutTile(
                    "Choose",
                    Modifier.weight(1f),
                    titleTone = ShortcutTitleTone.Primary,
                    titleSecondLine = "cashback",
                    badge = "3",
                    onClick = cashback,
                )
            } else {
                ShortcutTile("Cashback", Modifier.weight(1f), onClick = cashback) {
                    ShortcutTileGlyphRow(CashbackCategory.entries.filter { it in state.cashback }.mapNotNull { it.glyph })
                }
            }
            ShortcutTile(
                "MSI",
                Modifier.weight(1f),
                titleTone = ShortcutTitleTone.Primary,
                titleIcon = PlataIcons.Msi,
                subtitle = if (state.msiVisited) "109 partners" else "None to defer",
                badge = if (state.msiVisited) null else "1",
                onClick = { onAction(HomeAction.OpenMsi) },
            )
        }
        SummaryCard("Transactions", subtitle = spentLabel(state.spent, state.month), onClick = { onAction(HomeAction.OpenTransactions) }) {
            SpendOrb(if (state.spent > 0) SpendOrbState.Active else SpendOrbState.Empty)
        }
        CreditSummaryCard(
            formatMoney(state.available),
            "Available in Plata Crédito",
            Modifier.pressable(onClickLabel = "Plata Crédito") { onAction(HomeAction.OpenCredit) },
            status = state.month.creditStatus.homeLabel,
            onRowAction = { onAction(HomeAction.OpenPaymentDates) },
        ) {
            CardThumb(CardThumbKind.Physical, onClick = { onAction(HomeAction.OpenCard(CardId.Physical)) })
            CardThumb(CardThumbKind.Digital, onClick = { onAction(HomeAction.OpenCard(CardId.Digital)) })
            CardThumb(CardThumbKind.Add, onClick = { onAction(HomeAction.AddCard) })
        }
        val reward = if (state.month.inviteOffer == InviteOffer.Standard) "$200" else "$3,000"
        PromoCard(
            "Bring a friend and\nget $reward",
            Modifier.testTag("bring-a-friend"),
            layout = PromoCardLayout.Banner,
            onClick = { onAction(HomeAction.OpenReferFriend) },
        ) {
            Image(
                painterResource(Res.drawable.raster_promo_friend),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 1.dp).size(116.33.dp, 84.33.dp),
            )
        }
    }
}

@Composable
private fun DeliveryArrangedDialog(onAction: (HomeAction) -> Unit) {
    val close = { onAction(HomeAction.CloseDeliveryArranged) }
    DialogPlacement(onDismiss = close) {
        Dialog(
            "Delivery successfully arranged!",
            body = "An ambassador will call you before the\ndelivery. Don’t forget your INE!",
            onClose = close,
            mediaContent = { Image(painterResource(Res.drawable.raster_dialog_delivered), null, Modifier.size(85.dp, 90.33.dp)) },
            contentTop = 62.dp,
            mediaGap = 30.dp,
            textGap = PlataTheme.spacing.s12,
            actionsGap = PlataTheme.spacing.s40,
            closeTop = 21.dp,
            closeTone = DialogCloseTone.Primary,
            actions = {
                Button("Done", close, Modifier.fillMaxWidth())
                DialogSecondaryAction("Add to calendar", { onAction(HomeAction.AddToCalendar) }, icon = PlataIcons.CalendarGrid)
            },
        )
    }
}

@Composable
private fun AmbassadorFeedbackDialog(onAction: (HomeAction) -> Unit) {
    val close = { onAction(HomeAction.CloseAmbassadorFeedback) }
    DialogPlacement(onDismiss = close) {
        // b95e6964 sets its blocks tighter than the other dialogs
        Dialog(
            "How was the meeting with\nAmbassador?",
            body = "Thumbs up if it went well",
            onClose = close,
            mediaContent = { Image(painterResource(Res.drawable.raster_dialog_heart), null, Modifier.size(82.67.dp, 83.33.dp)) },
            contentTop = 55.dp,
            mediaGap = 29.dp,
            textGap = PlataTheme.spacing.s4,
            extrasGap = 33.dp,
            bottomPadding = 52.dp,
            closeTop = PlataTheme.spacing.s20,
            extras = {
                Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s40)) {
                    IconCircle(
                        PlataIcons.ThumbsDown,
                        IconCircleSize.S80,
                        Modifier.pressable(onClickLabel = "Thumbs down") { onAction(HomeAction.RateAmbassador(liked = false)) },
                        tint = PlataTheme.colors.textSecondary,
                    )
                    IconCircle(
                        PlataIcons.ThumbsUp,
                        IconCircleSize.S80,
                        Modifier.pressable(onClickLabel = "Thumbs up") { onAction(HomeAction.RateAmbassador(liked = true)) },
                        tone = IconCircleTone.Success,
                    )
                }
            },
        )
    }
}
