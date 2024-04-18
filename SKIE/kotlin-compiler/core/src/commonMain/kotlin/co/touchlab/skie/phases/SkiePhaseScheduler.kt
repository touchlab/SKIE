package co.touchlab.skie.phases

import co.touchlab.skie.phases.util.SkiePhaseGroup
import co.touchlab.skie.phases.util.run

interface SkiePhaseScheduler {

    val classExportPhases: SkiePhaseGroup<ClassExportPhase, ClassExportPhase.Context>

    val frontendIrPhases: SkiePhaseGroup<FrontendIrPhase, FrontendIrPhase.Context>

    val symbolTablePhases: SkiePhaseGroup<SymbolTablePhase, SymbolTablePhase.Context>

    val kotlinIrPhases: SkiePhaseGroup<KotlinIrPhase, KotlinIrPhase.Context>

    val kirPhases: SkiePhaseGroup<KirPhase, KirPhase.Context>

    val sirPhases: SkiePhaseGroup<SirPhase, SirPhase.Context>

    val linkPhases: SkiePhaseGroup<LinkPhase, LinkPhase.Context>

    context(ScheduledPhase.Context)
    fun runClassExportPhases(contextFactory: () -> ClassExportPhase.Context) {
        classExportPhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runFrontendIrPhases(contextFactory: () -> FrontendIrPhase.Context) {
        frontendIrPhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runSymbolTablePhases(contextFactory: () -> SymbolTablePhase.Context) {
        symbolTablePhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runKotlinIrPhases(contextFactory: () -> KotlinIrPhase.Context) {
        kotlinIrPhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runKirPhases(contextFactory: () -> KirPhase.Context) {
        kirPhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runSirPhases(contextFactory: () -> SirPhase.Context) {
        sirPhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runLinkPhases(contextFactory: () -> LinkPhase.Context) {
        linkPhases.run(contextFactory)
    }
}
