package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_card_credito
import plata.shared.generated.resources.raster_card_details
import plata.shared.generated.resources.raster_card_details_silver
import plata.shared.generated.resources.raster_card_digital
import plata.shared.generated.resources.raster_card_frozen
import plata.shared.generated.resources.raster_card_physical
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** The legacy face list; new code passes [CardProduct], [CardFace] and `frozen` instead. */
enum class CardArtKind { Physical, Digital, Frozen, Details, DetailsSilver, Credito }

enum class CardProduct { Physical, Digital, Credito }

/** [Details] is the back with the card number, expiry and CVC wells (silver on physical, violet on digital). */
enum class CardFace { Front, Details }

private val CardWidth = 313.dp
private val CardHeight = 199.dp
private val LabelBottom = 18.dp
private val LabelToNumber = 2.dp
private val FieldTextStart = 37.dp
private val CvcTextStart = 180.dp
private val NumberFieldTop = 77.5.dp
private val DateFieldTop = 141.5.dp
private val NumberLine = 20.dp
private val NumberDot = 3.dp
private val NumberDotPitch = 6.7.dp
private val NumberDotInset = 3.dp
private const val BULLET = "•"
private const val FlipMillis = 450

private fun faceArt(product: CardProduct, face: CardFace, frozen: Boolean): DrawableResource = when {
    product == CardProduct.Credito -> Res.drawable.raster_card_credito
    face == CardFace.Details -> if (product == CardProduct.Digital) Res.drawable.raster_card_details else Res.drawable.raster_card_details_silver
    product == CardProduct.Digital -> Res.drawable.raster_card_digital
    frozen -> Res.drawable.raster_card_frozen
    else -> Res.drawable.raster_card_physical
}

val CardArtKind.defaultLabel: String?
    get() = when (this) {
        CardArtKind.Physical, CardArtKind.Frozen -> "Physical card"
        CardArtKind.Digital -> "Digital card"
        else -> null
    }

val CardProduct.defaultLabel: String?
    get() = when (this) {
        CardProduct.Physical -> "Physical card"
        CardProduct.Digital -> "Digital card"
        CardProduct.Credito -> null
    }

@Composable
fun CardArt(
    kind: CardArtKind,
    modifier: Modifier = Modifier,
    label: String? = kind.defaultLabel,
    number: String = "••••",
    cardNumber: String? = null,
    expires: String? = null,
    cvc: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val product = when (kind) {
        CardArtKind.Digital, CardArtKind.Details -> CardProduct.Digital
        CardArtKind.Credito -> CardProduct.Credito
        else -> CardProduct.Physical
    }
    val face = if (kind == CardArtKind.Details || kind == CardArtKind.DetailsSilver) CardFace.Details else CardFace.Front
    CardArt(product, modifier, face, kind == CardArtKind.Frozen, label, number, cardNumber, expires, cvc, onClick)
}

/**
 * The 313×199 card, radius 20. The printed artwork (PLATA wordmark, Mastercard, Escudo lockup, chip, the details
 * field wells, the "crédito" print and the frost) is a raster cropped from the sources with the text painted out;
 * the code sets the variable text on it: [label] (15 regular, embossed ink) and [number] (near-black) at the bottom
 * left of the front, and on the [CardFace.Details] back the [cardNumber], [expires] and [cvc] values or their
 * placeholders. [frozen] frosts the physical front (697dbf17). Changing [face] flips the card around its vertical axis.
 * The source redacts the physical card number; [number] defaults to the "••••" the digital card shows.
 */
