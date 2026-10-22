package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_avatar_photo
import tech.bashga.plata.designsystem.icons.PersonPlaceholder
import tech.bashga.plata.designsystem.icons.PersonPlaceholderMedium
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class AvatarKind { Photo, Placeholder }

/** Small is 36: the inventory says 32, but the nav-bar circles measure 36 (59741a55, f5db1665). */
enum class AvatarSize(val diameter: Dp) { Small(36.dp), Medium(56.dp), Large(96.dp) }

@Composable
fun Avatar(
    size: AvatarSize,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    contentDescription: String? = null,
) {
    val colors = PlataTheme.colors
    val kind = if (image != null) AvatarKind.Photo else AvatarKind.Placeholder
    val gradientPlaceholder = kind == AvatarKind.Placeholder && size == AvatarSize.Medium
    Box(
        modifier
            .size(size.diameter)
            .clip(CircleShape)
            .background(if (gradientPlaceholder) PlataTheme.gradients.avatarPlaceholder else SolidColor(colors.surfaceCard)),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(image, contentDescription, Modifier.size(size.diameter), contentScale = ContentScale.Crop)
        } else {
            Icon(
                if (size == AvatarSize.Medium) PlataIcons.PersonPlaceholderMedium else PlataIcons.PersonPlaceholder,
                size = size.diameter / 2,
                tint = if (gradientPlaceholder) colors.textOnDark.copy(alpha = 0.9f) else colors.textTertiary,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun samplePhoto() = painterResource(Res.drawable.raster_avatar_photo)

@Preview
@Composable
internal fun AvatarPhotoSmallPreview() = PlataTheme { Avatar(AvatarSize.Small, image = samplePhoto()) }

@Preview
@Composable
internal fun AvatarPhotoMediumPreview() = PlataTheme { Avatar(AvatarSize.Medium, image = samplePhoto()) }

@Preview
@Composable
internal fun AvatarPhotoLargePreview() = PlataTheme { Avatar(AvatarSize.Large, image = samplePhoto()) }

@Preview
@Composable
internal fun AvatarPlaceholderSmallPreview() = PlataTheme { Avatar(AvatarSize.Small) }

@Preview
@Composable
internal fun AvatarPlaceholderMediumPreview() = PlataTheme { Avatar(AvatarSize.Medium) }

@Preview
@Composable
internal fun AvatarPlaceholderLargePreview() = PlataTheme { Avatar(AvatarSize.Large) }
