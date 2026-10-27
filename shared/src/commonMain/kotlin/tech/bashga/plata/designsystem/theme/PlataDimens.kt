package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PlataSpacing(
    val s4: Dp = 4.dp,
    val s8: Dp = 8.dp,
    val s12: Dp = 12.dp,
    val s16: Dp = 16.dp,
    val s20: Dp = 20.dp,
    val s24: Dp = 24.dp,
    val s32: Dp = 32.dp,
    val s40: Dp = 40.dp,
    val screenMargin: Dp = 20.dp,
    /** UISearchBar inset; the one element not on the 20pt margin. */
    val searchMargin: Dp = 16.dp,
    val cardPadding: Dp = 20.dp,
    val cardGap: Dp = 20.dp,
    val rowGap: Dp = 16.dp,
    val titleSubtitle: Dp = 4.dp,
    val sectionGap: Dp = 32.dp,
)

@Immutable
data class PlataSizes(
    val rowSingle: Dp = 56.dp,
    val rowAsset: Dp = 80.dp,
    val buttonLarge: Dp = 56.dp,
    val buttonMedium: Dp = 48.dp,
    val buttonSmall: Dp = 40.dp,
    val buttonXSmall: Dp = 32.dp,
    val search: Dp = 36.dp,
    val nav: Dp = 44.dp,
    val navSheet: Dp = 54.dp,
    val tabbar: Dp = 82.dp,
    val iconXSmall: Dp = 16.dp,
    val iconSmall: Dp = 20.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 28.dp,
    /** Leading tile / logo / IconCircle slot in rows and cards. */
    val leadingTile: Dp = 40.dp,
)

/** Radius tokens; `radius/2xl` is [xxl]. Turn one into a shape with `ContinuousRoundedShape(radius)`. */
@Immutable
data class PlataRadii(
    val xs: Dp = 4.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 32.dp,
    val sheet: Dp = 40.dp,
    /**
     * The display's corner: pages that shrink or grow off the screen round to it, so they meet the glass. 55 is the
     * iPhone 15/16 Pro's; PlataTheme puts in the platform's own where it reports one.
     */
    val device: Dp = 55.dp,
    val full: Dp = 9999.dp,
)

/** A CSS-style drop shadow: y offset, blur radius, colour (x offset is 0 for every Plata shadow). */
@Immutable
data class PlataShadow(val offsetY: Dp, val blur: Dp, val color: Color)

@Immutable
data class PlataShadows(
    val orb: PlataShadow = PlataShadow(8.dp, 16.dp, PlataPalette.Black.copy(alpha = 0.10f)),
    val thumb: PlataShadow = PlataShadow(2.dp, 4.dp, PlataPalette.Black.copy(alpha = 0.08f)),
    val knob: PlataShadow = PlataShadow(3.dp, 8.dp, PlataPalette.Black.copy(alpha = 0.15f)),
    // the Toggle on a6fae664 shows no shadow outside its track, so its knob shadow stays tight
    val switchKnob: PlataShadow = PlataShadow(1.dp, 2.dp, PlataPalette.Black.copy(alpha = 0.12f)),
)
