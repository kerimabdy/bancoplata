package tech.bashga.plata.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Currency
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SegmentedControl
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.AppNavigator
import tech.bashga.plata.navigation.Presentation
import tech.bashga.plata.navigation.screen

/** True in debug builds; Profile shows the Dev panel's long-press entry only then. */
@Composable
expect fun isDebugBuild(): Boolean

/**
 * The debug-only actions of SCREEN-MAP §2.1, for what the references show and the app can't produce by itself.
 * UI tests call them at the ⚠ steps of the flows.
 */
class DevActions(private val repository: AppRepository) {
    fun setMonth(month: Month) = repository.setMonth(month)

    fun seedPurchases() = repository.seedPurchases()

    fun buyDnut(): Boolean = repository.buyDnut()

    fun supportMessage() = repository.addSupportMessage()

    /** Deletes the snapshot and relaunches at Splash. */
    fun reset() = repository.reset()
}

@Serializable
data object DevPanelRoute

fun NavGraphBuilder.devPanel(nav: AppNavigator, repository: AppRepository) {
    screen<DevPanelRoute>(Presentation.Sheet) {
        val snapshot by repository.state.collectAsState()
        DevPanel(snapshot, DevActions(repository), onClose = nav::back)
    }
}

@Composable
fun DevPanel(snapshot: AppSnapshot, actions: DevActions, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    Column(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        NavigationBar(style = NavigationBarStyle.Sheet, title = "Dev panel", leading = { TextButton("Close", onClose) })
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenMargin)
                .padding(top = spacing.s12, bottom = PlataSafeArea.current.bottom + spacing.s20),
            verticalArrangement = Arrangement.spacedBy(spacing.s20),
        ) {
            SegmentedControl(Month.entries.map { it.label }, Month.entries.indexOf(snapshot.month), { actions.setMonth(Month.entries[it]) }, Modifier.fillMaxWidth())
            ListGroup {
                ListRow("Seed purchases", subtitle = "The ${snapshot.month.label} purchases with no in-app source", onClick = actions::seedPurchases)
                ListRow(
                    "Buy DNUT",
                    subtitle = "Inversiones cash ${formatMoney(snapshot.accounts.inversionesCashUsd, Currency.Usd, showCents = true)}",
                    enabled = snapshot.accounts.inversionesCashUsd >= 100,
                    onClick = { actions.buyDnut() },
                )
                ListRow("Support message", value = snapshot.supportUnread.toString(), onClick = actions::supportMessage)
            }
            ListGroup {
                ListRow("Reset", subtitle = "Delete the snapshot, relaunch at Splash", onClick = actions::reset)
            }
            PlataText(
                "Stage ${snapshot.stage::class.simpleName} · activation ${snapshot.activation} · available ${formatMoney(snapshot.creditAvailable)} · " +
                    "own balance ${formatMoney(snapshot.accounts.ownBalance)}",
                PlataTheme.typography.footnote,
                colors.textSecondary,
            )
        }
    }
}
