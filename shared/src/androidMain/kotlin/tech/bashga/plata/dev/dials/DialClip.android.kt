package tech.bashga.plata.dev.dials

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

internal actual fun dialClipEntry(text: String): ClipEntry = ClipEntry(ClipData.newPlainText("DialKit", text))
