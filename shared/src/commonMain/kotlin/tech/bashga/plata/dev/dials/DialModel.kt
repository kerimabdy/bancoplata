package tech.bashga.plata.dev.dials

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A port of DialKit (github.com/mikelikesdesign/dialkit-ios): panels of live controls for tuning values on device.
 * Make a [DialPanel], add controls to it, and read them through `by`; the drawer `DialRoot` draws every panel.
 */
object DialStore {
    val panels = mutableStateListOf<DialPanel>()

    // here rather than in the button, which leaves the screen while the drawer is open
    internal var fabCenter by mutableStateOf<Offset?>(null)
}

sealed class DialControl(val key: String, val label: String)

/** A panel or a folder in one: the controls are listed in the order they're added. */
sealed class DialContainer(key: String, label: String) : DialControl(key, label) {
    internal abstract val panel: DialPanel
    val controls = mutableStateListOf<DialControl>()

    internal fun <C : DialControl> add(control: C): C = control.also { controls += it }

    fun slider(
        key: String,
        default: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float = dialInferredStep(range),
        unit: String? = null,
        label: String = dialFormattedLabel(key),
    ): SliderDial = add(SliderDial(panel, key, label, default, range, step, unit))

    fun toggle(key: String, default: Boolean, label: String = dialFormattedLabel(key)): ToggleDial =
        add(ToggleDial(panel, key, label, default))

    fun select(key: String, options: List<DialOption>, default: String = options.first().value, label: String = dialFormattedLabel(key)): SelectDial =
        add(SelectDial(panel, key, label, options, default))

    fun spring(key: String, default: DialSpring = DialSpring.Default, label: String = dialFormattedLabel(key)): SpringDial =
        add(SpringDial(panel, key, label, default))

    fun folder(key: String, collapsed: Boolean = false, label: String = dialFormattedLabel(key)): DialFolder =
        add(DialFolder(panel, key, label, collapsed))

    fun action(key: String, label: String = dialFormattedLabel(key), onTrigger: () -> Unit): ActionDial =
        add(ActionDial(key, label, onTrigger))
}

class DialFolder internal constructor(override val panel: DialPanel, key: String, label: String, val collapsed: Boolean) :
    DialContainer(key, label)

/** A control holding a value, which presets save and restore. */
sealed interface ValueDial {
    val key: String
    fun snapshot(): Any
    fun restore(value: Any)
    fun json(): JsonElement
}

