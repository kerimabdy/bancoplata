package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme

// the intro pages (f5984c79, 72a00afe, f4ff1f66, dc6f5c96) set the title box 459 under the safe area and the CTA 20 above the home-indicator inset
internal val IntroTitleTop = 459.dp

// title → body on the intros measures 17, 2 more than HeadlineBlock medium's 15
internal val IntroBodyGap = 17.dp

/**
 * A page of the Activation modal: the bar ([onBack] chevron, optional [onClose]), [art] placed against the screen, a
 * centred [text] block from [textTop], and the [cta] pinned 20 above the safe-area bottom.
 */
@Composable
internal fun ActivationPage(
    cta: String,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    background: Color = PlataTheme.colors.surfaceCard,
    textTop: Dp = IntroTitleTop,
    art: @Composable BoxScope.() -> Unit,
    text: @Composable ColumnScope.() -> Unit,
) {
    val safeArea = PlataSafeArea.current
    Box(modifier.fillMaxSize().background(background)) {
        art()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(safeArea.top + textTop))
            text()
        }
        Button(
            cta,
            onCta,
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = PlataTheme.spacing.screenMargin, end = PlataTheme.spacing.screenMargin, bottom = safeArea.bottom + PlataTheme.spacing.s20),
        )
        NavigationBar(
            onBack = onBack,
            trailing = onClose?.let { close -> { TextButton("Close", close) } },
            includeStatusBar = true,
        )
    }
}
