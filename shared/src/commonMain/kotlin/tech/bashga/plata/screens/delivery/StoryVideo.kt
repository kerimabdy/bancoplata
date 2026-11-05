package tech.bashga.plata.screens.delivery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A muted, frame-accurate video the stories player drives: it seeks every frame while scrubbing or fast-forwarding. */
internal interface StoryVideoPlayer {
    /** Where a seek is heading while it runs, so callers never see the frame they just left. */
    val positionSeconds: Double
    val completed: Boolean

    fun play()

    fun pause()

    fun seekTo(seconds: Double)

    fun onFrame(frameNanos: Long) {}
}

/** The video at [uri] (a Compose resource URI), released when it leaves the composition. */
@Composable
internal expect fun rememberStoryVideoPlayer(uri: String, durationSeconds: Double): StoryVideoPlayer

@Composable
internal expect fun StoryVideoSurface(player: StoryVideoPlayer, modifier: Modifier)

/** Where no video can play (desktop, Robolectric): time runs on the frame clock over the poster. */
internal class ClockVideoPlayer(private val durationSeconds: Double) : StoryVideoPlayer {
    private var playing = false
    private var lastFrame = -1L

    override var positionSeconds = 0.0
        private set

    override val completed get() = positionSeconds >= durationSeconds

    override fun play() {
        playing = true
    }

    override fun pause() {
        playing = false
    }

    override fun seekTo(seconds: Double) {
        positionSeconds = seconds.coerceIn(0.0, durationSeconds)
    }

    override fun onFrame(frameNanos: Long) {
        if (playing && lastFrame >= 0) seekTo(positionSeconds + (frameNanos - lastFrame) / 1e9)
        lastFrame = frameNanos
    }
}
