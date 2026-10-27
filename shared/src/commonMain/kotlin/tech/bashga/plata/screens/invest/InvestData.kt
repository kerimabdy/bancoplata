package tech.bashga.plata.screens.invest

import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.DrawableResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_idea_ssga
import plata.shared.generated.resources.raster_idea_vanguard
import plata.shared.generated.resources.raster_logo_amazon
import plata.shared.generated.resources.raster_logo_apple
import plata.shared.generated.resources.raster_logo_broadcom
import plata.shared.generated.resources.raster_logo_fidelity
import plata.shared.generated.resources.raster_logo_google
import plata.shared.generated.resources.raster_logo_grayscale
import plata.shared.generated.resources.raster_logo_ishares
import plata.shared.generated.resources.raster_logo_jpmorgan
import plata.shared.generated.resources.raster_logo_krispy_kreme
import plata.shared.generated.resources.raster_logo_meta
import plata.shared.generated.resources.raster_logo_microsoft
import plata.shared.generated.resources.raster_logo_moderna
import plata.shared.generated.resources.raster_logo_nvidia
import plata.shared.generated.resources.raster_logo_progyny
import plata.shared.generated.resources.raster_logo_quantumscape
import plata.shared.generated.resources.raster_logo_recursion
import plata.shared.generated.resources.raster_logo_sarepta
import plata.shared.generated.resources.raster_logo_ssga
import plata.shared.generated.resources.raster_logo_tesla
import plata.shared.generated.resources.raster_logo_vanguard
import plata.shared.generated.resources.raster_logo_wolf
import tech.bashga.plata.data.AppSnapshot
import tech.bashga.plata.data.Currency
import tech.bashga.plata.data.EntryKind
import tech.bashga.plata.data.LedgerAccount
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.data.portfolioCost
import tech.bashga.plata.designsystem.components.TrendDirection

/** What a ticker looks like: its name, logo and whether the logo is a mark on the light disc. */
@Immutable
data class Asset(val ticker: String, val name: String, val logo: DrawableResource, val light: Boolean = false)

/** A quote: [price] in USD cents, [change] in hundredths of a percent, signed (−395 is ↓ 3.95%). */
@Immutable
data class Quote(val price: Long, val change: Int) {
    val direction: TrendDirection get() = if (change < 0) TrendDirection.Down else TrendDirection.Up
    val changeLabel: String get() = formatPercent(change)
    val priceLabel: String get() = usd(price)
}

/** An asset with the quote a list shows for it. */
@Immutable
data class Listing(val asset: Asset, val quote: Quote) {
    val ticker: String get() = asset.ticker
}

fun usd(cents: Long): String = formatMoney(cents, Currency.Usd, showCents = true)

/** "3.95%" from 395 hundredths; the sign goes to the trend arrow. */
fun formatPercent(hundredths: Int): String {
    val value = abs(hundredths)
    return "${value / 100}.${(value % 100).toString().padStart(2, '0')}%"
}

private fun asset(ticker: String, name: String, logo: DrawableResource, light: Boolean = false) = Asset(ticker, name, logo, light)