@Stable
class SliderDial internal constructor(
    private val panel: DialPanel,
    key: String,
    label: String,
    default: Float,
    val range: ClosedFloatingPointRange<Float>,
    val step: Float,
    val unit: String?,
) : DialControl(key, label), ValueDial, ReadOnlyProperty<Any?, Float> {
    var value by mutableFloatStateOf(dialRound(default, step, range))
        private set

    fun set(to: Float) {
        value = dialRound(to, step, range)
        panel.valuesChanged()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float = value

    override fun snapshot(): Any = value

    override fun restore(value: Any) {
        this.value = value as Float
    }

    override fun json(): JsonElement = JsonPrimitive(dialFormattedNumber(value, step).toDouble())
}

@Stable
class ToggleDial internal constructor(private val panel: DialPanel, key: String, label: String, default: Boolean) :
    DialControl(key, label), ValueDial, ReadOnlyProperty<Any?, Boolean> {
    var value by mutableStateOf(default)
        private set

    fun set(to: Boolean) {
        value = to
        panel.valuesChanged()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = value

    override fun snapshot(): Any = value

    override fun restore(value: Any) {
        this.value = value as Boolean
    }

    override fun json(): JsonElement = JsonPrimitive(value)
}

class DialOption(val value: String, val label: String = dialFormattedLabel(value))

@Stable
class SelectDial internal constructor(
    private val panel: DialPanel,
    key: String,
    label: String,
    val options: List<DialOption>,
    default: String,
) : DialControl(key, label), ValueDial, ReadOnlyProperty<Any?, String> {
    var value by mutableStateOf(default)
        private set

    fun set(to: String) {
        value = to
        panel.valuesChanged()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String = value

    override fun snapshot(): Any = value

    override fun restore(value: Any) {
        this.value = value as String
    }

    override fun json(): JsonElement = JsonPrimitive(value)
}

/** A spring by [Time] (SwiftUI's duration and bounce) or by [Physics] (stiffness, damping and mass). */
sealed interface DialSpring {
    data class Time(val duration: Float, val bounce: Float) : DialSpring

    data class Physics(val stiffness: Float, val damping: Float, val mass: Float) : DialSpring

    val resolved: ResolvedSpring
        get() = when (this) {
            is Time -> {
                val stiffness = (2 * PI / duration.coerceAtLeast(0.1f)).pow(2).toFloat()
                val ratio = 1f - bounce.coerceIn(0f, 1f)
                ResolvedSpring(stiffness, 2 * ratio * sqrt(stiffness), 1f)
            }
            is Physics -> ResolvedSpring(stiffness.coerceAtLeast(1f), damping.coerceAtLeast(1f), mass.coerceAtLeast(0.1f))
        }

    fun updatingTime(duration: Float? = null, bounce: Float? = null): DialSpring = when (this) {
        is Time -> Time(duration ?: this.duration, bounce ?: this.bounce)
        is Physics -> Time(duration ?: 0.3f, bounce ?: 0.2f)
    }

    fun updatingPhysics(stiffness: Float? = null, damping: Float? = null, mass: Float? = null): DialSpring = when (this) {
        is Time -> Physics(stiffness ?: 200f, damping ?: 25f, mass ?: 1f)
        is Physics -> Physics(stiffness ?: this.stiffness, damping ?: this.damping, mass ?: this.mass)
    }

    companion object {
        val Default: DialSpring = Time(0.35f, 0.24f)
    }
}

class ResolvedSpring(val stiffness: Float, val damping: Float, val mass: Float) {
    /** Compose springs have no mass: the same motion is a stiffness per unit mass at the same damping ratio. */
    val dampingRatio: Float get() = damping / (2 * sqrt(stiffness * mass))
}

@Stable
class SpringDial internal constructor(private val panel: DialPanel, key: String, label: String, default: DialSpring) :
    DialControl(key, label), ValueDial {
    var value by mutableStateOf(default)
        private set

    fun set(to: DialSpring) {
        value = to
        panel.valuesChanged()
    }

    /** This spring as a Compose spec; [slowdown] stretches it in time without changing its shape. */
    fun <T> spec(visibilityThreshold: T? = null, slowdown: Float = 1f): SpringSpec<T> {
        val physics = value.resolved
        return spring(physics.dampingRatio, physics.stiffness / physics.mass / (slowdown * slowdown), visibilityThreshold)
    }

    override fun snapshot(): Any = value

    override fun restore(value: Any) {
        this.value = value as DialSpring
    }

    override fun json(): JsonElement = when (val spring = value) {
        is DialSpring.Time -> buildJsonObject {
            put("time", buildJsonObject {
                put("bounce", dialFormattedNumber(spring.bounce, 0.01f).toDouble())
                put("duration", dialFormattedNumber(spring.duration, 0.001f).toDouble())
            })
        }
        is DialSpring.Physics -> buildJsonObject {
            put("physics", buildJsonObject {
                put("damping", dialFormattedNumber(spring.damping, 0.1f).toDouble())
                put("mass", dialFormattedNumber(spring.mass, 0.1f).toDouble())
                put("stiffness", dialFormattedNumber(spring.stiffness, 0.1f).toDouble())
            })
        }
    }
}

class ActionDial internal constructor(key: String, label: String, val trigger: () -> Unit) : DialControl(key, label)

class DialPreset internal constructor(val id: Int, val name: String, values: Map<String, Any>) {
    internal var values = values
}

/**
 * One panel of the drawer, registered as it's made. Its values start as "Version 1"; saving a version snapshots them,
 * and while a version is picked, changes go to it rather than to Version 1.
 */
class DialPanel(val name: String) : DialContainer(name, name) {
    override val panel: DialPanel get() = this

    val presets = mutableStateListOf<DialPreset>()
    var activePresetId by mutableStateOf<Int?>(null)
        private set
    private var baseValues: Map<String, Any> = emptyMap()
    private var nextPresetId = 1
    private var applying = false

    // folders open or shut by the user, kept across the drawer closing
    internal val expanded = mutableStateMapOf<String, Boolean>()

    init {
        DialStore.panels += this
    }

    val activePresetName: String get() = presets.firstOrNull { it.id == activePresetId }?.name ?: "Version 1"
    val nextPresetName: String get() = "Version ${presets.size + 2}"

    fun savePreset(name: String = nextPresetName) {
        leaveBase()
        val preset = DialPreset(nextPresetId++, name.trim().ifEmpty { nextPresetName }, snapshot())
        presets += preset
        activePresetId = preset.id
    }

    fun loadPreset(id: Int) {
        val preset = presets.firstOrNull { it.id == id } ?: return
        leaveBase()
        activePresetId = id
        restore(preset.values)
    }

    fun clearActivePreset() {
        activePresetId = null
        restore(baseValues)
    }

    fun deletePreset(id: Int) {
        presets.removeAll { it.id == id }
        if (activePresetId == id) clearActivePreset()
    }

    internal fun valuesChanged() {
        if (applying) return
        val active = presets.firstOrNull { it.id == activePresetId }
        if (active != null) active.values = snapshot() else baseValues = snapshot()
    }

    /** What DialKit's Copy puts on the clipboard: the values as JSON, asking for them to become the defaults. */
    fun copyInstructionText(): String =
        "Update the DialKit configuration for \"$name\" with these values:\n\n```json\n" +
            PrettyJson.encodeToString(JsonElement.serializer(), json(this)) +
            "\n```\n\nApply these values as the new defaults in the DialKit configuration."

    private fun json(container: DialContainer): JsonObject = JsonObject(
        container.controls.mapNotNull { control ->
            when (control) {
                is DialFolder -> control.key to json(control)
                is ValueDial -> control.key to control.json()
                else -> null
            }
        }.sortedBy { it.first }.toMap(),
    )

    private fun leaves(container: DialContainer = this, prefix: String = ""): List<Pair<String, ValueDial>> =
        container.controls.flatMap { control ->
            when (control) {
                is DialFolder -> leaves(control, "$prefix${control.key}.")
                is ValueDial -> listOf("$prefix${control.key}" to control)
                else -> emptyList()
            }
        }

    private fun snapshot(): Map<String, Any> = leaves().associate { (path, dial) -> path to dial.snapshot() }

    private fun restore(values: Map<String, Any>) {
        applying = true
        leaves().forEach { (path, dial) -> values[path]?.let(dial::restore) }
        applying = false
    }

    // Version 1 is the live values until another version is picked: keep them to come back to
    private fun leaveBase() {
        if (activePresetId == null) baseValues = snapshot()
    }
}

private val PrettyJson = Json { prettyPrint = true }

fun dialFormattedLabel(path: String): String {
    val token = path.substringAfterLast('.')
    return token
        .replace(Regex("([A-Z])"), " $1")
        .replace(Regex("[_-]+"), " ")
        .trim()
        .split(Regex("\\s+"))
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
}

fun dialInferredStep(range: ClosedFloatingPointRange<Float>): Float {
    val width = range.endInclusive - range.start
    return when {
        width <= 1f -> 0.01f
        width <= 10f -> 0.1f
        width <= 100f -> 1f
        else -> 10f
    }
}

internal fun dialStepPrecision(step: Float): Int {
    val safeStep = abs(step.toDouble())
    if (safeStep <= 0.0) return 0
    for (precision in 0..6) {
        val scaled = safeStep * 10.0.pow(precision)
        // a Float step is only exact to about 7 digits
        if (abs(round(scaled) - scaled) < 1e-5) return precision
    }
    return 6
}

internal fun dialRound(value: Float, step: Float, range: ClosedFloatingPointRange<Float>): Float {
    val safeStep = step.coerceAtLeast(0.000_001f)
    val stepped = round((value - range.start) / safeStep) * safeStep + range.start
    return dialFormattedNumber(stepped.coerceIn(range), safeStep).toFloat()
}

internal fun dialFormattedNumber(value: Float, step: Float): String {
    val precision = dialStepPrecision(step)
    val factor = 10.0.pow(precision).toLong()
    val scaled = (value.toDouble() * factor).roundToLong()
    val sign = if (scaled < 0) "-" else ""
    val whole = abs(scaled) / factor
    if (precision == 0) return "$sign$whole"
    return "$sign$whole." + (abs(scaled) % factor).toString().padStart(precision, '0')
}
