package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.phases.ApiNotesGenerationPhase
import co.touchlab.skie.api.phases.FixClassesConflictsPhase
import co.touchlab.skie.api.phases.FixHeaderFilePropertyOrderingPhase
import co.touchlab.skie.api.phases.FixNestedBridgedTypesPhase
import co.touchlab.skie.api.phases.SkieModuleConfigurationPhase
import co.touchlab.skie.api.phases.debug.DumpSwiftApiPhase
import co.touchlab.skie.api.phases.memberconflicts.FixCallableMembersConflictsPhase
import co.touchlab.skie.api.phases.memberconflicts.RemoveKonanManglingPhase
import co.touchlab.skie.api.phases.memberconflicts.RenameEnumRawValuePhase
import co.touchlab.skie.api.phases.typeconflicts.AddForwardDeclarationsPhase
import co.touchlab.skie.api.phases.typeconflicts.AddTypeDefPhase
import co.touchlab.skie.api.phases.typeconflicts.ObjCTypeRenderer
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.common.CommonBackendContext

class SkieLinkingPhaseScheduler(
    private val skieContext: SkieContext,
    skieModule: DefaultSkieModule,
    context: CommonBackendContext,
    framework: FrameworkLayout,
    swiftModelScope: MutableSwiftModelScope,
    builtinKotlinDeclarations: BuiltinDeclarations.Kotlin,
) {

    private val objCTypeRenderer = ObjCTypeRenderer()

    private val linkingPhases = listOf(
        DumpSwiftApiPhase(DumpSwiftApiPoint.BeforeApiNotes, context, framework),
        RemoveKonanManglingPhase(skieModule, context.descriptorProvider),
        RenameEnumRawValuePhase(skieModule, context.descriptorProvider),
        FixCallableMembersConflictsPhase(skieModule, context.descriptorProvider),
        FixClassesConflictsPhase(skieModule, context.descriptorProvider, builtinKotlinDeclarations, framework),
        FixNestedBridgedTypesPhase(skieModule, context.descriptorProvider),
        FixHeaderFilePropertyOrderingPhase(framework.kotlinHeader),
        SkieModuleConfigurationPhase(skieModule, swiftModelScope),
        ApiNotesGenerationPhase(swiftModelScope, objCTypeRenderer, context, framework),
        AddForwardDeclarationsPhase(framework.kotlinHeader, objCTypeRenderer),
        AddTypeDefPhase(framework.kotlinHeader, objCTypeRenderer),
        DumpSwiftApiPhase(DumpSwiftApiPoint.AfterApiNotes, context, framework),
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
