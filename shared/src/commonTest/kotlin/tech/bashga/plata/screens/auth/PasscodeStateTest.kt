package tech.bashga.plata.screens.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class PasscodeStateTest {
    private fun PasscodeState.type(digits: String) = digits.fold(this) { state, d -> state.reduce(PasscodeAction.Digit(d.digitToInt())) }

    @Test
    fun createThenConfirmSetsThePasscode() {
        val created = PasscodeState().type("12345")
        assertEquals(PasscodeState(PasscodeMode.Create, "12345"), created)
        val confirming = created.type("6")
        assertEquals(PasscodeState(PasscodeMode.Confirm, "", first = "123456"), confirming)
        assertEquals("Confirm passcode", confirming.title)
        val set = confirming.type("123456")
        assertEquals(PasscodeStatus.Success, set.status)
        assertEquals("Passcode was set!", set.title)
        assertEquals(set, set.type("7").reduce(PasscodeAction.Delete))
    }

    @Test
    fun mismatchClearsTheEntryAndStaysOnConfirm() {
        val mismatch = PasscodeState().type("123456").type("654321")
        assertEquals(PasscodeState(PasscodeMode.Confirm, "", first = "123456", status = PasscodeStatus.Mismatch), mismatch)
        assertEquals(PasscodeStatus.Entering, mismatch.type("1").status)
    }

    @Test
    fun deleteDropsTheLastDigit() {
        assertEquals("12", PasscodeState().type("123").reduce(PasscodeAction.Delete).entered)
        assertEquals("", PasscodeState().reduce(PasscodeAction.Delete).entered)
    }
}
