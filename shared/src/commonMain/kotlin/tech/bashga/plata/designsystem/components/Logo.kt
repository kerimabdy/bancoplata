package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import tech.bashga.plata.designsystem.icons.EscudoLogo
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlataLogo
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class LogoBrand { Plata, Escudo }

/** [height] defaults to the artwork's own (56 for plata, 40 for escudo); the width follows its aspect ratio. */
@Composable
fun Logo(
    brand: LogoBrand,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    tint: Color = PlataTheme.colors.textOnDark,
) {
    val vector = if (brand == LogoBrand.Plata) PlataIcons.PlataLogo else PlataIcons.EscudoLogo
    Image(
        rememberVectorPainter(vector),
        contentDescription = if (brand == LogoBrand.Plata) "Plata" else "Escudo de Plata",
        modifier = modifier
            .height(height ?: vector.defaultHeight)
            .aspectRatio(vector.viewportWidth / vector.viewportHeight),
        colorFilter = if (brand == LogoBrand.Plata) ColorFilter.tint(tint) else null,
    )
}

@Preview
@Composable
internal fun LogoPlataPreview() = PlataTheme { Logo(LogoBrand.Plata) }

@Preview
@Composable
internal fun LogoEscudoPreview() = PlataTheme { Logo(LogoBrand.Escudo) }
