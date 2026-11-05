package tech.bashga.plata.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.data.Ambassador
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Delivery
import tech.bashga.plata.data.DeliveryDay
import tech.bashga.plata.data.DeliverySlot
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.StaticLayer
import tech.bashga.plata.screens.delivery.CardPackagingContent
import tech.bashga.plata.screens.delivery.DeliveryAddressContent
import tech.bashga.plata.screens.delivery.DeliveryAddressMode
import tech.bashga.plata.screens.delivery.DeliveryAddressState
import tech.bashga.plata.screens.delivery.DeliveryDetailsContent
import tech.bashga.plata.screens.delivery.DeliveryTimeContent
import tech.bashga.plata.screens.delivery.DeliveryTimeState
import tech.bashga.plata.screens.delivery.FeedbackAspect
import tech.bashga.plata.screens.delivery.FeedbackContent
import tech.bashga.plata.screens.delivery.FeedbackState
import tech.bashga.plata.screens.delivery.LocalFakeKeyboard
import tech.bashga.plata.screens.delivery.ProductTourContent
import tech.bashga.plata.screens.delivery.ProductTourState
import tech.bashga.plata.screens.delivery.RescheduleReasonContent
import tech.bashga.plata.screens.delivery.RescheduleReasonState
import tech.bashga.plata.screens.delivery.Reward
import tech.bashga.plata.screens.delivery.RewardAmbassadorContent
import tech.bashga.plata.screens.delivery.RewardAmbassadorState
import tech.bashga.plata.screens.delivery.ScheduleDeliveryIntroContent
import tech.bashga.plata.screens.delivery.UnboxingStoriesContent
import tech.bashga.plata.screens.delivery.TourPage
import tech.bashga.plata.screens.delivery.WelcomeCardContent
import tech.bashga.plata.screens.delivery.WelcomeCardState
import tech.bashga.plata.screens.delivery.WelcomeToast
import tech.bashga.plata.screens.delivery.deliverySlots
import tech.bashga.plata.screens.delivery.geocode
import tech.bashga.plata.screens.delivery.toDeliveryDetailsState

// the iOS keyboards the references show (not drawn): letters 292 high (642128ff), with the suggestion bar 336 (9a8e3fd6)
private val LettersKeyboard = 292.dp
private val LettersWithSuggestions = 336.33.dp

// a focused field's caret is on 0–500 ms of every second after focus; 1100 also lets the scroll-into-view settle
private const val CaretMillis = 1_100L

private val picked = geocode("Av Rio").first().address

private val scheduled = AppSnapshot(
    stage = Stage.CardComing("3 Jun"),
    delivery = Delivery(picked, "", DeliveryDay.Closest, DeliverySlot.Morning),
)

private fun details(ambassador: Ambassador) =
    scheduled.copy(stage = Stage.CardComing("3 Jun", ambassador, ambassadorDueAt = 10_000)).toDeliveryDetailsState()

private val mapState = DeliveryAddressState(DeliveryAddressMode.Map, query = "Av Rio", address = picked)

/** The presenters these sheets sit on belong to other areas; only their top edge shows, so a page of their colour stands in. */
private fun presenterPage() = StaticLayer(Presentation.Push) { Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) }

private fun intro() = StaticLayer(Presentation.Push) { ScheduleDeliveryIntroContent(onAction = {}) }

private fun detailsSheet() = StaticLayer(Presentation.Sheet) { DeliveryDetailsContent(details(Ambassador.Unassigned), onAction = {}) }

@Composable
private fun Keyboard(height: Dp, content: @Composable () -> Unit) = CompositionLocalProvider(LocalFakeKeyboard provides height, content = content)

/** SCREEN-MAP §3.2: one test per reference id, named after it. */
class DeliveryStateScreenshots : ScreenshotTest() {
    @Test fun `dda69155`() = screenState("dda69155") { ScheduleDeliveryIntroContent(onAction = {}) }

    @Test fun `73a046d0`() = screenState(
        "73a046d0",
        presentation = Presentation.Sheet,
        under = listOf(intro()),
        clockMillis = CaretMillis,
        setup = { focus("query") },
    ) { DeliveryAddressContent(DeliveryAddressState(query = "Av Rio"), onAction = {}) }

    @Test fun `78f0c567`() = screenState("78f0c567", presentation = Presentation.Sheet, under = listOf(intro())) {
        DeliveryAddressContent(mapState, onAction = {})
    }

    @Test fun `642128ff`() = screenState(
        "642128ff",
        presentation = Presentation.Sheet,
        under = listOf(intro()),
        clockMillis = CaretMillis,
        setup = { focus("details") },
    ) { Keyboard(LettersKeyboard) { DeliveryAddressContent(mapState.copy(details = "Depto 4B"), onAction = {}) } }

    @Test fun `eb4cdf05`() = screenState(
        "eb4cdf05",
        presentation = Presentation.Sheet,
        under = listOf(StaticLayer(Presentation.Sheet) { DeliveryAddressContent(mapState, onAction = {}) }),
    ) { DeliveryTimeContent(DeliveryTimeState(slots = AppSnapshot().deliverySlots), onAction = {}) }

