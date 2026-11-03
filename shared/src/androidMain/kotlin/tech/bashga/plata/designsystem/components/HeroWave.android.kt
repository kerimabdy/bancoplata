package tech.bashga.plata.designsystem.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush

@Composable
internal actual fun rememberShaderBrush(source: String): (uniforms: ((name: String, values: FloatArray) -> Unit) -> Unit) -> Brush? {
    // RuntimeShader arrived in Android 13; older phones get the still fallbacks
    val shader = remember(source) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) AgslShader(source) else null }
    return { uniforms -> shader?.brush(uniforms) }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class AgslShader(source: String) {
    private val shader = RuntimeShader(source)

    fun brush(uniforms: ((name: String, values: FloatArray) -> Unit) -> Unit): Brush {
        uniforms(shader::setFloatUniform)
        return ShaderBrush(shader)
    }
}
