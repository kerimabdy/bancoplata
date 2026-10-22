package tech.bashga.plata.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_avatar_photo
import tech.bashga.plata.data.Avatar
import tech.bashga.plata.data.CashbackCategory
import tech.bashga.plata.data.Month
import tech.bashga.plata.data.formatMoney
import tech.bashga.plata.designsystem.icons.Baby
import tech.bashga.plata.designsystem.icons.Globe
import tech.bashga.plata.designsystem.icons.HomeDiy
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.TShirt
import tech.bashga.plata.designsystem.icons.Taxi
import tech.bashga.plata.designsystem.icons.Volleyball

/** The sources redact the customer's name. */
internal const val CustomerName = "Sofía"

/** The generated photo, or null for the placeholder silhouette. */
@Composable
internal fun Avatar.painter(): Painter? = if (this == Avatar.Generated) painterResource(Res.drawable.raster_avatar_photo) else null

/** "Nothing spent in June" or "$70 Spent in June" (Home's and CardDetails' Transactions card). */
internal fun spentLabel(spent: Long, month: Month): String =
    if (spent == 0L) "Nothing spent in ${month.label}" else "${formatMoney(spent)} Spent in ${month.label}"

internal val CashbackCategory.glyph: ImageVector?
    get() = when (this) {
        CashbackCategory.AllPurchases -> PlataIcons.Percent
        CashbackCategory.Airline -> PlataIcons.Plane
        CashbackCategory.ChildrensStores -> PlataIcons.Baby
        CashbackCategory.Clothing -> PlataIcons.TShirt
        CashbackCategory.Entertainment -> PlataIcons.Music
        CashbackCategory.HomeDiy -> PlataIcons.HomeDiy
        CashbackCategory.Mobile -> PlataIcons.Globe
        CashbackCategory.Restaurants -> PlataIcons.ForkKnife
        CashbackCategory.SportingGoods -> PlataIcons.Volleyball
        CashbackCategory.Taxi -> PlataIcons.Taxi
        CashbackCategory.Justo -> null
    }

// the source's blurred tab bar hides the content under it; Compose has no backdrop blur, so this fill stands in
@Composable
internal fun TabBarBackdrop(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(PlataSafeArea.current.bottom).background(PlataTheme.colors.surfaceBackground.copy(alpha = 0.9f)))
}
