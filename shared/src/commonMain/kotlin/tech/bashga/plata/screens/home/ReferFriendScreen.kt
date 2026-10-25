package tech.bashga.plata.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import plata.shared.generated.resources.raster_friend_heart
import plata.shared.generated.resources.raster_friend_hero
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.InviteOffer
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.inviteOffer
import tech.bashga.plata.designsystem.components.BulletItem
import tech.bashga.plata.designsystem.components.BulletMarker
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.Pill
import tech.bashga.plata.designsystem.components.PillTone
import tech.bashga.plata.designsystem.components.PromoHeadline
import tech.bashga.plata.designsystem.components.PromoHeadlineAlign
import tech.bashga.plata.designsystem.components.PromoHeadlineTone
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonWeight
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.HeartFill
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [offer] follows the fake clock, like the Invite tab item ($200 in June, 🔥 1,500 in July). */
@Immutable
data class ReferFriendState(val offer: InviteOffer = InviteOffer.Standard)

sealed interface ReferFriendAction {
    data object Close : ReferFriendAction

    data object CopyLink : ReferFriendAction

    data object ShowQr : ReferFriendAction

    data object ShareLink : ReferFriendAction

    data object AddContact : ReferFriendAction

    data object ShowContacts : ReferFriendAction

    data object Username : ReferFriendAction

    data object Conditions : ReferFriendAction
}

class ReferFriendViewModel(repository: AppRepository) : ViewModel() {
    val state: StateFlow<ReferFriendState> = repository.state.map { ReferFriendState(it.month.inviteOffer) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReferFriendState(repository.snapshot.month.inviteOffer))
}

/** invite: the Bring a friend sheet (the Invite tab and Home's banner present it). */
@Composable
fun ReferFriendScreen(onClose: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { ReferFriendViewModel(repository) }
    val state by viewModel.state.collectAsState()
    ReferFriendContent(state, onAction = { if (it == ReferFriendAction.Close) onClose() })
}

// 63be46ba: the 134×152 art 3 under the bar, the headline 22 under it. adeddd37: the 144×158 art right under the bar,
// the pill 8 under it, the headline 19 under the pill. Both: the link card 40 under the body
private val StandardArtTop = 3.dp
private val StandardArtGap = 22.dp
private val LimitedPillTop = 8.dp
private val LimitedHeadlineTop = 19.dp
private val CardsTop = 40.dp
// both arts sit 12 left of centre in the source
private val ArtShift = (-12).dp
private val StandardFieldHeight = 47.dp
private val LimitedFieldHeight = 48.dp
private val FieldPadding = 16.dp
// Not with Plata: the row 15.5 under the title, the Button 16 under the row; How it works: items 23 apart, 27.4 under the title
private val ContactRowGap = 15.5.dp
private val ContactButtonGap = 16.dp
private val StepsTop = 27.4.dp
private val StepGap = 23.dp
private val StepTextGap = 24.8.dp
private val StepsBottom = 19.dp
// the two-line bar takes over once the headline is under it (5f6bf512)
private val CollapseAfter = 300.dp

@Composable
fun ReferFriendContent(state: ReferFriendState, onAction: (ReferFriendAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    val limited = state.offer == InviteOffer.Limited
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("refer-scroll").backdropSource().verticalScroll(scroll), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(PlataTheme.sizes.navSheet))
            if (limited) LimitedHero() else StandardHero()
            Spacer(Modifier.height(CardsTop))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                InvitationCard(if (limited) LimitedFieldHeight else StandardFieldHeight, onAction)
                SectionCard(Modifier.fillMaxWidth().testTag("not-with-plata"), title = "Not with Plata", headerGap = ContactRowGap) {
                    ListRow("Add contact", horizontalPadding = 0.dp, onClick = { onAction(ReferFriendAction.AddContact) }, leading = {
                        IconCircle(PlataIcons.Plus, IconCircleSize.S40)
                    })
                    Spacer(Modifier.height(ContactButtonGap))
                    Button(
                        "Show my contacts",
                        { onAction(ReferFriendAction.ShowContacts) },
                        Modifier.fillMaxWidth(),
                        style = ButtonStyle.SecondaryOnCard,
                        size = ButtonSize.Medium,
                        labelStyle = PlataTheme.typography.headline,
                    )
                }
                if (limited) HowItWorks()
                ListGroup {
                    ListRow("Username", value = "sofia.plata", showChevron = true, onClick = { onAction(ReferFriendAction.Username) })
                    ListRow("“Bring a friend” conditions", showChevron = true, onClick = { onAction(ReferFriendAction.Conditions) })
                }
                Spacer(Modifier.height(PlataSafeArea.current.bottom + spacing.s40))
            }
        }
        val close: @Composable () -> Unit = { TextButton("Close", { onAction(ReferFriendAction.Close) }) }
        if (collapsed) {
            NavigationBar(
                style = NavigationBarStyle.Sheet,
                title = "Bring a friend",
                subtitle = if (limited) "Get $3,000 for 3 friends" else "Get $200",
                subtitleColor = colors.brandPrimary,
                leading = close,
                material = true,
            )
        } else {
            NavigationBar(style = NavigationBarStyle.Sheet, leading = close)
        }
    }
}

