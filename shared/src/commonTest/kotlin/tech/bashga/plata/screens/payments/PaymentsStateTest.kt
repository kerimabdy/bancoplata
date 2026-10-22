package tech.bashga.plata.screens.payments

import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.FavoriteKind
import tech.bashga.plata.designsystem.components.MethodCardBank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaymentsStateTest {
    @Test
    fun transferTileOpensTheMethodSheetAndAMethodClosesIt() {
        val open = PaymentsState().reduce(PaymentsAction.TransferTile)
        assertEquals(PaymentsOverlay.TransferMethod, open.overlay)
        assertEquals(open, open.reduce(PaymentsAction.ChooseMethod(TransferMethod.Phone)))
        assertNull(open.reduce(PaymentsAction.ChooseMethod(TransferMethod.Clabe)).overlay)
    }

    @Test
    fun anAddedFavoriteOpensItsDialog() {
        val card = Favorite("platacard-1", FavoriteKind.PlataCard, "Ana López", "4152313812345678")
        val state = PaymentsState(favorites = listOf(card, Favorite.MyMobile)).reduce(PaymentsAction.ShowFavoriteAdded(card.id))
        assertEquals(PaymentsOverlay.FavoriteAdded(card), state.overlay)
        assertNull(state.reduce(PaymentsAction.CloseOverlay).overlay)
        assertNull(PaymentsState().reduce(PaymentsAction.ShowFavoriteAdded("missing")).overlay)
    }

    @Test
    fun aClabeDetectsItsBankAndCapsAt18Digits() {
        val typed = TransferRecipientState().reduce(TransferRecipientAction.NumberChanged("01418065501234567899"))
        assertEquals("014180655012345678", typed.number)
        assertEquals(MethodCardBank.Santander, typed.bank)
        assertFalse(typed.isComplete)
        assertTrue(typed.reduce(TransferRecipientAction.NameChanged("Garcia")).isComplete)
        assertNull(TransferRecipientState().reduce(TransferRecipientAction.NumberChanged("002")).bank)
    }

    @Test
    fun aPlataCardIsGroupedAndLooksUpItsHolder() {
        val card = TransferRecipientState(TransferPurpose.AddFavorite, TransferMethod.Card)
        val typed = card.reduce(TransferRecipientAction.NumberChanged("415231381234567"))
        assertEquals("4152 3138 1234 567", typed.number)
        assertEquals(MethodCardBank.Plata, typed.bank)
        assertEquals(PLATA_CARD_HOLDER, typed.name)
        assertEquals("", typed.reduce(TransferRecipientAction.NumberChanged("4")).name)
    }

    @Test
    fun additionalFieldsApplyTheDraftOnDone() {
        val open = TransferRecipientState().reduce(TransferRecipientAction.OpenAdditionalFields)
        assertEquals(AdditionalFields(emptySet()), open.overlay)
        val drafted = open.reduce(TransferRecipientAction.ToggleDraft(TransferExtra.Reference))
        assertEquals(emptySet(), drafted.extras)
        val done = drafted.reduce(TransferRecipientAction.ConfirmAdditionalFields)
        assertEquals(setOf(TransferExtra.Reference), done.extras)
        assertNull(done.overlay)
        assertEquals("01", done.reduce(TransferRecipientAction.ExtraChanged(TransferExtra.Reference, "0a1")).reference)
    }

    @Test
    fun theAmountIsDecimalAndPayableOnlyFromTheBalance() {
        assertEquals("50.25", sanitizeAmount("50.257"))
        assertEquals("0.5", sanitizeAmount(".5"))
        assertEquals(5_025, amountCents("50.25"))
        val state = TransferAmountState(TransferRecipient("Garcia", "014180655012345678", TransferMethod.Clabe), ownBalance = 78_81)
        assertNull(state.note)
        val fifty = state.reduce(TransferAmountAction.AmountChanged("50"))
        assertTrue(fifty.canTransfer)
        assertEquals("Commission up to $230.84", fifty.note)
        assertFalse(state.reduce(TransferAmountAction.AmountChanged("80")).canTransfer)
    }

    @Test
    fun betweenAccountsConvertsAtTheRate() {
        val empty = BetweenAccountsState(ownBalance = 97_57, inversionesCash = 0)
        assertEquals("USD 1 = $18.76", empty.conversion)
        assertEquals("Transfer", empty.cta)
        val one = empty.reduce(BetweenAccountsAction.AmountChanged("1"))
        assertEquals("$18.76", one.conversion)
        assertEquals("Transfer $18.76", one.cta)
        assertTrue(one.canTransfer)
        assertFalse(empty.reduce(BetweenAccountsAction.AmountChanged("6")).canTransfer)
    }
}
