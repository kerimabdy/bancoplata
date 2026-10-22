package tech.bashga.plata.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_app_icon_black
import plata.shared.generated.resources.raster_app_icon_festive
import plata.shared.generated.resources.raster_app_icon_orange_edge
import plata.shared.generated.resources.raster_app_icon_plus
import plata.shared.generated.resources.raster_app_icon_sun
import plata.shared.generated.resources.raster_app_icon_vault
import plata.shared.generated.resources.raster_profile_picture
import tech.bashga.plata.data.AppIcon
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Appearance
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.appVersion
import tech.bashga.plata.data.tenure
import tech.bashga.plata.designsystem.components.AppIconImage
import tech.bashga.plata.designsystem.components.AppIconOption
import tech.bashga.plata.designsystem.components.Avatar as AvatarImage
import tech.bashga.plata.designsystem.components.AvatarSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.DialogSecondaryAction
import tech.bashga.plata.designsystem.components.GreetingHeader
import tech.bashga.plata.designsystem.components.GreetingHeaderLayout
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonSize
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.components.IconSize
import tech.bashga.plata.designsystem.components.InlineNotice
import tech.bashga.plata.designsystem.components.InlineNoticeTone
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.OnboardingHero
import tech.bashga.plata.designsystem.components.PlataAppIcon
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.ShieldHalf
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay
import tech.bashga.plata.navigation.SheetOverlay

sealed interface ProfileOverlay {
    data object PictureIntro : ProfileOverlay

    data object PersonalDetails : ProfileOverlay

    data class AppIconChanged(val icon: AppIcon) : ProfileOverlay

    data object LogOutConfirm : ProfileOverlay
}

/** [month] drives the tenure line and the app version (SCREEN-MAP §2.1). */
@Immutable
data class ProfileState(
    val appIcon: AppIcon = AppIcon.Main,
    val appearance: Appearance = Appearance.System,
    val month: Month = Month.June,
    val avatar: Avatar = Avatar.Generated,
    val overlay: ProfileOverlay? = null,
)

sealed interface ProfileAction {
    data object Back : ProfileAction

    data object OpenPlataPlus : ProfileAction

    data object OpenPersonalDetails : ProfileAction

    data object MessageInChat : ProfileAction

    data class SelectIcon(val icon: AppIcon) : ProfileAction

    data object LogOut : ProfileAction

    data object ConfirmLogOut : ProfileAction

    /** Got it, ✕ or a swipe down on PictureIntro. */
    data object DismissPicture : ProfileAction

    data object EditPicture : ProfileAction

    data object CloseOverlay : ProfileAction

    data object OpenDevPanel : ProfileAction

    /** Security, Appearance, Language, Notifications, social, legal: no reference yet. */
    data class OpenRow(val title: String) : ProfileAction
}

fun ProfileState.reduce(action: ProfileAction): ProfileState = when (action) {
    ProfileAction.OpenPersonalDetails -> copy(overlay = ProfileOverlay.PersonalDetails)
    is ProfileAction.SelectIcon -> copy(appIcon = action.icon, overlay = ProfileOverlay.AppIconChanged(action.icon))
    ProfileAction.LogOut -> copy(overlay = ProfileOverlay.LogOutConfirm)
    ProfileAction.DismissPicture, ProfileAction.CloseOverlay, ProfileAction.MessageInChat, ProfileAction.ConfirmLogOut -> copy(overlay = null)
    else -> this
}

private fun ProfileState.withRepository(snapshot: AppSnapshot) =
    copy(appIcon = snapshot.settings.appIcon, appearance = snapshot.settings.appearance, month = snapshot.month, avatar = snapshot.avatar)

class ProfileViewModel(private val repository: AppRepository) : ViewModel() {
    private val local = MutableStateFlow(ProfileState(overlay = ProfileOverlay.PictureIntro.takeIf { repository.snapshot.avatarIsNew }))
    val state: StateFlow<ProfileState> = combine(repository.state, local) { snapshot, state -> state.withRepository(snapshot) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, local.value.withRepository(repository.snapshot))

    fun onAction(action: ProfileAction) {
        val next = state.value.reduce(action)
        when (action) {
            is ProfileAction.SelectIcon -> repository.setAppIcon(action.icon)
            ProfileAction.DismissPicture -> repository.clearNewAvatar()
            ProfileAction.ConfirmLogOut -> repository.logOut()
            else -> Unit
        }
        local.value = next
    }
}

