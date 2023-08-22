package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.api.phases.CompileSwiftPhase
import co.touchlab.skie.api.phases.DisableWildcardExportPhase
import co.touchlab.skie.api.phases.FixHeaderFilePropertyOrderingPhase
import co.touchlab.skie.api.phases.FixNestedBridgedTypesPhase
import co.touchlab.skie.api.phases.GenerateFakeObjCDependenciesPhase
import co.touchlab.skie.api.phases.GenerateSwiftCodePhase
import co.touchlab.skie.api.phases.SkieModuleConfigurationPhase
import co.touchlab.skie.api.phases.SwiftCacheSetupPhase
import co.touchlab.skie.api.phases.apinotes.FinalApiNotesGenerationPhase
import co.touchlab.skie.api.phases.apinotes.SwiftCompilationApiNotesGenerationPhase
import co.touchlab.skie.api.phases.debug.DumpSwiftApiPhase
import co.touchlab.skie.api.phases.memberconflicts.FixCallableMembersConflictsPhase
import co.touchlab.skie.api.phases.memberconflicts.RemoveKonanManglingPhase
import co.touchlab.skie.api.phases.memberconflicts.RenameEnumRawValuePhase
import co.touchlab.skie.api.phases.typeconflicts.AddForwardDeclarationsPhase
import co.touchlab.skie.api.phases.typeconflicts.AddTypeDefPhase
import co.touchlab.skie.api.phases.typeconflicts.FixTypesConflictsPhase
import co.touchlab.skie.api.phases.typeconflicts.MangleTypesConflictingWithModulesPhase
import co.touchlab.skie.api.phases.typeconflicts.RenameInaccessibleNestedDeclarationsPhase
import co.touchlab.skie.api.phases.typeconflicts.RevertTypeManglingResultingFromConflictWithModulesPhase
import co.touchlab.skie.api.phases.util.ExternalTypesProvider
import co.touchlab.skie.api.phases.util.ObjCTypeRenderer
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.konan.target.AppleConfigurables

class SkieLinkingPhaseScheduler(
    private val skieContext: SkieContext,
    skieModule: DefaultSkieModule,
    framework: FrameworkLayout,
    descriptorProvider: DescriptorProvider,
    swiftModelScope: DefaultSwiftModelScope,
    builtinKotlinDeclarations: BuiltinDeclarations.Kotlin,
    configurables: AppleConfigurables,
    config: KonanConfig,
) {

    private val externalTypesProvider = ExternalTypesProvider(swiftModelScope)
    private val objCTypeRenderer = ObjCTypeRenderer()
    private val conflictingClassManglingContext = MangleTypesConflictingWithModulesPhase.Context()

    private val linkingPhases = listOf(
        DumpSwiftApiPhase.BeforeApiNotes(skieContext.skieConfiguration, skieContext, framework),
        RemoveKonanManglingPhase(skieModule, descriptorProvider),
        RenameEnumRawValuePhase(skieModule, descriptorProvider),
        FixCallableMembersConflictsPhase(skieModule, descriptorProvider),
        FixTypesConflictsPhase(skieModule, descriptorProvider, builtinKotlinDeclarations, framework),
        FixNestedBridgedTypesPhase(skieModule, descriptorProvider),
        FixHeaderFilePropertyOrderingPhase(framework.kotlinHeader),
        RenameInaccessibleNestedDeclarationsPhase(skieModule, externalTypesProvider),
        MangleTypesConflictingWithModulesPhase(skieModule, externalTypesProvider, conflictingClassManglingContext),
        SkieModuleConfigurationPhase(skieModule, swiftModelScope),
        SwiftCompilationApiNotesGenerationPhase(swiftModelScope, objCTypeRenderer, descriptorProvider, framework, skieContext.skieBuildDirectory),
        AddForwardDeclarationsPhase(framework.kotlinHeader, objCTypeRenderer),
        AddTypeDefPhase(framework.kotlinHeader, objCTypeRenderer),
        DisableWildcardExportPhase(skieContext, framework),
        GenerateSwiftCodePhase(skieContext, skieModule, swiftModelScope, framework),
        GenerateFakeObjCDependenciesPhase(externalTypesProvider, skieContext.skieDirectories.buildDirectory),
        SwiftCacheSetupPhase(skieContext, framework),
        CompileSwiftPhase(skieContext, framework, configurables, config),
        RevertTypeManglingResultingFromConflictWithModulesPhase(skieModule, conflictingClassManglingContext),
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
