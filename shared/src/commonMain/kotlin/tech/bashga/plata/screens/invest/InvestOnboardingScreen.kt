package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_dialog_success
import plata.shared.generated.resources.raster_profile_gauge
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.CheckCircle
import tech.bashga.plata.designsystem.components.CheckCircleState
import tech.bashga.plata.designsystem.components.DialSlider
import tech.bashga.plata.designsystem.components.Dialog
import tech.bashga.plata.designsystem.components.DialogPlacement
import tech.bashga.plata.designsystem.components.Footnote
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.ProgressBar
import tech.bashga.plata.designsystem.components.ProgressBarSize
import tech.bashga.plata.designsystem.components.SignaturePad
import tech.bashga.plata.designsystem.components.SignatureStroke
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonTone
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.Overlay
import tech.bashga.plata.navigation.PlataBackHandler
import tech.bashga.plata.navigation.SheetOverlay
import kotlin.math.roundToInt

enum class OnboardingStep { Affiliation, Acknowledgment, Volume, RiskProfile }

/** Affiliation rows; the line breaks are where the source wraps them in SF. */
enum class Affiliation(val label: String) {
    None("None of the below"),
    SeniorExecutive("I am senior executive at or a 10% or\ngreater shareholder of a publicly traded\ncompany"),
    PoliticalFigure("I am a senior political figure"),
    FamilyOfPolitical("I am a family member or relative of a\nsenior political figure"),
    BrokerDealer("I am affiliated or work with a US\nregistered broker-dealer or FINRA"),
}

sealed interface InvestOverlay {
    data object Signature : InvestOverlay
    data object AccountOpened : InvestOverlay
}

const val DEFAULT_VOLUME = 0.05f

@Immutable
data class InvestOnboardingState(
    val step: OnboardingStep = OnboardingStep.Affiliation,
    val affiliations: Set<Affiliation> = emptySet(),
    val signature: List<SignatureStroke> = emptyList(),
    val volume: Float = DEFAULT_VOLUME,
    val overlay: InvestOverlay? = null,
) {
    val volumeLabel: String get() = "~${(volume * 100).roundToInt()}%"
    val canContinue: Boolean get() = step != OnboardingStep.Affiliation || affiliations.isNotEmpty()
}

sealed interface InvestOnboardingAction {
    data class ToggleAffiliation(val affiliation: Affiliation) : InvestOnboardingAction
    data object Continue : InvestOnboardingAction
    data object Back : InvestOnboardingAction
    data object SignAgreements : InvestOnboardingAction
    data class Draw(val stroke: SignatureStroke) : InvestOnboardingAction
    data object Undo : InvestOnboardingAction
    data object Clear : InvestOnboardingAction
    data object SignatureDone : InvestOnboardingAction
    data object SignatureCancel : InvestOnboardingAction
    data class SetVolume(val volume: Float) : InvestOnboardingAction
    data object DecideLater : InvestOnboardingAction
    data object Agree : InvestOnboardingAction
    data object Retake : InvestOnboardingAction

    /** Close, Start investing or the dialog's ✕: the sheet goes away. */
    data object Dismiss : InvestOnboardingAction
}

