package tech.bashga.plata.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlin.math.roundToInt
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import platform.darwin.NSObject
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@OptIn(ExperimentalForeignApi::class)
internal actual val hasNativeTabBar: Boolean = NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(
    cValue<NSOperatingSystemVersion> { majorVersion = 26 },
)

private val TabLabels = listOf("Home", "Payments", "Invest", "Support", "Invite")

// the Value item's text matches TabBarItem's headline (17 semibold)
private const val InviteTextSize = 17.0
private const val InviteGlyphGap = 2.0

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun NativeTabBar(
    active: TabBarTab,
    inviteValue: String,
    inviteKind: TabBarItemKind,
    homeBadge: String?,
    supportBadge: String?,
    covered: () -> Float,
    onSelect: (TabBarTab) -> Unit,
    modifier: Modifier,
) {
    val colors = PlataTheme.colors
    val density = LocalDensity.current
    val iconSize = PlataTheme.sizes.iconLarge
    val glyphSize = PlataTheme.sizes.iconSmall
    val painters = listOf(PlataIcons.House, PlataIcons.DoubleChevron, PlataIcons.Bars, PlataIcons.Chat).map { rememberVectorPainter(it) }
    val flame = rememberVectorPainter(PlataIcons.Flame)
    val icons = remember(density) { painters.map { it.toTemplateImage(iconSize, density) } }
    val inviteImage = remember(inviteValue, inviteKind, density) {
        val glyph = if (inviteKind == TabBarItemKind.IconValue) flame.toTemplateImage(glyphSize, density) else null
        textImage(inviteValue, glyph)
    }
    val items = remember { TabBarTab.entries.map { tab -> UITabBarItem(TabLabels[tab.ordinal], null, tab.ordinal.toLong()) } }
    val currentActive = rememberUpdatedState(active)
    val currentOnSelect = rememberUpdatedState(onSelect)
    // UITabBar holds its delegate weakly
    val delegate = remember {
        TabBarDelegate { bar, index ->
            val tab = TabBarTab.entries[index]
            currentOnSelect.value(tab)
            // Invite presents a sheet and never becomes the active tab
            if (tab == TabBarTab.Invite) bar.selectedItem = items[currentActive.value.ordinal]
        }
    }
    UIKitView(
        factory = {
            UITabBar().apply {
                this.delegate = delegate
                setItems(items, animated = false)
            }
        },
        modifier = modifier,
        update = { bar ->
            items.forEachIndexed { index, item -> item.image = if (index < icons.size) icons[index] else inviteImage }
            items[TabBarTab.Home.ordinal].badgeValue = homeBadge
            items[TabBarTab.Support.ordinal].badgeValue = supportBadge
            bar.tintColor = colors.brandPrimary.toUIColor()
            bar.unselectedItemTintColor = colors.textSecondary.toUIColor()
            bar.selectedItem = items[active.ordinal]
            val shown = 1f - covered()
            bar.alpha = shown.toDouble()
            bar.userInteractionEnabled = shown == 1f
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
            placedAsOverlay = true,
        ),
    )
}

private class TabBarDelegate(private val onSelect: (UITabBar, Int) -> Unit) : NSObject(), UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) = onSelect(tabBar, didSelectItem.tag.toInt())
}

private fun Color.toUIColor(): UIColor = UIColor.colorWithRed(red.toDouble(), green.toDouble(), blue.toDouble(), alpha.toDouble())

@OptIn(ExperimentalForeignApi::class)
private fun Painter.toTemplateImage(size: Dp, density: Density): UIImage {
    val px = (size.value * density.density).roundToInt()
    val bitmap = ImageBitmap(px, px)
    val painter = this
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(px.toFloat(), px.toFloat())) {
        with(painter) { draw(this@draw.size) }
    }
    val png = Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)!!.bytes
    val data = png.usePinned { NSData.dataWithBytes(it.addressOf(0), png.size.toULong()) }
    return UIImage.imageWithData(data, density.density.toDouble())!!
        .imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
}

/** The Invite item has no icon: its value is the image, behind the flame for [TabBarItemKind.IconValue]. */
@OptIn(ExperimentalForeignApi::class)
private fun textImage(text: String, glyph: UIImage?): UIImage {
    val attributes = mapOf<Any?, Any?>(NSFontAttributeName to UIFont.systemFontOfSize(InviteTextSize, UIFontWeightSemibold))
    val string = NSString.create(string = text)
    val (textWidth, textHeight) = string.sizeWithAttributes(attributes).useContents { width to height }
    val (glyphWidth, glyphHeight) = glyph?.size?.useContents { width to height } ?: (0.0 to 0.0)
    val gap = if (glyph != null) InviteGlyphGap else 0.0
    val width = glyphWidth + gap + textWidth
    val height = maxOf(textHeight, glyphHeight)
    return UIGraphicsImageRenderer(size = CGSizeMake(width, height)).imageWithActions { _ ->
        glyph?.drawInRect(CGRectMake(0.0, (height - glyphHeight) / 2, glyphWidth, glyphHeight))
        string.drawAtPoint(CGPointMake(glyphWidth + gap, (height - textHeight) / 2), withAttributes = attributes)
    }.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
}
