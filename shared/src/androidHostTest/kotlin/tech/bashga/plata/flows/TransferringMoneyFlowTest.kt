package tech.bashga.plata.flows

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.Accounts
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.navigation.PaymentsTab
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals

/**
 * SCREEN-MAP §5.29, Transferring money: Payments ⟨TransferMethod⟩ → TransferRecipient (CLABE, name, reference) →
 * TransferAmount → TransferResult. Starts after flow 31 (own balance $78.81) with Dev › Month=July applied.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class TransferringMoneyFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val app = PaymentsDriver(rule)

    @Test
    fun aClabeTransferLeavesTheOwnBalance() {
        val repository = AppRepository.inMemory(LiveSnapshot.copy(month = Month.July, accounts = Accounts(ownBalance = 78_81)))
        app.launch(repository, PaymentsTab)

        app.waitFor("Favorites, bills, providers")
        app.enterSantanderRecipient(withReference = true)
        app.waitFor("$78.81")
        app.type("amount", "50")
        app.waitFor("Commission up to $230.84")
        app.tap("Transfer")

        app.waitFor("Done")
        app.waitFor("–$50")
        app.waitFor("Santander •• 5678")
        app.waitFor("$28.81")
        val snapshot = repository.snapshot
        assertEquals(28_81, snapshot.accounts.ownBalance)
        val sent = snapshot.ledger.single { it.kind == EntryKind.TransferOut }
        assertEquals(listOf(Month.July, -50_00L, LedgerAccount.OwnBalance), listOf(sent.month, sent.amount, sent.account))
        assertEquals("Santander", sent.title)
        val commission = snapshot.ledger.single { it.kind == EntryKind.Commission }
        assertEquals(listOf(-230_84L, LedgerAccount.CreditLine), listOf(commission.amount, commission.account))

        app.tap("Done")
        app.waitGone("Done")
        app.waitFor("Favorites, bills, providers")
    }
}