/** home/profile; the Dev panel's long-press entry only reaches [onDevPanel] in debug builds ([devPanelEnabled]). */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onPlataPlus: () -> Unit,
    onSupportChat: () -> Unit,
    onLoggedOut: () -> Unit,
    devPanelEnabled: Boolean,
    onDevPanel: () -> Unit,
) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { ProfileViewModel(repository) }
    val state by viewModel.state.collectAsState()
    ProfileContent(state, onAction = { action ->
        viewModel.onAction(action)
        when (action) {
            ProfileAction.Back -> onBack()
            ProfileAction.OpenPlataPlus -> onPlataPlus()
            ProfileAction.MessageInChat -> onSupportChat()
            ProfileAction.ConfirmLogOut -> onLoggedOut()
            ProfileAction.OpenDevPanel -> if (devPanelEnabled) onDevPanel()
            else -> Unit
        }
    })
}

private val AppIcon.raster: DrawableResource?
    get() = when (this) {
        AppIcon.Main -> null
        AppIcon.Festive -> Res.drawable.raster_app_icon_festive
        AppIcon.Plus -> Res.drawable.raster_app_icon_plus
        AppIcon.Sun -> Res.drawable.raster_app_icon_sun
        AppIcon.Black -> Res.drawable.raster_app_icon_black
        AppIcon.Vault -> Res.drawable.raster_app_icon_vault
        AppIcon.Orange -> Res.drawable.raster_app_icon_orange_edge
    }

// e4b65435 (unscrolled): the 96 avatar 38.3 under the safe area, the first card 47 under the greeting
private val HeaderTop = 38.3.dp
private val HeaderToCards = 47.dp
// the small avatar takes over once the large one is under the bar (8f22e22d)
private val CollapseAfter = 90.dp
// the App icons card: 17pt title 15.3 from the top, icons 25 under it, 21 under the labels; options sit 12 in, 80 apart
private val IconsCardTop = 15.3.dp
private val IconsCardTitleGap = 25.dp
private val IconsCardBottom = 21.dp
private val IconsRowInset = 12.dp
private val IconsGap = 8.dp
// only the left 21 of the Orange icon is ever on screen (the card clips it), so its raster is that sliver
private val OrangeEdgeWidth = 21.dp

@Composable
fun ProfileContent(state: ProfileState, onAction: (ProfileAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val gradients = PlataTheme.gradients
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    val row = { title: String -> { onAction(ProfileAction.OpenRow(title)) } }
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("profile-scroll")
                .verticalScroll(scroll)
                .padding(top = PlataSafeArea.current.top + HeaderTop, bottom = PlataSafeArea.current.bottom + spacing.s20)
                .padding(horizontal = spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            GreetingHeader(
                CustomerName,
                state.month.tenure,
                Modifier.fillMaxWidth().padding(bottom = HeaderToCards - spacing.cardGap),
                layout = GreetingHeaderLayout.Centered,
                avatarImage = state.avatar.painter(),
            )
            ListGroup(Modifier.testTag("plata-plus-row")) {
                ListRow("Plata+", value = "Active", showChevron = true, onClick = { onAction(ProfileAction.OpenPlataPlus) }, leading = {
                    Icon(PlataIcons.PlataPlus, IconSize.Medium)
                })
            }
            ListGroup {
                ListRow("Personal details", showChevron = true, onClick = { onAction(ProfileAction.OpenPersonalDetails) }, leading = {
                    Icon(PlataIcons.Person, IconSize.Medium, brush = gradients.glyph)
                })
                ListRow("Security", showChevron = true, onClick = row("Security"), leading = {
                    Icon(PlataIcons.ShieldHalf, IconSize.Medium, tint = colors.success)
                })
            }
            ListGroup {
                ListRow("Appearance", value = state.appearance.name, showChevron = true, onClick = row("Appearance"), leading = {
                    Icon(PlataIcons.Appearance, IconSize.Medium)
                })
                ListRow("Language", value = "English", showChevron = true, onClick = row("Language"), leading = {
                    Icon(PlataIcons.TextAa, IconSize.Medium, brush = gradients.notification)
                })
                ListRow("Notifications", showChevron = true, onClick = row("Notifications"), leading = {
                    Icon(PlataIcons.Notification, IconSize.Medium, brush = gradients.notification)
                })
            }
            AppIconsCard(state.appIcon, onAction)
            ListGroup {
                ListRow("Follow us on social media", showChevron = true, onClick = row("Follow us on social media"), leading = {
                    Icon(PlataIcons.Link, IconSize.Medium, brush = gradients.glyph)
                })
                ListRow("Legal documents", showChevron = true, onClick = row("Legal documents"), leading = {
                    Icon(PlataIcons.Document, IconSize.Medium, brush = gradients.glyph)
                })
                AppVersionRow(state.month.appVersion, onLongPress = { onAction(ProfileAction.OpenDevPanel) })
            }
            Button("Log out", { onAction(ProfileAction.LogOut) }, Modifier.fillMaxWidth(), style = ButtonStyle.Destructive)
        }
        TabBarBackdrop(Modifier.align(Alignment.BottomCenter))
        NavigationBar(
            onBack = { onAction(ProfileAction.Back) },
            center = if (collapsed) {
                { AvatarImage(AvatarSize.Small, image = state.avatar.painter()) }
            } else {
                null
            },
            material = collapsed,
            includeStatusBar = true,
        )
    }
    SheetOverlay(state.overlay as? ProfileOverlay.PictureIntro, onDismiss = { onAction(ProfileAction.DismissPicture) }) {
        PictureIntroSheet(onAction)
    }
    SheetOverlay(state.overlay as? ProfileOverlay.PersonalDetails, onDismiss = { onAction(ProfileAction.CloseOverlay) }) {
        PersonalDetailsSheet(onAction)
    }
    Overlay(state.overlay as? ProfileOverlay.AppIconChanged, onDismiss = { onAction(ProfileAction.CloseOverlay) }) { changed ->
        AppIconChangedAlert(changed.icon, onOk = { onAction(ProfileAction.CloseOverlay) })
    }
    Overlay(state.overlay as? ProfileOverlay.LogOutConfirm, onDismiss = { onAction(ProfileAction.CloseOverlay) }) {
        LogOutAlert(onCancel = { onAction(ProfileAction.CloseOverlay) }, onLogOut = { onAction(ProfileAction.ConfirmLogOut) })
    }
}

