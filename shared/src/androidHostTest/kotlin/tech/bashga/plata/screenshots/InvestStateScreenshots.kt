package tech.bashga.plata.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Invest
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.StaticLayer
import tech.bashga.plata.screens.invest.Affiliation
import tech.bashga.plata.screens.invest.InvestIdeas
import tech.bashga.plata.screens.invest.InvestOnboardingContent
import tech.bashga.plata.screens.invest.InvestOnboardingState
import tech.bashga.plata.screens.invest.InvestOverlay
import tech.bashga.plata.screens.invest.InvestToast
import tech.bashga.plata.screens.invest.InvestmentIdeaContent
import tech.bashga.plata.screens.invest.InvestmentIdeaState
import tech.bashga.plata.screens.invest.InvestmentsContent
import tech.bashga.plata.screens.invest.InvestmentsState
import tech.bashga.plata.screens.invest.LessonContent
import tech.bashga.plata.screens.invest.LessonPage
import tech.bashga.plata.screens.invest.LessonState
import tech.bashga.plata.screens.invest.OnboardingStep
import tech.bashga.plata.screens.invest.PortfolioAllocationContent
import tech.bashga.plata.screens.invest.PortfolioAllocationState
import tech.bashga.plata.screens.invest.PortfolioContent
import tech.bashga.plata.screens.invest.PortfolioOverlay
import tech.bashga.plata.screens.invest.PortfolioState
import tech.bashga.plata.screens.invest.SampleInvestSignature
import tech.bashga.plata.screens.invest.StockDetailContent
import tech.bashga.plata.screens.invest.StockDetailState
import tech.bashga.plata.screens.invest.StockListContent
import tech.bashga.plata.screens.invest.StockListKind
import tech.bashga.plata.screens.invest.StockListState

/** The July investor of flows 21, 26 and 31: account opened, two USD 1 moves in and Dev › Buy DNUT between them. */
internal fun investorSnapshot(refreshed: Boolean = true, secondDeposit: Boolean = true): AppSnapshot {
    val repository = AppRepository.inMemory(
        AppSnapshot(stage = Stage.Live, activation = Activation.Done, month = Month.July, invest = Invest(InvestAccount.Opened, recents = listOf("VGK", "NVDA"))),
    )
    repository.transferToInversiones(100)
    repository.buyDnut()
    if (secondDeposit) repository.transferToInversiones(100)
    if (refreshed) repository.refreshQuotes()
    return repository.snapshot
}

