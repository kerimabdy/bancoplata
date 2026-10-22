package tech.bashga.plata.screens.payments

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_card_plata_thumb
import plata.shared.generated.resources.raster_favorite_plata_card
import plata.shared.generated.resources.raster_logo_santander
import plata.shared.generated.resources.raster_logo_telcel
import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.FavoriteKind
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.IconCircle
import tech.bashga.plata.designsystem.components.IconCircleSize
import tech.bashga.plata.designsystem.components.MethodCard
import tech.bashga.plata.designsystem.components.MethodCardBank
import tech.bashga.plata.designsystem.components.MethodCardTone
import tech.bashga.plata.designsystem.components.diameter
import tech.bashga.plata.designsystem.icons.PlataIcons

/** The iOS keyboard the CTA rides on (not drawn). State tests fake the IME inset at these heights. */
enum class TransferKeyboard(val height: Dp) {
    // measured from the keyboard tops on 43e51dd1 / c5b99e20 (number pad) and 62147891 (letters with suggestions)
    NumberPad(292.dp),
    Letters(336.33.dp),
}

enum class TransferMethod { Clabe, Card, Phone }

val TransferMethod.digits: Int get() = if (this == TransferMethod.Card) 16 else 18

// iOS scrolls a focused field's form to its end, which leaves its last row this far above the CTA (c5b99e20, a6fae664)
val FocusedFormEndGap = 27.dp

/** The flat commission a transfer to another bank is charged on the credit line; the note says "up to" it. */
const val TRANSFER_COMMISSION = 230_84L

val NumberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

private const val SantanderClabe = "014"
private const val PlataCardBin = "415231"

/** The bank behind a CLABE (its first 3 digits) or a card number (its BIN). */
fun detectBank(digits: String, method: TransferMethod): MethodCardBank? = when {
    method == TransferMethod.Clabe && digits.startsWith(SantanderClabe) -> MethodCardBank.Santander
    method == TransferMethod.Card && digits.startsWith(PlataCardBin) -> MethodCardBank.Plata
    else -> null
}

/** The fake card directory: a Plata card number fills in its holder. */
const val PLATA_CARD_HOLDER = "Ana López"

/** "4152 3138 1234 5678" for cards; a CLABE stays one run of digits. */
fun formatAccountNumber(digits: String, method: TransferMethod): String =
    if (method == TransferMethod.Card) digits.chunked(4).joinToString(" ") else digits

fun lastFour(digits: String) = digits.takeLast(4)

/** "CLABE •• 5678", "Card •• 5678". */
fun accountLabel(digits: String, method: TransferMethod): String =
    "${if (method == TransferMethod.Card) "Card" else "CLABE"} •• ${lastFour(digits)}"

/** Typed amount text to cents: digits and one point, at most two decimals. */
fun sanitizeAmount(text: String, maxLength: Int = 9): String {
    val cleaned = StringBuilder()
    var point = false
    var decimals = 0
    text.forEach { c ->
        when {
            c.isDigit() && (!point || decimals < 2) -> {
                cleaned.append(c)
                if (point) decimals++
            }
            (c == '.' || c == ',') && !point -> {
                point = true
                cleaned.append(if (cleaned.isEmpty()) "0." else ".")
            }
        }
    }
    return cleaned.toString().take(maxLength)
}

fun amountCents(amount: String): Long {
    if (amount.isEmpty()) return 0
    val units = amount.substringBefore('.').ifEmpty { "0" }.toLong()
    val fraction = amount.substringAfter('.', "").padEnd(2, '0').take(2).toLong()
    return units * 100 + fraction
}

fun MethodCardBank?.favoriteKind(method: TransferMethod): FavoriteKind = when {
    this == MethodCardBank.Plata -> FavoriteKind.PlataCard
    method == TransferMethod.Card -> FavoriteKind.Card
    else -> FavoriteKind.Clabe
}

