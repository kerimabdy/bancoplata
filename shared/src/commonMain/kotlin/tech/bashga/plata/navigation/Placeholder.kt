package tech.bashga.plata.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

/** One outgoing action of an unconverted screen. */
class PlaceholderAction(val label: String, val onClick: () -> Unit)

infix fun String.does(onClick: () -> Unit) = PlaceholderAction(this, onClick)

/**
 * Stands in for a screen that isn't converted yet: its name, route and a button per outgoing navigation action, so
 * the app can be walked end to end. [status] lists repository values the screen would show.
 */
@Composable
fun PlaceholderScreen(name: String, route: String, vararg actions: PlaceholderAction, status: List<String> = emptyList()) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.surfaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenMargin)
            .padding(top = safeArea.top + spacing.s40, bottom = safeArea.bottom + spacing.s20),
        verticalArrangement = Arrangement.spacedBy(spacing.s12),
    ) {
        PlataText(name, PlataTheme.typography.largeTitle, colors.textPrimaryTitle)
        PlataText("$route · placeholder", PlataTheme.typography.footnote, colors.textSecondary)
        status.forEach { PlataText(it, PlataTheme.typography.subhead, colors.textPrimary) }
        Spacer(Modifier.height(spacing.s8))
        actions.forEach { action ->
            Button(action.label, action.onClick, Modifier.fillMaxWidth(), style = ButtonStyle.Secondary, size = ButtonSize.Medium)
        }
    }
}
