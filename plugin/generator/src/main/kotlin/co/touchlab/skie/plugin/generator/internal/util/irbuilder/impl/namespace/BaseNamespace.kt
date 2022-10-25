package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationTemplate
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.Namespace
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.resolve.descriptorUtil.module

internal abstract class BaseNamespace<D : DeclarationDescriptor> : Namespace<D> {

    private val templates = mutableListOf<DeclarationTemplate<*>>()

    override fun addTemplate(declarationTemplate: DeclarationTemplate<*>, symbolTable: SymbolTable) {
        templates.add(declarationTemplate)

        declarationTemplate.declareSymbol(symbolTable)

        addDescriptor(declarationTemplate.descriptor)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun generateIr(pluginContext: IrPluginContext, symbolTable: SymbolTable) {
        val generatorContext = GeneratorContext(
            Psi2IrConfiguration(ignoreErrors = false, allowUnboundSymbols = false),
            descriptor.module,
            pluginContext.bindingContext,
            pluginContext.languageVersionSettings,
            symbolTable,
            GeneratorExtensions(),
            pluginContext.typeTranslator,
            pluginContext.irBuiltIns,
            null,
        )

        val namespaceIr = generateNamespaceIr(generatorContext)

        templates.forEach {
            it.generateIr(namespaceIr, generatorContext)
        }
    }

    protected abstract fun addDescriptor(declarationDescriptor: DeclarationDescriptor)

    protected abstract fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer
}
