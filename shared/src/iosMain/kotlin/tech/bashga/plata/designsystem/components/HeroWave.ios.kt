package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

private val effects = mutableMapOf<String, RuntimeEffect>()

@Composable
internal actual fun rememberShaderBrush(source: String): (uniforms: ((name: String, values: FloatArray) -> Unit) -> Unit) -> Brush? {
    val builder = remember(source) { RuntimeShaderBuilder(effects.getOrPut(source) { RuntimeEffect.makeForShader(source) }) }
    return { uniforms ->
        uniforms(builder::uniform)
        ShaderBrush(builder.makeShader().asComposeShader())
    }
}

/** Draws the hero and the glass once at launch, under the app, so Skia compiles them to Metal before Home shows them. */
@Composable
internal fun PrewarmHeroWave() {
    val hero = remember { HeroWaveState(mutableFloatStateOf(0f)) }
    HeroWave(Modifier.size(1.dp), hero)
    CompositionLocalProvider(LocalHeroWave provides hero) {
        GlassSurface(Color.Transparent, Modifier.size(1.dp)) {}
    }
}

private fun RuntimeShaderBuilder.uniform(name: String, values: FloatArray) = when (values.size) {
    1 -> uniform(name, values[0])
    2 -> uniform(name, values[0], values[1])
    3 -> uniform(name, values[0], values[1], values[2])
    else -> uniform(name, values[0], values[1], values[2], values[3])
}
