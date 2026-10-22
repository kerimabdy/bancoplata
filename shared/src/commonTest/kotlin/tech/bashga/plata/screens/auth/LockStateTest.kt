package tech.bashga.plata.screens.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class LockStateTest {
    private fun LockState.type(digits: String, passcode: String = "123456") =
        digits.fold(this) { state, d -> state.reduce(LockAction.Digit(d.digitToInt()), passcode) }

    @Test
    fun theStoredPasscodeUnlocks() {
        assertEquals(LockState("12345"), LockState().type("12345"))
        assertEquals(LockStatus.Unlocked, LockState().type("123456").status)
    }

    @Test
    fun aWrongPasscodeClearsWithAnError() {
        val wrong = LockState().type("654321")
        assertEquals(LockState("", status = LockStatus.Error), wrong)
        assertEquals(LockState("1", status = LockStatus.Entering), wrong.type("1"))
    }

    @Test
    fun theLengthFollowsTheStoredPasscode() {
        assertEquals(LockStatus.Unlocked, LockState(length = 4).type("1234", passcode = "1234").status)
    }

    @Test
    fun faceIdUnlocksAndLogOutEndsTheEntry() {
        assertEquals(LockStatus.Unlocked, LockState().reduce(LockAction.FaceId, "123456").status)
        val out = LockState().reduce(LockAction.LogOut, "123456")
        assertEquals(LockStatus.LoggedOut, out.status)
        assertEquals(out, out.type("1"))
    }
}
