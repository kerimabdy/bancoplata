package tech.bashga.plata.screenshots

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.data.AppIcon
import tech.bashga.plata.data.Appearance
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.CardId
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.InviteOffer
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.CardFace
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.StaticLayer
import tech.bashga.plata.screens.delivery.ProductTourContent
import tech.bashga.plata.screens.delivery.ProductTourState
import tech.bashga.plata.screens.delivery.TourPage
import tech.bashga.plata.screens.home.CardDetailsContent
import tech.bashga.plata.screens.home.CardDetailsOverlay
import tech.bashga.plata.screens.home.CardDetailsState
import tech.bashga.plata.screens.home.CardReveal
import tech.bashga.plata.screens.home.CashbackSetupContent
import tech.bashga.plata.screens.home.CashbackSetupState
import tech.bashga.plata.screens.home.CashbackStep
import tech.bashga.plata.screens.home.HomeContent
import tech.bashga.plata.screens.home.HomeOverlay
import tech.bashga.plata.screens.home.HomeStage
import tech.bashga.plata.screens.home.HomeState
import tech.bashga.plata.screens.home.PartnersContent
import tech.bashga.plata.screens.home.PartnersState
import tech.bashga.plata.screens.home.PlataPlusContent
import tech.bashga.plata.screens.home.ProfileContent
import tech.bashga.plata.screens.home.ProfileOverlay
import tech.bashga.plata.screens.home.ProfileState
import tech.bashga.plata.screens.home.ReferFriendContent
import tech.bashga.plata.screens.home.ReferFriendState

/** SCREEN-MAP §3.3, Home part A: one test per reference id, named after it. */
class HomeStateScreenshots : ScreenshotTest() {
    private val firstDay = HomeState(avatar = Avatar.Generated, avatarIsNew = true)
    private val active = HomeState(
        avatar = Avatar.Generated,
        cashback = setOf(CashbackCategory.AllPurchases, CashbackCategory.Airline, CashbackCategory.Entertainment, CashbackCategory.Restaurants),
        msiVisited = true,
        available = 3_470_00,
        spent = 70_00,
    )
    private val profile = ProfileState(avatar = Avatar.Generated)

    // a sheet's top edge sits 69 down (safe area 59 + the 10 sheet drop)
    private val sheetTop = 69.dp

    private fun fromHome(state: HomeState = firstDay) = listOf(StaticLayer(Presentation.Push) { HomeContent(state, {}) })

    private fun fromProfile() = listOf(StaticLayer(Presentation.Push) { ProfileContent(profile, {}) })

    // the Activation cover's ProductTour(ViewPin) behind the Dimmed card sheet
    private fun fromProductTour() = listOf(StaticLayer(Presentation.Cover) { ProductTourContent(ProductTourState(TourPage.ViewPin), {}) })

    // Home

    @Test fun `ec4a6b30`() = screenState("ec4a6b30") {
        HomeContent(HomeState(stage = HomeStage.CardComing("3 Jun", "Calle Durango 263, Roma Norte"), overlay = HomeOverlay.DeliveryArranged), {})
    }

    @Test fun `b95e6964`() = screenState("b95e6964", shell = TestShell(TabBarTab.Home, homeBadge = 2)) {
        HomeContent(HomeState(avatar = Avatar.Placeholder, overlay = HomeOverlay.AmbassadorFeedback), {})
    }

    @Test fun `9f1aa9fc`() = screenState("9f1aa9fc", shell = TestShell(TabBarTab.Home, Month.June, homeBadge = 2)) {
        HomeContent(firstDay, {})
    }

    @Test fun `f5db1665`() = screenState(
        "f5db1665",
        shell = TestShell(TabBarTab.Home, Month.June, homeBadge = 2),
        setup = { scrollToAnchor("bring-a-friend", top = 663.dp, scrollableTag = "home-scroll") },
    ) { HomeContent(firstDay, {}) }

