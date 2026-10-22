package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.data.Address
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.DeliveryDay
import tech.bashga.plata.data.DeliverySlot
import tech.bashga.plata.data.Stage

/** The source redacts the customer's name and card digits; these stand in. */
internal const val CustomerName = "Sofía"
internal const val CardLastDigits = "4821"

/** One place the fake geocoder knows: [line2] is the borough and post code DeliveryDetails prints under the street. */
internal data class Place(val address: Address, val city: String, val line2: String)

private val places = listOf(
    Place(Address("Av. Río Mixcoac 25", "Actipan"), "Ciudad de México", "Benito Juárez, 03230 CDMX"),
    Place(Address("Av. Río Churubusco 1050", "Iztacalco"), "Ciudad de México", "Iztacalco, 08400 CDMX"),
    Place(Address("Av. Río San Joaquín 498", "Granada"), "Ciudad de México", "Miguel Hidalgo, 11520 CDMX"),
    Place(Address("Av. Río Consulado 800", "Peralvillo"), "Ciudad de México", "Cuauhtémoc, 06220 CDMX"),
    Place(Address("Av. Río Hondo 12", "Tecamachalco"), "Naucalpan, Edo. Méx.", "Naucalpan, 53950 Edo. Méx."),
    Place(Address("Calle Durango 263", "Roma Norte"), "Ciudad de México", "Cuauhtémoc, 06700 CDMX"),
    Place(Address("Durango 263", "Colonia Roma"), "Ciudad de México", "Cuauhtémoc, 06700 CDMX"),
    Place(Address("Durango 263", "Lomas Altas"), "Ciudad de México", "Miguel Hidalgo, 11950 CDMX"),
    Place(Address("Durango 263", "Del Valle"), "Ciudad de México", "Benito Juárez, 03100 CDMX"),
    Place(Address("Durango 263", "San Mateo"), "Naucalpan, Edo. Méx.", "Naucalpan, 53200 Edo. Méx."),
)

private fun String.searchTokens(): List<String> = lowercase()
    .map { c ->
        when (c) {
            'á' -> 'a'
            'é' -> 'e'
            'í' -> 'i'
            'ó' -> 'o'
            'ú', 'ü' -> 'u'
            'ñ' -> 'n'
            else -> if (c.isLetterOrDigit()) c else ' '
        }
    }
    .joinToString("")
    .split(' ')
    .filter { it.isNotEmpty() }

/** The fake geocoder: places whose words start with every word of [query], at most five. */
internal fun geocode(query: String): List<Place> {
    val words = query.searchTokens()
    if (words.isEmpty()) return emptyList()
    return places.filter { place ->
        val tokens = "${place.address.street} ${place.address.area}".searchTokens()
        words.all { word -> tokens.any { it.startsWith(word) } }
    }.take(5)
}

internal val Address.line: String get() = "$street, $area"

internal val Address.place: Place? get() = places.firstOrNull { it.address == this }

/** [date] is what Set up delivery saves as the stage date; [weekday] spells it out on DeliveryDetails. */
internal data class DayOption(val day: DeliveryDay, val title: String, val subtitle: String, val date: String, val weekday: String)

internal val deliveryDays = listOf(
    DayOption(DeliveryDay.Today, "Today", "Fri, 23 May", "23 May", "Friday"),
    // the tile shows 24f9ddbc's 27 May, but flows 4 and 7 (ec4a6b30) save 3 Jun; the spec wins
    DayOption(DeliveryDay.Closest, "Closest", "Tue, 27 May", "3 Jun", "Tuesday"),
    // the day picker has no reference, so Other stands for a fixed later day
    DayOption(DeliveryDay.Other, "Other", "Pick a day", "5 Jun", "Thursday"),
)

internal val DeliveryDay.date: String get() = deliveryDays.first { it.day == this }.date

internal val DeliverySlot.label: String
    get() = when (this) {
        DeliverySlot.Morning -> "9 a.m. – 12 p.m."
        DeliverySlot.Midday -> "12 p.m. – 3 p.m."
        DeliverySlot.Afternoon -> "3 p.m. – 6 p.m."
    }

/** The slots the backend offers per day; today is fully booked, as on eb4cdf05. */
internal val AppSnapshot.deliverySlots: Map<DeliveryDay, List<DeliverySlot>>
    get() = mapOf(
        DeliveryDay.Today to emptyList(),
        DeliveryDay.Closest to DeliverySlot.entries,
        DeliveryDay.Other to DeliverySlot.entries,
    )

/** DeliveryDetails' "Date and time" line, e.g. "Tuesday, Jun 3, 9 a.m. - 12 p.m.". */
internal val AppSnapshot.deliveryWhen: String?
    get() {
        val date = (stage as? Stage.CardComing)?.date ?: return null
        val day = deliveryDays.firstOrNull { it.date == date }
            ?.let { option -> date.split(' ').let { (d, month) -> "${option.weekday}, $month $d" } }
            ?: date
        val slot = delivery.slot ?: return day
        return "$day, ${slot.label.replace(" – ", " - ")}"
    }

/**
 * The software keyboard's height while one of the screen's fields has focus. On a device it is the IME inset; host tests
 * have no keyboard, so a state test provides the iOS keyboard height it fakes in [LocalFakeKeyboard].
 */
val LocalFakeKeyboard = staticCompositionLocalOf { 0.dp }

@Composable
internal fun keyboardHeight(fieldFocused: Boolean): Dp {
    val ime = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val fake = if (fieldFocused) LocalFakeKeyboard.current else 0.dp
    return maxOf(ime, fake)
}
