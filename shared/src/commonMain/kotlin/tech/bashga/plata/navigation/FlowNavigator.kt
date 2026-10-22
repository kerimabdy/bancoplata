package tech.bashga.plata.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/** What a screen's controls can ask of the flow it's shown in. Screens never take navigation parameters. */
interface FlowNavigator {
    /** Primary action: the main CTA, or a row that leads on. */
    fun next()

    /** Back chevron or system back. */
    fun back()

    /** Close/Cancel/Done that leaves the flow. */
    fun close()
}

object NoOpFlowNavigator : FlowNavigator {
    override fun next() = Unit
    override fun back() = Unit
    override fun close() = Unit
}

val LocalFlowNavigator = staticCompositionLocalOf<FlowNavigator> { NoOpFlowNavigator }
