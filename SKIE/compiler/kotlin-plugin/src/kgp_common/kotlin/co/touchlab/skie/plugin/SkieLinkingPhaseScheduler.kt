package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.api.phases.AddLambdaTypeArgumentErrorTypePhase
import co.touchlab.skie.api.phases.CompileSwiftPhase
import co.touchlab.skie.api.phases.DisableWildcardExportPhase
import co.touchlab.skie.api.phases.FixHeaderFilePropertyOrderingPhase
import co.touchlab.skie.api.phases.GenerateBridgingTypeAliasesPhase
import co.touchlab.skie.api.phases.GenerateFakeObjCDependenciesPhase
import co.touchlab.skie.api.phases.GenerateSwiftCodeFromSirPhase
import co.touchlab.skie.api.phases.GenerateSwiftCodeUsingSwiftPoetPhase
import co.touchlab.skie.api.phases.SkieModuleConfigurationPhase
import co.touchlab.skie.api.phases.SwiftCacheSetupPhase
import co.touchlab.skie.api.phases.WriteGeneratedSwiftCodeToDiskPhase
import co.touchlab.skie.api.phases.apinotes.FinalApiNotesGenerationPhase
import co.touchlab.skie.api.phases.apinotes.SwiftCompilationApiNotesGenerationPhase
import co.touchlab.skie.api.phases.debug.DumpSwiftApiPhase
import co.touchlab.skie.api.phases.memberconflicts.FixCallableMembersConflictsPhase
import co.touchlab.skie.api.phases.memberconflicts.RemoveKonanManglingPhase
import co.touchlab.skie.api.phases.typeconflicts.AddForwardDeclarationsPhase
import co.touchlab.skie.api.phases.typeconflicts.AddTypeDefPhase
import co.touchlab.skie.api.phases.typeconflicts.RenameNestedTypesConflictingWithExternalTypesPhase
import co.touchlab.skie.api.phases.typeconflicts.RenameTypesConflictingWithKotlinModulePhase
import co.touchlab.skie.api.phases.typeconflicts.RenameTypesConflictsWithOtherTypesPhase
import co.touchlab.skie.api.phases.typeconflicts.RevertRenamingOfTypesConflictingWithExternalModulesPhase
import co.touchlab.skie.api.phases.typeconflicts.TemporarilyRenameTypesConflictingWithExternalModulesPhase
import co.touchlab.skie.api.phases.util.ObjCTypeRenderer
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.sir.SirProvider
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.konan.target.AppleConfigurables

class SkieLinkingPhaseScheduler(
    private val skieContext: SkieContext,
    skieModule: DefaultSkieModule,
    framework: FrameworkLayout,
    descriptorProvider: DescriptorProvider,
    sirProvider: SirProvider,
    swiftModelScope: DefaultSwiftModelScope,
    configurables: AppleConfigurables,
    config: KonanConfig,
) {

    private val objCTypeRenderer = ObjCTypeRenderer()
    private val conflictingClassManglingContext = TemporarilyRenameTypesConflictingWithExternalModulesPhase.Context()

    private val linkingPhases = listOf(
        DumpSwiftApiPhase.BeforeApiNotes(skieContext.skieConfiguration, skieContext, framework),
        RemoveKonanManglingPhase(skieModule, descriptorProvider),
        RenameNestedTypesConflictingWithExternalTypesPhase(skieModule),
        RenameTypesConflictingWithKotlinModulePhase(skieModule, skieContext.reporter),
        RenameTypesConflictsWithOtherTypesPhase(skieModule),
        FixCallableMembersConflictsPhase(skieModule, descriptorProvider),
        TemporarilyRenameTypesConflictingWithExternalModulesPhase(skieModule, conflictingClassManglingContext),
        FixHeaderFilePropertyOrderingPhase(framework.kotlinHeader),
        AddLambdaTypeArgumentErrorTypePhase(framework.kotlinHeader),
        GenerateBridgingTypeAliasesPhase(skieModule),
        SkieModuleConfigurationPhase(skieModule, swiftModelScope),
        SwiftCompilationApiNotesGenerationPhase(
            swiftModelScope,
            objCTypeRenderer,
            descriptorProvider,
            framework,
            skieContext.skieBuildDirectory,
        ),
        AddForwardDeclarationsPhase(framework.kotlinHeader, objCTypeRenderer),
        AddTypeDefPhase(framework.kotlinHeader, objCTypeRenderer),
        DisableWildcardExportPhase(skieContext, framework),
        GenerateSwiftCodeFromSirPhase(skieModule, sirProvider),
        GenerateSwiftCodeUsingSwiftPoetPhase(skieModule, swiftModelScope),
        WriteGeneratedSwiftCodeToDiskPhase(skieContext, sirProvider),
        GenerateFakeObjCDependenciesPhase(sirProvider, skieContext.skieDirectories.buildDirectory),
        SwiftCacheSetupPhase(skieContext, framework),
        CompileSwiftPhase(skieContext, framework, configurables, config),
        RevertRenamingOfTypesConflictingWithExternalModulesPhase(skieModule, conflictingClassManglingContext),
        SkieModuleConfigurationPhase(skieModule, swiftModelScope),
        FinalApiNotesGenerationPhase(swiftModelScope, objCTypeRenderer, descriptorProvider, framework),
        DumpSwiftApiPhase.AfterApiNotes(skieContext.skieConfiguration, skieContext, framework),
    )

    fun runLinkingPhases() {
        linkingPhases
            .filter { it.isActive }
            .forEach {
                skieContext.skiePerformanceAnalyticsProducer.log("Linking Phase: ${it::class.simpleName}") {
                    it.execute()
                }
            }
    }
}
