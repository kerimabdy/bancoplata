package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_card_plata_thumb
import plata.shared.generated.resources.raster_tile_santander
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.icons.InfoFill
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** Card fill: [Dark] #272C34, [Orange] the Plata card #FF5A0F, [Bank] bank/red (pass `fill` for another partner colour). */
enum class MethodCardTone { Dark, Orange, Bank }

/**
 * Transfer-method card, radius 20, padding 20: a header (40 [leading], white title over an 80% white subtitle,
 * optional [trailing]) and, when [field] is given (expanded), a white TextField 20 below it. Expanded is 156 high,
 * collapsed 80.
 */
@Composable
fun MethodCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tone: MethodCardTone = MethodCardTone.Dark,
    fill: Color? = null,
    onClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    field: (@Composable () -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val background = fill ?: when (tone) {
        MethodCardTone.Dark -> colors.surfaceMethodDark
        MethodCardTone.Orange -> colors.surfaceMethodOrange
        MethodCardTone.Bank -> colors.bankRed
    }
    Column(
        modifier
            .tilePressable(onClick)
            .background(background, ContinuousRoundedShape(PlataTheme.radii.xl))
            .padding(spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.s20),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = PlataTheme.sizes.buttonSmall),
            horizontalArrangement = Arrangement.spacedBy(spacing.rowGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading()
            Column(Modifier.weight(1f)) {
                PlataText(title, PlataTheme.typography.body, colors.textOnDark)
                if (subtitle != null) PlataText(subtitle, PlataTheme.typography.footnote, colors.textOnDark.copy(alpha = 0.8f))
            }
            trailing?.invoke()
        }
        field?.invoke()
    }
}

/** The bank detected from the typed digits; it picks the card's fill, logo and subtitle. */
enum class MethodCardBank(val displayName: String) { Santander("Santander"), Plata("Plata") }

/**
 * A [MethodCard] recoloured by detection (43e51dd1 → 62147891, 5e785f05): no [bank] is the dark card with the bank
 * glyph, Santander the bank-red card with its tile, Plata the orange card with the card thumb.
 */
@Composable
fun MethodCard(
    title: String,
    bank: MethodCardBank?,
    modifier: Modifier = Modifier,
    subtitle: String? = bank?.displayName,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    field: (@Composable () -> Unit)? = null,
) = MethodCard(
    title,
    modifier,
    subtitle = subtitle,
    tone = when (bank) {
        null -> MethodCardTone.Dark
        MethodCardBank.Santander -> MethodCardTone.Bank
        MethodCardBank.Plata -> MethodCardTone.Orange
    },
    onClick = onClick,
    leading = {
        when (bank) {
            null -> IconTile(PlataIcons.Bank, fill = PlataTheme.gradients.glassFade)
            MethodCardBank.Santander -> IconTile(painterResource(Res.drawable.raster_tile_santander))
            MethodCardBank.Plata -> Image(
                painterResource(Res.drawable.raster_card_plata_thumb),
                contentDescription = "Plata card",
                modifier = Modifier.size(43.dp, 30.dp),
                contentScale = ContentScale.Crop,
            )
        }
    },
    trailing = trailing,
    field = field,
)

/** The white info disc on a collapsed card (a6fae664). */
@Composable
fun MethodCardInfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Icon(
        PlataIcons.InfoFill,
        modifier.pressable(onClickLabel = "Details", onClick = onClick),
        size = 19.dp,
        tint = PlataTheme.colors.textOnDark,
    )
}

@Preview
@Composable
internal fun MethodCardDarkExpandedPreview() = PlataTheme {
    MethodCard(
        "By CLABE",
        Modifier.width(353.dp),
        leading = { IconTile(PlataIcons.Bank, fill = PlataTheme.gradients.glassFade) },
        field = { TextField("", {}, Modifier.fillMaxWidth(), placeholder = "18 digits", label = "CLABE", alwaysShowLabel = true, showCaret = true) },
    )
}

@Preview
@Composable
internal fun MethodCardOrangeExpandedPreview() = PlataTheme {
    MethodCard(
        "By card number",
        Modifier.width(353.dp),
        subtitle = "Plata",
        tone = MethodCardTone.Orange,
        leading = {
            Image(
                painterResource(Res.drawable.raster_card_plata_thumb),
                contentDescription = "Plata card",
                modifier = Modifier.size(43.dp, 30.dp),
                contentScale = ContentScale.Crop,
            )
        },
        field = {
            TextField(
                "4152 3138 1234 5678",
                {},
                Modifier.fillMaxWidth(),
                placeholder = "16 digits",
                label = "Card number",
                trailing = { TextFieldClearButton({}) },
                showCaret = true,
            )
        },
    )
}

@Preview
@Composable
internal fun MethodCardBankExpandedPreview() = PlataTheme {
    MethodCard(
        "By CLABE",
        Modifier.width(353.dp),
        subtitle = "Santander",
        tone = MethodCardTone.Bank,
        leading = { IconTile(painterResource(Res.drawable.raster_tile_santander)) },
        field = { TextField("014180655012345678", {}, Modifier.fillMaxWidth(), placeholder = "18 digits", label = "CLABE") },
    )
}

@Preview
@Composable
internal fun MethodCardDetectedPreview() = PlataTheme {
    Column(verticalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12)) {
        MethodCard("By CLABE", null, Modifier.width(353.dp))
        MethodCard("By CLABE", MethodCardBank.Santander, Modifier.width(353.dp))
        MethodCard("By card number", MethodCardBank.Plata, Modifier.width(353.dp))
    }
}

@Preview
@Composable
internal fun MethodCardBankCollapsedPreview() = PlataTheme {
    MethodCard(
        "Ana López",
        Modifier.width(353.dp),
        subtitle = "CLABE •• 5678",
        tone = MethodCardTone.Bank,
        leading = { IconTile(painterResource(Res.drawable.raster_tile_santander)) },
        trailing = { MethodCardInfoButton({}) },
    )
}