@Composable
private fun ColumnScope.StandardHero() {
    Spacer(Modifier.height(StandardArtTop))
    Image(painterResource(Res.drawable.raster_friend_heart), null, Modifier.offset(x = ArtShift).size(134.dp, 152.dp))
    Spacer(Modifier.height(StandardArtGap))
    PromoHeadline(
        "Get $200",
        Modifier.padding(horizontal = PlataTheme.spacing.screenMargin).fillMaxWidth(),
        align = PromoHeadlineAlign.Center,
        tone = PromoHeadlineTone.Gradient,
        eyebrow = "Bring a friend",
        body = "Your friend will get a card with $0\nannual fee",
    )
}

@Composable
private fun ColumnScope.LimitedHero() {
    Image(painterResource(Res.drawable.raster_friend_hero), null, Modifier.offset(x = ArtShift).size(144.dp, 158.dp))
    Spacer(Modifier.height(LimitedPillTop))
    Pill("15 days left", tone = PillTone.White, icon = PlataIcons.Flame, labelBrush = null)
    Spacer(Modifier.height(LimitedHeadlineTop))
    PromoHeadline(
        "Get $3,000 for",
        Modifier.fillMaxWidth(),
        line2 = "3 friends",
        align = PromoHeadlineAlign.Center,
        tone = PromoHeadlineTone.Brand,
        eyebrow = "Bring a friend",
        body = "Get $500 for the first,\n$1,500 for two,\n$3,000 for three,\nand $500 more for each one after that",
    )
}

@Composable
private fun InvitationCard(fieldHeight: Dp, onAction: (ReferFriendAction) -> Unit) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    SectionCard(
        Modifier.fillMaxWidth(),
        title = "Invitation link",
        headerTrailing = {
            TextButton("Show QR", { onAction(ReferFriendAction.ShowQr) }, weight = TextButtonWeight.Semibold, leadingIcon = PlataIcons.Qr)
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .background(colors.surfaceBackground, ContinuousRoundedShape(PlataTheme.radii.md))
                .padding(horizontal = FieldPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlataText("plata.mx/i/sofia", typography.body, colors.textPrimary, Modifier.weight(1f), maxLines = 1)
            Box(Modifier.pressable(onClickLabel = "Copy link") { onAction(ReferFriendAction.CopyLink) }) {
                Icon(PlataIcons.Copy, size = PlataTheme.sizes.iconMedium, tint = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(PlataTheme.spacing.s20))
        Button("Share link", { onAction(ReferFriendAction.ShareLink) }, Modifier.fillMaxWidth(), size = ButtonSize.Medium, labelStyle = typography.headline)
    }
}

@Composable
private fun HowItWorks() {
    val spacing = PlataTheme.spacing
    val center = Alignment.CenterVertically
    SectionCard(
        title = "How it works",
        headerGap = StepsTop,
        bodySpacing = StepGap,
        contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = StepsBottom),
    ) {
        BulletItem("Share link or show QR to a friend", marker = BulletMarker.Number, number = 1, markerAlignment = center, markerGap = StepTextGap)
        BulletItem("Your friend joins and uses the card", marker = BulletMarker.Number, number = 2, markerAlignment = center, markerGap = StepTextGap)
        BulletItem(
            "Your friend will get a card with $0\nannual fee",
            marker = BulletMarker.Number,
            markerAlignment = center,
            markerGap = StepTextGap,
            ringGlyph = PlataIcons.HeartFill,
        )
    }
}
