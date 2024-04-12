@file:Suppress("MemberVisibilityCanBePrivate", "RemoveExplicitTypeArguments", "UNUSED_ANONYMOUS_PARAMETER")

package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.GenerateIrPhase
import co.touchlab.skie.phases.analytics.ClassExportAnalyticsPhase
import co.touchlab.skie.phases.analytics.KotlinIrAnalyticsPhase
import co.touchlab.skie.phases.analytics.performance.LogSkiePerformanceAnalyticsPhase
import co.touchlab.skie.phases.apinotes.ApiNotesGenerationPhase
import co.touchlab.skie.phases.apinotes.MoveBridgesToTopLevelPhase
import co.touchlab.skie.phases.debug.DumpSwiftApiPhase
import co.touchlab.skie.phases.debug.VerifyDescriptorProviderConsistencyPhase
import co.touchlab.skie.phases.features.defaultarguments.DefaultArgumentGenerator
import co.touchlab.skie.phases.features.defaultarguments.RemoveConflictingDefaultArgumentOverloadsPhase
import co.touchlab.skie.phases.features.enums.EnumEntryRenamingPhase
import co.touchlab.skie.phases.features.enums.ExhaustiveEnumsGenerator
import co.touchlab.skie.phases.features.flow.ConvertFlowsPhase
import co.touchlab.skie.phases.features.flow.FlowBridgingConfigurationPhase
import co.touchlab.skie.phases.features.flow.FlowConversionConstructorsGenerator
import co.touchlab.skie.phases.features.flow.UnifyFlowConfigurationForOverridesPhase
import co.touchlab.skie.phases.features.functions.FileScopeConvertor
import co.touchlab.skie.phases.features.sealed.SealedInteropGenerator
import co.touchlab.skie.phases.features.suspend.SuspendGenerator
import co.touchlab.skie.phases.header.AddTypeDefPhase
import co.touchlab.skie.phases.header.DeclareSkieErrorTypesPhase
import co.touchlab.skie.phases.header.FixForwardDeclarationsPhase
import co.touchlab.skie.phases.header.FixHeaderFilePropertyOrderingPhase
import co.touchlab.skie.phases.header.GenerateFakeObjCDependenciesPhase
import co.touchlab.skie.phases.kir.CreateExposedKirTypesPhase
import co.touchlab.skie.phases.kir.CreateKirMembersPhase
import co.touchlab.skie.phases.memberconflicts.RenameCallableDeclarationsConflictingWithTypeDeclarationsPhase
import co.touchlab.skie.phases.memberconflicts.RenameConflictingCallableDeclarationsPhase
import co.touchlab.skie.phases.memberconflicts.RenameParametersNamedSelfPhase
import co.touchlab.skie.phases.oir.ConfigureCInteropFrameworkNameForPlatformTypesPhase
import co.touchlab.skie.phases.oir.ConfigureExternalOirTypesBridgingPhase
import co.touchlab.skie.phases.oir.CreateExternalOirTypesPhase
import co.touchlab.skie.phases.oir.CreateFakeObjCConstructorsPhase
import co.touchlab.skie.phases.oir.CreateKotlinOirTypesPhase
import co.touchlab.skie.phases.oir.CreateOirMembersPhase
import co.touchlab.skie.phases.oir.FixOirFunctionSignaturesForApiNotesPhase
import co.touchlab.skie.phases.oir.InitializeOirSuperTypesPhase
import co.touchlab.skie.phases.other.AddAvailabilityBasedDeprecationLevelPhase
import co.touchlab.skie.phases.other.AddAvailabilityToAsyncFunctionsPhase
import co.touchlab.skie.phases.other.AddFoundationImportsPhase
import co.touchlab.skie.phases.other.AwaitAllBackgroundJobsPhase
import co.touchlab.skie.phases.other.ConfigureSwiftSpecificLinkerArgsPhase
import co.touchlab.skie.phases.other.DeclareMissingSymbolsPhase
import co.touchlab.skie.phases.other.DeleteSkieFrameworkContentPhase
import co.touchlab.skie.phases.other.ExtraClassExportPhase
import co.touchlab.skie.phases.other.FixDuplicatedOverriddenFunctionsPhase
import co.touchlab.skie.phases.other.FixLibrariesShortNamePhase
import co.touchlab.skie.phases.other.GenerateModulemapFilePhase
import co.touchlab.skie.phases.other.LinkObjectFilesPhase
import co.touchlab.skie.phases.other.LoadCustomSwiftSourceFilesPhase
import co.touchlab.skie.phases.other.ProcessReportedMessagesPhase
import co.touchlab.skie.phases.other.VerifyMinOSVersionPhase
import co.touchlab.skie.phases.other.VerifyModuleNamePhase
import co.touchlab.skie.phases.other.VerifyNoBitcodeEmbeddingPhase
import co.touchlab.skie.phases.runtime.ConfigureStableNameTypeAliasesForKotlinRuntimePhase
import co.touchlab.skie.phases.runtime.KotlinRuntimeHidingPhase
import co.touchlab.skie.phases.runtime.SwiftRuntimeGenerator
import co.touchlab.skie.phases.sir.member.CreateAsyncSirFunctionsPhase
import co.touchlab.skie.phases.sir.member.CreateSirMembersPhase
import co.touchlab.skie.phases.sir.member.InitializeSirMembersCachePhase
import co.touchlab.skie.phases.sir.member.InitializeSirOverridesPhase
import co.touchlab.skie.phases.sir.member.StripKonanCallableDeclarationManglingPhase
import co.touchlab.skie.phases.sir.type.CreateExternalSirTypesPhase
import co.touchlab.skie.phases.sir.type.CreateKotlinSirExtensionsPhase
import co.touchlab.skie.phases.sir.type.CreateKotlinSirTypesPhase
import co.touchlab.skie.phases.sir.type.CreateStableNameTypeAliasesPhase
import co.touchlab.skie.phases.sir.type.FixNamesOfInaccessibleNestedClassesPhase
import co.touchlab.skie.phases.sir.type.InitializeSirSuperTypesPhase
import co.touchlab.skie.phases.swift.CompileSwiftPhase
import co.touchlab.skie.phases.swift.ConvertSirIrFilesToSourceFilesPhase
import co.touchlab.skie.phases.swift.ConvertSirSourceFilesToCompilableFilesPhase
import co.touchlab.skie.phases.swift.SwiftKotlinFrameworkCacheSetupPhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictingWithKeywordsPhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictingWithKotlinModulePhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictsWithOtherTypesPhase
import co.touchlab.skie.phases.typeconflicts.TemporarilyRenameTypesConflictingWithExternalModulesPhase
import co.touchlab.skie.phases.util.SkiePhaseGroup
import co.touchlab.skie.phases.util.run
import co.touchlab.skie.util.addAll

