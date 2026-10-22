# Plata app architecture (builder guide)

The app is one real app: Splash → auth → the stage root → the tab shell (`../ds/SCREEN-MAP.md` §2). You convert the screens of one area from per-screenshot composables into stateful screens. Follow this file literally. `DESIGN_SYSTEM.md` still holds the component, token and command reference, and `../ds/COMPOSE.md` its rules (no Material, tokens only, `FontFamily.Default`, no comments except one-line "why").

Paths below are under `shared/src/commonMain/kotlin/tech/bashga/plata/` unless they start with `androidHostTest/` (`shared/src/androidHostTest/kotlin/tech/bashga/plata/`) or `commonTest/`.

## Layout

| path | what |
|---|---|
| `App.kt` | `App()`: PlataTheme, window safe area, the repository (held by a ViewModel for the process), `PlataApp` |
| `data/AppSnapshot.kt` | the persisted state of §2.1, one `@Serializable` value |
| `data/AppRepository.kt` | the fake backend: `state`, the write methods, `LocalAppRepository` |
| `data/Derived.kt` | values computed from the snapshot: `creditAvailable`, `spent()`, `received()`, `homeBadge`, `portfolioValue/Cost`, `Month.inviteOffer/appVersion/tenure`, `formatMoney` |
| `data/KeyValueStore.kt` (+ `.android.kt`, `.ios.kt`) | expect/actual store: SharedPreferences, NSUserDefaults; `InMemoryKeyValueStore` |
| `navigation/PlataNavigator.kt` | the navigator, `Presentation`, the `screen<Route>()` builder |
| `navigation/PlataNavHost.kt` | draws the back stack as layers (base, sheets, covers); `StaticLayers` for tests |
| `navigation/AppNavigator.kt` | what graph files call: `navigate`, `back`, `dismiss`, `dismissAll`, `switchTab`, `replaceRoot`, `goToStageRoot` |
| `navigation/Overlays.kt` | `Overlay`, `SheetOverlay`, `PlataBackHandler`, `OverlayHost` |
| `navigation/MainShell.kt` | tab graphs (`HomeTab`…), `TabShell`, the tab bar data from the repository |
| `navigation/PlataApp.kt` | the root graph: one call per area |
| `navigation/Placeholder.kt` | `PlaceholderScreen` for unconverted destinations |
| `navigation/graph/<Area>Graph.kt` | an area's routes and destination registrations |
| `dev/DevPanel.kt` | `DevActions`, `DevPanel`, `DevPanelRoute`, `isDebugBuild()` |
| `screens/<area>/` | converted screens |

## The screen pattern

Every screen has four parts, in one file `screens/<area>/<Name>Screen.kt`:

1. `<Name>State`: an immutable `data class` (`@Immutable`) with everything a user changes by tapping on this screen (mode, step, selection, typed text, toggles, the open overlay), plus the repository values it shows. Scroll position, focus and pager page are never fields (SCREEN-MAP §1): they live in `rememberScrollState()`, `FocusRequester` and `rememberPagerState` inside `<Name>Content`.
2. `<Name>Action`: a `sealed interface` of what the user can do.
3. `<Name>Content(state, onAction, modifier)`: stateless and `@Composable`; draws everything, including overlays. Screenshot tests render only this.
4. `<Name>Screen(…)`: the thin stateful wrapper. It gets the repository from `LocalAppRepository.current`, creates the ViewModel with `viewModel { … }` (scoped to the back-stack entry), collects its state with `collectAsState()` (not `collectAsStateWithLifecycle`: a presenter under a sheet is still on screen but only CREATED), and takes navigation as lambda parameters (`onPasscodeSet: () -> Unit`). It never touches routes or the NavController.

