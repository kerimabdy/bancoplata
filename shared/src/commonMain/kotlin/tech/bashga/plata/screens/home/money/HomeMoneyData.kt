package tech.bashga.plata.screens.home.money

import org.jetbrains.compose.resources.DrawableResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_abc
import plata.shared.generated.resources.raster_logo_adidas
import plata.shared.generated.resources.raster_logo_aeromexico
import plata.shared.generated.resources.raster_logo_aeropostale
import plata.shared.generated.resources.raster_logo_alfaparf
import plata.shared.generated.resources.raster_logo_amazon
import plata.shared.generated.resources.raster_logo_amazon_prime
import plata.shared.generated.resources.raster_logo_ancona
import plata.shared.generated.resources.raster_logo_credito_inversiones
import plata.shared.generated.resources.raster_logo_krispy_kreme
import plata.shared.generated.resources.raster_logo_liverpool
import plata.shared.generated.resources.raster_logo_mercadolibre
import plata.shared.generated.resources.raster_logo_santander
import plata.shared.generated.resources.raster_logo_walmart
import plata.shared.generated.resources.raster_media_mercadolibre
import plata.shared.generated.resources.raster_tile_liverpool
import plata.shared.generated.resources.raster_tile_mercadolibre
import plata.shared.generated.resources.raster_tile_walmart
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Currency
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerEntry
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.PESOS_PER_DOLLAR_CENTS
import tech.bashga.plata.data.creditAvailable
import tech.bashga.plata.data.formatMoney

// Credit line

/** What the credit line owes: the limit minus what is available (5e61179c: $3,500 − $3,457.97 = $42.03). */
val AppSnapshot.creditTotalBalance: Long get() = accounts.creditoLimit - creditAvailable

/** The statement status the fake clock shows (SCREEN-MAP §2.1: June NothingDue, July Paid). */
sealed interface CreditStatus {
    val nextStatement: String

    data class NothingDue(override val nextStatement: String) : CreditStatus

    /** [minimum] and [full] are the paid statement's amounts in cents. */
    data class Paid(val period: String, override val nextStatement: String, val minimum: Long, val full: Long) : CreditStatus
}

val Month.creditStatus: CreditStatus
    get() = when (this) {
        Month.June -> CreditStatus.NothingDue(nextStatement = "Jul 8")
        Month.July -> CreditStatus.Paid(period = "24 Jun - 7 Jul", nextStatement = "Aug 6", minimum = 200_00, full = 3_722_03)
    }

/** The status line on Home's credit card. */
val CreditStatus.homeLabel: String
    get() = when (this) {
        is CreditStatus.NothingDue -> "Nothing due"
        is CreditStatus.Paid -> "Paid"
    }

// Transactions

/** How a ledger entry is drawn: a merchant raster, or a glyph circle ([logo] null). */
data class TransactionRow(
    val title: String,
    val category: String,
    val amount: String,
    val incoming: Boolean,
    val secondary: String?,
    val secondaryTone: TransactionSecondaryTone,
    val logo: DrawableResource?,
    val kind: EntryKind,
)

enum class TransactionSecondaryTone { Neutral, Primary, Gradient }

private class Merchant(val category: String, val logo: DrawableResource, val cashback: Boolean)

// the merchants the ledger writes; only the Amazon ones pay the 1% cashback 894b1b01 and d58c1486 show
private val merchants = mapOf(
    "Amazon Prime" to Merchant("Subscriptions", Res.drawable.raster_logo_amazon_prime, cashback = true),
    "Reversal from Amazon Prime" to Merchant("Incoming", Res.drawable.raster_logo_amazon_prime, cashback = true),
    "Amazon" to Merchant("General purchases", Res.drawable.raster_logo_amazon, cashback = true),
    "Krispy Kreme" to Merchant("Buy shares", Res.drawable.raster_logo_krispy_kreme, cashback = false),
)

/** The cashback an entry earns (a purchase) or gives back (its refund), in cents: 1% at the cashback merchants. */
val LedgerEntry.cashback: Long
    get() {
        if (currency != Currency.Mxn || merchants[title]?.cashback != true) return 0
        return when (kind) {
            EntryKind.Purchase, EntryKind.Refund -> -amount / 100
            else -> 0
        }
    }

fun AppSnapshot.cashbackEarned(month: Month = this.month): Long =
    ledger.filter { !it.pending && it.month == month }.sumOf { it.cashback }

/** USD cents bought by a "Crédito → Inversiones" entry, which the ledger keeps in pesos. */
private val LedgerEntry.usdBought: Long get() = (-amount * 100 + PESOS_PER_DOLLAR_CENTS / 2) / PESOS_PER_DOLLAR_CENTS

