package tech.bashga.plata.designsystem.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush

@Composable
internal actual fun HeroWaveLayer(modifier: Modifier, animate: Boolean) {
    val clock = rememberWaveClock(animate)
    // RuntimeShader arrived in Android 13; older phones get the still gradient
    val shader = remember { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) AgslWave() else null }
    Spacer(modifier.drawWave(clock) { width, height, time -> shader?.brush(width, height, time) })
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class AgslWave {
    private val shader = RuntimeShader(WaveSksl)

    fun brush(width: Float, height: Float, time: Float): Brush {
        shader.setFloatUniform("size", width, height)
        HeroWaveDials.uniforms(time, shader::setFloatUniform)
        return ShaderBrush(shader)
    }
}