State holder: an `androidx.lifecycle.ViewModel` (multiplatform) holding a `MutableStateFlow<State>`. Put the transition logic in a pure `fun State.reduce(action): State` when it is more than a line, and unit-test it in `commonTest/`. Repository data comes in by combining `repository.state` into the screen state (`repository.state.map { … }.stateIn(viewModelScope, SharingStarted.Eagerly, initial)` or `combine`).

The reference conversion, `screens/auth/PasscodeScreen.kt` (abridged):

```kotlin
@Immutable
data class PasscodeState(
    val mode: PasscodeMode = PasscodeMode.Create,
    val entered: String = "",
    val first: String? = null,
    val status: PasscodeStatus = PasscodeStatus.Entering,
) {
    val title: String get() = when { … }
}

sealed interface PasscodeAction {
    data class Digit(val digit: Int) : PasscodeAction
    data object Delete : PasscodeAction
}

fun PasscodeState.reduce(action: PasscodeAction): PasscodeState { … }

class PasscodeViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(PasscodeState())
    val state: StateFlow<PasscodeState> = _state.asStateFlow()

    fun onAction(action: PasscodeAction) {
        val next = _state.value.reduce(action)
        if (next.status == PasscodeStatus.Success && _state.value.status != PasscodeStatus.Success) repository.savePasscode(next.entered)
        _state.value = next
    }
}

@Composable
fun PasscodeScreen(onPasscodeSet: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { PasscodeViewModel(repository) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.status) {
        if (state.status == PasscodeStatus.Success) {
            delay(SuccessDelayMillis)
            onPasscodeSet()
        }
    }
    PasscodeContent(state, viewModel::onAction)
}

@Composable
fun PasscodeContent(state: PasscodeState, onAction: (PasscodeAction) -> Unit, modifier: Modifier = Modifier) { … }
```

Its registration in `navigation/graph/AuthGraph.kt`:

```kotlin
screen<PasscodeRoute> {
    PasscodeScreen(onPasscodeSet = { nav.navigate(EnableFaceIdRoute) })
}
```

Its tests: `androidHostTest/screenshots/AuthStateScreenshots.kt` (one per reference id) and `commonTest/…/screens/auth/PasscodeStateTest.kt` (the reducer). Its old files (`CreatePasscodeScreen.kt`, `ConfirmPasscodeScreen.kt`, `PasscodeSetScreen.kt`), their registry entries and their Batch tests are deleted.

## Navigation

### Routes and destinations

Each route is a `@Serializable` `data object` or `data class` in your area's graph file. All 44 already exist; read their KDoc for the arguments. Register a destination inside `fun NavGraphBuilder.<area>Graph(nav: AppNavigator, repository: AppRepository)`:

```kotlin
screen<CardDetailsRoute>(Presentation.Sheet) { entry ->
    val route = entry.toRoute<CardDetailsRoute>()
    CardDetailsScreen(CardId.valueOf(route.cardId), onClose = nav::back, onTransactions = { nav.navigate(TransactionsRoute) })
}
```

