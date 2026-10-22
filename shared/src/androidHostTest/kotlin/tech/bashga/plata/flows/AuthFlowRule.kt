package tech.bashga.plata.flows

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import tech.bashga.plata.data.Activation
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Session
import tech.bashga.plata.data.Stage
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataApp

/** A live account that has logged in before: phone, passcode 123456 and device link stored. */
internal val returningUser = AppSnapshot(
    session = Session("5512345678", "123456", deviceLinked = true, loggedIn = true),
    stage = Stage.Live,
    activation = Activation.Done,
)

internal typealias FlowRule = ComposeContentTestRule

internal fun FlowRule.launch(repository: AppRepository) = setContent {
    PlataTheme { ProvideSafeArea(PlataSafeArea.IPhone) { PlataApp(repository) } }
}

@OptIn(ExperimentalTestApi::class)
internal fun FlowRule.waitFor(matcher: SemanticsMatcher) = waitUntilAtLeastOneExists(matcher, timeoutMillis = 5_000)

internal fun FlowRule.waitFor(text: String) = waitFor(hasText(text, substring = true))

@OptIn(ExperimentalTestApi::class)
internal fun FlowRule.waitUntilGone(text: String) = waitUntilDoesNotExist(hasText(text, substring = true), timeoutMillis = 5_000)

internal fun FlowRule.tapDigits(digits: String) = digits.forEach { onNodeWithText(it.toString()).performClick() }