object InvestCatalog {
    val MSFT = asset("MSFT", "Microsoft", Res.drawable.raster_logo_microsoft, light = true)
    val NVDA = asset("NVDA", "Nvidia", Res.drawable.raster_logo_nvidia)
    val AAPL = asset("AAPL", "Apple", Res.drawable.raster_logo_apple)
    val AMZN = asset("AMZN", "Amazon.com", Res.drawable.raster_logo_amazon)
    val GOOGL = asset("GOOGL", "Alphabet Class A", Res.drawable.raster_logo_google)
    val GOOG = asset("GOOG", "Alphabet Inc. Class C", Res.drawable.raster_logo_google)
    val META = asset("META", "Meta Platforms", Res.drawable.raster_logo_meta)
    val AVGO = asset("AVGO", "Broadcom", Res.drawable.raster_logo_broadcom)
    val TSLA = asset("TSLA", "Tesla", Res.drawable.raster_logo_tesla)
    val DNUT = asset("DNUT", "Krispy Kreme", Res.drawable.raster_logo_krispy_kreme)
    val VGK = asset("VGK", "Vanguard FTSE Europe ETF", Res.drawable.raster_logo_vanguard)
    val BWX = asset("BWX", "SPDR Bloomberg International Treasury Bond ETF", Res.drawable.raster_logo_ssga)
    val WOLF = asset("WOLF", "Wolfspeed", Res.drawable.raster_logo_wolf)
    val QS = asset("QS", "QuantumScape", Res.drawable.raster_logo_quantumscape)
    val PGNY = asset("PGNY", "Progyny", Res.drawable.raster_logo_progyny)
    val RXRX = asset("RXRX", "Recursion Pharmaceuticals", Res.drawable.raster_logo_recursion)
    val MRNA = asset("MRNA", "Moderna", Res.drawable.raster_logo_moderna)
    val SRPT = asset("SRPT", "Sarepta Therapeutics", Res.drawable.raster_logo_sarepta)
    val IBIT = asset("IBIT", "iShares Bitcoin Trust ETF", Res.drawable.raster_logo_ishares)
    val FBTC = asset("FBTC", "Fidelity Wise Origin Bitcoin Fund", Res.drawable.raster_logo_fidelity)
    val GBTC = asset("GBTC", "Grayscale Bitcoin Trust ETF", Res.drawable.raster_logo_grayscale)
    val GLD = asset("GLD", "SPDR Gold Trust, SPDR Gold Shares", Res.drawable.raster_logo_ssga)
    val IAU = asset("IAU", "iShares Gold Trust", Res.drawable.raster_logo_ishares)
    val SLV = asset("SLV", "iShares Silver Trust", Res.drawable.raster_logo_ishares)
    val BIL = asset("BIL", "SPDR Bloomberg 1-3 Month T-Bill ETF", Res.drawable.raster_logo_ssga)
    val SGOV = asset("SGOV", "iShares 0-3 Month Treasury Bond ETF", Res.drawable.raster_logo_ishares)
    val JPST = asset("JPST", "JPMorgan Ultra-Short Income ETF", Res.drawable.raster_logo_jpmorgan)
    val BND = asset("BND", "Vanguard Total Bond Market ETF", Res.drawable.raster_logo_vanguard)
    val LQD = asset("LQD", "iShares iBoxx Investment Grade Corporate Bond ETF", Res.drawable.raster_logo_ishares)
    val AGG = asset("AGG", "iShares Core US Aggregate Bond ETF", Res.drawable.raster_logo_ishares)
    val SHY = asset("SHY", "iShares 1-3 Year Treasury Bond ETF", Res.drawable.raster_logo_ishares)
    val IEF = asset("IEF", "iShares 7-10 Year Treasury Bond ETF", Res.drawable.raster_logo_ishares)
    val TLT = asset("TLT", "iShares 20+ Year Treasury Bond ETF", Res.drawable.raster_logo_ishares)

    // the references come from two sessions (§2.1 month): 11cfd731 and 083f8017 are June's quotes, 3d1d2b9b July's
    private val popularJune = listOf(
        Listing(MSFT, Quote(490_01, 82)),
        Listing(NVDA, Quote(147_71, 245)),
        Listing(AAPL, Quote(200_71, -39)),
        Listing(AMZN, Quote(212_92, 213)),
        Listing(GOOGL, Quote(167_01, 110)),
        Listing(META, Quote(711_88, 191)),
        Listing(AVGO, Quote(263_54, 385)),
        Listing(TSLA, Quote(338_76, -285)),
    )
    private val popularJuly = popularJune.map { listing ->
        when (listing.ticker) {
            "MSFT" -> listing.copy(quote = listing.quote.copy(change = 172))
            "NVDA" -> listing.copy(quote = listing.quote.copy(change = 152))
            "AAPL" -> listing.copy(quote = listing.quote.copy(change = 71))
            else -> listing
        }
    }

    fun popular(month: Month): List<Listing> = if (month == Month.June) popularJune else popularJuly

    val gainers = listOf(
        Listing(WOLF, Quote(1_36, 1580)),
        Listing(QS, Quote(4_38, 1488)),
        Listing(PGNY, Quote(22_61, 1217)),
        Listing(RXRX, Quote(5_09, 1065)),
        Listing(MRNA, Quote(27_37, 1059)),
        Listing(SRPT, Quote(19_31, 1023)),
    )

    val crypto = listOf(
        Listing(IBIT, Quote(61_93, 19)),
        Listing(FBTC, Quote(94_43, 18)),
        Listing(GBTC, Quote(82_74, 17)),
    )

    val commodities = listOf(
        Listing(GLD, Quote(306_45, -91)),
        Listing(IAU, Quote(62_75, -89)),
        Listing(SLV, Quote(33_49, 75)),
    )

