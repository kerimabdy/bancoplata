package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush

/**
 * Gradient brushes from ds/foundations.md. Each one spans the bounds of whatever it paints
 * (the infinite end points resolve to the drawn size), so the same brush works for a text run or a fill.
 */
@Immutable
data class PlataGradients(
    val brand: Brush = Brush.horizontalGradient(
        listOf(PlataPalette.BrandGradient1, PlataPalette.BrandGradient2, PlataPalette.BrandGradient3),
    ),
    val promo: Brush = Brush.verticalGradient(listOf(PlataPalette.PromoGradientTop, PlataPalette.PromoGradientBottom)),
    val progress: Brush = Brush.horizontalGradient(
        listOf(PlataPalette.ProgressGradientStart, PlataPalette.ProgressGradientEnd),
    ),
    // on "Portfolio analysis" (f1b38f8d) only "Po" reads purple; the rest of the run is plain ink
    val ai: Brush = Brush.horizontalGradient(0f to PlataPalette.AiGradientStart, 0.22f to PlataPalette.Ink, 1f to PlataPalette.Ink),
    val glyph: Brush = Brush.verticalGradient(listOf(PlataPalette.GlyphGradientTop, PlataPalette.GlyphGradientBottom)),
    // stop offsets come from the Figma ai-badge glyph; foundations.md lists colours only
    val aiBadge: Brush = Brush.linearGradient(
        0f to PlataPalette.AiBadge1,
        0.38f to PlataPalette.AiBadge2,
        0.62f to PlataPalette.AiBadge3,
        0.82f to PlataPalette.AiBadge4,
        1f to PlataPalette.AiBadge5,
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    ),
    val progressTrackTinted: Brush = Brush.horizontalGradient(
        listOf(PlataPalette.ProgressTrackTintedStart, PlataPalette.ProgressTrackTintedEnd),
    ),
    val avatarPlaceholder: Brush = Brush.verticalGradient(
        listOf(PlataPalette.AvatarPlaceholderTop, PlataPalette.AvatarPlaceholderBottom),
    ),
    // sampled from the Settings "Notifications" glyph (59741a55); not in foundations.md
    val notification: Brush = Brush.verticalGradient(
        listOf(PlataPalette.NotificationGradientTop, PlataPalette.NotificationGradientBottom),
    ),
    // sampled from the MSI badges (894b1b01, d55a4d32): light violet top-left to deep blue bottom-right
    val msi: Brush = Brush.linearGradient(listOf(PlataPalette.MsiGradientStart, PlataPalette.MsiGradientEnd)),
    // IconCircle tone=filled on d58c1486
    val violet: Brush = Brush.verticalGradient(listOf(PlataPalette.VioletGradientTop, PlataPalette.VioletGradientBottom)),
    // Figma gradient/iridescent: digital CardThumb face, Escudo lockup
    val iridescent: Brush = Brush.linearGradient(
        0f to PlataPalette.Iridescent1,
        0.5f to PlataPalette.Iridescent2,
        1f to PlataPalette.Iridescent3,
    ),
    // Figma gradient/silver: physical CardThumb face
    val silver: Brush = Brush.linearGradient(listOf(PlataPalette.SilverTop, PlataPalette.SilverBottom)),
    // IconTile fills sampled top/bottom from the category list (e7edfe4c) and the Plata app tile (40d2b403)
    val categoryOrange: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryOrangeTop, PlataPalette.CategoryOrangeBottom)),
    val categorySky: Brush = Brush.verticalGradient(listOf(PlataPalette.CategorySkyTop, PlataPalette.CategorySkyBottom)),
    val categoryPink: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryPinkTop, PlataPalette.CategoryPinkBottom)),
    val categoryLilac: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryLilacTop, PlataPalette.CategoryLilacBottom)),
    val categoryPlum: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryPlumTop, PlataPalette.CategoryPlumBottom)),
    val categoryBlue: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryBlueTop, PlataPalette.CategoryBlueBottom)),
    val categoryMagenta: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryMagentaTop, PlataPalette.CategoryMagentaBottom)),
    val categoryTeal: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryTealTop, PlataPalette.CategoryTealBottom)),
    val categoryYellow: Brush = Brush.verticalGradient(listOf(PlataPalette.CategoryYellowTop, PlataPalette.CategoryYellowBottom)),
    // portfolio allocation tiles (b9c441c0): Equity, Fixed income, Cash
    val allocationEquity: Brush = Brush.verticalGradient(listOf(PlataPalette.AllocationEquityTop, PlataPalette.AllocationEquityBottom)),
    val allocationFixedIncome: Brush = Brush.verticalGradient(listOf(PlataPalette.AllocationFixedTop, PlataPalette.AllocationFixedBottom)),
    val allocationCash: Brush = Brush.verticalGradient(listOf(PlataPalette.AllocationCashTop, PlataPalette.AllocationCashBottom)),
    // Plata Inversiones account tile and disc (6e1c2ab5, 705c4f5e): magenta top-right into violet bottom-left
    val plataInversiones: Brush = Brush.linearGradient(
        listOf(PlataPalette.PlataInversionesTopRight, PlataPalette.PlataInversionesBottomLeft),
        start = Offset(Float.POSITIVE_INFINITY, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
    ),
    // dark MethodCard tile (43e51dd1): white 34% at the top fading into the card by the bottom edge
    val glassFade: Brush = Brush.verticalGradient(listOf(PlataPalette.White.copy(alpha = 0.34f), PlataPalette.White.copy(alpha = 0f))),
    val appIcon: Brush = Brush.verticalGradient(listOf(PlataPalette.AppIconTop, PlataPalette.AppIconBottom)),
    val plataPlus: Brush = Brush.verticalGradient(
        0f to PlataPalette.PlataPlus1,
        0.35f to PlataPalette.PlataPlus2,
        0.65f to PlataPalette.PlataPlus3,
        1f to PlataPalette.PlataPlus4,
    ),
    // gradient titles "GET $200" (63be46ba) and "Frozen" (697dbf17) sample bluer and redder than [progress]
    val titleViolet: Brush = Brush.horizontalGradient(listOf(PlataPalette.TitleVioletStart, PlataPalette.TitleVioletEnd)),
    // SectionHeader gradient words sampled from the Plata+ cards (charges/rewards/support 835216e3, customization 5b0f3d7c)
    val wordViolet: Brush = Brush.horizontalGradient(listOf(PlataPalette.WordVioletStart, PlataPalette.WordVioletEnd)),
    val wordOrange: Brush = Brush.horizontalGradient(listOf(PlataPalette.WordOrangeStart, PlataPalette.WordOrangeEnd)),
    val wordBlue: Brush = Brush.horizontalGradient(listOf(PlataPalette.WordBlueStart, PlataPalette.WordBlueEnd)),
    val wordCoral: Brush = Brush.horizontalGradient(listOf(PlataPalette.WordCoralStart, PlataPalette.WordCoralEnd)),
    // SectionCard border=gradient (f1b38f8d): a sweep from the card centre, cyan top-left, pink top-right, yellow
    // bottom-right, violet bottom-left; the corner stops sit at the 353×281 card's diagonals
    val cardBorder: Brush = Brush.sweepGradient(
        0f to PlataPalette.CardBorderRight,
        0.106f to PlataPalette.CardBorderBottomRight,
        0.25f to PlataPalette.CardBorderBottom,
        0.394f to PlataPalette.CardBorderBottomLeft,
        0.5f to PlataPalette.CardBorderLeft,
        0.606f to PlataPalette.CardBorderTopLeft,
        0.75f to PlataPalette.CardBorderTop,
        0.894f to PlataPalette.CardBorderTopRight,
        1f to PlataPalette.CardBorderRight,
    ),
    /** PromoCard hero base (083f8017, sampled; bluer at the top than gradient/promo). Paint [promoGlow] over it. */
    val promoHero: Brush = Brush.verticalGradient(
        0f to PlataPalette.PromoHeroTop,
        0.1f to PlataPalette.PromoHeroUpper,
        0.29f to PlataPalette.PromoHeroMiddle,
        0.45f to PlataPalette.PromoHeroLower,
        1f to PlataPalette.PromoHeroBottom,
    ),
    val promoGlow: Brush = BottomGlowBrush,
    val promoBanner: Brush = Brush.horizontalGradient(listOf(PlataPalette.PromoBannerStart, PlataPalette.PromoBannerEnd)),
)

// centred low in the card and reaching ~65% of its width, like the source glow
private object BottomGlowBrush : ShaderBrush() {
    override fun createShader(size: Size): Shader = RadialGradientShader(
        center = Offset(size.width / 2, size.height * 0.85f),
        radius = size.width * 0.65f,
        colors = listOf(1f, 0.9f, 0.25f, 0f).map { PlataPalette.PromoHeroGlow.copy(alpha = it) },
        colorStops = listOf(0f, 0.3f, 0.7f, 1f),
    )
}
