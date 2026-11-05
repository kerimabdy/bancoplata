package tech.bashga.plata.screens.delivery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerActionAtItemEndPause
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.actionAtItemEnd
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.muted
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObjectProtocol

@Composable
internal actual fun rememberStoryVideoPlayer(uri: String, durationSeconds: Double): StoryVideoPlayer {
    val player = remember(uri) { AvStoryVideoPlayer(NSURL.URLWithString(uri)!!) }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun StoryVideoSurface(player: StoryVideoPlayer, modifier: Modifier) {
    if (player !is AvStoryVideoPlayer) return
    UIKitView(
        factory = { PlayerLayerView(player.player) },
        modifier = modifier,
        properties = UIKitInteropProperties(isInteractive = false, isNativeAccessibilityEnabled = false),
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PlayerLayerView(player: AVPlayer) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val playerLayer = AVPlayerLayer.playerLayerWithPlayer(player).apply { videoGravity = AVLayerVideoGravityResizeAspectFill }

    init {
        backgroundColor = UIColor.clearColor
        layer.addSublayer(playerLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        playerLayer.frame = bounds
    }
}

@OptIn(ExperimentalForeignApi::class)
private class AvStoryVideoPlayer(url: NSURL) : StoryVideoPlayer {
    val player = AVPlayer(uRL = url).apply {
        muted = true
        actionAtItemEnd = AVPlayerActionAtItemEndPause
    }
    private var seekTarget: Double? = null
    private var queuedSeek: Double? = null
    private val endObserver: NSObjectProtocol = NSNotificationCenter.defaultCenter.addObserverForName(
        AVPlayerItemDidPlayToEndTimeNotification,
        player.currentItem,
        NSOperationQueue.mainQueue,
    ) { completed = true }

    override var completed = false
        private set

    override val positionSeconds: Double
        get() = seekTarget ?: CMTimeGetSeconds(player.currentTime()).takeUnless { it.isNaN() } ?: 0.0

    override fun play() {
        if (!completed) player.play()
    }

    override fun pause() = player.pause()

    // a zero-tolerance seek decodes the exact frame, so while one runs only the latest request is kept
    override fun seekTo(seconds: Double) {
        completed = false
        if (seekTarget != null) {
            queuedSeek = seconds
            seekTarget = seconds
            return
        }
        seekTarget = seconds
        val exact = CMTimeMake(0, 1)
        player.seekToTime(CMTimeMakeWithSeconds(seconds, 600), exact, exact) { _ ->
            NSOperationQueue.mainQueue.addOperationWithBlock {
                val next = queuedSeek
                queuedSeek = null
                seekTarget = null
                if (next != null) seekTo(next)
            }
        }
    }

    fun release() {
        player.pause()
        NSNotificationCenter.defaultCenter.removeObserver(endObserver)
    }
}
