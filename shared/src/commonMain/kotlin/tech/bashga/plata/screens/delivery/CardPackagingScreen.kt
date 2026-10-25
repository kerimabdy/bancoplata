package tech.bashga.plata.screens.delivery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_card_packaging_hero
import plata.shared.generated.resources.raster_media_safe_bag
import plata.shared.generated.resources.raster_media_unpacking
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.Logo
import tech.bashga.plata.designsystem.components.LogoBrand
import tech.bashga.plata.designsystem.components.MediaCard
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.TextButtonTone
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.theme.PlataTheme

internal const val SupportPhone = "+52 55 9990 8880"

sealed interface CardPackagingAction {
    data object CallSupport : CardPackagingAction

    data object ContactSupport : CardPackagingAction

    data object Close : CardPackagingAction
}

/** delivery/packaging: a long dark page with nothing to change but its scroll, so no state and no ViewModel. */
@Composable
fun CardPackagingScreen(onCallSupport: () -> Unit, onContactSupport: () -> Unit, onClose: () -> Unit) {
    CardPackagingContent(onAction = { action ->
        when (action) {
            CardPackagingAction.CallSupport -> onCallSupport()
            CardPackagingAction.ContactSupport -> onContactSupport()
            CardPackagingAction.Close -> onClose()
        }
    })
}

// 34ec0c7a, measured from the sheet top: the hero art 61 down and 315 high, the title 10 under it, Escudo 22 under that
private val HeroTop = 61.dp
private val HeroHeight = 315.dp
private val TitleTop = 10.dp
private val EscudoTop = 22.dp
private val EscudoHeight = 44.dp
private val CardsTop = 42.dp
private val MediaImageHeight = 270.7.dp

@Composable
fun CardPackagingContent(onAction: (CardPackagingAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    val scroll = rememberScrollState()
    val collapsed = rememberCollapsed(scroll)
    val phone = buildAnnotatedString {
        append("If the bag has been damaged, please let us\nknow by calling our customer support at\n")
        withLink(LinkAnnotation.Clickable("phone", TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline))) { onAction(CardPackagingAction.CallSupport) }) {
            append(SupportPhone)
        }
        append(" or send us a chat")
    }
    Box(modifier.fillMaxSize().background(colors.surfaceSheetDark)) {
        Column(
            Modifier.fillMaxSize().backdropSource().verticalScroll(scroll).testTag("packaging-scroll"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(HeroTop))
            Image(
                painterResource(Res.drawable.raster_card_packaging_hero),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(HeroHeight),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(TitleTop))
            PlataText(
                "Secure packaging\nof Plata Card",
                typography.largeTitle,
                colors.textOnDark,
                Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(EscudoTop))
            Logo(LogoBrand.Escudo, height = EscudoHeight)
            Spacer(Modifier.height(CardsTop))
            PackagingCard(
                "Safe plastic bag",
                "Make sure that tear tape of the bag is intact\nand there are no damages on it before\naccepting the card",
                Res.drawable.raster_media_safe_bag,
                Modifier.testTag("safe-bag"),
            )
            Spacer(Modifier.height(spacing.cardGap))
            PackagingCard(
                "Unpacking",
                "Open the bag in a private place after the\ndelivery to protect your card details",
                Res.drawable.raster_media_unpacking,
                Modifier.testTag("unpacking"),
            )
            Spacer(Modifier.height(spacing.s40))
            PlataText("Problem reports", typography.value, colors.textOnDark, textAlign = TextAlign.Center)
            Spacer(Modifier.height(spacing.s12))
            PlataText(
                phone,
                typography.subheadTight,
                colors.textOnDark,
                Modifier.padding(horizontal = spacing.screenMargin).fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.s40))
            Button(
                "Contact support",
                { onAction(CardPackagingAction.ContactSupport) },
                Modifier.padding(horizontal = spacing.s40).fillMaxWidth(),
                style = ButtonStyle.Secondary,
                labelStyle = typography.headline,
            )
            Spacer(Modifier.height(spacing.s40 * 2 + PlataSafeArea.current.bottom))
        }
        NavigationBar(
            // the dark sheet's scroll-edge material: a translucent dark card fill (no backdrop blur)
            if (collapsed) Modifier.background(colors.surfaceDarkCard.copy(alpha = 0.9f)) else Modifier,
            style = NavigationBarStyle.SheetDark,
            leading = { TextButton("Close", { onAction(CardPackagingAction.Close) }, tone = TextButtonTone.OnDark) },
        )
    }
}

@Composable
private fun PackagingCard(title: String, body: String, image: DrawableResource, modifier: Modifier) {
    MediaCard(title, body, modifier.padding(horizontal = PlataTheme.spacing.screenMargin).fillMaxWidth()) {
        Image(
            painterResource(image),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(MediaImageHeight),
            contentScale = ContentScale.Crop,
        )
    }
}