@Composable
private fun AppIconsCard(selected: AppIcon, onAction: (ProfileAction) -> Unit) {
    val spacing = PlataTheme.spacing
    SectionCard(contentPadding = PaddingValues(top = IconsCardTop, bottom = IconsCardBottom)) {
        PlataText("App icons", PlataTheme.typography.body, PlataTheme.colors.textPrimary, Modifier.padding(horizontal = spacing.cardPadding))
        Spacer(Modifier.height(IconsCardTitleGap))
        Row(
            Modifier.fillMaxWidth().testTag("app-icons-row").horizontalScroll(rememberScrollState()).padding(horizontal = IconsRowInset),
            horizontalArrangement = Arrangement.spacedBy(IconsGap),
        ) {
            AppIcon.entries.forEach { icon ->
                AppIconOption(icon.name, selected = selected == icon, onClick = { onAction(ProfileAction.SelectIcon(icon)) }) {
                    val raster = icon.raster
                    when {
                        raster == null -> PlataAppIcon()
                        icon == AppIcon.Orange -> Box(Modifier.size(PlataTheme.sizes.buttonLarge)) {
                            Image(painterResource(raster), icon.name, Modifier.size(OrangeEdgeWidth, PlataTheme.sizes.buttonLarge), contentScale = ContentScale.FillBounds)
                        }
                        else -> AppIconImage(painterResource(raster), icon.name)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppVersionRow(version: String, onLongPress: () -> Unit) {
    ListRow(
        "App version",
        Modifier.testTag("app-version").combinedClickable(interactionSource = null, indication = null, onLongClickLabel = "Dev panel", onLongClick = onLongPress, onClick = {}),
        value = version,
        leading = { Icon(PlataIcons.Square, IconSize.Medium, brush = PlataTheme.gradients.glyph) },
    )
}

// e97d2568: the title 55 under the panel top, the Button 43.5 under the title, the secondary action ending 4.4 above the
// inset; the glass close disc 14.4 under the sheet top, 20.5 from its right edge
private val PictureTitleTop = 55.dp
private val PictureActionsGap = 43.5.dp
private val PictureActionsBottom = 4.4.dp
private val PictureCloseTop = 14.4.dp
private val PictureCloseEnd = 20.5.dp

@Composable
private fun PictureIntroSheet(onAction: (ProfileAction) -> Unit) {
    Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceCard)) {
        OnboardingHero(
            "$CustomerName,\nhere is your\nprofile picture",
            align = HeadlineBlockAlign.Center,
            horizontalPadding = PlataTheme.spacing.screenMargin,
            topPadding = PictureTitleTop,
            actionsGap = PictureActionsGap,
            bottomPadding = PictureActionsBottom,
            media = {
                Image(
                    painterResource(Res.drawable.raster_profile_picture),
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter,
                )
            },
        ) {
            Button("Got it", { onAction(ProfileAction.DismissPicture) }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(PlataTheme.spacing.s12))
            DialogSecondaryAction("Edit profile picture", { onAction(ProfileAction.EditPicture) }, Modifier.align(Alignment.CenterHorizontally))
        }
        IconButton(
            PlataIcons.CloseX,
            { onAction(ProfileAction.DismissPicture) },
            Modifier.align(Alignment.TopEnd).padding(top = PictureCloseTop, end = PictureCloseEnd),
            style = IconButtonStyle.Glass,
            size = IconButtonSize.S32,
            contentDescription = "Close",
        )
    }
}

// d4c491d6: the first card starts 22 under the sheet bar
private val DetailsTop = 22.dp

// the source redacts the values; these are placeholders
private val personalDetails = listOf(
    "Name" to "Sofía Martínez Ruiz",
    "Date of birth" to "12 Mar 1994",
    "RFC" to "MARS940312AB1",
    "Phone number" to "+52 55 1234 5678",
)

@Composable
private fun PersonalDetailsSheet(onAction: (ProfileAction) -> Unit) {
    val spacing = PlataTheme.spacing
    Column(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            title = "Personal details",
            leading = { TextButton("Close", { onAction(ProfileAction.CloseOverlay) }) },
        )
        Spacer(Modifier.height(DetailsTop))
        Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
            ListGroup {
                personalDetails.forEach { (label, value) -> ListRow(label, value = value) }
                InlineNotice(
                    "To update your details send us\na message in chat",
                    Modifier.fillMaxWidth().padding(start = spacing.s20, top = spacing.s12, end = spacing.s20, bottom = spacing.s20),
                    tone = InlineNoticeTone.Info,
                    onClick = { onAction(ProfileAction.MessageInChat) },
                )
            }
            ListGroup { ListRow("Email", value = "sofia.martinez@gmail.com") }
        }
    }
}

// iOS alerts: 270 wide, radius 14, 44 action rows. 748df345: the icon alert at y 366, 60 icon 8 in and 20 down, 16 to the
// title. d0087d8e: the log-out alert at y 357, text 19 from the top and 16 from the sides
private val AlertWidth = 270.dp
private val AlertRadius = 14.dp
private val AlertHairline = 0.5.dp
private val IconAlertTop = 366.dp
private val AlertIcon = 60.dp
private val AlertIconRadius = 13.5.dp
private val AlertIconInset = 8.dp
private val AlertIconTop = 20.dp
private val AlertTextGap = 16.dp
private val LogOutAlertTop = 357.33.dp
private val AlertTextTop = 19.dp
private val AlertTextInset = 16.dp
private val AlertMessageGap = 2.dp
private val AlertTextBottom = 21.dp

@Composable
private fun AlertFrame(top: Dp, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(PlataTheme.colors.sheetDim).clickable(interactionSource = null, indication = null) {}) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = top)
                .width(AlertWidth)
                .clip(ContinuousRoundedShape(AlertRadius))
                .background(PlataTheme.colors.surfaceControl),
        ) { content() }
    }
}

