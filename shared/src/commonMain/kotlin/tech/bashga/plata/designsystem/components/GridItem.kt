package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PhoneDuotone
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** 72-wide grid cell: 56 [media] (IconCircle, BrandLogo, CardThumb) over a centred 13pt label, 6 apart (a5a647ee). */
@Composable
fun GridItem(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    media: @Composable () -> Unit,
) {
    Column(
        modifier
            .width(72.dp)
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        media()
        PlataText(
            label,
            PlataTheme.typography.footnote,
            PlataTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
internal fun GridItemPreview() = PlataTheme {
    GridItem("Mobile") { IconCircle(PlataIcons.PhoneDuotone, IconCircleSize.S56) }
}
