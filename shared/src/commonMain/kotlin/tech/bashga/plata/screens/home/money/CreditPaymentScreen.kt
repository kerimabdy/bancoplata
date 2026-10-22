package tech.bashga.plata.screens.home.money

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_billing_timeline
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.components.Accordion
import tech.bashga.plata.designsystem.components.BulletItem
import tech.bashga.plata.designsystem.components.BulletMarker
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionCardAmount
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class CreditSection { BillingPeriod, WhenToPay, SixtyDays }

@Immutable
data class CreditPaymentState(
    val status: CreditStatus,
    val expanded: Set<CreditSection> = setOf(CreditSection.BillingPeriod),
)

sealed interface CreditPaymentAction {
    data class Toggle(val section: CreditSection) : CreditPaymentAction

    data object Back : CreditPaymentAction
}

fun CreditPaymentState.reduce(action: CreditPaymentAction): CreditPaymentState = when (action) {
    is CreditPaymentAction.Toggle -> copy(expanded = if (action.section in expanded) expanded - action.section else expanded + action.section)
    CreditPaymentAction.Back -> this
}

class CreditPaymentViewModel(repository: AppRepository) : ViewModel() {
    private val expanded = MutableStateFlow(setOf(CreditSection.BillingPeriod))
    val state: StateFlow<CreditPaymentState> = combine(repository.state, expanded) { snapshot, expanded ->
        CreditPaymentState(snapshot.month.creditStatus, expanded)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CreditPaymentState(repository.snapshot.month.creditStatus))

    fun onAction(action: CreditPaymentAction) {
        expanded.value = state.value.copy(expanded = expanded.value).reduce(action).expanded
    }
}

/** home/credito/payment: the statement status on the fake clock, then "How it works". */
@Composable
fun CreditPaymentScreen(onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel { CreditPaymentViewModel(repository) }
    val state by viewModel.state.collectAsState()
    CreditPaymentContent(state, { if (it == CreditPaymentAction.Back) onBack() else viewModel.onAction(it) })
}

// 22bf11f1 / d2e81313 / 8bda0dcf: title box 18 under the bar, body 9 under the title, cards 40 below; "How it works" 40 / 27
private val HeaderTop = 18.dp
private val HeaderBodyGap = 9.dp
private val HowItWorksBottomGap = 27.dp
private val AmountHeaderGap = 6.dp

// 22bf11f1 spaces the dates card 23 under its header and 24 above the bottom edge
private val DatesHeaderGap = 23.dp

private val SemiBold = SpanStyle(fontWeight = FontWeight.SemiBold)

@Composable
fun CreditPaymentContent(state: CreditPaymentState, onAction: (CreditPaymentAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("credit-payment")
                .verticalScroll(scroll)
                .padding(top = safeArea.top - spacing.s4 + PlataTheme.sizes.nav, bottom = safeArea.bottom + spacing.s20),
        ) {
            Spacer(Modifier.height(HeaderTop))
            HeadlineBlock("Credit payment", Modifier.fillMaxWidth(), size = HeadlineBlockSize.Medium, align = HeadlineBlockAlign.Center)
            Spacer(Modifier.height(HeaderBodyGap))
            // one text per line: a bold span before a line break grows an Android paragraph's line boxes
            state.status.summary().forEach {
                PlataText(it, PlataTheme.typography.bodyCompact, PlataTheme.colors.textPrimary, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(spacing.s40))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                when (val status = state.status) {
                    is CreditStatus.NothingDue -> PaymentDatesCard(status)
                    is CreditStatus.Paid -> {
                        PaymentCard(
                            "Minimum",
                            formatMoney(status.minimum),
                            "Includes part of the period’s debt and lets\nyou avoid the late fees, but still generates\ninterest charges on the debt",
                        )
                        PaymentCard("Full monthly payment", formatMoney(status.full), "Covers full debt of the period")
                    }
                }
            }
            Spacer(Modifier.height(spacing.s40))
            HowItWorks(state.expanded, onToggle = { onAction(CreditPaymentAction.Toggle(it)) })
        }
        NavigationBar(onBack = { onAction(CreditPaymentAction.Back) }, collapsed = rememberCollapsed(scroll), includeStatusBar = true)
    }
}