val LedgerEntry.row: TransactionRow
    get() {
        val merchant = merchants[title]
        val category = merchant?.category ?: when (kind) {
            EntryKind.Purchase -> "General purchases"
            EntryKind.Refund -> "Incoming"
            EntryKind.TransferIn, EntryKind.TransferOut, EntryKind.BetweenAccounts -> "Transfers"
            EntryKind.Commission -> "Financial services"
            EntryKind.Reward -> "Rewards"
            EntryKind.BuyShares -> "Buy shares"
        }
        val logo = merchant?.logo ?: when (kind) {
            EntryKind.BetweenAccounts -> Res.drawable.raster_logo_credito_inversiones
            EntryKind.TransferIn -> Res.drawable.raster_logo_abc
            EntryKind.TransferOut -> Res.drawable.raster_logo_santander
            else -> null
        }
        val amountText = when {
            kind == EntryKind.BetweenAccounts -> formatMoney(usdBought, Currency.Usd)
            amount > 0 -> "+" + formatMoney(amount, currency)
            else -> formatMoney(amount, currency)
        }
        val earned = cashback
        val (secondary, tone) = when {
            kind == EntryKind.Commission -> "with VAT" to TransactionSecondaryTone.Neutral
            earned > 0 -> "+" + formatMoney(earned) to TransactionSecondaryTone.Gradient
            earned < 0 -> formatMoney(earned) to TransactionSecondaryTone.Primary
            else -> null to TransactionSecondaryTone.Neutral
        }
        return TransactionRow(
            title = title,
            category = category,
            amount = amountText,
            incoming = amount > 0 && kind != EntryKind.BetweenAccounts,
            secondary = secondary,
            secondaryTone = tone,
            logo = logo,
            kind = kind,
        )
    }

/** "Today" on the fake clock's day, else "4 July". */
fun Month.dayLabel(day: Int): String = if (day == today) "Today" else "$day $label"

// MSI partners

enum class MsiCategory(val label: String) {
    SportingGoods("Sporting goods"),
    Beauty("Beauty"),
    Airline("Airline tickets"),
    Travel("Travel"),
    Clothing("Clothing"),
    Automotive("Automotive services"),
    General("General purchases"),
}

/** [photo] is the detail page header; partners without one use their [tile]. */
data class MsiPartner(
    val id: String,
    val name: String,
    val category: MsiCategory,
    val minimum: Long,
    val logo: DrawableResource,
    val website: String,
    val tile: DrawableResource? = null,
    val photo: DrawableResource? = null,
) {
    val minimumText: String get() = formatMoney(minimum)
}

object MsiPartners {
    val MercadoLibre = MsiPartner(
        "mercadolibre", "Mercado Libre", MsiCategory.General, 5_00, Res.drawable.raster_logo_mercadolibre, "mercadolibre.com.mx",
        tile = Res.drawable.raster_tile_mercadolibre, photo = Res.drawable.raster_media_mercadolibre,
    )
    val Walmart = MsiPartner(
        "walmart", "Walmart", MsiCategory.General, 1_000_00, Res.drawable.raster_logo_walmart, "walmart.com.mx",
        tile = Res.drawable.raster_tile_walmart,
    )
    val Liverpool = MsiPartner(
        "liverpool", "Liverpool", MsiCategory.General, 1_500_00, Res.drawable.raster_logo_liverpool, "liverpool.com.mx",
        tile = Res.drawable.raster_tile_liverpool,
    )

    val Amazon = MsiPartner("amazon", "Amazon", MsiCategory.General, 1_000_00, Res.drawable.raster_logo_amazon, "amazon.com.mx")

    /** The online partners: the hub's tiles (d7d78e0b, those with a tile) and the first hero pages. */
    val featured = listOf(MercadoLibre, Walmart, Liverpool, Amazon)

    /** "Stores with MSI", alphabetical (9356b352). */
    val stores = listOf(
        MsiPartner("adidas", "Adidas", MsiCategory.SportingGoods, 300_00, Res.drawable.raster_logo_adidas, "adidas.mx"),
        MsiPartner("aeromexico", "Aeromexico", MsiCategory.Airline, 3_000_00, Res.drawable.raster_logo_aeromexico, "aeromexico.com"),
        MsiPartner("aeromexico-vacations", "Aeromexico Vacations", MsiCategory.Travel, 1_200_00, Res.drawable.raster_logo_aeromexico, "aeromexicovacations.com"),
        MsiPartner("aeropostale", "Aeropostale", MsiCategory.Clothing, 500_00, Res.drawable.raster_logo_aeropostale, "aeropostale.com.mx"),
        MsiPartner("alfaparf", "Alfaparf", MsiCategory.Beauty, 300_00, Res.drawable.raster_logo_alfaparf, "alfaparf.mx"),
        MsiPartner("ancona", "Ancona Autopartes", MsiCategory.Automotive, 300_00, Res.drawable.raster_logo_ancona, "ancona.com.mx"),
    )

    val all = featured + stores

    /** MsiPartners' hero pages (ten dots on 9e2d2593). */
    val hero = all.take(10)

    fun byId(id: String): MsiPartner = all.firstOrNull { it.id == id } ?: MercadoLibre
}

/** The card terminals that take MSI; [minimum] in cents. */
enum class TerminalProvider(val label: String, val minimum: Long) {
    Clip("Clip", 100_00),
    MercadoPago("Mercado Pago", 100_00),
    Fiserv("Fiserv", 100_00),
}

/** MsiPartners' intro dialog shows on the first visit only. */
fun AppRepository.seeMsiPartnersIntro() = update { it.copy(msiPartnersIntroSeen = true) }
