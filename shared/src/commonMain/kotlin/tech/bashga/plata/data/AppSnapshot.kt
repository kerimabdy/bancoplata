package tech.bashga.plata.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Everything the fake backend knows (SCREEN-MAP §2.1), persisted as one JSON value. Every field has a default, so a
 * snapshot written by an older build still decodes. Money is in cents: MXN unless the field or entry says USD.
 */
@Serializable
data class AppSnapshot(
    val session: Session = Session(),
    val stage: Stage = Stage.NotScheduled,
    val activation: Activation = Activation.None,
    val delivery: Delivery = Delivery(),
    val avatar: Avatar = Avatar.Placeholder,
    val avatarIsNew: Boolean = false,
    val homeTiles: HomeTiles = HomeTiles(),
    val accounts: Accounts = Accounts(),
    val ledger: List<LedgerEntry> = emptyList(),
    val favorites: List<Favorite> = listOf(Favorite.MyMobile),
    val cards: Cards = Cards(),
    val invest: Invest = Invest(),
    val settings: Settings = Settings(),
    val supportUnread: Int = 0,
    val month: Month = Month.June,
    val msiPartnersIntroSeen: Boolean = false,
    /** Set by scheduling, cleared when Home's DeliveryArranged dialog closes, so it shows exactly once. */
    val deliveryArrangedPending: Boolean = false,
)

@Serializable
data class Session(
    val phone: String = "",
    val passcode: String = "",
    val deviceLinked: Boolean = false,
    val loggedIn: Boolean = false,
    val faceId: Boolean = false,
)

@Serializable
sealed interface Stage {
    @Serializable
    @SerialName("NotScheduled")
    data object NotScheduled : Stage

    /** [ambassadorDueAt] is epoch millis, set on the first DeliveryDetails open and kept across launches. */
    @Serializable
    @SerialName("CardComing")
    data class CardComing(
        val date: String,
        val ambassador: Ambassador = Ambassador.Unassigned,
        val ambassadorDueAt: Long? = null,
    ) : Stage

    @Serializable
    @SerialName("Live")
    data object Live : Stage
}

@Serializable
sealed interface Ambassador {
    @Serializable
    @SerialName("Unassigned")
    data object Unassigned : Ambassador

    @Serializable
    @SerialName("Assigned")
    data class Assigned(val name: String) : Ambassador
}

@Serializable
enum class Activation { None, FeedbackPending, InProgress, Done }

@Serializable
data class Delivery(
    val address: Address? = null,
    val details: String = "",
    val day: DeliveryDay? = null,
    val slot: DeliverySlot? = null,
)

@Serializable
data class Address(val street: String, val area: String)

@Serializable
enum class DeliveryDay { Today, Closest, Other }

@Serializable
enum class DeliverySlot { Morning, Midday, Afternoon }

@Serializable
enum class Avatar { Placeholder, Generated }

@Serializable
data class HomeTiles(
    val cashback: Set<CashbackCategory> = emptySet(),
    val msiVisited: Boolean = false,
)

@Serializable
enum class CashbackCategory {
    AllPurchases, Airline, ChildrensStores, Clothing, Entertainment, HomeDiy, Mobile, Restaurants, SportingGoods, Taxi, Justo,
}

@Serializable
data class Accounts(
    val creditoLimit: Long = 3_500_00,
    val ownBalance: Long = 97_57,
    val inversionesCashUsd: Long = 0,
)

@Serializable
data class LedgerEntry(
    val month: Month,
    val day: Int,
    val title: String,
    val kind: EntryKind,
    val account: LedgerAccount,
    val amount: Long,
    val currency: Currency = Currency.Mxn,
    val pending: Boolean = false,
)

@Serializable
enum class EntryKind { Purchase, Refund, TransferIn, TransferOut, Commission, Reward, BetweenAccounts, BuyShares }

@Serializable
enum class LedgerAccount { CreditLine, OwnBalance, Inversiones }

@Serializable
enum class Currency { Mxn, Usd }

@Serializable
data class Favorite(val id: String, val kind: FavoriteKind, val name: String, val number: String = "") {
    companion object {
        val MyMobile = Favorite("my-mobile", FavoriteKind.Mobile, "My mobile")
    }
}

@Serializable
enum class FavoriteKind { Mobile, PlataCard, Clabe, Card }

@Serializable
enum class CardId { Physical, Digital }

@Serializable
data class Cards(val frozen: Set<CardId> = emptySet())

@Serializable
data class Invest(
    val account: InvestAccount = InvestAccount.NotOpened,
    val holdings: List<Holding> = emptyList(),
    val recents: List<String> = emptyList(),
)

@Serializable
enum class InvestAccount { NotOpened, Opened }

/** USD cents. [marketValue] equals [costBasis] until the first Portfolio open after the buy refreshes the quote. */
@Serializable
data class Holding(
    val ticker: String,
    val name: String,
    val costBasis: Long,
    val marketValue: Long,
    val quoteRefreshed: Boolean = false,
)

@Serializable
data class Settings(val appIcon: AppIcon = AppIcon.Main, val appearance: Appearance = Appearance.System)

@Serializable
enum class AppIcon { Main, Festive, Plus, Sun, Black, Vault, Orange }

@Serializable
enum class Appearance { System, Light, Dark }

/** The fake clock: the two capture sessions of the references, 14 June and 23 July. */
@Serializable
enum class Month(val label: String, val today: Int) {
    June("June", 14),
    July("July", 23),
    ;

    val next: String get() = if (this == June) "July" else "August"
}
