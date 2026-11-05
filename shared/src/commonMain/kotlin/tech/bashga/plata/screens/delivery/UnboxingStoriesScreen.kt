package tech.bashga.plata.screens.delivery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_unboxing_poster
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.Logo
import tech.bashga.plata.designsystem.components.LogoBrand
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonTone
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

sealed interface UnboxingStoriesAction {
    data object CallSupport : UnboxingStoriesAction

    data object ContactSupport : UnboxingStoriesAction

    data object Close : UnboxingStoriesAction
}

/**
 * delivery/unboxing: the packaging steps as Stories over one continuous render. Playback is like a pager's page,
 * so it lives in [UnboxingStoriesContent] rather than a ViewModel.
 */
@Composable
fun UnboxingStoriesScreen(onCallSupport: () -> Unit, onContactSupport: () -> Unit, onClose: () -> Unit) {
    UnboxingStoriesContent(onAction = { action ->
        when (action) {
            UnboxingStoriesAction.CallSupport -> onCallSupport()
            UnboxingStoriesAction.ContactSupport -> onContactSupport()
            UnboxingStoriesAction.Close -> onClose()
        }
    })
}

private class StoryChapter(val start: Double, val end: Double, val title: String, val body: String)

private val Chapters = listOf(
    StoryChapter(0.0, 4.0, "Secure packaging\nof Plata Card", "Your card arrives sealed in a tamper-evident Escudo de Plata pouch."),
    StoryChapter(4.0, 6.7, "Check the tear tape", "Make sure the tear tape is intact and the bag has no damage before you accept it."),
    // the pocket first shows at frame 204 (6.8 s), so the card stays hidden until this step
    StoryChapter(6.7, 15.0, "Open it in private", "Open the bag in a private place after delivery to protect your card details."),
)

private const val VideoEnd = 15.0 - 0.04

// players snap a seek to the nearest frame, which can land in the previous chapter
private const val ChapterLead = 0.05
private const val HoldMillis = 220L
private const val TapMillis = 300L
private const val ScrubSecondsPerWidth = 7.5
private const val SwipeCommit = 0.2f
private const val CaptionLine = 0.7f
private val FlickPerSecond = 350.dp
private val ScrubSlop = 6.dp
private val EndedSwipe = 60.dp
private val FadeHeight = 88.dp
private val StoriesEasing = CubicBezierEasing(0.22f, 0.8f, 0.25f, 1f)

private fun chapterAt(seconds: Double) = Chapters.indexOfLast { seconds >= it.start }.coerceAtLeast(0)

private fun chapterTime(index: Int) = Chapters[index].start + ChapterLead

/** The video, the progress bars and the caption track, moved together by taps, swipes, holds and scrubs. */
@Stable
private class StoriesPlayback(private val player: StoryVideoPlayer, private val scope: CoroutineScope) {
    var index by mutableIntStateOf(0)
        private set
    var ended by mutableStateOf(false)
        private set
    var holding by mutableStateOf(false)
        private set
    var scrubLabel by mutableStateOf<String?>(null)
        private set
    private var time by mutableDoubleStateOf(0.0)

    /** The caption track in pages, plus the finger's share while a swipe is down. */
    val track = Animatable(0f)
    var dragPages by mutableFloatStateOf(0f)
        private set

    private var active = false
    private var dragging = false
    private var scrubbing = false
    private var dragFrom = 0.0
    private var scrubFrom = 0.0
    private var scrubTime = 0.0
    private var fastForward: Job? = null
    private var fastForwardTo: Int? = null

    private val shouldPlay get() = active && !ended && !holding && !dragging && !scrubbing && fastForward == null

    fun progress(i: Int): Float = when {
        ended || i < index -> 1f
        i > index -> 0f
        else -> Chapters[i].let { ((time - it.start) / (it.end - it.start)).coerceIn(0.0, 1.0).toFloat() }
    }

    fun setActive(value: Boolean) {
        active = value
        if (value) resume() else player.pause()
    }

    fun resume() {
        if (shouldPlay) player.play()
    }

