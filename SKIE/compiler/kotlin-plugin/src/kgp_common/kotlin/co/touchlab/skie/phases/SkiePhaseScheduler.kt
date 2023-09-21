package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.GenerateIrPhase
import co.touchlab.skie.phases.analytics.ClassExportAnalyticsPhase
import co.touchlab.skie.phases.analytics.KotlinIrAnalyticsPhase
import co.touchlab.skie.phases.analytics.performance.LogSkiePerformanceAnalyticsPhase
import co.touchlab.skie.phases.apinotes.ApiNotesGenerationPhase
import co.touchlab.skie.phases.apinotes.MoveBridgesToTopLevelPhase
import co.touchlab.skie.phases.debug.DumpSwiftApiPhase
import co.touchlab.skie.phases.features.defaultarguments.DefaultArgumentGenerator
import co.touchlab.skie.phases.features.enums.ExhaustiveEnumsGenerator
import co.touchlab.skie.phases.features.flow.FlowBridgingConfigurator
import co.touchlab.skie.phases.features.flow.FlowConversionConstructorsGenerator
import co.touchlab.skie.phases.features.flow.FlowMappingConfigurator
import co.touchlab.skie.phases.features.sealed.SealedInteropGenerator
import co.touchlab.skie.phases.features.suspend.SuspendGenerator
import co.touchlab.skie.phases.header.AddLambdaTypeArgumentErrorTypePhase
import co.touchlab.skie.phases.header.FixHeaderFilePropertyOrderingPhase
import co.touchlab.skie.phases.header.GenerateFakeObjCDependenciesPhase
import co.touchlab.skie.phases.memberconflicts.FixCallableMembersConflictsPhase
import co.touchlab.skie.phases.memberconflicts.RemoveKonanManglingPhase
import co.touchlab.skie.phases.other.DeclareMissingSymbolsPhase
import co.touchlab.skie.phases.other.DeleteSkieFrameworkContentPhase
import co.touchlab.skie.phases.other.DisableWildcardExportPhase
import co.touchlab.skie.phases.other.ExtraClassExportPhase
import co.touchlab.skie.phases.other.FixLibrariesShortNamePhase
import co.touchlab.skie.phases.other.VerifyMinOSVersionPhase
import co.touchlab.skie.phases.runtime.KotlinRuntimeHidingPhase
import co.touchlab.skie.phases.runtime.SwiftRuntimeGenerator
import co.touchlab.skie.phases.swift.CompileSwiftPhase
import co.touchlab.skie.phases.swift.GenerateSirFileCodePhase
import co.touchlab.skie.phases.swift.SwiftCacheSetupPhase
import co.touchlab.skie.phases.swift.WriteSirFileContentToDiskPhase
import co.touchlab.skie.phases.header.AddForwardDeclarationsPhase
import co.touchlab.skie.phases.header.AddTypeDefPhase
import co.touchlab.skie.phases.typeconflicts.RenameNestedTypesConflictingWithExternalTypesPhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictingWithKeywordsPhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictingWithKotlinModulePhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictsWithOtherTypesPhase
import co.touchlab.skie.phases.typeconflicts.TemporarilyRenameTypesConflictingWithExternalModulesPhase
import kotlin.reflect.KClass

object SkiePhaseScheduler {

    fun runClassExportPhases(context: ClassExportPhase.Context) {
        listOf(
            FixLibrariesShortNamePhase,
            ClassExportAnalyticsPhase,
            ExtraClassExportPhase(context),
            VerifyMinOSVersionPhase,
        ).run(context)
    }

    fun runDescriptorModificationPhases(context: DescriptorModificationPhase.Context) {
        listOf(
            DefaultArgumentGenerator(context),
            SuspendGenerator(context),
        ).run(context)
    }

    fun runSymbolTablePhases(context: SymbolTablePhase.Context) {
        listOf(
            DeclareMissingSymbolsPhase,
        ).run(context)
    }

    fun runKotlinIrPhases(context: KotlinIrPhase.Context) {
        listOf(
            KotlinIrAnalyticsPhase,
            GenerateIrPhase,
        ).run(context)
    }

    fun runSirPhases(context: SirPhase.Context) {
        listOf(
            DumpSwiftApiPhase.BeforeApiNotes,

            RemoveKonanManglingPhase,
            RenameTypesConflictingWithKeywordsPhase,
            RenameNestedTypesConflictingWithExternalTypesPhase,
            RenameTypesConflictingWithKotlinModulePhase,
            KotlinRuntimeHidingPhase,

            SwiftRuntimeGenerator,
            ExtraClassExportPhase.FinalizePhase,
            DefaultArgumentGenerator.FinalizePhase,
            FlowBridgingConfigurator,
            FlowConversionConstructorsGenerator,
            FlowMappingConfigurator(context),
            SuspendGenerator.KotlinBridgeConfigurationPhase,
            ExhaustiveEnumsGenerator(context),
            SealedInteropGenerator(context),

            MoveBridgesToTopLevelPhase,
            RenameTypesConflictsWithOtherTypesPhase,
            FixCallableMembersConflictsPhase,
            SuspendGenerator.SwiftBridgeGeneratorPhase,

            TemporarilyRenameTypesConflictingWithExternalModulesPhase,

            DeleteSkieFrameworkContentPhase,
            FixHeaderFilePropertyOrderingPhase,
            AddLambdaTypeArgumentErrorTypePhase,
            ApiNotesGenerationPhase.ForSwiftCompilation,
            AddForwardDeclarationsPhase(context),
            AddTypeDefPhase(context),
            GenerateSirFileCodePhase,
            WriteSirFileContentToDiskPhase,
            GenerateFakeObjCDependenciesPhase,
            SwiftCacheSetupPhase,
            DisableWildcardExportPhase,
            CompileSwiftPhase(context),

            TemporarilyRenameTypesConflictingWithExternalModulesPhase.RevertPhase,
            ApiNotesGenerationPhase.ForFramework,

            DumpSwiftApiPhase.AfterApiNotes,

            LogSkiePerformanceAnalyticsPhase,
        ).run(context)
    }
}

private fun <P : SkiePhase<C>, C : SkiePhase.Context> List<P>.run(context: C) {
    with(context) {
        filter { it.isActive() }.forEach {
            context.skiePerformanceAnalyticsProducer.log(it::class.nameForLogger) {
                it.execute()
            }
        }
    }
}

private val KClass<*>.nameForLogger: String
    get() = qualifiedName
        ?.split(".")
        ?.dropWhile { !it.first().isUpperCase() }
        ?.joinToString(".")
        ?.takeUnless { it.isBlank() }
        ?: "<Unknown>"
