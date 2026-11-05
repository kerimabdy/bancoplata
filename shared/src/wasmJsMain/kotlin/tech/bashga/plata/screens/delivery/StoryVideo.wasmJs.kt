@file:OptIn(ExperimentalWasmJsInterop::class)

package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.browser.document
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private fun copyFrameToSkia(
    context: CanvasRenderingContext2D,
    video: HTMLVideoElement,
    width: Int,
    height: Int,
    skiko: JsAny,
    address: Int,
): Unit = js("{ context.drawImage(video, 0, 0, width, height); new Uint8Array(skiko._.memory.buffer, address, width * height * 4).set(context.getImageData(0, 0, width, height).data); }")

private fun readOnlyContext2d(canvas: HTMLCanvasElement): CanvasRenderingContext2D =
    js("canvas.getContext('2d', { willReadFrequently: true })")

private fun playIgnoringInterrupts(video: HTMLVideoElement): Unit = js("{ video.play().catch(() => {}); }")

@Composable
internal actual fun rememberStoryVideoPlayer(uri: String, durationSeconds: Double): StoryVideoPlayer {
    val player = remember(uri) { HtmlStoryVideoPlayer(uri) }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}

// a <video> element would sit above Compose's canvas and cover the story chrome, so its frames are drawn by Compose instead
@Composable
internal actual fun StoryVideoSurface(player: StoryVideoPlayer, modifier: Modifier) {
    if (player !is HtmlStoryVideoPlayer) return
    var size by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(player, size) {
        while (true) withFrameNanos { player.capture(size) }
    }
    Spacer(
        modifier
            .onSizeChanged { size = it }
            .drawBehind {
                val frame = player.frame ?: return@drawBehind
                val scale = max(this.size.width / frame.width, this.size.height / frame.height)
                val source = IntSize((this.size.width / scale).roundToInt(), (this.size.height / scale).roundToInt())
                drawImage(
                    frame,
                    srcOffset = IntOffset((frame.width - source.width) / 2, (frame.height - source.height) / 2),
                    srcSize = source,
                    dstSize = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt()),
                )
            },
    )
}

private class HtmlStoryVideoPlayer(uri: String) : StoryVideoPlayer {
    // kept in the page but invisible: some browsers stop decoding a video that is detached
    private val video = (document.createElement("video") as HTMLVideoElement).apply {
        muted = true
        preload = "auto"
        setAttribute("playsinline", "")
        setAttribute("style", "position:fixed;left:0;top:0;width:1px;height:1px;opacity:0;pointer-events:none")
        src = uri
        document.body?.appendChild(this)
    }
    private val canvas = document.createElement("canvas") as HTMLCanvasElement
    private val context = readOnlyContext2d(canvas)
    private var pixels: Data? = null
    private var capturedTime = -1.0
    private var capturedSize = IntSize.Zero
    private var seekTarget: Double? = null
    private var queuedSeek: Double? = null

    var frame by mutableStateOf<ImageBitmap?>(null)
        private set

    init {
        video.addEventListener("ended") { completed = true }
        video.addEventListener("seeked") {
            val next = queuedSeek
            queuedSeek = null
            seekTarget = null
            if (next != null) seekTo(next)
        }
    }

    override var completed = false
        private set

    override val positionSeconds: Double
        get() = seekTarget ?: video.currentTime

    // play() rejects whenever a pause lands before playback starts, which the stories do on every hold
    override fun play() {
        if (!completed) playIgnoringInterrupts(video)
    }

    override fun pause() = video.pause()

    // assigning currentTime mid-seek restarts the decode, so scrubbing would show no frame until it stops; keep only the latest
    override fun seekTo(seconds: Double) {
        completed = false
        if (seekTarget != null) {
            queuedSeek = seconds
            seekTarget = seconds
            return
        }
        seekTarget = seconds
        video.currentTime = seconds
    }

    /** Copies the current video frame at no more than [display] pixels, skipping frames the video hasn't moved past. */
    fun capture(display: IntSize) {
        val videoWidth = video.videoWidth
        val videoHeight = video.videoHeight
        if (display == IntSize.Zero || videoWidth == 0 || video.readyState < 2) return
        val scale = min(1.0, max(display.width.toDouble() / videoWidth, display.height.toDouble() / videoHeight))
        val target = IntSize((videoWidth * scale).roundToInt(), (videoHeight * scale).roundToInt())
        if (video.currentTime == capturedTime && target == capturedSize) return

        val byteCount = target.width * target.height * 4
        val data = pixels?.takeIf { target == capturedSize } ?: run {
            pixels?.close()
            canvas.width = target.width
            canvas.height = target.height
            Data.makeUninitialized(byteCount).also { pixels = it }
        }
        copyFrameToSkia(context, video, target.width, target.height, loadedWasm, data.writableData())
        val info = ImageInfo(target.width, target.height, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL)
        frame = Image.makeRaster(info, data, target.width * 4).toComposeImageBitmap()
        capturedTime = video.currentTime
        capturedSize = target
    }

    fun release() {
        video.pause()
        video.removeAttribute("src")
        video.load()
        video.remove()
        pixels?.close()
        pixels = null
    }
}
