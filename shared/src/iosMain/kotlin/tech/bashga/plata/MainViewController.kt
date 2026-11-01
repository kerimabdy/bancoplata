package tech.bashga.plata

import androidx.compose.ui.window.ComposeUIViewController
import tech.bashga.plata.designsystem.components.PrewarmHeroWave

fun MainViewController() = ComposeUIViewController {
    PrewarmHeroWave()
    App()
}