class SkiePhaseScheduler {

    val classExportPhases = SkiePhaseGroup<ClassExportPhase, ClassExportPhase.Context> { context ->
        addAll(
            VerifyModuleNamePhase,
            FixLibrariesShortNamePhase,
            ClassExportAnalyticsPhase,
            ExtraClassExportPhase(context),
            VerifyMinOSVersionPhase,
            VerifyNoBitcodeEmbeddingPhase,
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

    val descriptorConversionPhases = SkiePhaseGroup<DescriptorConversionPhase, DescriptorConversionPhase.Context> { context ->
        addAll(
            VerifyDescriptorProviderConsistencyPhase,

            CreateExposedKirTypesPhase(context),
            CreateKirMembersPhase(context),

            // Flows
            UnifyFlowConfigurationForOverridesPhase(context),
            SuspendGenerator.FlowMappingConfigurationPhase,
            ConvertFlowsPhase(context),

            ExtraClassExportPhase.HideExportFunctionsInitPhase,

            DefaultArgumentGenerator.RegisterOverloadsPhase,
            DefaultArgumentGenerator.RemoveManglingOfOverloadsInitPhase,

            SuspendGenerator.KotlinBridgingFunctionVisibilityConfigurationInitPhase,
            SuspendGenerator.SwiftBridgeGeneratorInitPhase,
        )
    }

    val sirPhases = SkiePhaseGroup<SirPhase, SirPhase.Context> { context ->
        addAll(
            // Debug(before)

            DumpSwiftApiPhase.BeforeApiNotes,

            // IR Setup

            CreateKotlinOirTypesPhase(context),
            CreateExternalOirTypesPhase,
            InitializeOirSuperTypesPhase,

            CreateOirMembersPhase(context),
            ConfigureCInteropFrameworkNameForPlatformTypesPhase,

            CreateKotlinSirTypesPhase,
            CreateKotlinSirExtensionsPhase,
            CreateExternalSirTypesPhase,
            InitializeSirSuperTypesPhase,
            ConfigureExternalOirTypesBridgingPhase(context),

            ConfigureStableNameTypeAliasesForKotlinRuntimePhase,
            CreateStableNameTypeAliasesPhase,

            CreateSirMembersPhase(context),
            CreateAsyncSirFunctionsPhase,
            InitializeSirOverridesPhase,
            InitializeSirMembersCachePhase,
            StripKonanCallableDeclarationManglingPhase,

            FixNamesOfInaccessibleNestedClassesPhase,

            RenameTypesConflictingWithKeywordsPhase,
            RenameTypesConflictingWithKotlinModulePhase,

            KotlinRuntimeHidingPhase,
            SwiftRuntimeGenerator,
            LoadCustomSwiftSourceFilesPhase,

            RenameConflictingCallableDeclarationsPhase(),

            // Features

            ExtraClassExportPhase.HideExportFunctionsFinalizePhase,

            DefaultArgumentGenerator.RemoveManglingOfOverloadsFinalizePhase,
            RemoveConflictingDefaultArgumentOverloadsPhase,

            SuspendGenerator.KotlinBridgingFunctionVisibilityConfigurationFinalizePhase,
            SuspendGenerator.SwiftBridgeGeneratorFinalizePhase,

            FlowBridgingConfigurationPhase,
            FlowConversionConstructorsGenerator(context),

            EnumEntryRenamingPhase,
            ExhaustiveEnumsGenerator,
            SealedInteropGenerator(context),

            FileScopeConvertor(context),

            ExhaustiveEnumsGenerator.NestedTypeDeclarationsPhase,

            // IR finalization

            AddAvailabilityBasedDeprecationLevelPhase,
            AddAvailabilityToAsyncFunctionsPhase,
            MoveBridgesToTopLevelPhase,
            RenameTypesConflictingWithKeywordsPhase,
            RenameTypesConflictingWithKotlinModulePhase,
            RenameTypesConflictsWithOtherTypesPhase,
            RenameCallableDeclarationsConflictingWithTypeDeclarationsPhase,
            RenameParametersNamedSelfPhase,
            RenameConflictingCallableDeclarationsPhase(),
            FixDuplicatedOverriddenFunctionsPhase,
            TemporarilyRenameTypesConflictingWithExternalModulesPhase,
            FixOirFunctionSignaturesForApiNotesPhase(context),
            CreateFakeObjCConstructorsPhase,
            AddFoundationImportsPhase,

            // Compilation

            DeleteSkieFrameworkContentPhase,
            FixHeaderFilePropertyOrderingPhase,
            DeclareSkieErrorTypesPhase,
            ApiNotesGenerationPhase.ForSwiftCompilation,
            FixForwardDeclarationsPhase(context),
            AddTypeDefPhase(context),
            ConvertSirIrFilesToSourceFilesPhase,
            ConvertSirSourceFilesToCompilableFilesPhase,
            GenerateFakeObjCDependenciesPhase,
            GenerateModulemapFilePhase.ForSwiftCompilation,
            SwiftKotlinFrameworkCacheSetupPhase,
            CompileSwiftPhase(context),
            TemporarilyRenameTypesConflictingWithExternalModulesPhase.RevertPhase,
            GenerateModulemapFilePhase.ForFramework,
            ApiNotesGenerationPhase.ForFramework,

            // Debug(after)

            DumpSwiftApiPhase.AfterApiNotes,
        )
    }

    val linkPhases = SkiePhaseGroup<LinkPhase, LinkPhase.Context> { context ->
        addAll(
            ConfigureSwiftSpecificLinkerArgsPhase,
            AwaitAllBackgroundJobsPhase,
            LinkObjectFilesPhase,
            ProcessReportedMessagesPhase,
            LogSkiePerformanceAnalyticsPhase,
        )
    }

    context(ScheduledPhase.Context)
    fun runClassExportPhases(contextFactory: () -> ClassExportPhase.Context) {
        classExportPhases.run(contextFactory)
    }

    context(ScheduledPhase.Context)
    fun runDescriptorModificationPhases(contextFactory: () -> DescriptorModificationPhase.Context) {
        descriptorModificationPhases.run(contextFactory)
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
    fun runDescriptorConversionPhases(contextFactory: () -> DescriptorConversionPhase.Context) {
        descriptorConversionPhases.run(contextFactory)
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