    val shortTermBonds = listOf(
        Listing(BIL, Quote(91_57, 1)),
        Listing(SGOV, Quote(100_52, 1)),
        Listing(JPST, Quote(50_61, 3)),
    )

    /** The All stocks card (fef10b1d), quoted later in the June session than Popular stocks. */
    val allStocks = listOf(
        Listing(AAPL, Quote(200_79, -35)),
        Listing(GOOG, Quote(167_99, 119)),
        Listing(META, Quote(711_85, 191)),
        Listing(MSFT, Quote(490_37, 90)),
        Listing(NVDA, Quote(147_79, 251)),
        Listing(AMZN, Quote(212_92, 213)),
        Listing(AVGO, Quote(263_54, 385)),
        Listing(DNUT, Quote(3_30, -395)),
        Listing(GOOGL, Quote(167_01, 110)),
        Listing(TSLA, Quote(338_76, -285)),
    )

    val fixedIncomeEtfs = listOf(
        Listing(BND, Quote(72_61, 38)),
        Listing(LQD, Quote(108_02, 55)),
        Listing(AGG, Quote(97_95, 37)),
        Listing(SHY, Quote(82_45, 8)),
        Listing(IEF, Quote(94_17, 44)),
        Listing(TLT, Quote(86_23, 120)),
    )

    fun list(kind: StockListKind, month: Month): List<Listing> = when (kind) {
        StockListKind.Popular -> popular(month)
        StockListKind.Crypto -> crypto
        StockListKind.Commodities -> commodities
        StockListKind.ShortTermBonds -> shortTermBonds
        StockListKind.All -> allStocks
    }

    /** Losers have no reference: the quotes the app already shows that went down, biggest drop first. */
    fun losers(month: Month): List<Listing> =
        (popular(month) + allStocks + commodities).filter { it.quote.change < 0 }.distinctBy { it.ticker }.sortedBy { it.quote.change }.take(6)

    private val assets: Map<String, Asset> =
        (allStocks + popularJune + gainers + crypto + commodities + shortTermBonds + fixedIncomeEtfs)
            .map { it.asset }
            .plus(listOf(VGK, BWX))
            .associateBy { it.ticker }

    fun asset(ticker: String): Asset = assets[ticker] ?: Asset(ticker, ticker, Res.drawable.raster_logo_krispy_kreme)

    /** Search runs over every stock the app lists, Popular's quotes first. */
    fun search(query: String, month: Month): List<Listing> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return (popular(month) + allStocks).distinctBy { it.ticker }
            .filter { it.asset.name.contains(q, ignoreCase = true) || it.ticker.contains(q, ignoreCase = true) }
    }

    /** The quote StockDetail and Portfolio show: DNUT has its own session quotes (341fcc04 in June, b658c9da in July). */
    fun quote(ticker: String, month: Month): Quote = when {
        ticker == "DNUT" && month == Month.July -> Quote(3_26, 110)
        ticker == "DNUT" -> Quote(3_27, -509)
        ticker == "VGK" -> Quote(78_21, 147)
        else -> (popular(month) + allStocks + gainers + crypto + commodities + shortTermBonds + fixedIncomeEtfs)
            .firstOrNull { it.ticker == ticker }?.quote ?: Quote(1_00, 0)
    }
}

enum class StockListKind(val title: String) {
    Popular("Popular stocks"),
    Crypto("Crypto ETFs"),
    Commodities("Commodities ETFs"),
    ShortTermBonds("Short term bonds ETFs"),
    All("All stocks"),
}

fun stockListKind(name: String): StockListKind = StockListKind.entries.firstOrNull { it.name == name } ?: StockListKind.All

/**
 * An investment idea (InvestmentIdea): the fund, its photo, the article and the report. The card and the page it grows
 * into show the same photo and the same [cardTrend], so nothing swaps mid-flight.
 */
@Immutable
data class InvestmentIdea(
    val id: String,
    val fund: Asset,
    val title: String,
    val headerTitle: String,
    val cardTrend: Int,
    val cardImage: DrawableResource,
    val summary: String,
    val releaseDate: String,
    val bullets: List<String>,
    val report: String,
)

/** The key an idea's feed card and its page share, so the page grows out of the card. */
fun ideaCardKey(ideaId: String): Any = "idea-card:$ideaId"

