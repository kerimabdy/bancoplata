package tech.bashga.plata.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.FavoriteKind
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.MethodCardBank
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.foundation.SafeAreaInsets
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.StaticLayer
import tech.bashga.plata.screens.payments.AdditionalFields
import tech.bashga.plata.screens.payments.BetweenAccountsContent
import tech.bashga.plata.screens.payments.BetweenAccountsState
import tech.bashga.plata.screens.payments.FavoritesContent
import tech.bashga.plata.screens.payments.FavoritesOverlay
import tech.bashga.plata.screens.payments.FavoritesState
import tech.bashga.plata.screens.payments.PaymentsContent
import tech.bashga.plata.screens.payments.PaymentsOverlay
import tech.bashga.plata.screens.payments.PaymentsState
import tech.bashga.plata.screens.payments.TransferAmountContent
import tech.bashga.plata.screens.payments.TransferAmountState
import tech.bashga.plata.screens.payments.TransferExtra
import tech.bashga.plata.screens.payments.TransferKeyboard
import tech.bashga.plata.screens.payments.TransferMethod
import tech.bashga.plata.screens.payments.TransferOutcome
import tech.bashga.plata.screens.payments.TransferPurpose
import tech.bashga.plata.screens.payments.TransferRecipient
import tech.bashga.plata.screens.payments.TransferRecipientContent
import tech.bashga.plata.screens.payments.TransferRecipientState
import tech.bashga.plata.screens.payments.TransferResultContent
import tech.bashga.plata.screens.payments.TransferResultState

private val PlataCard = Favorite("platacard-4152313812345678", FavoriteKind.PlataCard, "Ana López", "4152313812345678")

// the source CLABE and beneficiary are redacted; any Santander CLABE (bank code 014) stands in
private const val SantanderClabe = "014180655012345678"

private const val Phone = "5512345678"

/** The iOS keyboard is not drawn; its height is faked as the IME inset the CTA rides on. */
@Composable
private fun WithKeyboard(keyboard: TransferKeyboard, content: @Composable () -> Unit) =
    ProvideSafeArea(SafeAreaInsets(top = 0.dp, bottom = keyboard.height), content)

private val UnderPayments = listOf(StaticLayer(Presentation.Push) { PaymentsContent(PaymentsState(), onAction = {}) })

// Portfolio (invest) presents BetweenAccounts; only its light top edge shows above the sheet
private val UnderPortfolio = listOf(StaticLayer(Presentation.Push) { Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) })

private val Santander = TransferRecipientState(number = SantanderClabe, name = "Garcia")

/** SCREEN-MAP §3.4: one test per reference id, named after it. */
class PaymentsStateScreenshots : ScreenshotTest() {
    @Test fun `a5a647ee`() = screenState("a5a647ee", shell = TestShell(TabBarTab.Payments, Month.June)) {
        PaymentsContent(PaymentsState(favorites = emptyList()), onAction = {})
    }

    @Test fun `b7b04c52`() = screenState("b7b04c52", shell = TestShell(TabBarTab.Payments, Month.July, supportBadge = 1)) {
        PaymentsContent(PaymentsState(favorites = listOf(Favorite.MyMobile, PlataCard)), onAction = {})
    }

    @Test fun `0b2e699a`() = screenState(
        "0b2e699a",
        shell = TestShell(TabBarTab.Payments),
        setup = { scrollBy(52.dp, "payments-scroll") },
    ) {
        PaymentsContent(PaymentsState(favorites = listOf(Favorite.MyMobile), overlay = PaymentsOverlay.TransferMethod), onAction = {})
    }

    @Test fun `873d0226`() = screenState("873d0226", shell = TestShell(TabBarTab.Payments, Month.July, supportBadge = 1)) {
        PaymentsContent(PaymentsState(favorites = listOf(PlataCard, Favorite.MyMobile), overlay = PaymentsOverlay.FavoriteAdded(PlataCard)), onAction = {})
    }

    @Test fun `2be9ff97`() = screenState("2be9ff97", presentation = Presentation.Sheet, under = UnderPayments) {
        FavoritesContent(FavoritesState(items = listOf(Favorite.MyMobile), phone = Phone), onAction = {})
    }

