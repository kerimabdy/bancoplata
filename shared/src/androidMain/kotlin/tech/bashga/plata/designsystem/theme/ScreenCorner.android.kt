package tech.bashga.plata.designsystem.theme

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp

@Composable
internal actual fun screenCornerRadius(): Dp? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val radius = LocalView.current.rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: return null
    return with(LocalDensity.current) { radius.toDp() }
}
