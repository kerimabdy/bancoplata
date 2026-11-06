package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_promo_friend
import plata.shared.generated.resources.raster_promo_investing
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class PromoCardLayout { Hero, Banner }

// 083f8017: the art band above the title is 163 high, the title→body gap 8; the banner is 86 (f5db1665)
private val HeroArtBand = 163.dp
private val HeroTitleBodyGap = 8.dp
private val BannerHeight = 86.dp

/**
 * Gradient promo card, r20, clipping its [illustration].
 * - [PromoCardLayout.Hero] (083f8017, 73569385): 163 art band, 28 bold white [title] centred (the inventory's 24 measures
 *   214 wide in the source, 28 bold), 15/18 [body] 8 below,
 *   white large Button ([actionLabel]) 24 below that; padding 20.
 * - [PromoCardLayout.Banner] (f5db1665): 86 high dark card, 20 bold white [title] centred vertically 20 in; the
 *   illustration sits on the right and [onClick] makes the whole card tappable. Line breaks are the caller's.
 */
@Composable
fun PromoCard(
    title: String,
    modifier: Modifier = Modifier,
    layout: PromoCardLayout = PromoCardLayout.Hero,
    body: String? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    illustration: (@Composable BoxScope.() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val gradients = PlataTheme.gradients
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    val card = modifier
        .fillMaxWidth()
        .tilePressable(onClick)
        .clip(shape)
    when (layout) {
        PromoCardLayout.Hero -> Column(
            card
                .background(gradients.promoHero)
                .background(gradients.promoGlow)
                .padding(start = PlataTheme.spacing.s20, end = PlataTheme.spacing.s20, bottom = PlataTheme.spacing.s20),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(HeroArtBand)) { illustration?.invoke(this) }
            PlataText(title, typography.title1, colors.textOnDark, textAlign = TextAlign.Center)
            if (body != null) {
                Spacer(Modifier.height(HeroTitleBodyGap))
                PlataText(body, typography.subheadTight, colors.textOnDark, textAlign = TextAlign.Center)
            }
            if (actionLabel != null) {
                Spacer(Modifier.height(PlataTheme.spacing.s24))
                Button(actionLabel, onAction, Modifier.fillMaxWidth(), style = ButtonStyle.White)
            }
        }
        PromoCardLayout.Banner -> Box(card.height(BannerHeight).background(gradients.promoBanner)) {
            illustration?.invoke(this)
            PlataText(
                title,
                typography.title3,
                colors.textOnDark,
                Modifier.align(Alignment.CenterStart).padding(start = PlataTheme.spacing.s20),
            )
        }
    }
}

@Preview
@Composable
internal fun PromoCardHeroPreview() = PlataTheme {
    PromoCard(
        "Investing is easy",
        Modifier.width(353.dp),
        body = "Start investing with as little as USD 1.\nOpen an account in 5 minutes.",
        actionLabel = "Open an account",
    ) {
        Image(
            painterResource(Res.drawable.raster_promo_investing),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 6.33.dp).size(143.dp, 148.33.dp),
        )
    }
}

@Preview
@Composable
internal fun PromoCardBannerPreview() = PlataTheme {
    PromoCard("Bring a friend and\nget $200", Modifier.width(353.dp), layout = PromoCardLayout.Banner, onClick = {}) {
        Image(
            painterResource(Res.drawable.raster_promo_friend),
            contentDescription = null,
            // cut 1 inside the card's right edge with the corners masked out, so no page background is left in it
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 1.dp).size(116.33.dp, 84.33.dp),
        )
    }
}