fun InvestOnboardingState.reduce(action: InvestOnboardingAction): InvestOnboardingState = when (action) {
    is InvestOnboardingAction.ToggleAffiliation -> {
        val a = action.affiliation
        copy(
            affiliations = when {
                a in affiliations -> affiliations - a
                a == Affiliation.None -> setOf(Affiliation.None)
                else -> affiliations - Affiliation.None + a
            },
        )
    }
    InvestOnboardingAction.Continue -> when (step) {
        OnboardingStep.Affiliation -> if (canContinue) copy(step = OnboardingStep.Acknowledgment) else this
        OnboardingStep.Acknowledgment -> copy(overlay = InvestOverlay.Signature)
        OnboardingStep.Volume -> copy(step = OnboardingStep.RiskProfile)
        OnboardingStep.RiskProfile -> this
    }
    InvestOnboardingAction.Back -> when {
        overlay != null -> copy(overlay = null)
        step == OnboardingStep.Affiliation -> this
        else -> copy(step = OnboardingStep.entries[step.ordinal - 1])
    }
    InvestOnboardingAction.SignAgreements -> copy(overlay = InvestOverlay.Signature)
    is InvestOnboardingAction.Draw -> copy(signature = signature + action.stroke)
    InvestOnboardingAction.Undo -> copy(signature = signature.dropLast(1))
    InvestOnboardingAction.Clear -> copy(signature = emptyList())
    InvestOnboardingAction.SignatureDone -> if (signature.isEmpty()) this else copy(overlay = null, step = OnboardingStep.Volume)
    InvestOnboardingAction.SignatureCancel -> copy(overlay = null)
    is InvestOnboardingAction.SetVolume -> copy(volume = action.volume.coerceIn(0f, 1f))
    InvestOnboardingAction.DecideLater -> copy(step = OnboardingStep.RiskProfile)
    InvestOnboardingAction.Agree -> copy(overlay = InvestOverlay.AccountOpened)
    InvestOnboardingAction.Retake -> InvestOnboardingState()
    InvestOnboardingAction.Dismiss -> this
}

class InvestOnboardingViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(InvestOnboardingState())
    val state: StateFlow<InvestOnboardingState> = _state.asStateFlow()

    fun onAction(action: InvestOnboardingAction) {
        if (action == InvestOnboardingAction.Agree) repository.openInvestAccount()
        _state.value = _state.value.reduce(action)
    }
}

/** invest/open: the account-opening steps in one sheet; [onDismiss] closes it (Close, Start investing, the dialog ✕). */
@Composable
fun InvestOnboardingScreen(onDismiss: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { InvestOnboardingViewModel(repository) }
    val state by viewModel.state.collectAsState()
    PlataBackHandler(enabled = state.step != OnboardingStep.Affiliation && state.overlay == null) {
        viewModel.onAction(InvestOnboardingAction.Back)
    }
    InvestOnboardingContent(state, onAction = { action ->
        if (action == InvestOnboardingAction.Dismiss) onDismiss() else viewModel.onAction(action)
    })
}

// the account-opening steps (00e6fc01, 80971162, b3c28fcb, f4915e4c): title box 21.5 under the bar, footnote 18 above
// the CTA, the CTA 20 above the home-indicator inset, or 14 above the secondary text button that ends 6 above it
private val BarToTitle = 21.5.dp
private val FootnoteToButton = 18.dp
private val ButtonToSecondary = 14.dp
private val SecondaryBottom = 6.dp

// 00e6fc01: the first row starts 31 under the body; b3c28fcb: the 259 dial 88 under it
private val BodyToList = 31.dp
private val BodyToDial = 88.dp

// the copy scrolls under the floating button; this keeps its last line clear of it
private val CopyBottomInset = 120.dp

// 5ae897e1: the gauge 136 under the sheet top, the title 50 under it; the CTA 15.5 above "Pass the screening again",
// which ends 24.5 above the home-indicator inset
private val SheetToGauge = 136.dp
private val GaugeToTitle = 50.dp
private val TitleToBody = 12.dp
private val ProfileButtonToSecondary = 15.5.dp
private val ProfileSecondaryBottom = 24.5.dp

// line breaks as the source wraps them in SF
private const val AcknowledgmentCopy =
    "Please confirm that you have read,\nunderstood, and agree to be bound by Alpaca\nSecurities LLC and VestFi, Asesor en\n" +
        "Inversiones Independiente, S.A.P.I. de C.V.\naccount terms, and all other contracts,\nagreements, terms, disclosures and\n" +
        "disclaimers applicable to you, as referenced in\nthe Alpaca Customer Agreement and other\n" +
        "applicable documents. Among others, you\nacknowledge that the Alpaca Customer\n" +
        "Agreement contains a pre-dispute arbitration\nclause in Section 43.\n\n" +
        "You understand that by clicking Accept and\ncontinue you are signing this agreement\n" +
        "electronically, and that your electronic\nsignature will have the same effect as\n" +
        "physically signing and returning the\nApplication Agreement.\n\n" +
        "By signing you instruct Tecnologías Diffiere,\nS.A.P.I. de C.V. to share with VestFi, Asesor en\n" +
        "Inversiones Independiente, S.A.P.I. de C.V. the\ninformation and documents that are strictly\n" +
        "necessary to open your investment account.\n\n" +
        "Under penalties of perjury, you declare that\nthe information you provided is true, correct\nand complete."