/** "55 1234 5678" */
fun formatPhone(phone: String): String =
    if (phone.length == 10) "${phone.take(2)} ${phone.substring(2, 6)} ${phone.drop(6)}" else phone

fun Favorite.subtitle(phone: String): String = when (kind) {
    FavoriteKind.Mobile -> formatPhone(number.ifEmpty { phone })
    FavoriteKind.PlataCard, FavoriteKind.Card -> accountLabel(number, TransferMethod.Card)
    FavoriteKind.Clabe -> accountLabel(number, TransferMethod.Clabe)
}

// the raster keeps the wordmark that overhangs the 64×44 card, so it is lifted to centre the card in the 56 slot (b7b04c52)
private val FavoriteArtWidth = 68.dp
private val FavoriteArtHeight = 55.dp
private val FavoriteArtLift = (-2.5).dp

/** A favorite's 56 art in the Payments grid. */
@Composable
fun FavoriteArt(favorite: Favorite) {
    when (favorite.kind) {
        FavoriteKind.Mobile -> BrandLogo(painterResource(Res.drawable.raster_logo_telcel), BrandLogoSize.S56)
        FavoriteKind.PlataCard -> Box(Modifier.size(IconCircleSize.S56.diameter), contentAlignment = Alignment.Center) {
            Image(
                painterResource(Res.drawable.raster_favorite_plata_card),
                contentDescription = "Plata card",
                modifier = Modifier.requiredSize(FavoriteArtWidth, FavoriteArtHeight).offset(y = FavoriteArtLift),
            )
        }
        FavoriteKind.Clabe, FavoriteKind.Card -> when (detectBank(favorite.number, if (favorite.kind == FavoriteKind.Card) TransferMethod.Card else TransferMethod.Clabe)) {
            MethodCardBank.Santander -> BrandLogo(painterResource(Res.drawable.raster_logo_santander), BrandLogoSize.S56)
            else -> IconCircle(PlataIcons.Bank, IconCircleSize.S56)
        }
    }
}

/** A favorite's 40 art in the Favorites list. */
@Composable
fun FavoriteRowArt(favorite: Favorite) {
    when (favorite.kind) {
        FavoriteKind.Mobile -> BrandLogo(painterResource(Res.drawable.raster_logo_telcel), BrandLogoSize.S40)
        FavoriteKind.PlataCard -> Image(
            painterResource(Res.drawable.raster_favorite_plata_card),
            contentDescription = "Plata card",
            modifier = Modifier.size(IconCircleSize.S40.diameter),
        )
        FavoriteKind.Clabe, FavoriteKind.Card -> IconCircle(PlataIcons.Bank, IconCircleSize.S40)
    }
}

// the thumb raster is 43 wide but iOS lays it in the 40 leading slot, so the title starts 16 after 40 (5e785f05)
private val PlataThumbWidth = 43.dp
private val PlataThumbHeight = 30.dp

/** A [MethodCard] recoloured by the detected [bank]; the Plata thumb overhangs its 40 slot as in the source. */
@Composable
fun RecipientMethodCard(
    title: String,
    bank: MethodCardBank?,
    modifier: Modifier = Modifier,
    subtitle: String? = bank?.displayName,
    trailing: (@Composable () -> Unit)? = null,
    field: (@Composable () -> Unit)? = null,
) {
    if (bank != MethodCardBank.Plata) {
        MethodCard(title, bank, modifier, subtitle = subtitle, trailing = trailing, field = field)
        return
    }
    MethodCard(
        title,
        modifier,
        subtitle = subtitle,
        tone = MethodCardTone.Orange,
        leading = {
            Box(Modifier.size(IconCircleSize.S40.diameter), contentAlignment = Alignment.Center) {
                Image(
                    painterResource(Res.drawable.raster_card_plata_thumb),
                    contentDescription = "Plata card",
                    modifier = Modifier.requiredSize(PlataThumbWidth, PlataThumbHeight),
                    contentScale = ContentScale.Crop,
                )
            }
        },
        trailing = trailing,
        field = field,
    )
}