    fun onFrame(frameNanos: Long) {
        player.onFrame(frameNanos)
        time = player.positionSeconds
        if (ended || dragging || scrubbing || fastForward != null) return
        if (player.completed || time >= VideoEnd) return finish()
        // forward only, so a seek landing a frame early never pulls the captions back a chapter
        if (chapterAt(time) > index) showChapter(chapterAt(time))
    }

    private fun showChapter(i: Int) {
        index = i
        scope.launch {
            if (dragPages != 0f) {
                track.snapTo(track.value + dragPages)
                dragPages = 0f
            }
            track.animateTo(i.toFloat(), tween(450, easing = StoriesEasing))
        }
    }

    private fun goTo(i: Int) {
        if (i >= Chapters.size) return finish()
        ended = false
        showChapter(i.coerceAtLeast(0))
        player.seekTo(chapterTime(index))
        resume()
    }

    private fun finish() {
        ended = true
        index = Chapters.lastIndex
        player.pause()
        player.seekTo(VideoEnd)
    }

    private fun cancelFastForward() {
        val job = fastForward ?: return
        job.cancel()
        if ((fastForwardTo ?: 0) >= Chapters.size) ended = false
        fastForward = null
        fastForwardTo = null
    }

    // eases currentTime there frame by frame: the render is all keyframes, so every seek lands at once
    private fun fastForward(target: Double, toIndex: Int, onDone: () -> Unit) {
        cancelFastForward()
        player.pause()
        val from = player.positionSeconds
        val millis = (220 + (target - from) * 110).coerceIn(320.0, 900.0)
        fastForwardTo = toIndex
        fastForward = scope.launch {
            val start = withFrameNanos { it }
            var p = 0.0
            while (p < 1) {
                p = ((withFrameNanos { it } - start) / 1e6 / millis).coerceAtMost(1.0)
                val eased = if (p < 0.5) 2 * p * p else 1 - (-2 * p + 2).let { it * it } / 2
                player.seekTo(from + (target - from) * eased)
            }
            fastForward = null
            fastForwardTo = null
            onDone()
        }
    }

    /** The captions move at once; the video catches up. Repeated taps chain from where a running one is heading. */
    fun next() {
        if (ended) return
        val to = (fastForwardTo ?: index) + 1
        if (to >= Chapters.size) {
            ended = true
            fastForward(VideoEnd, to, ::finish)
        } else {
            showChapter(to)
            fastForward(chapterTime(to), to, ::resume)
        }
    }

    fun previous() {
        val towardEnd = ended || (fastForwardTo ?: 0) >= Chapters.size
        cancelFastForward()
        goTo(if (towardEnd) Chapters.lastIndex else index - 1)
    }

    fun startHold() {
        if (ended) return
        cancelFastForward()
        holding = true
        scrubLabel = null
        player.pause()
    }

    fun startScrub() {
        scrubbing = true
        scrubFrom = player.positionSeconds
        scrubTime = scrubFrom
    }

    fun scrub(widths: Float) {
        scrubTime = (scrubFrom + widths * ScrubSecondsPerWidth).coerceIn(0.0, VideoEnd)
        player.seekTo(scrubTime)
        time = scrubTime
        if (chapterAt(scrubTime) != index) showChapter(chapterAt(scrubTime))
        scrubLabel = "${if (scrubTime >= scrubFrom) "▶▶" else "◀◀"}  0:${floor(scrubTime).toInt().toString().padStart(2, '0')}"
    }

    fun endHold() {
        holding = false
        if (scrubbing) {
            scrubbing = false
            if (scrubTime >= VideoEnd - 0.06) return finish()
        }
        resume()
    }

    fun startSwipe() {
        if (ended) return
        cancelFastForward()
        dragging = true
        player.pause()
        dragFrom = player.positionSeconds
    }

    /** One page per swipe: the track can't be pulled past the neighbouring page. */
    fun swipe(widths: Float) {
        if (!dragging) return
        var pull = widths.coerceIn(-1f, 1f)
        if (index == 0 && pull > 0) pull *= 0.3f
        dragPages = -pull
        val target = when {
            pull < 0 -> if (index < Chapters.lastIndex) Chapters[index + 1].start else Chapters[index].end
            index > 0 -> Chapters[index - 1].start
            else -> 0.0
        }
        player.seekTo(dragFrom + (target - dragFrom) * abs(pull))
    }

