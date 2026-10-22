package tech.bashga.plata

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.KeyValueStore
import tech.bashga.plata.data.rememberKeyValueStore
import tech.bashga.plata.designsystem.foundation.ProvideWindowSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataApp

// a ViewModel so the repository lives as long as the process: rotation keeps it, a cold launch builds a new one
private class AppRepositoryHolder(store: KeyValueStore) : ViewModel() {
    val repository = AppRepository(store, viewModelScope).also { it.coldLaunch() }
}

@Composable
fun App() {
    PlataTheme {
        ProvideWindowSafeArea {
            val store = rememberKeyValueStore()
            PlataApp(viewModel { AppRepositoryHolder(store) }.repository)
        }
    }
}
