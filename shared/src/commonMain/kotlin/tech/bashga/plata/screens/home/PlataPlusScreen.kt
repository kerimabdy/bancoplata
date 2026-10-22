package tech.bashga.plata.screens.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_plata_plus_hero
import tech.bashga.plata.designsystem.components.ListGroup
import tech.bashga.plata.designsystem.components.ListRow
import tech.bashga.plata.designsystem.components.ListRowGlyphSlot
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.PromoHeadline
import tech.bashga.plata.designsystem.components.PromoHeadlineAlign
import tech.bashga.plata.designsystem.components.PromoHeadlineTone
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.components.SectionHeaderEmphasis
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.ChatStar
import tech.bashga.plata.designsystem.icons.Crown
import tech.bashga.plata.designsystem.icons.DiamondPlus
import tech.bashga.plata.designsystem.icons.DollarDown
import tech.bashga.plata.designsystem.icons.Gauge
import tech.bashga.plata.designsystem.icons.HeartBolt
import tech.bashga.plata.designsystem.icons.PaintBrush
import tech.bashga.plata.designsystem.icons.People
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.ShieldCheck
import tech.bashga.plata.designsystem.icons.Smile
import tech.bashga.plata.designsystem.icons.ThumbsUpOutline
import tech.bashga.plata.designsystem.icons.Times2
import tech.bashga.plata.designsystem.icons.Times3
import tech.bashga.plata.designsystem.theme.PlataTheme

/** Plata+ has no state of its own (SCREEN-MAP §3.3): its three references are scroll positions. */
sealed interface PlataPlusAction {
    data object Close : PlataPlusAction

    data class OpenBenefit(val title: String) : PlataPlusAction

    data object ManageSubscription : PlataPlusAction

    data object Terms : PlataPlusAction
}

/** profile/plata-plus: the membership page sheet. */
@Composable
fun PlataPlusScreen(onClose: () -> Unit) {
    PlataPlusContent(onAction = { if (it == PlataPlusAction.Close) onClose() })
}

private class Benefit(val glyph: ImageVector, val title: String, val subtitle: String, val offsetY: Dp = 0.dp)

private class BenefitGroup(val title: String, val brush: @Composable () -> Brush, val benefits: List<Benefit>, val textMaxWidth: Dp = BenefitTextWidth, val rowPadding: Dp = 0.dp)

// the other benefit subtitles wrap at about 215 on 835216e3
private val BenefitTextWidth = 215.dp

private val benefitGroups = listOf(
    BenefitGroup(
        "Improved cashback",
        { PlataTheme.gradients.brand },
        listOf(
            Benefit(PlataIcons.Crown, "10 cashback categories", "Instead of 7 each month", (-2.2).dp),
            Benefit(PlataIcons.Times2, "Double cashback", "Get 1% on all purchases instead of 0.5%", (-3).dp),
            Benefit(PlataIcons.Times3, "Triple cashback limit", "Get $3,000 maximum, instead of $1,000", (-3).dp),
        ),
        // SF breaks the ×2 and ×3 subtitles short of the chevron (787363a9)
        textMaxWidth = 200.dp,
    ),
    BenefitGroup(
        "No charges",
        { PlataTheme.gradients.wordViolet },
        listOf(
            Benefit(PlataIcons.ThumbsUpOutline, "No annual fee", "For your Plata Card"),
            Benefit(PlataIcons.Gauge, "Free interbank transfers", "Transfer to other people without commission"),
            Benefit(PlataIcons.DollarDown, "Lower investment commission", "0.2% instead of 0.3%. 0.15% if portfolio’s above USD 25,000"),
        ),
    ),
    BenefitGroup(
        "Better rewards",
        { PlataTheme.gradients.wordOrange },
        listOf(Benefit(PlataIcons.People, "Increased bonus", "Receive $200 instead of $100 when bringing a friend to Plata")),
    ),
    BenefitGroup(
        "Superior support",
        { PlataTheme.gradients.wordBlue },
        listOf(
            Benefit(PlataIcons.ShieldCheck, "Fraud compensation", "For your card and account"),
            Benefit(PlataIcons.HeartBolt, "Purchase protection", "Fast refund if there’s a problem with a purchase"),
            Benefit(PlataIcons.ChatStar, "SMS notifications", "If you don’t receive the push notifications"),
        ),
    ),
    BenefitGroup(
        "App customization",
        { PlataTheme.gradients.wordCoral },
        listOf(
            Benefit(PlataIcons.Smile, "Exclusive avatars", "Avatars from Plata+ collection"),
            Benefit(PlataIcons.PaintBrush, "Special app icons", "Customize Plata app icon"),
            Benefit(PlataIcons.DiamondPlus, "Plata+ badge", "Next to your name in the app"),
        ),
        // 5b0f3d7c: these rows sit 72 apart where the other one-line rows sit 68
        rowPadding = 2.dp,
    ),
)

