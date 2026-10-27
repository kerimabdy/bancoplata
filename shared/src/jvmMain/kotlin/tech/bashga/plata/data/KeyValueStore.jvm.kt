package tech.bashga.plata.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.util.Properties

// a properties file rather than java.util.prefs, whose values stop at 8 KB and the snapshot runs past that
private class PropertiesFileStore(private val file: File) : KeyValueStore {
    private val properties = Properties().apply { if (file.exists()) file.inputStream().use(::load) }

    override fun get(key: String): String? = properties.getProperty(key)

    override fun put(key: String, value: String) {
        properties.setProperty(key, value)
        save()
    }

    override fun remove(key: String) {
        properties.remove(key)
        save()
    }

    private fun save() {
        file.parentFile.mkdirs()
        file.outputStream().use { properties.store(it, null) }
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore =
    remember { PropertiesFileStore(File(System.getProperty("user.home"), ".plata/store.properties")) }
