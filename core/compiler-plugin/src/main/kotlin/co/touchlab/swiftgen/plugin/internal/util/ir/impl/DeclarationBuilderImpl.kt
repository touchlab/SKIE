package co.touchlab.swiftgen.plugin.internal.util.ir.impl

import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationTemplate
import co.touchlab.swiftgen.plugin.internal.util.ir.FunctionBuilder
import co.touchlab.swiftgen.plugin.internal.util.ir.Namespace
import co.touchlab.swiftgen.plugin.internal.util.ir.impl.namespace.NewFileNamespace
import co.touchlab.swiftgen.plugin.internal.util.ir.impl.template.FunctionTemplate
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ContextReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.SymbolTableBaseReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.SymbolTableReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.Name

internal class DeclarationBuilderImpl(
    context: CommonBackendContext,
) : DeclarationBuilder {

    private val symbolTable = context.reflectedBy<ContextReflector>().symbolTable

    private lateinit var mairIrModuleFragment: IrModuleFragment

    private val newFileNamespaceFactory = NewFileNamespace.Factory(context, lazy { mairIrModuleFragment })

    private val newFileNamespacesByName = mutableMapOf<String, NewFileNamespace>()

    private val allNamespaces: List<Namespace>
        get() = newFileNamespacesByName.values.toList()

    override fun getNamespace(name: String): Namespace =
        newFileNamespacesByName.getOrPut(name) {
            newFileNamespaceFactory.create(name)
        }

    override fun createFunction(
        name: Name,
        namespace: Namespace,
        annotations: Annotations,
        builder: FunctionBuilder.() -> Unit,
    ): FunctionDescriptor =
        create(namespace) { FunctionTemplate(name, namespace, annotations, builder) }

    private fun <D : DeclarationDescriptor> create(
        namespace: Namespace,
        templateBuilder: () -> DeclarationTemplate<D>,
    ): D {
        val declarationTemplate = templateBuilder()

        namespace.addTemplate(declarationTemplate)

        return declarationTemplate.descriptor
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun suppressUnboundSymbolsError() {
        val functionSymbolTable = symbolTable.reflectedBy<SymbolTableReflector>().simpleFunctionSymbolTable
        val unboundFunctions = functionSymbolTable.reflectedBy<SymbolTableBaseReflector>().unboundSymbols

        val allDescriptors = allNamespaces.flatMap { it.declarations }.toSet()

        unboundFunctions.removeIf { it.descriptor in allDescriptors }
    }

    fun generateIr(mairIrModuleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        this.mairIrModuleFragment = mairIrModuleFragment

        allNamespaces.forEach {
            it.generateIr(pluginContext, symbolTable)
        }
    }
}
