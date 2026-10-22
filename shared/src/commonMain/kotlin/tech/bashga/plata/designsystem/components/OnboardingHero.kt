package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_onboarding_delivery
import tech.bashga.plata.designsystem.foundation.ContinuousTopRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

private val TitleToBody = 28.dp
private val BodyToActions = 48.dp
private val BulletGap = 30.dp
private val BottomPadding = 21.dp

/**
 * Full-screen hero: [media] (a photo) fills the screen behind a white panel with 40 top corners, anchored to the bottom
 * and hugging its content (dda69155). The panel insets its content [horizontalPadding] (40) and [topPadding] (40) and
 * stacks the title (34 bold), [body] 28 below it, then [actions] 48 below that, 21 above the home-indicator inset.
 * [OnboardingBullets] is the body the source uses; e97d2568 centres the title and sets the actions 20 in, 41.5 under the
 * title ([actionsGap]) with a secondary action ending 4.4 above the inset ([bottomPadding]).
 */
@Composable
fun OnboardingHero(
    title: String,
    modifier: Modifier = Modifier,
    align: HeadlineBlockAlign = HeadlineBlockAlign.Leading,
    horizontalPadding: Dp = PlataTheme.spacing.s40,
    topPadding: Dp = PlataTheme.spacing.s40,
    actionsGap: Dp = BodyToActions,
    bottomPadding: Dp = BottomPadding,
    media: @Composable BoxScope.() -> Unit,
    body: (@Composable ColumnScope.() -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        media()
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(PlataTheme.colors.surfaceCard, ContinuousTopRoundedShape(PlataTheme.radii.sheet))
                .padding(start = horizontalPadding, end = horizontalPadding, top = topPadding)
                .padding(bottom = bottomPadding + PlataSafeArea.current.bottom),
        ) {
            HeadlineBlock(title, Modifier.fillMaxWidth(), align = align)
            if (body != null) {
                Spacer(Modifier.height(TitleToBody))
                body()
            }
            Spacer(Modifier.height(actionsGap))
            actions()
        }
    }
}

/** The hero's bullet list: 28 icons, 30 between items, explicit line breaks as in the source. */
@Composable
fun OnboardingBullets(items: List<Pair<ImageVector, String>>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BulletGap)) {
        items.forEach { (icon, text) -> BulletItem(text, marker = BulletMarker.Icon, icon = icon) }
    }
}

@Preview
@Composable
internal fun OnboardingHeroPreview() = PlataTheme {
    OnboardingHero(
        "Schedule\nthe delivery",
        Modifier.size(393.dp, 852.dp),
        media = {
            Image(
                painterResource(Res.drawable.raster_onboarding_delivery),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
            )
        },
        body = {
            OnboardingBullets(
                listOf(
                    PlataIcons.MapPin to "Choose a place for your\ncard delivery",
                    PlataIcons.CardWide to "You’ll get a card and cool gift",
                    PlataIcons.Ine to "You’ll need your INE to open\nan account",
                ),
            )
        },
        actions = { Button("Schedule now", {}, Modifier.fillMaxWidth()) },
    )
}