// 787363a9: the hero art runs 231 from the sheet top, the headline's line box starts 244 down, the cards 40 under the body
private val HeroHeight = 231.dp
private val HeadlineTop = 244.dp
private val BenefitHeaderGap = 18.dp
// the last row's text ends 18 above the card edge; ListRow keeps 12 of that
private val BenefitCardBottom = 6.dp
// the bar takes the two-line title once the headline is under it (835216e3)
private val CollapseAfter = 250.dp
// 5b0f3d7c: the subscription rows sit 41 under the last benefit card, twice the card gap
private val SubscriptionGroupGap = 21.dp

@Composable
fun PlataPlusContent(onAction: (PlataPlusAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll, CollapseAfter)
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().testTag("plata-plus-scroll").verticalScroll(scroll)) {
            Box {
                Image(
                    painterResource(Res.drawable.raster_plata_plus_hero),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(HeroHeight),
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter,
                )
                PromoHeadline(
                    "Plata+ is yours".uppercase(),
                    Modifier.padding(top = HeadlineTop, start = spacing.screenMargin, end = spacing.screenMargin).fillMaxWidth(),
                    align = PromoHeadlineAlign.Center,
                    tone = PromoHeadlineTone.Plain,
                    body = "$39/mo + VAT starting on your first credit card due date",
                    bodyColor = PlataTheme.colors.textPrimary,
                    bodyGap = spacing.s12,
                )
            }
            Spacer(Modifier.height(spacing.s40))
            Column(Modifier.padding(horizontal = spacing.screenMargin), verticalArrangement = Arrangement.spacedBy(spacing.cardGap)) {
                benefitGroups.forEachIndexed { index, group ->
                    SectionCard(
                        Modifier.fillMaxWidth().testTag("benefits-$index"),
                        title = group.title,
                        titleEmphasis = SectionHeaderEmphasis.Gradient,
                        titleBrush = group.brush(),
                        headerGap = BenefitHeaderGap,
                        contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = BenefitCardBottom),
                    ) {
                        group.benefits.forEach { benefit ->
                            ListRow(
                                benefit.title,
                                Modifier.fillMaxWidth().padding(vertical = group.rowPadding),
                                subtitle = benefit.subtitle,
                                showChevron = true,
                                onClick = { onAction(PlataPlusAction.OpenBenefit(benefit.title)) },
                                horizontalPadding = 0.dp,
                                subtitleStyle = PlataTheme.typography.footnoteTight,
                                textMaxWidth = group.textMaxWidth,
                                leading = { ListRowGlyphSlot(benefit.glyph, offsetY = benefit.offsetY) },
                            )
                        }
                    }
                }
                ListGroup(Modifier.fillMaxWidth().padding(top = SubscriptionGroupGap)) {
                    ListRow("Manage subscription", showChevron = true, onClick = { onAction(PlataPlusAction.ManageSubscription) })
                    ListRow("Terms and conditions", showChevron = true, onClick = { onAction(PlataPlusAction.Terms) })
                }
            }
            Spacer(Modifier.height(spacing.s40 + PlataSafeArea.current.bottom))
        }
        NavigationBar(
            style = NavigationBarStyle.Sheet,
            leading = { TextButton("Close", { onAction(PlataPlusAction.Close) }) },
            center = if (collapsed) {
                {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PlataText("Plata+", PlataTheme.typography.headline, PlataTheme.colors.textPrimary, textAlign = TextAlign.Center)
                        PlataText("$39/mo + VAT", PlataTheme.typography.subheadTight, PlataTheme.colors.textPrimary, textAlign = TextAlign.Center)
                    }
                }
            } else {
                null
            },
            material = collapsed,
        )
    }
}
