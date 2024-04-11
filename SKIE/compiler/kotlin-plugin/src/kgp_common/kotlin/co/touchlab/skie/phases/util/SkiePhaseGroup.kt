package co.touchlab.skie.phases.util

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SkiePhase
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class SkiePhaseGroup<P : SkiePhase<C>, C : SkiePhase.Context>(
    defaultPhasesBuilder: MutableList<P>.(C) -> Unit,
) {

    private val modifications = mutableListOf(defaultPhasesBuilder)

    fun modify(modification: MutableList<P>.(C) -> Unit) {
        modifications.add(modification)
    }

    context(SkiePhase.Context)
    inline fun <reified RC : C> run(noinline contextFactory: () -> RC): RC =
        run(RC::class, contextFactory)

    context(SkiePhase.Context)
    fun <RC : C> run(contextClass: KClass<RC>, contextFactory: () -> RC): RC =
        runBlocking {
            prepareAndExecute(contextClass, contextFactory)
        }

    context(SkiePhase.Context)
    inline fun <reified RC : C> launch(noinline contextFactory: () -> RC) =
        launch(RC::class, contextFactory)

    context(SkiePhase.Context)
    fun <RC : C> launch(contextClass: KClass<RC>, contextFactory: () -> RC) {
        if (SkieConfigurationFlag.Build_ConcurrentSkieCompilation.isEnabled) {
            this@Context.launch {
                prepareAndExecute(contextClass, contextFactory)
            }
        } else {
            run(contextClass, contextFactory)
        }
    }

    context(SkiePhase.Context)
    private suspend fun <RC : C> prepareAndExecute(contextClass: KClass<RC>, contextFactory: () -> RC): RC {
        val phasesName = contextClass.nameForLogger.removeSuffix("Phase.Context") + "Phases"

        val (context, phases) = skiePerformanceAnalyticsProducer.log("Initialize$phasesName") {
            val context = contextFactory()

            val phases = buildPhases(context)

            context to phases
        }

        with(context) {
            phases.execute()
        }

        return context
    }

    context(C)
    private suspend fun List<P>.execute() {
        this.forEach {
            if (it.isActive()) {
                context.skiePerformanceAnalyticsProducer.log(it::class.nameForLogger) {
                    it.execute()
                }
            } else {
                context.skiePerformanceAnalyticsProducer.logSkipped(it::class.nameForLogger)
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
