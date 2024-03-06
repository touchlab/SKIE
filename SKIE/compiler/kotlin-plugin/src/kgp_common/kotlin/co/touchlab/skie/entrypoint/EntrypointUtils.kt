@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.context.ClassExportPhaseContext
import co.touchlab.skie.context.DescriptorModificationPhaseContext
import co.touchlab.skie.context.MainSkieContext
import co.touchlab.skie.context.SirPhaseContext
import co.touchlab.skie.context.SymbolTablePhaseContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.ir.util.SymbolTable

internal object EntrypointUtils {

    fun runClassExportPhases(
        mainSkieContext: MainSkieContext,
    ) {
        val classExportPhaseContext = ClassExportPhaseContext(mainSkieContext)
        classExportPhaseContext.skiePhaseScheduler.runClassExportPhases(classExportPhaseContext)
    }

    fun runDescriptorModificationPhases(
        mainSkieContext: MainSkieContext,
    ): ObjCExportedInterface {
        val descriptorModificationPhaseContext = DescriptorModificationPhaseContext(mainSkieContext)
        descriptorModificationPhaseContext.skiePhaseScheduler.runDescriptorModificationPhases(descriptorModificationPhaseContext)

        return mainSkieContext.finalizeDescriptorProvider()
    }

    fun runSymbolTablePhases(mainSkieContext: MainSkieContext, symbolTable: SymbolTable) {
        val symbolTableContext = SymbolTablePhaseContext(
            mainSkieContext = mainSkieContext,
            symbolTable = symbolTable,
        )

        symbolTableContext.skiePhaseScheduler.runSymbolTablePhases(symbolTableContext)
    }

    fun runSirPhases(mainSkieContext: MainSkieContext) {
        val sirPhaseContext = SirPhaseContext(mainSkieContext)

        sirPhaseContext.skiePhaseScheduler.runSirPhases(sirPhaseContext)
    }
}