private fun CreditStatus.summary(): List<AnnotatedString> = when (this) {
    is CreditStatus.NothingDue -> listOf(
        buildAnnotatedString {
            append("Statement expected on ")
            withStyle(SemiBold) { append(nextStatement) }
            append(".")
        },
        AnnotatedString("No payments due right now."),
    )
    is CreditStatus.Paid -> listOf(
        buildAnnotatedString {
            append("All paid for ")
            withStyle(SemiBold) { append(period) }
        },
        buildAnnotatedString {
            append("New statement expected on ")
            withStyle(SemiBold) { append(nextStatement) }
            append(".")
        },
    )
}

@Composable
private fun PaymentDatesCard(status: CreditStatus.NothingDue) {
    val spacing = PlataTheme.spacing
    val items = listOf(
        buildAnnotatedString {
            append("We’ll send your statement ")
            withStyle(SemiBold) { append("on ${status.nextStatement}") }
        },
        AnnotatedString("Statement describes how much\nyou’ve spent"),
        buildAnnotatedString {
            append("You’ll have ")
            withStyle(SemiBold) { append("a month to pay") }
            append("\nyour balance")
        },
        AnnotatedString("The following statements will be\ngenerated monthly"),
    )
    SectionCard(
        Modifier.fillMaxWidth(),
        title = "About payment dates",
        headerGap = DatesHeaderGap,
        bodySpacing = spacing.s20,
        contentPadding = PaddingValues(start = spacing.s20, top = spacing.s20, end = spacing.s20, bottom = spacing.s24),
    ) {
        items.forEachIndexed { index, text ->
            BulletItem(text, marker = BulletMarker.Number, number = index + 1, markerAlignment = Alignment.CenterVertically)
        }
    }
}

@Composable
private fun PaymentCard(title: String, amount: String, description: String) {
    SectionCard(title = title, headerTrailing = { SectionCardAmount(amount) }, headerGap = AmountHeaderGap) {
        PlataText(description, PlataTheme.typography.subheadTight, PlataTheme.colors.textPrimary)
        Spacer(Modifier.height(PlataTheme.spacing.s20))
        Button("Paid", {}, Modifier.fillMaxWidth(), style = ButtonStyle.Success, size = ButtonSize.Medium, icon = PlataIcons.Check)
    }
}

@Composable
private fun HowItWorks(expanded: Set<CreditSection>, onToggle: (CreditSection) -> Unit) {
    val spacing = PlataTheme.spacing
    HeadlineBlock("How it works", Modifier.fillMaxWidth().testTag("how-it-works"), size = HeadlineBlockSize.Medium, align = HeadlineBlockAlign.Center)
    Spacer(Modifier.height(HowItWorksBottomGap))
    Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
        Accordion(
            "Billing period",
            expanded = CreditSection.BillingPeriod in expanded,
            onToggle = { onToggle(CreditSection.BillingPeriod) },
            modifier = Modifier.fillMaxWidth(),
            paragraphs = listOf(
                "You spend money with you card during the billing period by the end of which you receive your statement with amount to pay and the payment date.",
                "You can either pay off the full amount or make the minimum payment.",
            ),
        ) {
            Image(
                painterResource(Res.drawable.raster_billing_timeline),
                contentDescription = "Billing period timeline",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        }
        Accordion(
            "When to pay",
            expanded = CreditSection.WhenToPay in expanded,
            onToggle = { onToggle(CreditSection.WhenToPay) },
            modifier = Modifier.fillMaxWidth(),
            paragraphs = listOf(
                "Pay by the payment date on your statement. Paying the full amount keeps you clear of interest; the minimum payment keeps you clear of late fees.",
            ),
        )
        Accordion(
            "60 days with no interest",
            expanded = CreditSection.SixtyDays in expanded,
            onToggle = { onToggle(CreditSection.SixtyDays) },
            modifier = Modifier.fillMaxWidth(),
            paragraphs = listOf(
                "A purchase made on the first day of the billing period can wait up to 60 days with no interest: the rest of the period plus a month to pay.",
            ),
        )
    }
}