@Composable
fun CardArt(
    product: CardProduct,
    modifier: Modifier = Modifier,
    face: CardFace = CardFace.Front,
    frozen: Boolean = false,
    label: String? = product.defaultLabel,
    number: String = "••••",
    cardNumber: String? = null,
    expires: String? = null,
    cvc: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val rotation by animateFloatAsState(if (face == CardFace.Details) 180f else 0f, tween(FlipMillis), label = "flip")
    val showing = if (rotation > 90f) CardFace.Details else CardFace.Front
    val turning = rotation != 0f && rotation != 180f
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    val density = LocalDensity.current
    Box(
        modifier
            .size(CardWidth, CardHeight)
            .then(
                if (turning) {
                    Modifier.graphicsLayer {
                        rotationY = if (showing == CardFace.Details) rotation - 180f else rotation
                        cameraDistance = 12f * density.density
                    }
                } else {
                    Modifier
                },
            )
            .plataShadow(PlataTheme.shadows.thumb, shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier),
    ) {
        Image(painterResource(faceArt(product, showing, frozen)), contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        when {
            product == CardProduct.Credito -> Unit
            showing == CardFace.Details -> DetailsFields(product, cardNumber, expires, cvc)
            label != null -> CardLabel(product, frozen, label, number, Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
private fun CardLabel(product: CardProduct, frozen: Boolean, label: String, number: String, modifier: Modifier) {
    val colors = PlataTheme.colors
    val ink = when {
        product == CardProduct.Digital -> colors.cardLabelTeal
        frozen -> colors.cardEmbossSilver.copy(alpha = 0.75f)
        else -> colors.cardEmbossSilver.copy(alpha = 0.8f)
    }
    val type = PlataTheme.typography.subhead
    Column(
        modifier.padding(start = PlataTheme.spacing.s20, bottom = LabelBottom),
        verticalArrangement = Arrangement.spacedBy(LabelToNumber),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4)) {
            if (product == CardProduct.Digital) Icon(PlataIcons.Cloud, size = PlataTheme.sizes.iconSmall, tint = ink)
            PlataText(label, type, ink, maxLines = 1)
        }
        CardNumber(number, type, colors.textPrimary)
    }
}

/** Bullets are drawn as 3pt dots on the source's 6.7 pitch (202bee50); other characters are set as text. */
@Composable
private fun CardNumber(number: String, style: TextStyle, ink: Color) {
    val runs = Regex("$BULLET|[^$BULLET]+").findAll(number).map { it.value }.toList()
    Row(Modifier.height(NumberLine), verticalAlignment = Alignment.CenterVertically) {
        runs.forEach { run ->
            if (run == BULLET) {
                Box(Modifier.width(NumberDotPitch).padding(start = NumberDotInset)) {
                    Box(Modifier.size(NumberDot).background(ink, CircleShape))
                }
            } else {
                PlataText(run, style, ink, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DetailsFields(product: CardProduct, cardNumber: String?, expires: String?, cvc: String?) {
    val colors = PlataTheme.colors
    val placeholder = if (product == CardProduct.Digital) colors.cardEmbossViolet.copy(alpha = 0.6f) else colors.cardEmbossSilver.copy(alpha = 0.5f)
    FieldText(cardNumber ?: "Card number", if (cardNumber != null) colors.textPrimary else placeholder, FieldTextStart, NumberFieldTop)
    FieldText(expires ?: "Expires", if (expires != null) colors.textPrimary else placeholder, FieldTextStart, DateFieldTop)
    FieldText(cvc ?: "CVC", if (cvc != null) colors.textPrimary else placeholder, CvcTextStart, DateFieldTop)
}

@Composable
private fun FieldText(text: String, color: Color, x: Dp, y: Dp) {
    PlataText(text, PlataTheme.typography.body, color, Modifier.offset(x, y), maxLines = 1)
}

@Preview
@Composable
internal fun CardArtPhysicalPreview() = PlataTheme { CardArt(CardArtKind.Physical) }

@Preview
@Composable
internal fun CardArtDigitalPreview() = PlataTheme { CardArt(CardArtKind.Digital) }

@Preview
@Composable
internal fun CardArtFrozenPreview() = PlataTheme { CardArt(CardArtKind.Frozen) }

@Preview
@Composable
internal fun CardArtDetailsPreview() = PlataTheme { CardArt(CardArtKind.Details) }

@Preview
@Composable
internal fun CardArtDetailsSilverPreview() = PlataTheme { CardArt(CardArtKind.DetailsSilver) }

@Preview
@Composable
internal fun CardArtPhysicalFrozenDetailsPreview() = PlataTheme { CardArt(CardProduct.Physical, face = CardFace.Details, frozen = true) }

@Preview
@Composable
internal fun CardArtCreditoPreview() = PlataTheme { CardArt(CardArtKind.Credito) }
