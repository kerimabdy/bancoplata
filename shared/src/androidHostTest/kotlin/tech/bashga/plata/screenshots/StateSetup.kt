package tech.bashga.plata.screenshots

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.Dp
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.TabBarTab
import tech.bashga.plata.navigation.TabBarData
import tech.bashga.plata.navigation.tabBarData

/** The tab bar a state test draws: SCREEN-MAP §6 "shell" (June · $200 · Home 2 → `TestShell(Home, June, homeBadge = 2)`). */
data class TestShell(val tab: TabBarTab, val month: Month = Month.June, val homeBadge: Int = 0, val supportBadge: Int = 0) {
    val data: TabBarData
        get() = AppSnapshot(month = month, supportUnread = supportBadge).tabBarData.copy(homeBadge = homeBadge.takeIf { it > 0 }?.toString())
}

/**
 * What a state test does after composing, the way a user would (SCREEN-MAP §3 "test setup"). Screens tag the nodes
 * these find with `Modifier.testTag`.
 */
class StateSetup(val rule: ComposeContentTestRule) {
    private fun scrollable(tag: String?): SemanticsNodeInteraction =
        if (tag != null) rule.onNodeWithTagUnmerged(tag) else rule.onAllNodes(hasScrollAction()).onFirst()

    /** Scrolls [tag] (or the first scrollable) by exactly [distance]; positive scrolls the content up. */
    fun scrollBy(distance: Dp, tag: String? = null) {
        val px = with(rule.density) { distance.toPx() }
        scrollable(tag).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, px) }
        rule.waitForIdle()
    }

    /** Scrolls until the top of the node tagged [anchor] sits [top] below the screen top. */
    fun scrollToAnchor(anchor: String, top: Dp, scrollableTag: String? = null) {
        val nodeTop = rule.onNodeWithTagUnmerged(anchor).fetchSemanticsNode().boundsInRoot.top
        val px = nodeTop - with(rule.density) { top.toPx() }
        scrollable(scrollableTag).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, px) }
        rule.waitForIdle()
    }

    /** Scrolls a horizontal scrollable (an icon row) by [distance]. */
    fun scrollHorizontallyBy(distance: Dp, tag: String) {
        val px = with(rule.density) { distance.toPx() }
        rule.onNodeWithTagUnmerged(tag).performSemanticsAction(SemanticsActions.ScrollBy) { it(px, 0f) }
        rule.waitForIdle()
    }

    /** Focuses the text field tagged [tag] (or inside it), caret after the text; pair with `clockMillis` to capture the caret. */
    fun focus(tag: String) {
        val field = rule.onNode((hasTestTag(tag) or hasAnyAncestor(hasTestTag(tag))) and hasSetTextAction(), useUnmergedTree = true)
        field.requestFocus()
        val length = field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text.length
        field.performTextInputSelection(TextRange(length))
    }

    /** Swipes the pager tagged [tag] to [page]. */
    fun swipePager(tag: String, page: Int) {
        rule.onNodeWithTagUnmerged(tag).performScrollToIndex(page)
        rule.waitForIdle()
    }

    fun advanceClock(millis: Long) = rule.mainClock.advanceTimeBy(millis)

    private fun ComposeContentTestRule.onNodeWithTagUnmerged(tag: String) =
        onNode(hasTestTag(tag), useUnmergedTree = true)
}
