@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.context.ClassExportPhaseContext
import co.touchlab.skie.context.DescriptorModificationPhaseContext
import co.touchlab.skie.context.MainSkieContext
import co.touchlab.skie.context.SirPhaseContext
import co.touchlab.skie.context.SymbolTablePhaseContext
import co.touchlab.skie.phases.SkiePhaseScheduler
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.ir.util.SymbolTable

internal object EntrypointUtils {

    fun runClassExportPhases(
        mainSkieContext: MainSkieContext,
        produceObjCExportInterface: () -> ObjCExportedInterface,
    ) {
        val classExportPhaseContext = ClassExportPhaseContext(mainSkieContext)
        SkiePhaseScheduler.runClassExportPhases(classExportPhaseContext)

        val updatedExportedInterface = produceObjCExportInterface()
        mainSkieContext.reloadDescriptorProvider(updatedExportedInterface)
    }

    fun runDescriptorModificationPhases(
        mainSkieContext: MainSkieContext,
        produceObjCExportInterface: () -> ObjCExportedInterface,
    ): ObjCExportedInterface {
        val descriptorModificationPhaseContext = DescriptorModificationPhaseContext(mainSkieContext)
        SkiePhaseScheduler.runDescriptorModificationPhases(descriptorModificationPhaseContext)

        val finalExportedInterface = produceObjCExportInterface()
        mainSkieContext.finalizeDescriptorProvider(finalExportedInterface)

        return finalExportedInterface
    }

    fun runSymbolTablePhases(mainSkieContext: MainSkieContext, symbolTable: SymbolTable) {
        val symbolTableContext = SymbolTablePhaseContext(
            mainSkieContext = mainSkieContext,
            symbolTable = symbolTable,
        )

        SkiePhaseScheduler.runSymbolTablePhases(symbolTableContext)
    }

    fun runSirPhases(mainSkieContext: MainSkieContext) {
        val sirPhaseContext = SirPhaseContext(mainSkieContext)

        SkiePhaseScheduler.runSirPhases(sirPhaseContext)
    }
}
