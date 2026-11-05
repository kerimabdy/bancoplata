package tech.bashga.plata.screens.delivery

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

private const val AssetPrefix = "file:///android_asset/"

// Robolectric can't decode video, so screenshot tests draw the poster on the frame clock
private val isRobolectric get() = Build.FINGERPRINT == "robolectric"

@Composable
internal actual fun rememberStoryVideoPlayer(uri: String, durationSeconds: Double): StoryVideoPlayer {
    val context = LocalContext.current
    val player = remember(uri) {
        if (isRobolectric) ClockVideoPlayer(durationSeconds) else MediaStoryVideoPlayer(context, uri.removePrefix(AssetPrefix))
    }
    DisposableEffect(player) { onDispose { (player as? MediaStoryVideoPlayer)?.release() } }
    return player
}

@Composable
internal actual fun StoryVideoSurface(player: StoryVideoPlayer, modifier: Modifier) {
    if (player !is MediaStoryVideoPlayer) return
    AndroidView(
        factory = { context ->
            TextureView(context).apply {
                isOpaque = false
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) = player.attach(Surface(texture))

                    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                        player.attach(null)
                        return true
                    }

                    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = Unit

                    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
                }
            }
        },
        modifier = modifier,
    )
}

private class MediaStoryVideoPlayer(context: Context, assetPath: String) : StoryVideoPlayer {
    private val media = MediaPlayer()
    private var prepared = false
    private var wantsPlay = false
    private var seekTarget: Double? = null
    private var queuedSeek: Double? = null
    private var surface: Surface? = null

    override var completed = false
        private set

    override val positionSeconds: Double
        get() = seekTarget ?: if (prepared) media.currentPosition / 1000.0 else 0.0

    init {
        context.assets.openFd(assetPath).use { media.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
        media.setVolume(0f, 0f)
        media.setOnPreparedListener {
            prepared = true
            seekTarget?.let { target -> seekTarget = null; seekTo(target) }
            if (wantsPlay) media.start()
        }
        media.setOnSeekCompleteListener {
            val next = queuedSeek
            queuedSeek = null
            seekTarget = null
            if (next != null) seekTo(next)
        }
        media.setOnCompletionListener { completed = true }
        media.prepareAsync()
    }

    fun attach(next: Surface?) {
        media.setSurface(next)
        surface?.release()
        surface = next
    }

    override fun play() {
        wantsPlay = true
        if (prepared && !completed && !media.isPlaying) media.start()
    }

    override fun pause() {
        wantsPlay = false
        if (prepared && media.isPlaying) media.pause()
    }

    // MediaPlayer queues every seek, so while one runs only the latest request is kept
    override fun seekTo(seconds: Double) {
        completed = false
        if (!prepared || seekTarget != null) {
            if (prepared) queuedSeek = seconds
            seekTarget = seconds
            return
        }
        seekTarget = seconds
        media.seekTo((seconds * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
    }

    fun release() {
        media.release()
        surface?.release()
        surface = null
    }
}