    @Test fun `24f9ddbc`() = screenState(
        "24f9ddbc",
        presentation = Presentation.Sheet,
        under = listOf(StaticLayer(Presentation.Sheet) { DeliveryAddressContent(mapState, onAction = {}) }),
    ) { DeliveryTimeContent(DeliveryTimeState(DeliveryDay.Closest, DeliverySlot.Morning, AppSnapshot().deliverySlots), onAction = {}) }

    @Test fun `1d655cd9`() = screenState("1d655cd9", presentation = Presentation.Sheet, under = listOf(presenterPage())) {
        DeliveryDetailsContent(details(Ambassador.Unassigned), onAction = {})
    }

    @Test fun `88790268`() = screenState(
        "88790268",
        presentation = Presentation.Sheet,
        under = listOf(presenterPage()),
        setup = { scrollToAnchor("address-card", top = 138.dp, scrollableTag = "details-scroll") },
    ) { DeliveryDetailsContent(details(Ambassador.Assigned("Carlos")), onAction = {}) }

    @Test fun `34ec0c7a`() = screenState("34ec0c7a", presentation = Presentation.Sheet, under = listOf(detailsSheet())) {
        CardPackagingContent(onAction = {})
    }

    @Test fun `dd6c539e`() = screenState(
        "dd6c539e",
        presentation = Presentation.Sheet,
        under = listOf(detailsSheet()),
        setup = { scrollToAnchor("safe-bag", top = 138.dp, scrollableTag = "packaging-scroll") },
    ) { CardPackagingContent(onAction = {}) }

    @Test fun `8397cca1`() = screenState(
        "8397cca1",
        presentation = Presentation.Sheet,
        under = listOf(detailsSheet()),
        setup = { scrollBy(10_000.dp, "packaging-scroll") },
    ) { CardPackagingContent(onAction = {}) }

    // the stories have no source screenshot: one render per step and the end, reviewed by eye
    @Test fun `unboxing-stories-1`() = unboxingStories("1", 1_500)

    @Test fun `unboxing-stories-2`() = unboxingStories("2", 5_000)

    @Test fun `unboxing-stories-3`() = unboxingStories("3", 9_000)

    @Test fun `unboxing-stories-end`() = unboxingStories("end", 16_000)

    private fun unboxingStories(step: String, millis: Long) =
        screenState("unboxing-stories-$step", presentation = Presentation.Sheet, under = listOf(detailsSheet()), clockMillis = millis) { UnboxingStoriesContent(onAction = {}) }

    @Test fun `586ac28a`() = screenState("586ac28a", presentation = Presentation.Sheet, under = listOf(detailsSheet())) {
        RescheduleReasonContent(RescheduleReasonState(), onAction = {})
    }

    @Test fun `4e9baf69`() = screenState(
        "4e9baf69",
        presentation = Presentation.Sheet,
        backdrop = PageSheetBackdrop.Dimmed,
        under = listOf(presenterPage()),
    ) { FeedbackContent(FeedbackState(), onAction = {}) }

    @Test fun `f57ce187`() = screenState(
        "f57ce187",
        presentation = Presentation.Sheet,
        backdrop = PageSheetBackdrop.Dimmed,
        under = listOf(presenterPage()),
    ) { FeedbackContent(FeedbackState(setOf(FeedbackAspect.ClearIntro)), onAction = {}) }

    @Test fun `9a8e3fd6`() = screenState(
        "9a8e3fd6",
        presentation = Presentation.Sheet,
        backdrop = PageSheetBackdrop.Dimmed,
        under = listOf(presenterPage()),
        clockMillis = CaretMillis,
        setup = { focus("comment") },
    ) { Keyboard(LettersWithSuggestions) { FeedbackContent(FeedbackState(setOf(FeedbackAspect.ClearIntro), "Everything was so easy!"), onAction = {}) } }

    @Test fun `dc6f5c96`() = screenState("dc6f5c96", presentation = Presentation.Cover) { WelcomeCardContent(WelcomeCardState(), onAction = {}) }

    @Test fun `5b9474e2`() = screenState("5b9474e2", presentation = Presentation.Cover) {
        WelcomeCardContent(WelcomeCardState(WelcomeToast.FeedbackThanks), onAction = {})
    }

    @Test fun `3cbc958b`() = screenState("3cbc958b", presentation = Presentation.Cover) {
        RewardAmbassadorContent(RewardAmbassadorState(Reward.ThankYou), onAction = {})
    }

    @Test fun `f5984c79`() = screenState("f5984c79", presentation = Presentation.Cover) { ProductTourContent(ProductTourState(TourPage.ViewPin), onAction = {}) }

    @Test fun `72a00afe`() = screenState("72a00afe", presentation = Presentation.Cover) { ProductTourContent(ProductTourState(TourPage.GoDigital), onAction = {}) }

    @Test fun `f4ff1f66`() = screenState("f4ff1f66", presentation = Presentation.Cover) {
        ProductTourContent(ProductTourState(TourPage.CommissionFree), onAction = {})
    }
}
