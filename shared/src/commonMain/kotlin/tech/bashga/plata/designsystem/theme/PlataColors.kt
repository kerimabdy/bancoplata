package tech.bashga.plata.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Semantic colours from ds/foundations.md; `brand/primary` becomes [brandPrimary], `surface/card` becomes [surfaceCard]. */
@Immutable
data class PlataColors(
    val brandPrimary: Color = PlataPalette.BrandPrimary,
    val accent: Color = PlataPalette.Accent,
    val accentTint: Color = PlataPalette.AccentTint,
    val accentTrack: Color = PlataPalette.AccentTrack,
    val progressTrackFull: Color = PlataPalette.ProgressTrackFull,
    val glyphTint: Color = PlataPalette.GlyphTint,
    val walletPocket: Color = PlataPalette.WalletPocket,
    val walletBlue: Color = PlataPalette.WalletBlue,
    val walletYellow: Color = PlataPalette.WalletYellow,
    val walletGreen: Color = PlataPalette.WalletGreen,
    val walletCoral: Color = PlataPalette.WalletCoral,

    /** `text/primary` has two values in foundations.md: this one for body text, [textPrimaryTitle] for large titles. */
    val textPrimary: Color = PlataPalette.Ink,
    val textPrimaryTitle: Color = PlataPalette.Black,
    val textSecondary: Color = PlataPalette.TextSecondary,
    val textTertiary: Color = PlataPalette.TextTertiary,
    val textOnDark: Color = PlataPalette.White,
    val textSuccess: Color = PlataPalette.TextSuccess,
    val textNegative: Color = PlataPalette.TextNegative,
    val textDestructive: Color = PlataPalette.TextDestructive,

    val surfaceBackground: Color = PlataPalette.SurfaceBackground,
    val surfaceCard: Color = PlataPalette.White,
    val surfaceInset: Color = PlataPalette.SurfaceInset,
    val surfaceField: Color = PlataPalette.SurfaceField,
    val surfaceSearch: Color = PlataPalette.SurfaceSearch,
    val surfaceSecondaryButton: Color = PlataPalette.SurfaceSecondaryButton,
    val surfaceDark: Color = PlataPalette.SurfaceDark,
    val surfaceDarkCard: Color = PlataPalette.SurfaceDarkCard,
    val surfaceBlack: Color = PlataPalette.SurfaceBlack,
    val surfaceSheetDark: Color = PlataPalette.SurfaceSheetDark,
    val surfaceToast: Color = PlataPalette.SurfaceToast,
    /** `surface/control` in Figma: grey IconButton disc on the page. */
    val surfaceControl: Color = PlataPalette.SurfaceControl,
    /** Fill behind light brand logos (BrandLogo). */
    val surfaceLogo: Color = PlataPalette.SurfaceLogo,
    /** Glass over media: IconButton glass (white 30%), Pill glass (black 30%). */
    val glassLight: Color = PlataPalette.White.copy(alpha = 0.3f),
    val glassDark: Color = PlataPalette.Black.copy(alpha = 0.3f),
    /** Glass Pill: a grey blur material, not a darkening (09c22ffa reads #3A556D over #2E628E). */
    val glassMaterial: Color = PlataPalette.GlassMaterial.copy(alpha = 0.47f),
    /** Hairline around CardThumb and light BrandLogos. */
    val borderSubtle: Color = PlataPalette.BorderSubtle,
    /** TabBar material where nothing is blurred. */
    val surfaceTabbar: Color = PlataPalette.SurfaceBackground.copy(alpha = 0.9f),
    /** The same material over a backdrop blur, thinner so the frosted content reads through. */
    val surfaceTabbarFrost: Color = PlataPalette.SurfaceBackground.copy(alpha = 0.7f),
    /** NavigationBar material (scroll edge) where nothing is blurred: reads #F8F8FA–#FAFAFB over the page (baff34c6, d58c1486, b50f7634). */
    val surfaceNavMaterial: Color = PlataPalette.SurfaceNavMaterial.copy(alpha = 0.85f),
    /** The same material over a backdrop blur, thinner so the frosted content reads through. */
    val surfaceNavMaterialFrost: Color = PlataPalette.SurfaceNavMaterial.copy(alpha = 0.7f),
    /** NavigationBar bottom hairline (537a4b5a): one device pixel. */
    val navSeparator: Color = PlataPalette.NavSeparator,
    val scrim: Color = PlataPalette.Black.copy(alpha = 0.5f),

    val success: Color = PlataPalette.Success,
    val successTint: Color = PlataPalette.SuccessTint,
    val danger: Color = PlataPalette.Danger,
    /** iOS system red: tab-bar badges read #FF3B30 (9f1aa9fc, d58c1486, b9c441c0), card-corner badges [danger]. */
    val dangerSystem: Color = PlataPalette.DangerSystem,
    // iOS system blue: the Cancel action of a system alert (d0087d8e)
    val accentSystem: Color = PlataPalette.AccentSystem,
    val dangerTint: Color = PlataPalette.DangerTint,
    val bankRed: Color = PlataPalette.BankRed,
    /** MethodCard fills sampled flat from 43e51dd1 and 5e785f05 (the inventory's #23272D and brand/primary read a little off). */
    val surfaceMethodDark: Color = PlataPalette.MethodDark,
    val surfaceMethodOrange: Color = PlataPalette.MethodOrange,
    val controlOff: Color = PlataPalette.ControlOff,
    /** iOS switch track, off. */
    val controlTrackOff: Color = PlataPalette.ControlTrackOff,
    val onGradientInk: Color = PlataPalette.OnGradientInk,
    val keypadOnGradientInk: Color = PlataPalette.KeypadOnGradientInk,
    val keypadSystem: Color = PlataPalette.KeypadSystem,
    val cardEmbossViolet: Color = PlataPalette.CardEmbossViolet,
    val cardEmbossTeal: Color = PlataPalette.CardEmbossTeal,
    val cardEmbossSilver: Color = PlataPalette.CardEmbossSilver,
    val orbBlue: Color = PlataPalette.OrbBlue,
    val orbViolet: Color = PlataPalette.OrbViolet,
    val orbRed: Color = PlataPalette.OrbRed,
    /** DialSlider fill stops at 0°, 90°, 150° and 235° clockwise from 12 o'clock; ticks are the 25/50/75% dots. */
    val dialStart: Color = PlataPalette.DialStart,
    val dialQuarter: Color = PlataPalette.DialQuarter,
    val dialMid: Color = PlataPalette.DialMid,
    val dialEnd: Color = PlataPalette.DialEnd,
    val dialTick: Color = PlataPalette.DialTick,
    /** PageSheet: the presenting screen as the card peeking above the sheet, and the dim over a light presenter (3b3eaed7). */
    val surfaceSheetStacked: Color = PlataPalette.SheetStacked,
    // a light presenter shrunk behind a page sheet (d4c491d6: the Profile screen under Personal details)
    val surfaceSheetStackedLight: Color = PlataPalette.SheetStackedLight,
    val sheetDim: Color = PlataPalette.Black.copy(alpha = 0.23f),
    // the live presenter shrunk behind a Stacked sheet: #4D4D51 over ScheduleDeliveryIntro (73a046d0), #D7D8DC over Payments (2be9ff97)
    val stackedPresenterDim: Color = PlataPalette.Black.copy(alpha = 0.11f),
    val sheetBackdrop: Color = PlataPalette.Black,
    val cardLabelTeal: Color = PlataPalette.CardLabelTeal,
)
