package tech.bashga.plata.designsystem.foundation

import android.os.Build

internal actual val isBackdropBlurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
