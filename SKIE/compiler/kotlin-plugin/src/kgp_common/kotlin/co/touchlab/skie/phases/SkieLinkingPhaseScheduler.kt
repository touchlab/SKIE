package co.touchlab.skie.phases

import co.touchlab.skie.swiftmodel.DefaultSwiftModelScope
import co.touchlab.skie.phases.apinotes.FinalApiNotesGenerationPhase
import co.touchlab.skie.phases.apinotes.SwiftCompilationApiNotesGenerationPhase
import co.touchlab.skie.phases.debug.DumpSwiftApiPhase
import co.touchlab.skie.phases.memberconflicts.FixCallableMembersConflictsPhase
import co.touchlab.skie.phases.memberconflicts.RemoveKonanManglingPhase
import co.touchlab.skie.phases.typeconflicts.AddForwardDeclarationsPhase
import co.touchlab.skie.phases.typeconflicts.AddTypeDefPhase
import co.touchlab.skie.phases.typeconflicts.RenameNestedTypesConflictingWithExternalTypesPhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictingWithKotlinModulePhase
import co.touchlab.skie.phases.typeconflicts.RenameTypesConflictsWithOtherTypesPhase
import co.touchlab.skie.phases.typeconflicts.RevertRenamingOfTypesConflictingWithExternalModulesPhase
import co.touchlab.skie.phases.typeconflicts.TemporarilyRenameTypesConflictingWithExternalModulesPhase
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.apinotes.GenerateBridgingTypeAliasesPhase
import co.touchlab.skie.phases.header.AddLambdaTypeArgumentErrorTypePhase
import co.touchlab.skie.phases.header.FixHeaderFilePropertyOrderingPhase
import co.touchlab.skie.phases.header.GenerateFakeObjCDependenciesPhase
import co.touchlab.skie.phases.swift.CompileSwiftPhase
import co.touchlab.skie.phases.swift.GenerateSwiftCodeFromSirPhase
import co.touchlab.skie.phases.swift.GenerateSwiftCodeUsingSwiftPoetPhase
import co.touchlab.skie.phases.swift.SwiftCacheSetupPhase
import co.touchlab.skie.phases.swift.WriteGeneratedSwiftCodeToDiskPhase
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import co.touchlab.skie.util.FrameworkLayout
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
