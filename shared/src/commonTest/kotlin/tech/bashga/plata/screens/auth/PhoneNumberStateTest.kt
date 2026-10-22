package tech.bashga.plata.screens.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhoneNumberStateTest {
    @Test
    fun continueNeedsTenDigits() {
        val partial = PhoneNumberState().reduce(PhoneNumberAction.TypePhone("55 123"))
        assertEquals("55123", partial.phone)
        assertFalse(partial.canContinue)
        assertNull(partial.reduce(PhoneNumberAction.Continue).overlay)

        val full = partial.reduce(PhoneNumberAction.TypePhone("55 1234 567890"))
        assertEquals("5512345678", full.phone)
        assertTrue(full.canContinue)
        assertEquals(PhoneOverlay.Otp("", OTP_RESEND_SECONDS), full.reduce(PhoneNumberAction.Continue).overlay)
    }

    @Test
    fun theCodeTakesFourDigitsAndCancelCloses() {
        val otp = PhoneNumberState("5512345678", PhoneOverlay.Otp())
        assertEquals(PhoneOverlay.Otp("1234"), otp.reduce(PhoneNumberAction.TypeCode("1a23456")).overlay)
        assertNull(otp.reduce(PhoneNumberAction.CancelOtp).overlay)
        assertEquals(PhoneNumberState("5512345678"), PhoneNumberState("5512345678").reduce(PhoneNumberAction.TypeCode("12")))
    }

    @Test
    fun resendWorksOnceTheTimerRunsOut() {
        val otp = PhoneNumberState("5512345678", PhoneOverlay.Otp())
        assertEquals(otp, otp.reduce(PhoneNumberAction.Resend))
        val expired = (1..OTP_RESEND_SECONDS + 1).fold(otp) { state, _ -> state.tick() }
        assertEquals(PhoneOverlay.Otp(resendIn = 0), expired.overlay)
        assertEquals(PhoneOverlay.Otp(resendIn = OTP_RESEND_SECONDS), expired.reduce(PhoneNumberAction.Resend).overlay)
    }

    @Test
    fun formatsACompleteNumber() {
        assertEquals("55 1234 5678", formatPhone("5512345678"))
        assertEquals("551", formatPhone("551"))
    }
}
