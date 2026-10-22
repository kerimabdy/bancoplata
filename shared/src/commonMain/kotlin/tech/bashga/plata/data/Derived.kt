package tech.bashga.plata.data

import kotlin.math.abs

private fun LedgerEntry.settledPesos(month: Month? = null) =
    !pending && currency == Currency.Mxn && (month == null || this.month == month)

/** Limit plus every settled credit-line entry. */
val AppSnapshot.creditAvailable: Long
    get() = accounts.creditoLimit + ledger.filter { it.settledPesos() && it.account == LedgerAccount.CreditLine }.sumOf { it.amount }

/** The month's settled outgoing entries; moving your own money to Inversiones isn't spending. */
fun AppSnapshot.spent(month: Month = this.month): Long =
    -ledger.filter { it.settledPesos(month) && it.amount < 0 && it.kind != EntryKind.BetweenAccounts }.sumOf { it.amount }

fun AppSnapshot.received(month: Month = this.month): Long =
    ledger.filter { it.settledPesos(month) && it.amount > 0 }.sumOf { it.amount }

/** Badged Home tiles: cashback until chosen, MSI until visited (FirstDay 2, Active 0). */
val AppSnapshot.homeBadge: Int
    get() = (if (homeTiles.cashback.isEmpty()) 1 else 0) + (if (homeTiles.msiVisited) 0 else 1)

/** USD cents: cash plus holdings at market value (Portfolio). */
val AppSnapshot.portfolioValue: Long
    get() = accounts.inversionesCashUsd + invest.holdings.sumOf { it.marketValue }

/** USD cents: cash plus holdings at cost (PortfolioAllocation). */
val AppSnapshot.portfolioCost: Long
    get() = accounts.inversionesCashUsd + invest.holdings.sumOf { it.costBasis }

sealed interface InviteOffer {
    /** June: "$200" on the Invite tab. */
    data object Standard : InviteOffer

    /** July: "🔥 1,500" on the Invite tab; $3,000 for 3 friends, 15 days left. */
    data object Limited : InviteOffer
}

val Month.inviteOffer: InviteOffer get() = if (this == Month.June) InviteOffer.Standard else InviteOffer.Limited

val Month.appVersion: String get() = if (this == Month.June) "1.53.0(417)" else "1.54.0(424)"

val Month.tenure: String get() = if (this == Month.June) "Welcome to Plata" else "3 weeks with Plata"

/** "$3,470", "$3,457.97", "−$70", "USD 2.01". [showCents] defaults to only when there are cents. */
fun formatMoney(cents: Long, currency: Currency = Currency.Mxn, showCents: Boolean = cents % 100 != 0L): String {
    val units = abs(cents) / 100
    val grouped = units.toString().reversed().chunked(3).joinToString(",").reversed()
    val fraction = if (showCents) "." + (abs(cents) % 100).toString().padStart(2, '0') else ""
    val sign = if (cents < 0) "−" else ""
    return when (currency) {
        Currency.Mxn -> "$sign$$grouped$fraction"
        Currency.Usd -> "${sign}USD $grouped$fraction"
    }
}
