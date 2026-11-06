package tech.bashga.plata.designsystem.foundation

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import tech.bashga.plata.dev.dials.DialOption
import tech.bashga.plata.dev.dials.DialPanel
import tech.bashga.plata.dev.dials.DialSpring
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The idea card's press, expansion and dismissal. The dials panel tunes how the page opens, closes and is dismissed
 * (debug builds, live from the drawer); the press is settled. Springs are SwiftUI's duration and bounce (bounce 0 is
 * critically damped).
 */
object CardMotion {
    private val panel = DialPanel("Card motion")

    private val speed = panel.select(
        "slowMotion",
        listOf(DialOption("1", "Off"), DialOption("2", "2×"), DialOption("4", "4×"), DialOption("10", "10×")),
    )

    /** How many times slower every motion here plays, to watch it frame by frame. */
    val slowdown: Float get() = speed.value.toFloat()

    val pressedScale = 0.97f

    fun pressInSpring(): SpringSpec<Float> = timeSpring(0.25f, 0f, 0.0001f)

    fun pressOutSpring(): SpringSpec<Float> = timeSpring(0.3f, 0f, 0.0001f)

    // a card near the top barely moves and lands firmly; one further down takes longer and bounces as it arrives
    private val open = panel.folder("open")
    val openShortest by open.slider("shortest", 0.3f, 0.1f..1.5f, 0.01f, "s")
    val openLongest by open.slider("longest", 0.5f, 0.1f..1.5f, 0.01f, "s")
    val openDampingNear by open.slider("dampingNear", 0.86f, 0.3f..1f, 0.01f)
    val openDampingFar by open.slider("dampingFar", 0.75f, 0.3f..1f, 0.01f)

    // the card's growth to full size runs over this share of the flight, done before the position settles
    val growShare by open.slider("growShare", 0.65f, 0.1f..1f, 0.01f)

    // a linear growth stops dead at full size; the spring (no bounce) and the ease-out arrive at rest
    private val growCurveDial = open.select("growCurve", GrowCurve.entries.map { DialOption(it.name, it.label) })
    val growCurve: GrowCurve get() = GrowCurve.valueOf(growCurveDial.value)

    // the titles trade places this far into the motion, opening or closing alike; a fade length of 0 swaps them outright
    private val title = panel.folder("title")
    val titleSwapAt by title.slider("swapAt", 0.05f, 0f..1f, 0.01f)
    val titleFadeLength by title.slider("fadeLength", 0f, 0f..0.6f, 0.01f)

    // the article under the header rises into place and fades in over the last stretch of the growth, this share of it,
    // from this far below; closing, it sinks and fades out over the first stretch of the shrinking
    private val content = panel.folder("content")
    val contentFade by content.slider("fade", 0.4f, 0.1f..1f, 0.01f)
    val contentSlide by content.slider("slide", 24f, 0f..80f, 1f, "dp")

    // the page flies home a touch under critical, the card it lands in settling rather than stopping dead; its size gets
    // there in this share of the flight, with no bounce, so its white edges are in before the position settles
    private val close = panel.folder("close")
    private val flightHome = close.spring("flightHome", DialSpring.Time(0.4f, 0.2f))
    val shrinkShare by close.slider("shrinkShare", 0.5f, 0.1f..1f, 0.01f)

    fun <T> closeSpring(visibilityThreshold: T): SpringSpec<T> = flightHome.spec(visibilityThreshold, slowdown)

    fun <T> closeShrinkSpring(visibilityThreshold: T): SpringSpec<T> {
        val physics = flightHome.value.resolved
        val period = (2 * PI / sqrt(physics.stiffness / physics.mass)).toFloat() * shrinkShare
        return timeSpring(period, 0f, visibilityThreshold)
    }

    private val drag = panel.folder("dragToClose")

    // fully dragged, the page sits this much smaller under the finger, as the App Store's cards do before they fly home
    val dragShrink by drag.slider("shrink", 0.14f, 0f..0.4f, 0.01f)

    // a drag this long shrinks the page all the way and closes it, without waiting for the finger to lift
    val dismissReach by drag.slider("reach", 100f, 40f..300f, 1f, "dp")

    // how far a flick's velocity carries the drag: UIScrollView's projection for a normal deceleration
    val projection by drag.slider("projection", 0.2f, 0f..0.6f, 0.01f, "s")

    // a fling that runs into the top dips the page by its speed, a hint that it can be pulled away, and springs it
    // back; it never closes it
    private val swing = panel.folder("swing")
    private val swingStrength by swing.slider("strength", 1f, 0f..3f, 0.05f)
    private val swingMax by swing.slider("maxShrink", 0.05f, 0f..0.14f, 0.005f)
    private val springBack = swing.spring("springBack", DialSpring.Time(0.45f, 0f))

    /** The deepest the swing goes, as a share of a full drag: Max Shrink of the page's size. */
    val swingReach: Float get() = if (dragShrink > 0f) (swingMax / dragShrink).coerceAtMost(0.99f) else 0f

    /** The swing's starting speed (drag progress a second) for a fling at [velocity], held to what peaks at [swingReach]. */
    fun swingVelocity(velocity: Float): Float {
        val physics = springBack.value.resolved
        val omega = sqrt(physics.stiffness / physics.mass) / slowdown
        // a spring leaving rest at speed v peaks near v / (ωe) when critically damped
        return (velocity * swingStrength).coerceAtMost(swingReach * omega * E.toFloat())
    }

    fun swingSpring(): SpringSpec<Float> = springBack.spec(0.0005f, slowdown)

    private fun <T> timeSpring(duration: Float, bounce: Float, visibilityThreshold: T): SpringSpec<T> =
        spring(1f - bounce, (2 * PI / (duration * slowdown)).pow(2).toFloat(), visibilityThreshold)
}

enum class GrowCurve(val label: String) { Spring("Spring"), EaseOut("Ease Out"), Linear("Linear") }