object InvestIdeas {
    const val VANGUARD = "vanguard-ftse-europe"
    const val SPDR = "spdr-international-treasury"

    val Vanguard = InvestmentIdea(
        id = VANGUARD,
        fund = InvestCatalog.VGK,
        title = "Vanguard FTSE Europe ETF",
        headerTitle = "Vanguard FTSE\nEurope ETF",
        cardTrend = 38,
        cardImage = Res.drawable.raster_idea_vanguard,
        summary = "VGK is a passively managed ETF that aims to track the performance of the FTSE Developed Europe All Cap " +
            "Index, which measures the investment returns of stocks issued by companies in major European markets, including the " +
            "United Kingdom, France, Germany, and 13 other countries. VGK holds over 1,200 large, mid, and small cap stocks across " +
            "various sectors, providing broad, low-cost diversification across 16 nations through a single ETF.",
        releaseDate = "29 May 2025",
        bullets = listOf(
            "Attractive valuation gap: European equities trade at a steep discount to U.S. stocks, which makes VGK a relatively low-cost trade.",
            "Favorable European policy cycle: The European Central Bank has already begun cutting rates, Germany is easing its " +
                "“debt brake” to fund infrastructure from 2026, and euro-area inflation is projected to moderate to 2.1% by the end of " +
                "2025. Leading houses such as JPMorgan and Citi expect European equities to outperform the S&P 500 as these policies " +
                "take hold. VGK is positioned to capture the resulting policy driven of European markets.",
            "Diversification with an embedded currency tailwind: VGK spreads exposure across major European sectors at lower " +
                "valuations, providing true diversification for U.S.-centric portfolios. Meanwhile, the U.S. dollar’s safe haven premium " +
                "is eroding as rate differentials narrow and fiscal concerns mount, highlighted by Moody’s 16 May credit rating " +
                "downgrade. Because VGK’s cash flows are earned in euros, sterling, and other continental currencies, any sustained " +
                "dollar weakness can translate directly into higher U.S. dollar returns, offering a built-in currency kicker while " +
                "mitigating concentration risk.",
        ),
        report = "Buy VGK. Research report\n2025-05-29.pdf",
    )

    // no reference beyond its card (09c22ffa); the page reuses the idea layout
    val Spdr = InvestmentIdea(
        id = SPDR,
        fund = InvestCatalog.BWX,
        title = "SPDR Bloomberg International\nTreasury Bond ETF",
        headerTitle = "SPDR Bloomberg\nInternational\nTreasury Bond ETF",
        cardTrend = 84,
        cardImage = Res.drawable.raster_idea_ssga,
        summary = "BWX tracks the Bloomberg Global Treasury ex-US Capped Index: investment-grade government bonds issued by " +
            "developed countries outside the United States, in their local currencies.",
        releaseDate = "29 May 2025",
        bullets = emptyList(),
        report = "Buy BWX. Research report\n2025-05-29.pdf",
    )

    val all = listOf(Vanguard, Spdr)

    fun idea(id: String): InvestmentIdea = all.firstOrNull { it.id == id } ?: Vanguard
}

/** USD cents a month may still be moved into Inversiones (537a4b5a: USD 1,269.00 left after USD 2.00). */
const val INVESTMENT_LIMIT_USD = 1_271_00L

/** AI portfolio analysis unlocks at USD 1,000 (f1b38f8d). */
const val ANALYSIS_THRESHOLD_USD = 1_000_00L

/** The Moderate profile's equity ceiling (RiskProfile, PortfolioAllocation's recommendation). */
const val MODERATE_EQUITY_SHARE = 0.40

val AppSnapshot.investmentLimitLeft: Long get() = (INVESTMENT_LIMIT_USD - portfolioCost).coerceAtLeast(0)

/** Portfolio › Transactions: this month's Inversiones movements (deposits and buys). */
val AppSnapshot.investTransactionCount: Int
    get() = ledger.count { it.month == month && (it.account == LedgerAccount.Inversiones || it.kind == EntryKind.BetweenAccounts) }

fun AppSnapshot.sharesOf(ticker: String): Double {
    val holding = invest.holdings.firstOrNull { it.ticker == ticker } ?: return 0.0
    return holding.marketValue.toDouble() / InvestCatalog.quote(ticker, month).price
}

/** "0.31" from 0.3098. */
fun formatShares(shares: Double): String {
    val hundredths = (shares * 100).roundToLong()
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')}"
}