    fun endSwipe(widths: Float, flick: Boolean, backFromEnd: Boolean) {
        if (ended) {
            if (backFromEnd) goTo(Chapters.lastIndex)
            return
        }
        dragging = false
        val commit = abs(widths) > SwipeCommit || flick
        when {
            commit && widths < 0 -> goTo(index + 1)
            commit && widths > 0 && index > 0 -> goTo(index - 1)
            else -> {
                showChapter(index)
                player.seekTo(dragFrom)
                resume()
            }
        }
    }

}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UnboxingStoriesContent(onAction: (UnboxingStoriesAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val safe = PlataSafeArea.current
    val scope = rememberCoroutineScope()
    val player = rememberStoryVideoPlayer(Res.getUri("files/unboxing.mp4"), durationSeconds = 15.0)
    val playback = remember(player) { StoriesPlayback(player, scope) }

    // the end is a still frame, so the clock stops there and tests can go idle
    LaunchedEffect(playback, playback.ended) {
        while (!playback.ended) withFrameNanos(playback::onFrame)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, playback) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> playback.setActive(true)
                Lifecycle.Event.ON_PAUSE -> playback.setActive(false)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            playback.setActive(false)
        }
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(colors.surfaceVideo)
            .pointerInput(playback) {
                val width = size.width.toFloat()
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val velocity = VelocityTracker().apply { addPosition(down.uptimeMillis, down.position) }
                    var moved: PointerInputChange? = null
                    var released: PointerInputChange? = null
                    val beforeHold = withTimeoutOrNull(HoldMillis) {
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            velocity.addPosition(change.uptimeMillis, change.position)
                            if (!change.pressed) {
                                released = change
                                break
                            }
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                moved = change
                                break
                            }
                        }
                    }
                    val up = released
                    if (up != null) {
                        if (!up.isConsumed && up.uptimeMillis - down.uptimeMillis < TapMillis) {
                            if (down.position.x < width / 3) playback.previous() else playback.next()
                        }
                        return@awaitEachGesture
                    }
                    if (beforeHold == null) {
                        playback.startHold()
                        var scrubbing = false
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val dx = change.position.x - down.position.x
                            if (!scrubbing && abs(dx) > ScrubSlop.toPx()) {
                                scrubbing = true
                                playback.startScrub()
                            }
                            if (scrubbing) playback.scrub(dx / width)
                            change.consume()
                        }
                        playback.endHold()
                        return@awaitEachGesture
                    }
                    val first = moved ?: return@awaitEachGesture
                    // vertical drags stay unconsumed: they belong to the sheet, which closes on a swipe down
                    if (abs(first.position.x - down.position.x) <= abs(first.position.y - down.position.y)) return@awaitEachGesture
                    playback.startSwipe()
                    var last = first
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        velocity.addPosition(change.uptimeMillis, change.position)
                        last = change
                        if (!change.pressed) break
                        playback.swipe((change.position.x - down.position.x) / width)
                        change.consume()
                    }
                    val dx = last.position.x - down.position.x
                    playback.endSwipe(
                        dx / width,
                        flick = abs(velocity.calculateVelocity().x) > FlickPerSecond.toPx(),
                        backFromEnd = dx > EndedSwipe.toPx(),
                    )
                }
            },
    ) {
        val width = maxWidth
        val videoTop = safe.top + 2.dp
        val videoHeight = width * 16f / 9f
        val captionTop = videoTop + videoHeight * CaptionLine
        val ended = playback.ended
        val videoShift by animateDpAsState(if (ended) (-24).dp else 0.dp, tween(500, easing = StoriesEasing))
        val videoDim by animateFloatAsState(if (ended) 0.55f else 0f, tween(500, easing = StoriesEasing))
        val captionsAlpha by animateFloatAsState(if (ended) 0f else 1f, tween(300, easing = StoriesEasing))
        val chromeAlpha by animateFloatAsState(if (playback.holding) 0.4f else 1f, tween(200))
        val badgeAlpha by animateFloatAsState(if (playback.holding) 1f else 0f, tween(200))
        val endRise = with(LocalDensity.current) { 24.dp.roundToPx() }

        Box(Modifier.fillMaxSize()) {
            Box(Modifier.offset(y = videoTop + videoShift).fillMaxWidth().height(videoHeight)) {
                Image(painterResource(Res.drawable.raster_unboxing_poster), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                StoryVideoSurface(player, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().drawBehind { drawRect(colors.surfaceVideo.copy(alpha = videoDim)) })
            }
            // clips the video at the caption line and fades it out above, so falling pouches never run under the text
            Box(
                Modifier
                    .offset(y = captionTop - FadeHeight)
                    .fillMaxWidth()
                    .height(FadeHeight)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, colors.surfaceVideo))),
            )
            Box(Modifier.offset(y = captionTop).fillMaxSize().background(colors.surfaceVideo))

            Row(
                Modifier
                    .padding(top = safe.top + 8.dp, start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = chromeAlpha },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Chapters.indices.forEach { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(colors.textOnDark.copy(alpha = 0.3f))
                            .drawBehind { drawRect(colors.textOnDark, size = Size(size.width * playback.progress(i), size.height)) },
                    )
                }
            }

            TextButton(
                "Close",
                { onAction(UnboxingStoriesAction.Close) },
                Modifier.align(Alignment.TopEnd).padding(top = safe.top + 20.dp, end = 16.dp).graphicsLayer { alpha = chromeAlpha },
                tone = TextButtonTone.OnDark,
            )

            PlataText(
                playback.scrubLabel ?: "Paused",
                typography.footnote,
                colors.textOnDark,
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = safe.top + 70.dp)
                    .graphicsLayer { alpha = badgeAlpha }
                    .background(colors.glassTile, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )

            Box(
                Modifier
                    .offset(y = captionTop)
                    .fillMaxWidth()
                    .clipToBounds()
                    .graphicsLayer {
                        alpha = captionsAlpha
                        translationY = (1f - captionsAlpha) * 12.dp.toPx()
                    }
                    .testTag("stories-captions"),
            ) {
                Row(
                    Modifier
                        .wrapContentWidth(Alignment.Start, unbounded = true)
                        .offset { IntOffset((-(playback.track.value + playback.dragPages) * width.toPx()).roundToInt(), 0) },
                ) {
                    Chapters.forEachIndexed { i, chapter ->
                        Column(
                            Modifier.width(width).padding(horizontal = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PlataText(chapter.title, typography.title1, colors.textOnDark, textAlign = TextAlign.Center)
                            PlataText(chapter.body, typography.subhead, colors.textOnDark.copy(alpha = 0.75f), textAlign = TextAlign.Center)
                            if (i == 0) Logo(LogoBrand.Escudo, Modifier.padding(top = 4.dp), height = 40.dp)
                        }
                    }
                }
            }

            AnimatedVisibility(
                ended,
                Modifier.align(Alignment.BottomCenter).padding(start = 40.dp, end = 40.dp, bottom = safe.bottom + 66.dp),
                enter = fadeIn(tween(400, 100, StoriesEasing)) + slideInVertically(tween(400, 100, StoriesEasing)) { endRise },
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { endRise },
            ) {
                StoriesEnd(onAction)
            }
        }
    }
}

@Composable
private fun StoriesEnd(onAction: (UnboxingStoriesAction) -> Unit) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val phone = buildAnnotatedString {
        append("If the bag is damaged, call us at ")
        withLink(LinkAnnotation.Clickable("phone", TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline))) { onAction(UnboxingStoriesAction.CallSupport) }) {
            append(SupportPhone.replace(' ', '\u00A0'))
        }
        append(" or send us a chat.")
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        PlataText("Problem reports", typography.title2, colors.textOnDark, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        PlataText(phone, typography.subhead, colors.textOnDark.copy(alpha = 0.75f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button("Contact support", { onAction(UnboxingStoriesAction.ContactSupport) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TextButton("Done", { onAction(UnboxingStoriesAction.Close) }, tone = TextButtonTone.OnDark)
    }
}