@Composable
fun InvestOnboardingContent(state: InvestOnboardingState, onAction: (InvestOnboardingAction) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        when (state.step) {
            OnboardingStep.Affiliation -> OnboardingStepPage(
                "Affiliation",
                "Please read and mark every option that\napplies to you",
                progress = 0.06f,
                cta = "Continue",
                onCta = { onAction(InvestOnboardingAction.Continue) },
                onAction = onAction,
                ctaEnabled = state.canContinue,
                showBack = false,
            ) {
                Spacer(Modifier.height(BodyToList))
                Affiliation.entries.forEach { affiliation ->
                    ListRow(
                        affiliation.label,
                        Modifier.fillMaxWidth(),
                        onClick = { onAction(InvestOnboardingAction.ToggleAffiliation(affiliation)) },
                        titleStyle = PlataTheme.typography.bodyCompact,
                        trailing = { CheckCircle(if (affiliation in state.affiliations) CheckCircleState.Checked else CheckCircleState.Unchecked) },
                    )
                }
            }
            OnboardingStep.Acknowledgment -> OnboardingStepPage(
                "Acknowledgment",
                AcknowledgmentCopy,
                progress = 0.56f,
                cta = "Sign agreements",
                onCta = { onAction(InvestOnboardingAction.SignAgreements) },
                onAction = onAction,
                footnote = false,
                // a blank line between paragraphs, as the source sets them
                paragraphSpacing = PlataTheme.spacing.s20,
                bodyStyle = PlataTheme.typography.bodyLegal,
                contentBottomInset = CopyBottomInset,
            )
            OnboardingStep.Volume -> OnboardingStepPage(
                "Investment volume",
                "What percentage of your money would you\nlike to invest?",
                progress = 0.87f,
                cta = "Continue",
                onCta = { onAction(InvestOnboardingAction.Continue) },
                onAction = onAction,
                secondary = "I’ll decide later",
                onSecondary = { onAction(InvestOnboardingAction.DecideLater) },
            ) {
                Spacer(Modifier.height(BodyToDial))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DialSlider(
                        state.volume,
                        state.volumeLabel,
                        Modifier.testTag("volume-dial"),
                        onValueChange = { onAction(InvestOnboardingAction.SetVolume(it)) },
                    )
                }
            }
            OnboardingStep.RiskProfile -> RiskProfilePage(onAction)
        }
    }
    SheetOverlay(state.overlay as? InvestOverlay.Signature, onDismiss = { onAction(InvestOnboardingAction.SignatureCancel) }) {
        Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceCard)) {
            SignaturePad(
                state.signature,
                onStroke = { onAction(InvestOnboardingAction.Draw(it)) },
                onUndo = { onAction(InvestOnboardingAction.Undo) },
                onClear = { onAction(InvestOnboardingAction.Clear) },
                onDone = { onAction(InvestOnboardingAction.SignatureDone) },
                onCancel = { onAction(InvestOnboardingAction.SignatureCancel) },
                modifier = Modifier.testTag("signature-pad"),
            )
        }
    }
    Overlay(state.overlay as? InvestOverlay.AccountOpened, onDismiss = { onAction(InvestOnboardingAction.Dismiss) }) {
        DialogPlacement(onDismiss = { onAction(InvestOnboardingAction.Dismiss) }) {
            Dialog(
                "We opened your\ninvesting account!",
                body = "Now you can buy your first stocks.\nGood luck and have fun!",
                onClose = { onAction(InvestOnboardingAction.Dismiss) },
                mediaContent = { Image(painterResource(Res.drawable.raster_dialog_success), null, Modifier.size(82.dp, 87.dp)) },
                actions = { Button("Start investing", { onAction(InvestOnboardingAction.Dismiss) }, Modifier.fillMaxWidth()) },
            )
        }
    }
}