@Composable
private fun AppIconChangedAlert(icon: AppIcon, onOk: () -> Unit) {
    val colors = PlataTheme.colors
    AlertFrame(IconAlertTop) {
        Row(Modifier.padding(start = AlertIconInset, top = AlertIconTop, bottom = AlertIconTop), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(AlertIcon).clip(ContinuousRoundedShape(AlertIconRadius))) {
                val raster = icon.raster
                if (raster == null) PlataAppIcon() else Image(painterResource(raster), "${icon.name} app icon", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Spacer(Modifier.width(AlertTextGap))
            PlataText("You have changed the\nicon for “Plata”.", PlataTheme.typography.headline, colors.textPrimary)
        }
        Box(Modifier.fillMaxWidth().height(AlertHairline).background(colors.borderSubtle))
        Box(Modifier.fillMaxWidth().height(PlataTheme.sizes.nav).pressable(onClick = onOk), contentAlignment = Alignment.Center) {
            PlataText("OK", PlataTheme.typography.body, colors.textPrimary)
        }
    }
}

@Composable
private fun LogOutAlert(onCancel: () -> Unit, onLogOut: () -> Unit) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    AlertFrame(LogOutAlertTop) {
        Column(
            Modifier.fillMaxWidth().padding(start = AlertTextInset, end = AlertTextInset, top = AlertTextTop, bottom = AlertTextBottom),
            verticalArrangement = Arrangement.spacedBy(AlertMessageGap),
        ) {
            PlataText("Are you sure you want to\nlog out?", typography.headline, colors.textPrimary, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            PlataText(
                "You will need to enter your phone\nnumber to log in again",
                typography.footnoteTight,
                colors.textPrimary,
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        Box(Modifier.fillMaxWidth().height(AlertHairline).background(colors.borderSubtle))
        Row(Modifier.fillMaxWidth().height(PlataTheme.sizes.nav)) {
            Box(Modifier.weight(1f).fillMaxHeight().pressable(onClick = onCancel), contentAlignment = Alignment.Center) {
                PlataText("Cancel", typography.body, colors.accentSystem)
            }
            Box(Modifier.width(AlertHairline).fillMaxHeight().background(colors.borderSubtle))
            Box(Modifier.weight(1f).fillMaxHeight().pressable(onClick = onLogOut), contentAlignment = Alignment.Center) {
                PlataText("Log out", typography.body, colors.dangerSystem)
            }
        }
    }
}