/** scrollToAnchor reads the anchor's on-screen bounds, so first scroll an anchor further down the feed into view. */
internal fun StateSetup.scrollIntoViewThenAnchor(anchor: String, top: Dp, scrollableTag: String) {
    fun nodeTop() = rule.onNode(hasTestTag(anchor), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
    val screen = with(rule.density) { 600.dp.toPx() }
    repeat(20) {
        val at = nodeTop()
        if (at > 0f && at < screen) return@repeat
        scrollBy(500.dp, scrollableTag)
    }
    scrollToAnchor(anchor, top, scrollableTag)
}

private val recents = listOf("VGK", "NVDA")
private val july = TestShell(TabBarTab.Invest, Month.July)
private val june = TestShell(TabBarTab.Invest, Month.June)
private val opened = InvestmentsState(InvestAccount.Opened, portfolioValue = 0, recents = recents, month = Month.July)

/** SCREEN-MAP §3.5: one test per reference id, named after it. */
class InvestStateScreenshots : ScreenshotTest() {
    // Investments

    @Test fun `73569385`() = screenState("73569385", shell = july) {
        InvestmentsContent(InvestmentsState(InvestAccount.NotOpened, recents = recents, month = Month.July, toast = InvestToast.VestFiInfo), onAction = {})
    }

    @Test fun `083f8017`() = screenState("083f8017", shell = june) {
        InvestmentsContent(InvestmentsState(InvestAccount.NotOpened, month = Month.June), onAction = {})
    }

    @Test fun `3d1d2b9b`() = screenState("3d1d2b9b", shell = july) { InvestmentsContent(opened, onAction = {}) }

    @Test fun `fdc6c570`() = screenState("fdc6c570", shell = july, setup = { scrollIntoViewThenAnchor("top-movers", 158.4.dp, "investments-scroll") }) {
        InvestmentsContent(opened, onAction = {})
    }

    @Test fun `09c22ffa`() = screenState("09c22ffa", shell = june, setup = { scrollIntoViewThenAnchor("investment-ideas", 167.7.dp, "investments-scroll") }) {
        InvestmentsContent(opened.copy(month = Month.June), onAction = {})
    }

    @Test fun `5ab1e0c9`() = screenState("5ab1e0c9", shell = july, setup = { scrollIntoViewThenAnchor("crypto-etfs", 147.7.dp, "investments-scroll") }) {
        InvestmentsContent(opened, onAction = {})
    }

    @Test fun `5d3ebc67`() = screenState("5d3ebc67", shell = july, setup = { scrollIntoViewThenAnchor("commodities-etfs", 146.7.dp, "investments-scroll") }) {
        InvestmentsContent(opened, onAction = {})
    }

    @Test fun `fef10b1d`() = screenState("fef10b1d", shell = june, setup = { scrollIntoViewThenAnchor("short-term-bonds", 143.7.dp, "investments-scroll") }) {
        InvestmentsContent(opened.copy(month = Month.June), onAction = {})
    }

    // InvestOnboarding, presented from the July Investments of flow 2

    private fun onboarding(id: String, state: InvestOnboardingState) = screenState(
        id,
        presentation = Presentation.Sheet,
        under = listOf(StaticLayer(Presentation.Push) { presenter() }),
    ) { InvestOnboardingContent(state, onAction = {}) }

    @Composable
    private fun presenter() = InvestmentsContent(InvestmentsState(InvestAccount.NotOpened, recents = recents, month = Month.July), onAction = {})

    @Test fun `00e6fc01`() = onboarding("00e6fc01", InvestOnboardingState(affiliations = setOf(Affiliation.None)))

    @Test fun `80971162`() = onboarding("80971162", InvestOnboardingState(OnboardingStep.Acknowledgment, setOf(Affiliation.None)))

    @Test fun `aee18063`() = onboarding(
        "aee18063",
        InvestOnboardingState(OnboardingStep.Acknowledgment, setOf(Affiliation.None), signature = SampleInvestSignature, overlay = InvestOverlay.Signature),
    )

    @Test fun `b3c28fcb`() = onboarding("b3c28fcb", InvestOnboardingState(OnboardingStep.Volume, setOf(Affiliation.None), SampleInvestSignature, volume = 0.05f))

    @Test fun `f4915e4c`() = onboarding("f4915e4c", InvestOnboardingState(OnboardingStep.Volume, setOf(Affiliation.None), SampleInvestSignature, volume = 0.70f))

    @Test fun `5ae897e1`() = onboarding("5ae897e1", InvestOnboardingState(OnboardingStep.RiskProfile, setOf(Affiliation.None), SampleInvestSignature, volume = 0.70f))

    @Test fun `6f8f3145`() = onboarding(
        "6f8f3145",
        InvestOnboardingState(OnboardingStep.RiskProfile, setOf(Affiliation.None), SampleInvestSignature, volume = 0.70f, overlay = InvestOverlay.AccountOpened),
    )

    // StockList, StockDetail

    @Test fun `11cfd731`() = screenState("11cfd731", shell = june) {
        StockListContent(StockListState(StockListKind.Popular, month = Month.June), onAction = {})
    }

    @Test fun `7e1c7742`() = screenState("7e1c7742", clockMillis = 100, setup = { focus("stock-search") }) {
        StockListContent(StockListState(StockListKind.Popular, searching = true, query = "krisp", month = Month.June), onAction = {})
    }

    @Test fun `341fcc04`() = screenState("341fcc04") { StockDetailContent(StockDetailState.of("DNUT", Month.June), onAction = {}) }

    // Portfolio: the trend percentages are the source's own (0.55% on 0.01 of 2.00, −0.09% on an unmoved 1.00)

    @Test fun `b658c9da`() = screenState("b658c9da", shell = july) {
        PortfolioContent(PortfolioState.from(investorSnapshot()).copy(changePercent = 55), onAction = {})
    }

    @Test fun `537a4b5a`() = screenState("537a4b5a", shell = july, setup = { scrollBy(299.8.dp, "portfolio-scroll") }) {
        PortfolioContent(PortfolioState.from(investorSnapshot()).copy(changePercent = 55), onAction = {})
    }

    @Test fun `99ceac12`() = screenState("99ceac12", shell = july) {
        val before = investorSnapshot(refreshed = false, secondDeposit = false)
        PortfolioContent(PortfolioState.from(before).copy(changePercent = -9, overlay = PortfolioOverlay.TransferMethod), onAction = {})
    }

    // PortfolioAllocation

    private val allocationShell = TestShell(TabBarTab.Invest, Month.July, supportBadge = 1)

    @Test fun `b9c441c0`() = screenState("b9c441c0", shell = allocationShell) {
        PortfolioAllocationContent(PortfolioAllocationState.from(investorSnapshot()), onAction = {})
    }

    @Test fun `baff34c6`() = screenState("baff34c6", shell = allocationShell, setup = { scrollBy(567.33.dp, "allocation-scroll") }) {
        PortfolioAllocationContent(PortfolioAllocationState.from(investorSnapshot()), onAction = {})
    }

    @Test fun `f1b38f8d`() = screenState("f1b38f8d", shell = allocationShell, setup = { scrollBy(942.33.dp, "allocation-scroll") }) {
        PortfolioAllocationContent(PortfolioAllocationState.from(investorSnapshot()), onAction = {})
    }

    // InvestmentIdea

    @Test fun `cc4f1c47`() = screenState("cc4f1c47") { InvestmentIdeaContent(InvestmentIdeaState(InvestIdeas.Vanguard), onAction = {}) }

    @Test fun `a4ac3ec6`() = screenState("a4ac3ec6", setup = { scrollBy(4_000.dp, "idea-scroll") }) {
        InvestmentIdeaContent(InvestmentIdeaState(InvestIdeas.Vanguard), onAction = {})
    }

    // Lesson

    @Test fun `c5882da1`() = screenState("c5882da1", presentation = Presentation.Cover) { LessonContent(LessonState(), onAction = {}) }

    @Test fun `7ab581a2`() = screenState("7ab581a2", presentation = Presentation.Cover) {
        LessonContent(LessonState(page = LessonPage.Content(0)), onAction = {})
    }

    @Test fun `8de66942`() = screenState("8de66942", presentation = Presentation.Cover) {
        LessonContent(LessonState(page = LessonPage.Content(1)), onAction = {})
    }

    @Test fun `48490884`() = screenState("48490884", presentation = Presentation.Cover, clockMillis = 1_500) {
        LessonContent(LessonState(page = LessonPage.Complete), onAction = {})
    }
}
