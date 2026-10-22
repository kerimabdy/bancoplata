package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.Holding
import tech.bashga.plata.data.Invest
import tech.bashga.plata.data.InvestAccount
import tech.bashga.plata.data.LedgerAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.navigation.InvestTab
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals

/**
 * SCREEN-MAP §5.31, Transferring between accounts: Portfolio ⟨TransferMethod⟩ → BetweenAccounts → TransferResult,
 * then §2.1's own-balance chain $97.57 → $78.81 → $8.81 with the $70 transfer of flow 29.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class TransferringBetweenAccountsFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val app = PaymentsDriver(rule)

    // §5.31 step 2 fixture: the portfolio before this transfer, cash 0.00 and DNUT at cost; Dev › Month=July
    private val beforeTransfer = LiveSnapshot.copy(
        month = Month.July,
        invest = Invest(InvestAccount.Opened, listOf(Holding("DNUT", "Krispy Kreme", costBasis = 100, marketValue = 100))),
    )

    // Investments › portfolio card, Portfolio › Cash + ⟨TransferMethod⟩ › To my Plata account (the invest area's screens)
    private fun openBetweenAccountsFromPortfolio() {
        app.tap("Investment portfolio")
        app.tapDescribed("Add cash")
        app.tap("To my Plata account")
        app.waitFor("Between accounts")
    }

    @Test
    fun oneDollarMovesIntoInversiones() {
        val repository = AppRepository.inMemory(beforeTransfer)
        app.launch(repository, InvestTab)

        openBetweenAccountsFromPortfolio()
        app.waitFor("$97.57")
        app.waitFor("USD 0")
        app.waitFor("USD 1 = $18.76")
        app.type("amount", "1")
        app.waitFor("No commission")
        app.tap("Transfer $18.76")

        app.waitFor("Plata Inversiones")
        app.waitFor("$78.81")
        app.waitFor("USD 1")
        val snapshot = repository.snapshot
        assertEquals(78_81, snapshot.accounts.ownBalance)
        assertEquals(100, snapshot.accounts.inversionesCashUsd)
        val move = snapshot.ledger.single()
        assertEquals(listOf(EntryKind.BetweenAccounts, LedgerAccount.OwnBalance, -18_76L, Month.July), listOf(move.kind, move.account, move.amount, move.month))

        app.tap("Done")
        app.waitGone("From Plata Crédito")
        app.waitFor(hasContentDescription("Add cash"))
    }

    @Test
    fun theOwnBalanceChainHolds() {
        val repository = AppRepository.inMemory(beforeTransfer)
        app.launch(repository, InvestTab)

        openBetweenAccountsFromPortfolio()
        app.type("amount", "1")
        app.tap("Transfer $18.76")
        app.waitFor("Plata Inversiones")
        assertEquals(78_81, repository.snapshot.accounts.ownBalance)
        app.tap("Done")

        app.tap("Payments")
        app.waitFor("Favorites, bills, providers")
        app.enterSantanderRecipient(withReference = false)
        app.waitFor("$78.81")
        app.type("amount", "70")
        app.tap("Transfer")
        app.waitFor("–$70")
        app.waitFor("$8.81")
        assertEquals(8_81, repository.snapshot.accounts.ownBalance)
    }
}
