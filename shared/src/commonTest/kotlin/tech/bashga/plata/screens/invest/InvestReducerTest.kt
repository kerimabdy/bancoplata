package tech.bashga.plata.screens.invest

import androidx.compose.ui.geometry.Offset
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.SignatureStroke
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InvestReducerTest {
    private val stroke = SignatureStroke(listOf(Offset(0.1f, 0.1f), Offset(0.5f, 0.5f)))

    private fun InvestOnboardingState.run(vararg actions: InvestOnboardingAction) = actions.fold(this) { s, a -> s.reduce(a) }

    @Test
    fun noneClearsTheOtherAffiliationsAndAnyOtherClearsNone() {
        val some = InvestOnboardingState().run(
            InvestOnboardingAction.ToggleAffiliation(Affiliation.PoliticalFigure),
            InvestOnboardingAction.ToggleAffiliation(Affiliation.BrokerDealer),
        )
        assertEquals(setOf(Affiliation.PoliticalFigure, Affiliation.BrokerDealer), some.affiliations)
        val none = some.reduce(InvestOnboardingAction.ToggleAffiliation(Affiliation.None))
        assertEquals(setOf(Affiliation.None), none.affiliations)
        assertEquals(setOf(Affiliation.SeniorExecutive), none.reduce(InvestOnboardingAction.ToggleAffiliation(Affiliation.SeniorExecutive)).affiliations)
    }

    @Test
    fun continueNeedsAnAffiliation() {
        assertEquals(OnboardingStep.Affiliation, InvestOnboardingState().reduce(InvestOnboardingAction.Continue).step)
    }

    @Test
    fun theStepsRunInOrderAndBackWalksThem() {
        val signing = InvestOnboardingState().run(
            InvestOnboardingAction.ToggleAffiliation(Affiliation.None),
            InvestOnboardingAction.Continue,
            InvestOnboardingAction.SignAgreements,
        )
        assertEquals(OnboardingStep.Acknowledgment, signing.step)
        assertEquals(InvestOverlay.Signature, signing.overlay)
        assertEquals(signing, signing.reduce(InvestOnboardingAction.SignatureDone), "Done needs a stroke")

        val drawn = signing.run(InvestOnboardingAction.Draw(stroke), InvestOnboardingAction.Draw(stroke), InvestOnboardingAction.Undo)
        assertEquals(1, drawn.signature.size)
        assertTrue(drawn.reduce(InvestOnboardingAction.Clear).signature.isEmpty())

        val volume = drawn.reduce(InvestOnboardingAction.SignatureDone)
        assertEquals(OnboardingStep.Volume, volume.step)
        assertNull(volume.overlay)
        assertEquals("~5%", volume.volumeLabel)
        assertEquals("~70%", volume.reduce(InvestOnboardingAction.SetVolume(0.7f)).volumeLabel)

        val profile = volume.reduce(InvestOnboardingAction.Continue)
        assertEquals(OnboardingStep.RiskProfile, profile.step)
        assertEquals(OnboardingStep.Volume, profile.reduce(InvestOnboardingAction.Back).step)
        assertEquals(OnboardingStep.RiskProfile, volume.reduce(InvestOnboardingAction.DecideLater).step)
        assertEquals(InvestOverlay.AccountOpened, profile.reduce(InvestOnboardingAction.Agree).overlay)
        assertEquals(InvestOnboardingState(), profile.reduce(InvestOnboardingAction.Retake))
    }

    @Test
    fun cancelClosesTheSignatureKeepingTheStep() {
        val signing = InvestOnboardingState(OnboardingStep.Acknowledgment, overlay = InvestOverlay.Signature)
        assertEquals(InvestOnboardingState(OnboardingStep.Acknowledgment), signing.reduce(InvestOnboardingAction.SignatureCancel))
    }

    @Test
    fun aLessonGoesIntroContentComplete() {
        val first = LessonState().reduce(LessonAction.GetStarted)
        assertEquals(LessonPage.Content(0), first.page)
        assertEquals(0.25f, first.progress)
        val second = first.reduce(LessonAction.Next)
        assertEquals(0.75f, second.progress)
        val complete = second.reduce(LessonAction.Next)
        assertEquals(LessonPage.Complete, complete.page)
        assertEquals(LessonPage.Content(1), complete.reduce(LessonAction.Back).page)
        assertEquals(LessonPage.Intro, first.reduce(LessonAction.Back).page)
    }

    @Test
    fun searchFiltersEveryStockAsYouType() {
        val typing = StockListState(StockListKind.Popular, month = Month.June).reduce(StockListAction.Type("krisp"))
        assertTrue(typing.searching)
        assertEquals(listOf("DNUT"), typing.rows.map { it.ticker })
        assertEquals("USD 3.30", typing.rows.single().quote.priceLabel)
        val cancelled = typing.reduce(StockListAction.Cancel)
        assertEquals(StockListState(StockListKind.Popular, month = Month.June), cancelled)
        assertEquals(8, cancelled.rows.size)
    }
}
