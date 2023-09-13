package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.SkiePhase
import org.jetbrains.kotlin.config.CompilerConfigurationKey

// Final implementation must be an object
abstract class StatefulSkiePhase<C : SkiePhase.Context> : SkiePhase<C> {

    internal val key = object : CompilerConfigurationKey<MutableList<C.() -> Unit>>("StatefulSkiePhase(${this::class.qualifiedName}) actions") {}

    context(C)
    override fun execute() {
        val actions = context.getOrNull(key) ?: emptyList()

        actions.forEach {
            it(this@C)
        }
    }
}

fun <T : StatefulSkiePhase<C>, C : SkiePhase.Context> SkiePhase.Context.doInPhase(phase: T, action: C.() -> Unit) {
    val actions = getOrNull(phase.key) ?: mutableListOf<C.() -> Unit>().also { put(phase.key, it) }

    actions.add(action)
}

abstract class StatefulSirPhase : SirPhase, StatefulSkiePhase<SirPhase.Context>()
