package tech.bashga.plata.screens.home

import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppIcon
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.CardId
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.components.CardFace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeReducersTest {
    @Test
    fun feedbackDialogFollowsActivationAndStepsAsideForItsSheet() {
        val pending = AppSnapshot(stage = Stage.Live, activation = Activation.FeedbackPending)
        assertEquals(HomeOverlay.AmbassadorFeedback, homeState(pending, HomeLocal()).overlay)
        val liked = HomeLocal().reduce(HomeAction.RateAmbassador(liked = true))
        assertNull(homeState(pending, liked).overlay)
        assertEquals(HomeOverlay.AmbassadorFeedback, homeState(pending, liked.reduce(HomeAction.Resumed)).overlay)
        assertNull(homeState(pending.copy(activation = Activation.InProgress), HomeLocal()).overlay)
    }

    @Test
    fun deliveryArrangedClosesOnce() {
        val coming = AppSnapshot(stage = Stage.CardComing("3 Jun"), deliveryArrangedPending = true)
        assertEquals(HomeOverlay.DeliveryArranged, homeState(coming, HomeLocal()).overlay)
        assertEquals(HomeStage.CardComing("3 Jun", ""), homeState(coming, HomeLocal()).stage)
        assertNull(homeState(coming.copy(deliveryArrangedPending = false), HomeLocal()).overlay)
    }

    @Test
    fun physicalCardFlipsAndFreezesThroughTheConfirm() {
        val card = CardDetailsState(CardId.Physical)
        assertEquals(CardFace.Details, card.reduce(CardDetailsAction.FlipCard).face)
        val confirm = card.reduce(CardDetailsAction.Freeze)
        assertEquals(CardDetailsOverlay.FreezeConfirm, confirm.overlay)
        val frozen = confirm.reduce(CardDetailsAction.ConfirmFreeze)
        assertEquals(true, frozen.frozen)
        assertNull(frozen.overlay)
        assertEquals(false, frozen.reduce(CardDetailsAction.Unfreeze).frozen)
    }

    @Test
    fun digitalCardRevealsAfterLoading() {
        val card = CardDetailsState(CardId.Digital)
        assertEquals(card, card.reduce(CardDetailsAction.FlipCard))
        val loading = card.reduce(CardDetailsAction.ShowDetails)
        assertEquals(CardReveal.Loading, loading.reveal)
        assertEquals(CardFace.Details, loading.face)
        assertEquals(CardReveal.Shown, loading.reduce(CardDetailsAction.DetailsLoaded).reveal)
    }

    @Test
    fun cashbackCapsAtFourCategories() {
        var state = CashbackSetupState().reduce(CashbackSetupAction.StartSelecting)
        assertEquals(CashbackStep.Select, state.step)
        assertEquals(state, state.reduce(CashbackSetupAction.Done))
        listOf(CashbackCategory.AllPurchases, CashbackCategory.Airline, CashbackCategory.HomeDiy, CashbackCategory.Mobile, CashbackCategory.Taxi)
            .forEach { state = state.reduce(CashbackSetupAction.Toggle(it)) }
        assertEquals(4, state.selected.size)
        assertEquals(3, state.reduce(CashbackSetupAction.Toggle(CashbackCategory.Mobile)).selected.size)
        assertEquals(CashbackStep.Confirmed, state.reduce(CashbackSetupAction.Done).step)
    }

    @Test
    fun profileOverlays() {
        val profile = ProfileState()
        assertEquals(ProfileOverlay.AppIconChanged(AppIcon.Black), profile.reduce(ProfileAction.SelectIcon(AppIcon.Black)).overlay)
        assertEquals(ProfileOverlay.LogOutConfirm, profile.reduce(ProfileAction.LogOut).overlay)
        assertNull(profile.copy(overlay = ProfileOverlay.PictureIntro).reduce(ProfileAction.DismissPicture).overlay)
        assertNull(profile.copy(overlay = ProfileOverlay.PersonalDetails).reduce(ProfileAction.MessageInChat).overlay)
    }

    @Test
    fun partnersChipToggles() {
        val chosen = PartnersState().reduce(PartnersAction.ToggleCategory(PartnerFilter.CarRental))
        assertEquals(PartnerFilter.CarRental, chosen.category)
        assertNull(chosen.reduce(PartnersAction.ToggleCategory(PartnerFilter.CarRental)).category)
    }
}
