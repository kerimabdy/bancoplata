package tech.bashga.plata.flows

import org.junit.Test
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.HomeTiles
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.homeBadge
import tech.bashga.plata.dev.DevActions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** SCREEN-MAP §5.15 MSI partner detail, §5.19 MSI, §5.23 View a terminal, §5.24 MSI partner. */
class MsiFlowsTest : HomeMoneyFlowTest() {
    private fun openMsiHome(repository: AppRepository) {
        launchAtHome(repository)
        tap("MSI")
        waitFor("Nothing to defer")
    }

    /** MsiHome's second "Show all" belongs to the partners card. */
    private fun showAllPartners() = tap("Show all", index = 1)

    @Test
    fun msiPartnerDetail() {
        val repository = AppRepository.inMemory(live.copy(month = Month.July))
        openMsiHome(repository)
        tap("Mercado Libre")
        waitFor("Buy in MSI")
        seen("You will be redirected to mercadolibre.com.mx")
        back()
        waitFor("Nothing to defer")
    }

    @Test
    fun msiClearsTheHomeBadge() {
        val repository = AppRepository.inMemory(live.copy(homeTiles = HomeTiles(cashback = setOf(CashbackCategory.AllPurchases))))
        assertEquals(1, repository.snapshot.homeBadge)
        openMsiHome(repository)
        assertTrue(repository.snapshot.homeTiles.msiVisited)
        assertEquals(0, repository.snapshot.homeBadge)

        DevActions(repository).setMonth(Month.July)
        waitFor("1,500")
        scrollTo("Active MSI")
        seen("Defer to MSI")
    }

    @Test
    fun viewATerminal() {
        val repository = AppRepository.inMemory(live.copy(month = Month.July, msiPartnersIntroSeen = true))
        openMsiHome(repository)
        showAllPartners()
        waitFor("Terminals with MSI")
        tap("Clip")
        waitFor("on Clip terminals", substring = true)
        tap("Great")
        waitGone("on Clip terminals", substring = true)
    }

    @Test
    fun msiPartnerIntroShowsOnTheFirstVisitOnly() {
        val repository = AppRepository.inMemory(live.copy(month = Month.July))
        openMsiHome(repository)
        showAllPartners()
        waitFor("Start exploring")
        tap("Start exploring")
        waitGone("Start exploring")
        assertTrue(repository.snapshot.msiPartnersIntroSeen)

        scrollTo("Stores with MSI")
        seen("Adidas")
        tap("Beauty")
        waitGone("Adidas")
        seen("Alfaparf")

        back()
        waitFor("Nothing to defer")
        showAllPartners()
        waitFor("Terminals with MSI")
        assertFalse(rule.onAllNodes(androidx.compose.ui.test.hasText("Start exploring")).fetchSemanticsNodes().isNotEmpty())
    }
}