    @Test fun `025e71c5 9533117e`() = screenState("025e71c5", "9533117e", shell = TestShell(TabBarTab.Home, Month.June)) {
        HomeContent(active, {})
    }

    // CardDetails

    @Test fun `3b3eaed7 cd7708d3`() = screenState("3b3eaed7", "cd7708d3", presentation = Presentation.Sheet, under = fromProductTour()) {
        CardDetailsContent(CardDetailsState(CardId.Physical), {})
    }

    @Test fun `a634583b`() = screenState("a634583b", presentation = Presentation.Sheet, under = fromProductTour()) {
        CardDetailsContent(CardDetailsState(CardId.Physical, face = CardFace.Details), {})
    }

    @Test fun `fa8cfa51`() = screenState("fa8cfa51", presentation = Presentation.Sheet, under = fromProductTour()) {
        CardDetailsContent(CardDetailsState(CardId.Physical, overlay = CardDetailsOverlay.FreezeConfirm), {})
    }

    @Test fun `697dbf17`() = screenState("697dbf17", presentation = Presentation.Sheet, under = fromProductTour()) {
        CardDetailsContent(CardDetailsState(CardId.Physical, frozen = true), {})
    }

    // fixture: the digital card shows $3,450 where Home shows $3,500 (§2.1)
    @Test fun `202bee50`() = screenState("202bee50", presentation = Presentation.Sheet, under = fromHome()) {
        CardDetailsContent(CardDetailsState(CardId.Digital, available = 3_450_00), {})
    }

    @Test fun `7464b63b`() = screenState("7464b63b", presentation = Presentation.Sheet, under = fromHome(), clockMillis = 600) {
        CardDetailsContent(CardDetailsState(CardId.Digital, face = CardFace.Details, reveal = CardReveal.Loading, available = 3_450_00), {})
    }

    // Profile

    @Test fun `e97d2568`() = screenState("e97d2568") {
        ProfileContent(profile.copy(overlay = ProfileOverlay.PictureIntro), {})
    }

    @Test fun `e4b65435`() = screenState("e4b65435", shell = TestShell(TabBarTab.Home, Month.June)) {
        ProfileContent(profile, {})
    }

    // fixture: settings.appearance=Light (the Appearance picker has no reference)
    @Test fun `8f22e22d`() = screenState(
        "8f22e22d",
        shell = TestShell(TabBarTab.Home, Month.July),
        setup = {
            scrollToAnchor("plata-plus-row", top = 157.4.dp, scrollableTag = "profile-scroll")
            scrollHorizontallyBy(169.3.dp, "app-icons-row")
        },
    ) { ProfileContent(profile.copy(appIcon = AppIcon.Black, appearance = Appearance.Light, month = Month.July), {}) }

    @Test fun `59741a55`() = screenState("59741a55", shell = TestShell(TabBarTab.Home, Month.June), setup = { scrollBy(2_000.dp, "profile-scroll") }) {
        ProfileContent(profile, {})
    }

    @Test fun `748df345`() = screenState(
        "748df345",
        shell = TestShell(TabBarTab.Home, Month.June),
        setup = {
            scrollBy(2_000.dp, "profile-scroll")
            scrollHorizontallyBy(169.3.dp, "app-icons-row")
        },
    ) { ProfileContent(profile.copy(appIcon = AppIcon.Black, overlay = ProfileOverlay.AppIconChanged(AppIcon.Black)), {}) }

    @Test fun `d0087d8e`() = screenState(
        "d0087d8e",
        shell = TestShell(TabBarTab.Home, Month.July, supportBadge = 4),
        setup = { scrollBy(2_000.dp, "profile-scroll") },
    ) { ProfileContent(profile.copy(month = Month.July, overlay = ProfileOverlay.LogOutConfirm), {}) }

