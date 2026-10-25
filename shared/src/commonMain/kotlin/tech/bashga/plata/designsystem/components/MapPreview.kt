package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.theme.PlataTheme

@Composable
fun MapPreview(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    PlataMap(
        latitude,
        longitude,
        modifier.fillMaxWidth().height(100.dp).clip(ContinuousRoundedShape(PlataTheme.radii.lg)),
        interactive = false,
    )
}

@Preview
@Composable
internal fun MapPreviewPreview() = PlataTheme {
    MapPreview(19.3727, -99.1795, Modifier.width(313.dp))
}
