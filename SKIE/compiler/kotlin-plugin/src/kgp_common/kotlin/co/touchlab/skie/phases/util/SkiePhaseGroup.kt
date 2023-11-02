package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.SkiePhase
import kotlin.reflect.KClass

class SkiePhaseGroup<P : SkiePhase<C>, C : SkiePhase.Context>(
    defaultPhasesBuilder: MutableList<P>.(C) -> Unit,
) {

    private val modifications = mutableListOf(defaultPhasesBuilder)

    fun modify(modification: MutableList<P>.(C) -> Unit) {
        modifications.add(modification)
    }

    fun run(context: C) {
        with(context) {
            buildPhases(context)
                .forEach {
                    if (it.isActive()) {
                        context.skiePerformanceAnalyticsProducer.log(it::class.nameForLogger) {
                            it.execute()
                        }
                    } else {
                        context.skiePerformanceAnalyticsProducer.logSkipped(it::class.nameForLogger)
                    }
                }
        }
    }

    private fun buildPhases(context: C): List<P> {
        val phases = mutableListOf<P>()

        modifications.forEach { modification ->
            phases.modification(context)
        }

        return phases
    }
}

private val KClass<*>.nameForLogger: String
    get() = qualifiedName
        ?.split(".")
        ?.dropWhile { !it.first().isUpperCase() }
        ?.joinToString(".")
        ?.takeUnless { it.isBlank() }
        ?: "<Unknown>"