    @Test fun `d4c491d6`() = screenState("d4c491d6") {
        ProfileContent(profile.copy(overlay = ProfileOverlay.PersonalDetails), {})
    }

    // PlataPlus

    @Test fun `787363a9`() = screenState("787363a9", presentation = Presentation.Sheet, under = fromProfile()) { PlataPlusContent({}) }

    @Test fun `835216e3`() = screenState(
        "835216e3",
        presentation = Presentation.Sheet,
        under = fromProfile(),
        setup = { scrollToAnchor("benefits-1", top = sheetTop + 69.7.dp, scrollableTag = "plata-plus-scroll") },
    ) { PlataPlusContent({}) }

    @Test fun `5b0f3d7c`() = screenState(
        "5b0f3d7c",
        presentation = Presentation.Sheet,
        under = fromProfile(),
        setup = { scrollBy(5_000.dp, "plata-plus-scroll") },
    ) { PlataPlusContent({}) }

    // CashbackSetup

    @Test fun `2d2d03c9`() = screenState("2d2d03c9", presentation = Presentation.Sheet, under = fromHome()) {
        CashbackSetupContent(CashbackSetupState(CashbackStep.Intro), {})
    }

    @Test fun `e7edfe4c`() = screenState("e7edfe4c", presentation = Presentation.Sheet, under = fromHome()) {
        CashbackSetupContent(CashbackSetupState(CashbackStep.Select), {})
    }

    @Test fun `84e28520`() = screenState("84e28520", presentation = Presentation.Sheet, under = fromHome()) {
        val selected = setOf(CashbackCategory.AllPurchases, CashbackCategory.Airline, CashbackCategory.HomeDiy, CashbackCategory.Mobile)
        CashbackSetupContent(CashbackSetupState(CashbackStep.Select, selected), {})
    }

    @Test fun `b68fc03a`() = screenState("b68fc03a", presentation = Presentation.Sheet, under = fromHome()) {
        CashbackSetupContent(CashbackSetupState(CashbackStep.Confirmed), {})
    }

    // Partners, pushed inside the CashbackSetup sheet

    @Test fun `816019bf`() = screenState("816019bf", presentation = Presentation.Sheet, under = fromHome()) {
        PartnersContent(PartnersState(), {})
    }

    @Test fun `8417fbd4`() = screenState(
        "8417fbd4",
        presentation = Presentation.Sheet,
        under = fromHome(),
        setup = {
            rule.onNode(hasScrollToIndexAction() and hasAnyAncestor(hasTestTag("partners-hero")), useUnmergedTree = true).performScrollToIndex(1)
            rule.waitForIdle()
        },
    ) { PartnersContent(PartnersState(), {}) }

    @Test fun `917b0cb2`() = screenState(
        "917b0cb2",
        presentation = Presentation.Sheet,
        under = fromHome(),
        // the anchor has to be on screen for its bounds to measure
        setup = {
            scrollBy(500.dp, "directory-scroll")
            scrollToAnchor("partner-booking", top = sheetTop + 69.dp, scrollableTag = "directory-scroll")
        },
    ) { PartnersContent(PartnersState(), {}) }

    // ReferFriend

    @Test fun `63be46ba`() = screenState("63be46ba", presentation = Presentation.Sheet, under = fromHome()) {
        ReferFriendContent(ReferFriendState(InviteOffer.Standard), {})
    }

    @Test fun `adeddd37`() = screenState("adeddd37", presentation = Presentation.Sheet, under = fromHome()) {
        ReferFriendContent(ReferFriendState(InviteOffer.Limited), {})
    }

    @Test fun `5f6bf512`() = screenState(
        "5f6bf512",
        presentation = Presentation.Sheet,
        under = fromHome(),
        setup = { scrollToAnchor("not-with-plata", top = 192.8.dp, scrollableTag = "refer-scroll") },
    ) { ReferFriendContent(ReferFriendState(InviteOffer.Limited), {}) }
}
