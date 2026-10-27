package tech.bashga.plata.dev.dials

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun dialClipEntry(text: String): ClipEntry = ClipEntry(StringSelection(text))
