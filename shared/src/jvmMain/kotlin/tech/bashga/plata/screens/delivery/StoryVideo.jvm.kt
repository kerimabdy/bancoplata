package tech.bashga.plata.screens.delivery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
internal actual fun rememberStoryVideoPlayer(uri: String, durationSeconds: Double): StoryVideoPlayer =
    remember(uri) { ClockVideoPlayer(durationSeconds) }

// no video on desktop: the poster under it stands in
@Composable
internal actual fun StoryVideoSurface(player: StoryVideoPlayer, modifier: Modifier) = Unit
