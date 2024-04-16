package co.touchlab.skie.phases.util

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.BackgroundPhase
import co.touchlab.skie.phases.ForegroundPhase
import co.touchlab.skie.phases.ScheduledPhase
import co.touchlab.skie.analytics.performance.SkiePerformanceAnalytics
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class SkiePhaseGroup<P : ScheduledPhase<C>, C : ScheduledPhase.Context>(
    defaultPhasesBuilder: MutableList<P>.(C) -> Unit,
) {

    private val modifications = mutableListOf(defaultPhasesBuilder)

    fun modify(modification: MutableList<P>.(C) -> Unit) {
        modifications.add(modification)
    }

    context(C)
    internal suspend fun List<P>.execute(kind: SkiePerformanceAnalytics.Kind) {
        this.forEach {
            if (it.isActive()) {
                context.skiePerformanceAnalyticsProducer.log(it::class.nameForLogger, kind) {
                    it.execute()
                }
            } else {
                context.skiePerformanceAnalyticsProducer.logSkipped(it::class.nameForLogger)
            }
        }
    }

    internal fun buildPhases(context: C): List<P> {
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

context(ScheduledPhase.Context)
inline fun <P, reified C : ScheduledPhase.Context> SkiePhaseGroup<P, C>.run(
    noinline contextFactory: () -> C,
) where P : ScheduledPhase<C>, P : BackgroundPhase<C> {
    run(C::class, contextFactory)
}

context(ScheduledPhase.Context)
fun <P, C : ScheduledPhase.Context> SkiePhaseGroup<P, C>.run(
    contextClass: KClass<C>,
    contextFactory: () -> C,
) where P : ScheduledPhase<C>, P : BackgroundPhase<C> {
    if (SkieConfigurationFlag.Build_ConcurrentSkieCompilation.isEnabled) {
        this@Context.launch {
            prepareAndExecute(contextClass, contextFactory, SkiePerformanceAnalytics.Kind.Background)
        }
    } else {
        runBlocking(contextClass, contextFactory)
    }
}

context(ScheduledPhase.Context)
inline fun <P, reified C : ScheduledPhase.Context> SkiePhaseGroup<P, C>.run(
    noinline contextFactory: () -> C,
): C where P : ScheduledPhase<C>, P : ForegroundPhase<C> =
    run(C::class, contextFactory)

context(ScheduledPhase.Context)
fun <P, C : ScheduledPhase.Context> SkiePhaseGroup<P, C>.run(
    contextClass: KClass<C>,
    contextFactory: () -> C,
): C where P : ScheduledPhase<C>, P : ForegroundPhase<C> =
    runBlocking(contextClass, contextFactory)

context(ScheduledPhase.Context)
private fun <P : ScheduledPhase<C>, C : ScheduledPhase.Context> SkiePhaseGroup<P, C>.runBlocking(contextClass: KClass<C>, contextFactory: () -> C): C =
    runBlocking {
        prepareAndExecute(contextClass, contextFactory, SkiePerformanceAnalytics.Kind.Foreground)
    }

context(ScheduledPhase.Context)
private suspend fun <P : ScheduledPhase<C>, C : ScheduledPhase.Context> SkiePhaseGroup<P, C>.prepareAndExecute(
    contextClass: KClass<C>,
    contextFactory: () -> C,
    kind: SkiePerformanceAnalytics.Kind,
): C {
    val phasesName = contextClass.nameForLogger.removeSuffix("Phase.Context") + "Phases"

    val (context, phases) = skiePerformanceAnalyticsProducer.log("Initialize$phasesName", kind) {
        val context = contextFactory()

        val phases = buildPhases(context)

        context to phases
    }

    with(context) {
        phases.execute(kind)
    }

    return context
}
