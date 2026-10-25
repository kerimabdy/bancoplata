package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CheckCircle
import tech.bashga.plata.designsystem.components.CheckCircleState
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.components.TextFieldClearButton
import tech.bashga.plata.designsystem.components.TextFieldSurface
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataBackHandler

/** [dislikedLabel] is the 👎 wording, which has no reference. */
enum class FeedbackAspect(val label: String, val dislikedLabel: String) {
    GreatCommunication("Great communication", "Poor communication"),
    ClearIntro("Clear introduction of the product", "Unclear introduction of the product"),
    QuickProcess("Quick process", "Slow process"),
    SimpleProcess("Simple process", "Complicated process"),
    WalletAndPackage("Cool wallet and package", "Damaged wallet or package"),
    DecentLimit("Decent limit", "Low limit"),
}

/** [positive]: opened with 👍; 👎 asks what went wrong instead. */
@Immutable
data class FeedbackState(val liked: Set<FeedbackAspect> = emptySet(), val comment: String = "", val positive: Boolean = true) {
    val title: String get() = if (positive) "Great! Tell us what\nyou liked" else "Sorry! Tell us what\nwent wrong"

    fun label(aspect: FeedbackAspect): String = if (positive) aspect.label else aspect.dislikedLabel
}

sealed interface FeedbackAction {
    data class Toggle(val aspect: FeedbackAspect) : FeedbackAction

    data class Comment(val text: String) : FeedbackAction

    data object Send : FeedbackAction

    data object Close : FeedbackAction
}

fun FeedbackState.reduce(action: FeedbackAction): FeedbackState = when (action) {
    is FeedbackAction.Toggle -> copy(liked = if (action.aspect in liked) liked - action.aspect else liked + action.aspect)
    is FeedbackAction.Comment -> copy(comment = action.text)
    FeedbackAction.Send, FeedbackAction.Close -> this
}

class FeedbackViewModel(private val repository: AppRepository, positive: Boolean) : ViewModel() {
    private val _state = MutableStateFlow(FeedbackState(positive = positive))
    val state: StateFlow<FeedbackState> = _state.asStateFlow()

    fun onAction(action: FeedbackAction) {
        _state.value = _state.value.reduce(action)
    }

    /** Send, Close or a swipe down all leave for the Activation modal; there is no backend to send the form to. */
    fun finish() = repository.startActivation()
}

/** feedback: what the user liked, or after 👎 what went wrong, about the delivery. Send and Close both lead on to Activation, Send with a thank-you toast. */
@Composable
fun FeedbackScreen(liked: Boolean, onSent: () -> Unit, onClosed: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { FeedbackViewModel(repository, liked) }
    val state by viewModel.state.collectAsState()
    val close = {
        viewModel.finish()
        onClosed()
    }
    PlataBackHandler(onBack = close)
    FeedbackContent(state, onAction = { action ->
        when (action) {
            FeedbackAction.Send -> {
                viewModel.finish()
                onSent()
            }
            FeedbackAction.Close -> close()
            else -> viewModel.onAction(action)
        }
    })
}

// the reason rows start 8 below the card top, as on 4e9baf69
private val ReasonsTop = 8.dp
private val TitleTop = 22.dp
private val FieldAboveButton = 16.dp

@Composable
fun FeedbackContent(state: FeedbackState, onAction: (FeedbackAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val density = LocalDensity.current
    val scroll = rememberScrollState()
    var commentFocused by remember { mutableStateOf(false) }
    val keyboard = keyboardHeight(commentFocused)
    val footerInset = if (keyboard > 0.dp) keyboard else PlataSafeArea.current.bottom
    val buttonTop = footerInset + spacing.s20 + PlataTheme.sizes.buttonLarge
    var fieldBottom by remember { mutableFloatStateOf(0f) }
    var viewportBottom by remember { mutableFloatStateOf(0f) }
    // as iOS does for a focused field, scroll it up to sit 16 above the lifted button (9a8e3fd6)
    LaunchedEffect(keyboard) {
        if (keyboard > 0.dp) {
            val overlap = fieldBottom - (viewportBottom - with(density) { (buttonTop + FieldAboveButton).toPx() })
            if (overlap > 0f) scroll.animateScrollBy(overlap)
        }
    }
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { viewportBottom = it.boundsInRoot().bottom }
                .backdropSource()
                .verticalScroll(scroll)
                .padding(top = PlataTheme.sizes.navSheet, start = spacing.screenMargin, end = spacing.screenMargin),
        ) {
            Spacer(Modifier.height(TitleTop))
            HeadlineBlock(state.title, Modifier.fillMaxWidth())
            Spacer(Modifier.height(spacing.s24 + spacing.s4))
            SectionCard(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = ReasonsTop, bottom = spacing.s20),
            ) {
                FeedbackAspect.entries.forEach { aspect ->
                    val checked = aspect in state.liked
                    ListRow(
                        state.label(aspect),
                        Modifier.fillMaxWidth(),
                        onClick = { onAction(FeedbackAction.Toggle(aspect)) },
                        trailing = { CheckCircle(if (checked) CheckCircleState.Checked else CheckCircleState.Unchecked) },
                    )
                }
                Spacer(Modifier.height(spacing.s20))
                TextField(
                    state.comment,
                    { onAction(FeedbackAction.Comment(it)) },
                    Modifier
                        .padding(horizontal = spacing.s20)
                        .fillMaxWidth()
                        .testTag("comment")
                        .onGloballyPositioned { fieldBottom = it.positionInRoot().y + it.size.height }
                        .onFocusChanged { commentFocused = it.isFocused },
                    placeholder = "Any additional comments?",
                    surface = TextFieldSurface.Grey,
                    trailing = if (state.comment.isNotEmpty()) ({ TextFieldClearButton({ onAction(FeedbackAction.Comment("")) }) }) else null,
                )
            }
            // room to scroll the card above the keyboard and the pinned button
            Spacer(Modifier.height(footerInset + PlataTheme.sizes.buttonLarge + spacing.s40 * 2))
        }
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            leading = { TextButton("Close", { onAction(FeedbackAction.Close) }) },
            material = rememberCollapsed(scroll),
        )
        Button(
            "Send feedback",
            { onAction(FeedbackAction.Send) },
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = spacing.s20 + footerInset)
                .fillMaxWidth(),
        )
    }
}
