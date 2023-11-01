@file:Suppress("MemberVisibilityCanBePrivate", "RemoveExplicitTypeArguments", "UNUSED_ANONYMOUS_PARAMETER")

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
import co.touchlab.skie.phases.header.AddForwardDeclarationsPhase
import co.touchlab.skie.phases.header.AddLambdaTypeArgumentErrorTypePhase
import co.touchlab.skie.phases.header.AddTypeDefPhase
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
import co.touchlab.skie.phases.typeconflicts.RenameNestedKotlinTypesConflictingWithKeywordsPhase
import co.touchlab.skie.phases.typeconflicts.RenameNestedTypesConflictingWithExternalTypesPhase
import co.touchlab.skie.phases.typeconflicts.RenameSkieNamespacesConflictingWithKeywordsPhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictingWithKotlinModulePhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictsWithOtherTypesPhase
import co.touchlab.skie.phases.typeconflicts.TemporarilyRenameTypesConflictingWithExternalModulesPhase
import co.touchlab.skie.phases.util.SkiePhaseGroup
import co.touchlab.skie.util.addAll

class SkiePhaseScheduler {

    val classExportPhases = SkiePhaseGroup<ClassExportPhase, ClassExportPhase.Context> { context ->
        addAll(
            FixLibrariesShortNamePhase,
            ClassExportAnalyticsPhase,
            ExtraClassExportPhase(context),
            VerifyMinOSVersionPhase,
        )
    }

    val descriptorModificationPhases = SkiePhaseGroup<DescriptorModificationPhase, DescriptorModificationPhase.Context> { context ->
        addAll(
            DefaultArgumentGenerator(context),
            SuspendGenerator,
        )
    }

    val symbolTablePhases = SkiePhaseGroup<SymbolTablePhase, SymbolTablePhase.Context> { context ->
        addAll(
            DeclareMissingSymbolsPhase,
        )
    }

    val kotlinIrPhases = SkiePhaseGroup<KotlinIrPhase, KotlinIrPhase.Context> { context ->
        addAll(
            KotlinIrAnalyticsPhase,
            GenerateIrPhase,
        )
    }

    val sirPhases = SkiePhaseGroup<SirPhase, SirPhase.Context> { context ->
        addAll(
            DumpSwiftApiPhase.BeforeApiNotes,

            RemoveKonanManglingPhase,
            RenameNestedTypesConflictingWithExternalTypesPhase,
            RenameNestedKotlinTypesConflictingWithKeywordsPhase,
            RenameSkieNamespacesConflictingWithKeywordsPhase,
            RenameTypesConflictingWithKotlinModulePhase,
            KotlinRuntimeHidingPhase,

            SwiftRuntimeGenerator,
            ExtraClassExportPhase.FinalizePhase,
            DefaultArgumentGenerator.FinalizePhase,
            FlowBridgingConfigurator,
            FlowMappingConfigurator(context),
            SuspendGenerator.KotlinBridgeConfigurationPhase,
            ExhaustiveEnumsGenerator,
            SealedInteropGenerator(context),

            MoveBridgesToTopLevelPhase,
            RenameTypesConflictsWithOtherTypesPhase,
            FixCallableMembersConflictsPhase,

            FlowConversionConstructorsGenerator,
            ExhaustiveEnumsGenerator.MembersGeneratorPhase,
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
            DisableWildcardExportPhase,
            SwiftCacheSetupPhase,
            CompileSwiftPhase(context),

            TemporarilyRenameTypesConflictingWithExternalModulesPhase.RevertPhase,
            ApiNotesGenerationPhase.ForFramework,

            DumpSwiftApiPhase.AfterApiNotes,

            LogSkiePerformanceAnalyticsPhase,
        )
    }

    fun runClassExportPhases(context: ClassExportPhase.Context) {
        classExportPhases.run(context)
    }

    fun runDescriptorModificationPhases(context: DescriptorModificationPhase.Context) {
        descriptorModificationPhases.run(context)
    }

    fun runSymbolTablePhases(context: SymbolTablePhase.Context) {
        symbolTablePhases.run(context)
    }

    fun runKotlinIrPhases(context: KotlinIrPhase.Context) {
        kotlinIrPhases.run(context)
    }

    fun runSirPhases(context: SirPhase.Context) {
        sirPhases.run(context)
    }
}