- `screen<Route>(presentation, backdrop, hidesTabBar)` is the only builder. Don't use `composable` or `dialog`.
- To convert a destination, replace its `PlaceholderScreen(…)` call with your `XScreen(…)`, keeping the outgoing actions the placeholder lists (they are §3's action tables).
- Destinations of a tab with a page of its own sit inside that tab's `navigation<HomeTab>(…) { }` block (also `PaymentsTab`, `InvestTab`, `SupportTab`); sheets and covers sit outside it. Don't move a destination between the two.

### Presentations

| | how | |
|---|---|---|
| push | `Presentation.Push` (default) | slides in over the layer on top: the base screen, or inside the sheet or cover that is showing |
| sheet | `Presentation.Sheet` | an iOS page sheet. The nav host picks the backdrop: Dimmed over a cover, Stacked (presenter shrinks into the card on black) otherwise. Pass `backdrop = PageSheetBackdrop.Dimmed` only where §2 says the presenter is a dialog (Feedback). |
| full-screen cover | `Presentation.Cover` | Activation (`WelcomeCardRoute`), `LessonRoute`, `SupportCoverRoute` |
| no tab bar | `hidesTabBar = true` | StockDetail, InvestmentIdea |

A sheet's content fills the sheet and sees `PlataSafeArea.current.top == 0`; it starts with `NavigationBar(style = NavigationBarStyle.Sheet, …)` and paints its own background. Don't call `PageSheet(…)` in new screens: the nav host draws the sheet and its presenter. Tab screens and screens pushed in a tab see a safe-area bottom that already includes the tab bar.

### Moving around (`AppNavigator`)

| call | does |
|---|---|
| `nav.navigate(route)` | push, present a sheet or a cover, per the destination's presentation |
| `nav.back()` | pop the top entry (back from a push, or close a sheet with nothing pushed inside) |
| `nav.dismiss()` | close the topmost sheet or cover with everything pushed inside it |
| `nav.dismiss(result = KEY to value)` | the same, handing a String to the screen under it: read `entry.savedStateHandle.getStateFlow<String?>(KEY, null)` in that screen's registration (see `FAVORITE_ADDED` in `PaymentsGraph.kt`) |
| `nav.dismissAll()` | close every sheet and cover |
| `nav.switchTab(tab)` | what the tab bar does; Invite presents ReferFriend |
| `nav.replaceRoot(route)` | root replace (log out → PhoneNumber) |
| `nav.goToStageRoot()` | the stage root (ScheduleDeliveryIntro or Home), reopening Activation when it is InProgress |
| `nav.controller.popBackStack<Route>(inclusive = false)` | back to a specific destination (DeliveryTime → DeliveryDetails) |

"Replace the sheet" in §3 is `nav.back()` then `nav.navigate(other)`.

### Back

System back, the iOS edge swipe and a swipe down on a sheet all pop the top entry. When back must mean something else (a step screen's previous step, DeliveryDetails' close event), put `PlataBackHandler { onAction(Back) }` in `XScreen` or `XContent`. It is active only while that screen is on top. Never use `androidx.activity.compose.BackHandler`.

### Overlays

Overlays are part of the host screen's state and are drawn by `XContent`, over the live screen and over the tab bar:

```kotlin
Overlay(state.overlay as? HomeOverlay.DeliveryArranged, onDismiss = { onAction(HomeAction.CloseOverlay) }) {
    DialogPlacement(onDismiss = { onAction(HomeAction.CloseOverlay) }) { Dialog(…) }
}
SheetOverlay(state.overlay as? PhoneOverlay.Otp, onDismiss = { onAction(PhoneAction.CancelOtp) }) { otp ->
    OtpSheet(otp, onAction)
}
```

- `Overlay(value)` shows while `value` is non-null and keeps the last value while it fades out. Its content draws its own scrim (`DialogPlacement`, `ModalScrim { BottomSheet(…) }`, or a `ToastHost`; pass `dismissOnBack = false` for toasts).
- `SheetOverlay(value, backdrop)` is a page sheet that returns to its host (Otp, PictureIntro, PersonalDetails, Signature). The host shrinks behind it like a presented sheet.
- Back closes an open overlay through `onDismiss`.

## The repository

- Read: `val snapshot by repository.state.collectAsState()`, or map `repository.state` into the ViewModel's state. Use the derived values in `data/Derived.kt` instead of recomputing them.
- Write: call the methods on `AppRepository` (`confirmOtp`, `setUpDelivery`, `openDeliveryDetails`, `closeDeliveryDetails`, `startActivation`, `finishActivation`, `saveCashback`, `rewardAmbassador`, `transfer`, `transferToInversiones`, `addFavorite`, `setFrozen`, `openInvestAccount`, `addRecent`, `refreshQuotes`, `clearSupportUnread`, …). They implement every §2.1 write, event and ledger rule. Writes persist immediately.
- Events fire by themselves: AmbassadorAssigned (10 s after `openDeliveryDetails()`), CardDelivered (`closeDeliveryDetails()` returns true), AvatarGenerated (`finishActivation()`). Pending ledger entries settle on a cold launch.
- Dev actions (`DevActions`): month, seed purchases, buy DNUT, support message, reset. Profile hosts the panel: `nav.navigate(DevPanelRoute)` from a long-press on App version, only when `isDebugBuild()`.

Adding repository API:

1. A write only your area needs: an extension in your package, built on the one write primitive: `fun AppRepository.markLessonDone(id: String) = update { it.copy(…) }` in `screens/<area>/<Area>Data.kt`. A derived value: an extension property in the same file.
2. Never edit `AppRepository.kt` or `Derived.kt`. If a §2.1 rule is missing or wrong there, stop and report it.
3. A new persisted field: report it to the orchestrator, who appends it to `AppSnapshot.kt` with a default value. Never rename or remove a field (old snapshots must decode).

## State screenshot tests

One test per reference id in `androidHostTest/screenshots/<Area>StateScreenshots.kt`, named after the id; ids that show the same state share one test that lists both:

```kotlin
class HomeStateScreenshots : ScreenshotTest() {
    @Test fun `9f1aa9fc`() = screenState("9f1aa9fc", shell = TestShell(TabBarTab.Home, Month.June, homeBadge = 2)) {
        HomeContent(HomeState(…), onAction = {})
    }

    @Test fun `025e71c5 9533117e`() = screenState("025e71c5", "9533117e", shell = TestShell(TabBarTab.Home)) { HomeContent(…) {} }

    // a sheet on its presenter (§3 "test setup": presented from Home, Stacked)
    @Test fun `202bee50`() = screenState(
        "202bee50",
        presentation = Presentation.Sheet,
        under = listOf(StaticLayer(Presentation.Push) { HomeContent(…) {} }),
    ) { CardDetailsContent(…) {} }

    // scrolled, focused, mid-animation
    @Test fun `642128ff`() = screenState("642128ff", presentation = Presentation.Sheet, clockMillis = 100, setup = { focus("details") }) { … }
    @Test fun `f5db1665`() = screenState("f5db1665", shell = …, setup = { scrollToAnchor("bring-a-friend", top = 412.dp) }) { … }
}
```

`screenState(ids, presentation, backdrop, under, shell, repository, clockMillis, replacing, setup) { content }` renders 393×852 with the iPhone safe area, provides `LocalAppRepository` (default: a first-run in-memory repository) and an overlay host, and writes `screenshots/screen/<id>.png`.

- `under`: the presenters, bottom first, as `StaticLayer(presentation) { … }`. A sheet over a sheet hides everything two levels down, so `under` holds at most one layer.
- `shell`: §6's "shell" column. "covered (any)" means any `TestShell`; "no tab bar" means none.
- `setup` (`StateSetup`): `scrollBy(dp, tag?)`, `scrollToAnchor(tag, top, scrollableTag?)`, `scrollHorizontallyBy(dp, tag)`, `focus(tag)` (caret after the text), `swipePager(tag, page)`, `advanceClock(ms)`. Tag the nodes in `XContent` with `Modifier.testTag("…")`.
- `clockMillis`: pauses the clock and captures that many ms in. Use it for every focused field (the caret blinks) and every mid-animation reference. `replacing = { OldContent() }` plays the root-replace transition into `content` (b76b9dda, f8f29bdc).
- §2.1 test fixtures are plain state or `AppSnapshot` values you pass in; the running app never sees them.

Run and compare:

```sh
./gradlew :shared:testAndroidHostTest --tests '*HomeStateScreenshots'
tools/compare.sh 9f1aa9fc                 # screenshots/compare/9f1aa9fc.png; read it
tools/compare.sh 9f1aa9fc 0 700 393 152   # zoom into a region (points)
```

When an id has a state test, delete its line from `Legacy<Area>ScreenScreenshots.kt` in the same change (two tests must never write one PNG).

Flow UI tests (§5) go in `androidHostTest/flows/<Flow>FlowTest.kt` and drive `PlataApp(AppRepository.inMemory(snapshot))` through taps; `FirstRunFlowTest.kt` is the example. Apply a ⚠ step with `DevActions(repository)` or a starting snapshot.

## Converting a screen, step by step

1. Read the screen's §3 entry, its references (`../mobbin/img/<id>.jpg`) and the old files in its "Files" line.
2. Write `screens/<area>/<Name>Screen.kt` (State, Action, Content, Screen, ViewModel), reusing the old internals; move them into your file or package rather than importing old files you will delete.
3. Swap the placeholder in your graph file for `<Name>Screen`, wired to the §3 actions.
4. Write the state tests; delete the ids' legacy tests; run them; compare every id and fix what is off (layout over 2pt, colour, type, text).
6. `./gradlew :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug` and `./gradlew :shared:testAndroidHostTest` must pass.
7. Append one line per id to `../ds/compose-log.md`.

## Ownership

Each builder edits only the files of their area's row, plus new files in their own packages.

| area (§3) | screens | graph file | new code | state tests | legacy files to empty |
|---|---|---|---|---|---|
| auth (3.1) | Splash, Lock, PhoneNumber, ScanCardQr, Passcode ✓, EnableFaceId | `navigation/graph/AuthGraph.kt` | `screens/auth/` | `AuthStateScreenshots.kt` | `AuthLegacyScreens.kt`, `LegacyAuthScreenScreenshots.kt` |
| delivery (3.2) | ScheduleDeliveryIntro, DeliveryAddress, DeliveryTime, DeliveryDetails, CardPackaging, RescheduleReason, Feedback, WelcomeCard, RewardAmbassador, ProductTour | `navigation/graph/DeliveryGraph.kt` | `screens/delivery/` | `DeliveryStateScreenshots.kt` | `DeliveryLegacyScreens.kt`, `LegacyDeliveryScreenScreenshots.kt` |
| home (3.3) | Home, CardDetails, Profile, PlataPlus, CashbackSetup, Partners, ReferFriend, Transactions, PlataCredito, CreditPayment, MsiHome, MsiPartnerDetail, MsiPartners | `navigation/graph/HomeGraph.kt` | `screens/home/` | `HomeStateScreenshots.kt` | `HomeLegacyScreens.kt`, `LegacyHomeScreenScreenshots.kt` |
| payments (3.4) | Payments, Favorites, TransferRecipient, TransferAmount, BetweenAccounts, TransferResult | `navigation/graph/PaymentsGraph.kt` | `screens/payments/` | `PaymentsStateScreenshots.kt` | `PaymentsLegacyScreens.kt`, `LegacyPaymentsScreenScreenshots.kt` |
| invest (3.5) | Investments, InvestOnboarding, StockList, StockDetail, Portfolio, PortfolioAllocation, InvestmentIdea, Lesson | `navigation/graph/InvestGraph.kt` | `screens/invest/` | `InvestStateScreenshots.kt` | `InvestLegacyScreens.kt`, `LegacyInvestScreenScreenshots.kt` |
| support (3.6) | SupportChat (tab root and `SupportCoverRoute`) | `navigation/graph/SupportGraph.kt` | `screens/support/` | `SupportStateScreenshots.kt` | `SupportLegacyScreens.kt`, `LegacySupportScreenScreenshots.kt` |

Old screen files each belong to exactly one area: the one whose §3 "Files" lines name it. Where a "Files" line names a target in another package (`profile/ProfileScreen.kt`, `msi/`), put the new file in your area's package instead. Old files by area:

- **auth**: `auth/` all except `LogOutAlertScreen.kt`; `onboarding/LockScreen.kt`, `onboarding/SplashScreen.kt`.
- **delivery**: `cards/CardPackaging*`, `cards/DeliveryAddress*`, `cards/DeliveryTime*`, `cards/RescheduleReasonScreen.kt`; `onboarding/AmbassadorAssignedSheetScreen.kt`, `CommissionFreeIntroScreen.kt`, `DeliveryDetailsSheetScreen.kt`, `GoDigitalIntroScreen.kt`, `OnboardingIntroPage.kt`, `RewardAmbassadorScreen.kt`, `ScheduleDeliveryIntroScreen.kt`, `ViewPinIntroScreen.kt`, `WelcomeCardScreen.kt`; `support/Feedback*`.
- **home**: `auth/LogOutAlertScreen.kt`; `cards/CardDetailsScreen.kt`, `CardFrozenScreen.kt`, `DigitalCard*`, `FreezeCardDialogScreen.kt`; `home/HomeScrolledScreen.kt`, `HomeWelcomeScreen.kt`, `TransactionsHomeScreen.kt`, `TransactionsScreen.kt`, `TransactionsScrolledScreen.kt`; `msi/`; `onboarding/AmbassadorFeedbackDialogScreen.kt`, `CardDetailsSheetScreen.kt`, `CardSheetScreen.kt`, `DeliveryArrangedDialogScreen.kt`, `HomeScreen.kt`; `payments/`; `profile/`; `rewards/`; `terminals/`.
- **payments**: `home/FavoriteAddedDialogScreen.kt`, `FavoriteCardFormScreen.kt`, `FavoritesAddSheetScreen.kt`, `FavoritesEditScreen.kt`, `PaymentsFavoritesScreen.kt`, `PaymentsScreen.kt`; `transfers/` all except `TransferFromPortfolioSheetScreen.kt`.
- **invest**: `investments/`; `learn/`; `onboarding/Acknowledgment*`, `Affiliation*`, `InvestingAccountOpenedDialogScreen.kt`, `InvestmentOnboardingSheet.kt`, `InvestmentVolume*`, `InvestmentsScreen.kt`, `InvestmentsWelcomeScreen.kt`, `RiskProfileScreen.kt`, `SignatureScreen.kt`; `transfers/TransferFromPortfolioSheetScreen.kt`.
- **support**: `support/SupportChatScreen.kt`.

### Shared files

Frozen (report a needed change instead of making it): everything in `navigation/` outside `graph/`, `App.kt`, `data/`, `dev/`, `screens/ScreenRegistry.kt`, `androidHostTest/screenshots/ScreenshotTest.kt`, `StateSetup.kt`, `SpecimenFrame.kt`, the flow tests you didn't write, `gradle/`, the build files, `tools/`.

Append-only (add, never change or remove what is there; keep existing renders identical):

- Route classes in other areas' graph files are read-only to you; in your own file you may add a route parameter only with a default value, and never rename a route (other areas navigate to it).
- `designsystem/` components, icons and theme: the smallest backwards-compatible addition (a parameter with a default, a new overload, a new token, a glyph in a new icon file). Then render the whole suite before and after and diff the PNGs (`DESIGN_SYSTEM.md`); every existing render must stay identical.
- `../ds/compose-log.md`: append lines only.

## Commands

| what | command |
|---|---|
| build both platforms | `./gradlew :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug` |
| all tests (unit, flow, screenshots) | `./gradlew :shared:testAndroidHostTest` (same as `recordRoborazziAndroidHostTest`) |
| one test class | `./gradlew :shared:testAndroidHostTest --tests '*HomeStateScreenshots'` |
| compare an id | `tools/compare.sh <id>` |
| run on Android | `./gradlew :androidApp:installDebug && adb shell am start -n tech.bashga.plata/.MainActivity`; `adb shell pm clear tech.bashga.plata` for a first run |
| run on iOS | see `DESIGN_SYSTEM.md` § App and devices |