/**
 * A step with the progress bar in the sheet's bar, back (when [showBack]) and Close, the large [title] and [body], the
 * step [content], then the floating footer: the optional VestFi [footnote], the [cta] and an optional [secondary].
 */
@Composable
private fun OnboardingStepPage(
    title: String,
    body: String,
    progress: Float,
    cta: String,
    onCta: () -> Unit,
    onAction: (InvestOnboardingAction) -> Unit,
    ctaEnabled: Boolean = true,
    showBack: Boolean = true,
    footnote: Boolean = true,
    secondary: String? = null,
    onSecondary: () -> Unit = {},
    paragraphSpacing: Dp = 10.dp,
    bodyStyle: TextStyle = PlataTheme.typography.bodyCompact,
    contentBottomInset: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val spacing = PlataTheme.spacing
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(PlataTheme.sizes.navSheet + BarToTitle))
            HeadlineBlock(
                title,
                Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin),
                body = body,
                bodyStyle = bodyStyle,
                paragraphSpacing = paragraphSpacing,
            )
            content()
            Spacer(Modifier.height(contentBottomInset))
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = spacing.screenMargin)
                .padding(bottom = PlataSafeArea.current.bottom + if (secondary != null) SecondaryBottom else spacing.s20),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (footnote) {
                Footnote("Services offered by VestFi")
                Spacer(Modifier.height(FootnoteToButton))
            }
            Button(cta, onCta, Modifier.fillMaxWidth(), enabled = ctaEnabled)
            if (secondary != null) {
                Spacer(Modifier.height(ButtonToSecondary))
                TextButton(secondary, onSecondary, tone = TextButtonTone.Primary)
            }
        }
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            onBack = if (showBack) ({ onAction(InvestOnboardingAction.Back) }) else null,
            center = { ProgressBar(progress, size = ProgressBarSize.Nav) },
            trailing = { TextButton("Close", { onAction(InvestOnboardingAction.Dismiss) }) },
        )
    }
}

@Composable
private fun RiskProfilePage(onAction: (InvestOnboardingAction) -> Unit) {
    val spacing = PlataTheme.spacing
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(SheetToGauge))
            HeadlineBlock(
                "Your profile is\nModerate",
                Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin),
                body = "You're looking for a balance between\nsecurity and growth. We recommend\ninvesting up to " +
                    "${(MODERATE_EQUITY_SHARE * 100).roundToInt()}% of your portfolio in\nequities.",
                size = HeadlineBlockSize.Medium,
                align = HeadlineBlockAlign.Center,
                gap = TitleToBody,
                mediaGap = GaugeToTitle,
                media = { Image(painterResource(Res.drawable.raster_profile_gauge), null, Modifier.size(138.dp, 134.dp)) },
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = spacing.screenMargin)
                .padding(bottom = PlataSafeArea.current.bottom + ProfileSecondaryBottom),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button("I agree", { onAction(InvestOnboardingAction.Agree) }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(ProfileButtonToSecondary))
            TextButton("Pass the screening again", { onAction(InvestOnboardingAction.Retake) }, tone = TextButtonTone.Primary)
        }
    }
}

/** A signature like the one on aee18063 (the source blurs it), for the state test. */
val SampleInvestSignature = listOf(
    SignatureStroke(listOf(0.42f to 0.22f, 0.50f to 0.30f, 0.44f to 0.40f, 0.56f to 0.46f, 0.47f to 0.58f, 0.58f to 0.66f, 0.50f to 0.76f).map { Offset(it.first, it.second) }),
)
