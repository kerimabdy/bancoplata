package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

private val effects = mutableMapOf<String, RuntimeEffect>()

// the desktop preview window draws the same SkSL through Skia
@Composable
internal actual fun rememberShaderBrush(source: String): (uniforms: ((name: String, values: FloatArray) -> Unit) -> Unit) -> Brush? {
    val builder = remember(source) { RuntimeShaderBuilder(effects.getOrPut(source) { RuntimeEffect.makeForShader(source) }) }
    return { uniforms ->
        uniforms(builder::uniform)
        ShaderBrush(builder.makeShader().asComposeShader())
    }
}

private fun RuntimeShaderBuilder.uniform(name: String, values: FloatArray) = when (values.size) {
    1 -> uniform(name, values[0])
    2 -> uniform(name, values[0], values[1])
    3 -> uniform(name, values[0], values[1], values[2])
    else -> uniform(name, values[0], values[1], values[2], values[3])
}
