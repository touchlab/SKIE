package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.SkiePhase
import org.jetbrains.kotlin.config.CompilerConfigurationKey

// Final implementation must be an object
abstract class StatefulSkiePhase<C : SkiePhase.Context> : SkiePhase<C> {

    internal val key =
        object : CompilerConfigurationKey<StateHolder<C.() -> Unit>>("StatefulSkiePhase(${this::class.qualifiedName}) actions") {}

    context(C)
    override fun execute() {
        val state = context.getOrNull(key) ?: StateHolder()

        state.forEach {
            it(this@C)
        }
    }

    // Wrapper class avoids problem with configuration automatically returning immutable copy of the list
    internal class StateHolder<STATE> {

        private val actions = mutableListOf<STATE>()

        fun add(action: STATE) {
            actions.add(action)
        }

        fun forEach(action: (STATE) -> Unit) {
            actions.forEach(action)
        }
    }
}

fun <T : StatefulSkiePhase<C>, C : SkiePhase.Context> SkiePhase.Context.doInPhase(phase: T, action: C.() -> Unit) {
    val stateHolder = getOrCreate(phase.key) {
        StatefulSkiePhase.StateHolder()
    }

    stateHolder.add(action)
}
