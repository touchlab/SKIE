package co.touchlab.swiftgen.plugin.internal.util.ir

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ContextReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.SymbolTableBaseReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.SymbolTableReflector
import co.touchlab.swiftlink.plugin.moduleDescriptor
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable

internal class IrGenerator(
    private val context: CommonBackendContext,
) {

    private val descriptorRegistrar = DescriptorRegistrar(
        moduleDescriptor = requireNotNull(context.moduleDescriptor) { "Context must have a module descriptor." }
    )

    val builder: IrBuilder = IrBuilder(descriptorRegistrar)

    fun generateDescriptors() {
        descriptorRegistrar.registerDescriptors()
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun suppressUnboundSymbolsError() {
        val symbolTable = getSymbolTable()
        val functionSymbolTable = symbolTable.reflectedBy<SymbolTableReflector>().simpleFunctionSymbolTable
        val unboundFunctions = functionSymbolTable.reflectedBy<SymbolTableBaseReflector>().unboundSymbols

        val allDescriptors = descriptorRegistrar.packages.flatMap { it.descriptors }.toSet()

        unboundFunctions.removeIf { it.descriptor in allDescriptors }
    }

    fun generateIr(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val symbolTable = getSymbolTable()

        val irRegistrar = IrRegistrar(moduleFragment, pluginContext, symbolTable)

        irRegistrar.registerIr(descriptorRegistrar.packages)
    }

    private fun getSymbolTable(): SymbolTable =
        context.reflectedBy<ContextReflector>().symbolTable
}