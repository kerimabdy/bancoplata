package tech.bashga.plata.screens.home.money

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tech.bashga.plata.designsystem.components.BrandLogo
import tech.bashga.plata.designsystem.foundation.ProgressiveBlurImage
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonSize
import tech.bashga.plata.designsystem.components.Icon
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.SectionCard
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@Immutable
data class MsiPartnerDetailState(val partner: MsiPartner)

sealed interface MsiPartnerDetailAction {
    data object ShopOnline : MsiPartnerDetailAction

    data object Back : MsiPartnerDetailAction
}

/** home/msi/partner/{id}: a partner's MSI terms; Shop online opens the partner's site in the browser. */
@Composable
fun MsiPartnerDetailScreen(partnerId: String, onBack: () -> Unit) {
    val state = MsiPartnerDetailState(MsiPartners.byId(partnerId))
    val uriHandler = LocalUriHandler.current
    MsiPartnerDetailContent(state, { action ->
        when (action) {
            MsiPartnerDetailAction.ShopOnline -> uriHandler.openUri("https://www.${state.partner.website}")
            MsiPartnerDetailAction.Back -> onBack()
        }
    })
}

// b78c58b4: a 464 photo; the 72 logo at y 238.7, the title box at 327.5, the first card overlapping the photo at 407.7
private val PhotoHeight = 464.dp
private val LogoTop = 238.7.dp
private val TitleGap = 16.8.dp
private val CardTop = 407.7.dp
private val BackLift = 11.dp
private val MsiMark = 17.dp
private val MarkGap = 4.dp
private val HeaderBodyGap = 11.7.dp
private val ButtonTop = 20.4.dp
private val NoteGap = 6.4.dp
private val BuyCardBottom = 18.6.dp
private val TermsHeaderTop = 18.7.dp
private val TermsBodyGap = 12.3.dp
private val FeedBottom = 37.dp

@Composable
fun MsiPartnerDetailContent(state: MsiPartnerDetailState, onAction: (MsiPartnerDetailAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PlataTheme.spacing
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val partner = state.partner
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            val photo = partner.photo ?: partner.tile
            if (photo != null) {
                ProgressiveBlurImage(painterResource(photo), Modifier.fillMaxWidth().height(PhotoHeight))
            } else {
                Box(Modifier.fillMaxWidth().height(PhotoHeight).background(PlataTheme.gradients.violet))
            }
            Column(Modifier.padding(top = LogoTop, start = spacing.s40, end = spacing.s40)) {
                BrandLogo(painterResource(partner.logo), BrandLogoSize.S72)
                Spacer(Modifier.height(TitleGap))
                PlataText(partner.name, typography.largeTitle, colors.textOnDark)
            }
            Column(
                Modifier.padding(top = CardTop, start = spacing.screenMargin, end = spacing.screenMargin, bottom = PlataSafeArea.current.bottom + FeedBottom),
                verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
            ) {
                SectionCard(contentPadding = PaddingValues(start = spacing.cardPadding, top = spacing.cardPadding, end = spacing.cardPadding, bottom = BuyCardBottom)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PlataIcons.Msi, size = MsiMark, brush = PlataTheme.gradients.glyph)
                        Spacer(Modifier.width(MarkGap))
                        PlataText("Buy in MSI", typography.title3, colors.textPrimary)
                    }
                    Spacer(Modifier.height(HeaderBodyGap))
                    PlataText("MSI eligible only for online purchases\nfrom ${partner.minimumText}", typography.bodyCompact, colors.textPrimary)
                    Spacer(Modifier.height(ButtonTop))
                    Button("Shop online", { onAction(MsiPartnerDetailAction.ShopOnline) }, Modifier.fillMaxWidth(), size = ButtonSize.Medium, icon = PlataIcons.Link)
                    Spacer(Modifier.height(NoteGap))
                    PlataText(
                        "You will be redirected to ${partner.website}",
                        typography.footnote,
                        colors.textSecondary,
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                SectionCard(contentPadding = PaddingValues(start = spacing.cardPadding, top = TermsHeaderTop, end = spacing.cardPadding, bottom = spacing.cardPadding)) {
                    PlataText("Terms of MSI", typography.title3, colors.textPrimary)
                    Spacer(Modifier.height(TermsBodyGap))
                    PlataText(
                        "The list of products covered by the MSI and additional conditions can be clarified in partner stores " +
                            "and on the official website of ${partner.name}.",
                        typography.bodyCompact,
                        colors.textPrimary,
                    )
                }
            }
        }
        NavigationBar(
            Modifier.offset(y = PlataSafeArea.current.top - BackLift),
            style = NavigationBarStyle.OverMedia,
            onBack = { onAction(MsiPartnerDetailAction.Back) },
        )
    }
}