    @Test fun `e94a49a9`() = screenState("e94a49a9", presentation = Presentation.Sheet, under = UnderPayments) {
        FavoritesContent(FavoritesState(items = listOf(Favorite.MyMobile), phone = Phone, overlay = FavoritesOverlay.AddFavorite), onAction = {})
    }

    @Test fun `43e51dd1`() = screenState(
        "43e51dd1",
        presentation = Presentation.Sheet,
        under = UnderPayments,
        clockMillis = 100,
        setup = { focus("number") },
    ) {
        WithKeyboard(TransferKeyboard.NumberPad) { TransferRecipientContent(TransferRecipientState(), onAction = {}) }
    }

    @Test fun `62147891`() = screenState(
        "62147891",
        presentation = Presentation.Sheet,
        under = UnderPayments,
        clockMillis = 100,
        setup = { focus("name") },
    ) {
        WithKeyboard(TransferKeyboard.Letters) { TransferRecipientContent(Santander, onAction = {}) }
    }

    @Test fun `782187fe`() = screenState("782187fe", presentation = Presentation.Sheet, under = UnderPayments) {
        TransferRecipientContent(Santander.copy(overlay = AdditionalFields(emptySet())), onAction = {})
    }

    @Test fun `c5b99e20`() = screenState(
        "c5b99e20",
        presentation = Presentation.Sheet,
        under = UnderPayments,
        clockMillis = 1_100,
        setup = { focus("reference") },
    ) {
        WithKeyboard(TransferKeyboard.NumberPad) {
            TransferRecipientContent(Santander.copy(extras = setOf(TransferExtra.Reference), reference = "01"), onAction = {})
        }
    }

    @Test fun `5e785f05`() = screenState(
        "5e785f05",
        presentation = Presentation.Sheet,
        under = UnderPayments,
        clockMillis = 100,
        setup = { focus("number") },
    ) {
        WithKeyboard(TransferKeyboard.NumberPad) {
            TransferRecipientContent(
                TransferRecipientState(TransferPurpose.AddFavorite, TransferMethod.Card, number = "4152 3138 1234 567", name = "Ana López"),
                onAction = {},
            )
        }
    }

    @Test fun `a6fae664`() = screenState(
        "a6fae664",
        presentation = Presentation.Sheet,
        under = UnderPayments,
        clockMillis = 1_100,
        setup = { focus("amount") },
    ) {
        WithKeyboard(TransferKeyboard.NumberPad) {
            TransferAmountContent(
                TransferAmountState(TransferRecipient("Garcia", SantanderClabe, TransferMethod.Clabe), ownBalance = 78_81, amount = "50"),
                onAction = {},
            )
        }
    }

    @Test fun `6e1c2ab5`() = screenState(
        "6e1c2ab5",
        presentation = Presentation.Sheet,
        under = UnderPortfolio,
        clockMillis = 100,
        setup = { focus("amount") },
    ) {
        WithKeyboard(TransferKeyboard.NumberPad) { BetweenAccountsContent(BetweenAccountsState(ownBalance = 97_57, inversionesCash = 0), onAction = {}) }
    }

    @Test fun `40d2b403`() = screenState(
        "40d2b403",
        presentation = Presentation.Sheet,
        under = UnderPortfolio,
        clockMillis = 100,
        setup = { focus("amount") },
    ) {
        WithKeyboard(TransferKeyboard.NumberPad) {
            BetweenAccountsContent(BetweenAccountsState(ownBalance = 97_57, inversionesCash = 0, amount = "1"), onAction = {})
        }
    }

    // §2.1 fixture: the source shows −$70 for the $50 typed on a6fae664
    @Test fun `372e6fd9`() = screenState("372e6fd9", presentation = Presentation.Cover, under = UnderPayments) {
        TransferResultContent(
            TransferResultState(TransferOutcome.External("Garcia", "Santander •• 4821", MethodCardBank.Santander, 70_00, 230_84, 78_81, 8_81)),
            onAction = {},
        )
    }

    @Test fun `705c4f5e`() = screenState("705c4f5e", presentation = Presentation.Cover, under = UnderPortfolio) {
        TransferResultContent(TransferResultState(TransferOutcome.BetweenAccounts(100, 0, 97_57, 78_81)), onAction = {})
    }
}
