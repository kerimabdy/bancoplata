package tech.bashga.plata.data

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

const val SNAPSHOT_KEY = "plata.snapshot"
const val AMBASSADOR_DELAY_MILLIS = 10_000L
const val ASSIGNED_AMBASSADOR = "Carlos"
const val PESOS_PER_DOLLAR_CENTS = 18_76L

enum class AuthStep { ScanCardQr, CreatePasscode, EnableFaceId }

/** What a transfer moved, for TransferResult. Amounts in cents of [currency]; balances are `ownBalance` in MXN cents. */
data class TransferReceipt(
    val recipient: String,
    val amount: Long,
    val currency: Currency,
    val commission: Long,
    val balanceBefore: Long,
    val balanceAfter: Long,
)

private val snapshotJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * The fake backend (SCREEN-MAP §2.1). Screens observe [state] and call the methods below; every change is written
 * to [store] as JSON under [SNAPSHOT_KEY]. [now] is the wall clock for the ambassador timer, so tests can pass virtual time.
 * Pass [fixture] to start from a given snapshot instead of the stored one.
 */
class AppRepository(
    private val store: KeyValueStore,
    private val scope: CoroutineScope,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    fixture: AppSnapshot? = null,
) {
    private val _state = MutableStateFlow(fixture ?: load())
    val state: StateFlow<AppSnapshot> = _state.asStateFlow()
    val snapshot: AppSnapshot get() = _state.value

    private val _launches = MutableStateFlow(0)

    /** Bumped by [reset]; the app keys its root graph on it, so a reset relaunches at Splash. */
    val launches: StateFlow<Int> = _launches.asStateFlow()

    private var ambassadorTimer: Job? = null

    /** The one write primitive. Area code adds its own writes as `fun AppRepository.x() = update { … }` extensions. */
    fun update(transform: (AppSnapshot) -> AppSnapshot) {
        val next = _state.updateAndGet(transform)
        store.put(SNAPSHOT_KEY, snapshotJson.encodeToString(AppSnapshot.serializer(), next))
    }

    private fun load(): AppSnapshot {
        val raw = store.get(SNAPSHOT_KEY) ?: return AppSnapshot()
        return try {
            snapshotJson.decodeFromString(AppSnapshot.serializer(), raw)
        } catch (_: IllegalArgumentException) {
            store.remove(SNAPSHOT_KEY)
            AppSnapshot()
        }
    }

    /** Once per process: pending ledger entries settle, and a due ambassador gets assigned. */
    fun coldLaunch() {
        if (snapshot.ledger.any { it.pending }) update { s -> s.copy(ledger = s.ledger.map { it.copy(pending = false) }) }
        armAmbassador()
    }

    fun onForeground() = armAmbassador()

    /** Dev › Reset: forget everything and relaunch at Splash. */
    fun reset() {
        ambassadorTimer?.cancel()
        store.remove(SNAPSHOT_KEY)
        _state.value = AppSnapshot()
        _launches.value += 1
    }

    // Session

    /** Otp accepted (any code): saves the phone and says where auth goes next. */
    fun confirmOtp(phone: String): AuthStep {
        update { it.copy(session = it.session.copy(phone = phone)) }
        val session = snapshot.session
        return when {
            session.deviceLinked && session.passcode.isNotEmpty() -> AuthStep.EnableFaceId
            session.deviceLinked -> AuthStep.CreatePasscode
            else -> AuthStep.ScanCardQr
        }
    }

    fun qrScanned() = update { it.copy(session = it.session.copy(deviceLinked = true)) }

    fun savePasscode(passcode: String) = update { it.copy(session = it.session.copy(passcode = passcode)) }

    fun checkPasscode(entered: String): Boolean = entered == snapshot.session.passcode

    /** EnableFaceId › Enable or Not now. */
    fun logIn(faceId: Boolean) = update { it.copy(session = it.session.copy(loggedIn = true, faceId = faceId)) }

    /** Keeps the phone, passcode and device link, so the next login skips QR and passcode. */
    fun logOut() = update { it.copy(session = it.session.copy(loggedIn = false)) }

    // Delivery and stage

    fun saveDeliveryAddress(address: Address, details: String) =
        update { it.copy(delivery = it.delivery.copy(address = address, details = details)) }

    /** DeliveryTime › Set up delivery. Scheduling moves NotScheduled to CardComing; rescheduling only changes the time. */
    fun setUpDelivery(day: DeliveryDay, slot: DeliverySlot, date: String) = update {
        val scheduling = it.stage == Stage.NotScheduled
        val stage = if (scheduling) Stage.CardComing(date) else it.stage
        it.copy(
            stage = stage,
            delivery = it.delivery.copy(day = day, slot = slot),
            deliveryArrangedPending = it.deliveryArrangedPending || scheduling,
        )
    }

    fun consumeDeliveryArranged() {
        if (snapshot.deliveryArrangedPending) update { it.copy(deliveryArrangedPending = false) }
    }

    /** The first open arms AmbassadorAssigned [AMBASSADOR_DELAY_MILLIS] later; the due time survives relaunches. */
    fun openDeliveryDetails() {
        val stage = snapshot.stage as? Stage.CardComing ?: return
        if (stage.ambassadorDueAt == null && stage.ambassador == Ambassador.Unassigned) {
            update { it.copy(stage = stage.copy(ambassadorDueAt = now() + AMBASSADOR_DELAY_MILLIS)) }
        }
        armAmbassador()
    }

    /** Close or swipe-down on DeliveryDetails. Returns true when that delivered the card (event CardDelivered). */
    fun closeDeliveryDetails(): Boolean {
        val stage = snapshot.stage as? Stage.CardComing ?: return false
        if (stage.ambassador !is Ambassador.Assigned) return false
        update { it.copy(stage = Stage.Live, avatar = Avatar.Placeholder, activation = Activation.FeedbackPending) }
        return true
    }

    private fun armAmbassador() {
        val stage = snapshot.stage as? Stage.CardComing ?: return
        val due = stage.ambassadorDueAt ?: return
        if (stage.ambassador != Ambassador.Unassigned) return
        ambassadorTimer?.cancel()
        val left = due - now()
        if (left <= 0) {
            assignAmbassador()
        } else {
            ambassadorTimer = scope.launch {
                delay(left)
                assignAmbassador()
            }
        }
    }

    private fun assignAmbassador() = update {
        val stage = it.stage as? Stage.CardComing ?: return@update it
        it.copy(stage = stage.copy(ambassador = Ambassador.Assigned(ASSIGNED_AMBASSADOR)))
    }

    // Activation

    /** Feedback dialog ✕, Send feedback or Feedback › Close: the Activation modal opens at WelcomeCard. */
    fun startActivation() = update { it.copy(activation = Activation.InProgress) }

    /** Let's Plata! or Close dismisses Activation; event AvatarGenerated. */
    fun finishActivation() = update { it.copy(activation = Activation.Done, avatar = Avatar.Generated, avatarIsNew = true) }

    /** PictureIntro › Got it. */
    fun clearNewAvatar() = update { it.copy(avatarIsNew = false) }

    // Home tiles

    fun saveCashback(categories: Set<CashbackCategory>) = update { it.copy(homeTiles = it.homeTiles.copy(cashback = categories)) }

    fun visitMsi() = update { it.copy(homeTiles = it.homeTiles.copy(msiVisited = true)) }

    // Ledger

    /** RewardAmbassador › Reward $X: a pending charge on the credit line that settles on the next cold launch; a repeat replaces it. */
    fun rewardAmbassador(amount: Long) = update {
        val others = it.ledger.filterNot { e -> e.kind == EntryKind.Reward && e.pending }
        it.copy(ledger = others + it.entry("Ambassador reward", EntryKind.Reward, LedgerAccount.CreditLine, -amount, pending = true))
    }

    /** TransferAmount › Transfer: the amount leaves the own balance, the commission is charged to the credit line. */
    fun transfer(recipient: String, amount: Long, commission: Long): TransferReceipt {
        val before = snapshot.accounts.ownBalance
        update {
            val entries = buildList {
                add(it.entry(recipient, EntryKind.TransferOut, LedgerAccount.OwnBalance, -amount))
                if (commission > 0) add(it.entry("Commission for operation", EntryKind.Commission, LedgerAccount.CreditLine, -commission))
            }
            it.copy(ledger = it.ledger + entries, accounts = it.accounts.copy(ownBalance = before - amount))
        }
        return TransferReceipt(recipient, amount, Currency.Mxn, commission, before, before - amount)
    }

    /** BetweenAccounts › Transfer: [usd] cents bought at [PESOS_PER_DOLLAR_CENTS] from the own balance into Inversiones cash. */
    fun transferToInversiones(usd: Long): TransferReceipt {
        val before = snapshot.accounts.ownBalance
        val pesos = usd * PESOS_PER_DOLLAR_CENTS / 100
        update {
            it.copy(
                ledger = it.ledger + it.entry("Crédito → Inversiones", EntryKind.BetweenAccounts, LedgerAccount.OwnBalance, -pesos),
                accounts = it.accounts.copy(ownBalance = before - pesos, inversionesCashUsd = it.accounts.inversionesCashUsd + usd),
            )
        }
        return TransferReceipt("Plata Inversiones", usd, Currency.Usd, 0, before, before - pesos)
    }

    /** Dev › Buy DNUT, standing in for StockDetail's Buy: USD 1 of Krispy Kreme. False when cash is under USD 1. */
    fun buyDnut(): Boolean {
        if (snapshot.accounts.inversionesCashUsd < 100) return false
        update {
            val held = it.invest.holdings.firstOrNull { h -> h.ticker == "DNUT" }
            val holding = held?.copy(costBasis = held.costBasis + 100, marketValue = held.marketValue + 100, quoteRefreshed = false)
                ?: Holding("DNUT", "Krispy Kreme", costBasis = 100, marketValue = 100)
            it.copy(
                accounts = it.accounts.copy(inversionesCashUsd = it.accounts.inversionesCashUsd - 100),
                invest = it.invest.copy(holdings = it.invest.holdings.filter { h -> h.ticker != "DNUT" } + holding),
                ledger = it.ledger + it.entry("Krispy Kreme", EntryKind.BuyShares, LedgerAccount.Inversiones, -100, Currency.Usd),
            )
        }
        return true
    }

    /** Dev › Seed purchases: the month's credit-line purchases that have no in-app source. Seeding twice adds nothing. */
    fun seedPurchases() = update {
        val seed = when (it.month) {
            Month.June -> listOf(
                it.entry("Amazon Prime", EntryKind.Purchase, LedgerAccount.CreditLine, -20_00),
                it.entry("Reversal from Amazon Prime", EntryKind.Refund, LedgerAccount.CreditLine, 20_00),
                it.entry("Carlos Ruiz", EntryKind.TransferIn, LedgerAccount.CreditLine, 20_00),
                it.entry("OXXO", EntryKind.Purchase, LedgerAccount.CreditLine, -20_00),
            )
            Month.July -> listOf(it.entry("Amazon", EntryKind.Purchase, LedgerAccount.CreditLine, -1_832_40).copy(day = 1))
        }
        it.copy(ledger = it.ledger + seed.filter { entry -> entry !in it.ledger })
    }

    // Favorites

    fun addFavorite(favorite: Favorite) = update { it.copy(favorites = listOf(favorite) + it.favorites.filter { f -> f.id != favorite.id }) }

    fun removeFavorite(id: String) = update { it.copy(favorites = it.favorites.filter { f -> f.id != id }) }

    fun moveFavorite(from: Int, to: Int) = update {
        val list = it.favorites.toMutableList()
        list.add(to, list.removeAt(from))
        it.copy(favorites = list)
    }

    // Cards

    fun setFrozen(card: CardId, frozen: Boolean) =
        update { it.copy(cards = Cards(if (frozen) it.cards.frozen + card else it.cards.frozen - card)) }

    // Invest

    /** InvestOnboarding › I agree. */
    fun openInvestAccount() = update { it.copy(invest = it.invest.copy(account = InvestAccount.Opened)) }

    /** Opening StockDetail or InvestmentIdea: most recent first, no repeats. */
    fun addRecent(ticker: String) = update { it.copy(invest = it.invest.copy(recents = listOf(ticker) + (it.invest.recents - ticker))) }

    /** Portfolio open: holdings bought since the last refresh move +1% (DNUT 1.00 → 1.01). */
    fun refreshQuotes() {
        if (snapshot.invest.holdings.all { it.quoteRefreshed }) return
        update {
            val holdings = it.invest.holdings.map { h ->
                if (h.quoteRefreshed) h else h.copy(marketValue = h.costBasis * 101 / 100, quoteRefreshed = true)
            }
            it.copy(invest = it.invest.copy(holdings = holdings))
        }
    }

    // Settings, support, clock

    fun setAppIcon(icon: AppIcon) = update { it.copy(settings = it.settings.copy(appIcon = icon)) }

    fun setAppearance(appearance: Appearance) = update { it.copy(settings = it.settings.copy(appearance = appearance)) }

    /** Dev › Support message. */
    fun addSupportMessage() = update { it.copy(supportUnread = it.supportUnread + 1) }

    /** Opening SupportChat. */
    fun clearSupportUnread() = update { it.copy(supportUnread = 0) }

    /** Dev › Month. */
    fun setMonth(month: Month) = update { it.copy(month = month) }

    companion object {
        /** A repository for tests and previews: starts from [snapshot], keeps writes in memory. */
        fun inMemory(
            snapshot: AppSnapshot = AppSnapshot(),
            scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
            now: () -> Long = { 0L },
        ) = AppRepository(InMemoryKeyValueStore(), scope, now, snapshot)
    }
}

private fun AppSnapshot.entry(
    title: String,
    kind: EntryKind,
    account: LedgerAccount,
    amount: Long,
    currency: Currency = Currency.Mxn,
    pending: Boolean = false,
) = LedgerEntry(month, month.today, title, kind, account, amount, currency, pending)

val LocalAppRepository = staticCompositionLocalOf<AppRepository> { error("No AppRepository provided") }
