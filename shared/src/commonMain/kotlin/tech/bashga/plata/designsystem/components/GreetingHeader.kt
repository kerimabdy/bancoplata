package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_avatar_photo
import plata.shared.generated.resources.raster_avatar_sparkle
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class GreetingHeaderLayout { Inline, Centered }

/**
 * Inline: 56 avatar, 16 gap, 20pt name with the Plata+ mark, 15pt greeting (025e71c5). Centered: 96 avatar, 34pt name,
 * 17pt greeting (e4b65435; the redacted name band is a 41 line box, so the inventory's 20pt name would leave it short).
 * [onDark] sets the text in white over the hero wave; [trailing] ends the inline row. [sparkle] rings the inline avatar with the festive sparkle of a freshly generated avatar (9f1aa9fc; `avatarIsNew`).
 * The source names are Mobbin-redacted; screens pass a placeholder name.
 */
// the sparkle ring crop starts 10 left of and 10 above the 56 avatar
private val SparkleOffset = (-10).dp
private val SparkleWidth = 72.dp
private val SparkleHeight = 74.dp

@Composable
fun GreetingHeader(
    name: String,
    greeting: String,
    modifier: Modifier = Modifier,
    layout: GreetingHeaderLayout = GreetingHeaderLayout.Inline,
    avatarImage: Painter? = null,
    showBadge: Boolean = layout == GreetingHeaderLayout.Inline,
    sparkle: Boolean = false,
    onDark: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val ink = if (onDark) colors.textOnDark else colors.textPrimary
    val nameRow: @Composable (Boolean) -> Unit = { large ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4)) {
            PlataText(name, if (large) typography.largeTitle else typography.title3, ink, maxLines = 1)
            if (showBadge) Icon(PlataIcons.PlataPlus, size = 22.dp)
        }
    }
    when (layout) {
        GreetingHeaderLayout.Inline -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            Box {
                Avatar(AvatarSize.Medium, image = avatarImage)
                if (sparkle) {
                    Image(
                        painterResource(Res.drawable.raster_avatar_sparkle),
                        null,
                        Modifier
                            .matchParentSize()
                            .wrapContentSize(Alignment.TopStart, unbounded = true)
                            .offset(SparkleOffset, SparkleOffset)
                            .size(SparkleWidth, SparkleHeight),
                    )
                }
            }
            Spacer(Modifier.width(PlataTheme.spacing.s16))
            Column {
                nameRow(false)
                PlataText(greeting, typography.subhead, ink, maxLines = 1)
            }
            if (trailing != null) {
                Spacer(Modifier.weight(1f))
                trailing()
            }
        }
        GreetingHeaderLayout.Centered -> Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(AvatarSize.Large, image = avatarImage)
            Spacer(Modifier.height(PlataTheme.spacing.s20))
            nameRow(true)
            Spacer(Modifier.height(PlataTheme.spacing.s8))
            PlataText(greeting, typography.body, ink, maxLines = 1)
        }
    }
}

@Preview
@Composable
internal fun GreetingHeaderInlinePreview() = PlataTheme {
    GreetingHeader("Sofía", "Welcome to Plata", avatarImage = painterResource(Res.drawable.raster_avatar_photo))
}

@Preview
@Composable
internal fun GreetingHeaderSparklePreview() = PlataTheme {
    Box(Modifier.padding(12.dp)) {
        GreetingHeader("Sofía", "Welcome to Plata", avatarImage = painterResource(Res.drawable.raster_avatar_photo), sparkle = true)
    }
}

@Preview
@Composable
internal fun GreetingHeaderCenteredPreview() = PlataTheme {
    GreetingHeader(
        "Sofía",
        "Welcome to Plata",
        layout = GreetingHeaderLayout.Centered,
        avatarImage = painterResource(Res.drawable.raster_avatar_photo),
    )
}
