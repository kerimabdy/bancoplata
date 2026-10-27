package tech.bashga.plata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.KeyValueStore
import tech.bashga.plata.data.rememberKeyValueStore
import tech.bashga.plata.designsystem.foundation.CardMotion
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideWindowSafeArea
import tech.bashga.plata.designsystem.foundation.SafeAreaInsets
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.dev.dials.DialRoot
import tech.bashga.plata.dev.isDebugBuild
import tech.bashga.plata.navigation.PlataApp

// a ViewModel so the repository lives as long as the process: rotation keeps it, a cold launch builds a new one
private class AppRepositoryHolder(store: KeyValueStore) : ViewModel() {
    val repository = AppRepository(store, viewModelScope).also { it.coldLaunch() }
}

/** The app; [safeArea] stands in for the window's insets where it has none, as in the desktop window. */
@Composable
fun App(safeArea: SafeAreaInsets? = null) {
    PlataTheme {
        ProvideSafeAreaOrWindow(safeArea) {
            val store = rememberKeyValueStore()
            val repository = viewModel { AppRepositoryHolder(store) }.repository
            if (isDebugBuild()) {
                // reaching the object registers its panel, so the dials button is there from launch
                remember { CardMotion }
                DialRoot { PlataApp(repository) }
            } else {
                PlataApp(repository)
            }
        }
    }
}

@Composable
private fun ProvideSafeAreaOrWindow(insets: SafeAreaInsets?, content: @Composable () -> Unit) =
    if (insets != null) ProvideSafeArea(insets, content) else ProvideWindowSafeArea(content)
