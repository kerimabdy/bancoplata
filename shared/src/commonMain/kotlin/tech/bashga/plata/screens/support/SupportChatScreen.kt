package tech.bashga.plata.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.ChatAvatar
import tech.bashga.plata.designsystem.components.ChatBubble
import tech.bashga.plata.designsystem.components.ChatComposer
import tech.bashga.plata.designsystem.components.ChatDateSeparator
import tech.bashga.plata.designsystem.components.ChatDirection
import tech.bashga.plata.designsystem.components.ChatHeader
import tech.bashga.plata.designsystem.components.ChatMessage
import tech.bashga.plata.designsystem.components.ChatSenderLabel
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@Immutable
data class SupportChatState(
    val thread: List<SupportMessage> = emptyList(),
    val draft: String = "",
    val sent: List<String> = emptyList(),
    val closable: Boolean = false,
)

sealed interface SupportChatAction {
    data class TypeDraft(val text: String) : SupportChatAction

    data object Send : SupportChatAction

    data object Attach : SupportChatAction

    data object Close : SupportChatAction
}

/** Sending has no backend yet: the message stays in this chat until it closes. */
fun SupportChatState.reduce(action: SupportChatAction): SupportChatState = when (action) {
    is SupportChatAction.TypeDraft -> copy(draft = action.text)
    SupportChatAction.Send -> if (draft.isBlank()) this else copy(sent = sent + draft.trim(), draft = "")
    SupportChatAction.Attach, SupportChatAction.Close -> this
}

class SupportChatViewModel(private val repository: AppRepository, closable: Boolean) : ViewModel() {
    private val local = MutableStateFlow(SupportChatState(closable = closable))
    val state: StateFlow<SupportChatState> = combine(repository.state, local) { snapshot, state -> state.copy(thread = snapshot.supportThread) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, local.value.copy(thread = repository.snapshot.supportThread))

    fun onAction(action: SupportChatAction) {
        local.value = local.value.reduce(action)
    }
}

/** support: the chat with Plata, as the Support tab root, or as a cover before the tab bar exists ([onClose]). */
@Composable
fun SupportChatScreen(onClose: (() -> Unit)? = null) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { SupportChatViewModel(repository, closable = onClose != null) }
    val state by viewModel.state.collectAsState()
    val snapshot by repository.state.collectAsState()
    LaunchedEffect(snapshot.supportUnread) { if (snapshot.supportUnread > 0) repository.clearSupportUnread() }
    SupportChatContent(state, { action ->
        if (action == SupportChatAction.Close) onClose?.invoke() else viewModel.onAction(action)
    })
}

// fb63cde9 thread rhythm: header 8 under the safe area, header → date 50, date → sender 20, sender → bubble 6, bubble → date 22
private val HeaderTop = 8.dp
private val HeaderToDate = 50.dp
private val SenderToBubble = 6.dp
private val BubbleToDate = 22.dp
private val ThreadBottom = 24.dp

@Composable
fun SupportChatContent(state: SupportChatState, onAction: (SupportChatAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    val keyboard = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .testTag("thread")
                    .verticalScroll(rememberScrollState())
                    .padding(top = safeArea.top + HeaderTop, bottom = ThreadBottom),
            ) {
                ChatHeader("Support", "We’re here 24/7")
                Spacer(Modifier.height(HeaderToDate))
                state.thread.forEachIndexed { index, message ->
                    if (index > 0) Spacer(Modifier.height(BubbleToDate))
                    ChatDateSeparator(message.date)
                    Spacer(Modifier.height(spacing.s20))
                    ChatSenderLabel(message.sender)
                    Spacer(Modifier.height(SenderToBubble))
                    ChatMessage(ChatDirection.Incoming, avatar = { ChatAvatar() }) {
                        ChatBubble(message.paragraphs.map { it.styled() }, time = message.time)
                    }
                }
                state.sent.forEach { text ->
                    Spacer(Modifier.height(spacing.s12))
                    ChatMessage(ChatDirection.Outgoing) { ChatBubble(text, direction = ChatDirection.Outgoing) }
                }
            }
            ChatComposer(
                state.draft,
                { onAction(SupportChatAction.TypeDraft(it)) },
                Modifier.background(PlataTheme.colors.surfaceTabbar).padding(bottom = max(safeArea.bottom, keyboard)).testTag("composer"),
                onAttach = { onAction(SupportChatAction.Attach) },
                onSend = { onAction(SupportChatAction.Send) },
            )
        }
        if (state.closable) {
            IconButton(
                PlataIcons.CloseX,
                { onAction(SupportChatAction.Close) },
                Modifier.align(Alignment.TopEnd).padding(top = safeArea.top + spacing.s12, end = spacing.s20),
                style = IconButtonStyle.Grey,
                contentDescription = "Close",
            )
        }
    }
}

@Composable
private fun String.styled(): AnnotatedString = withPhoneLink(PlataTheme.colors.accent)

/** The support number becomes a tel: link, which BasicText opens through LocalUriHandler (the dialer). */
internal fun String.withPhoneLink(accent: Color): AnnotatedString {
    val start = indexOf(SUPPORT_PHONE)
    if (start < 0) return AnnotatedString(this)
    return buildAnnotatedString {
        append(substring(0, start))
        withLink(LinkAnnotation.Url(SUPPORT_TEL_URI, TextLinkStyles(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)))) {
            append(SUPPORT_PHONE)
        }
        append(substring(start + SUPPORT_PHONE.length))
    }
}
