package co.touchlab.skie.kir.irbuilder.impl.namespace

import co.touchlab.skie.kir.DescriptorRegistrationScope
import co.touchlab.skie.kir.irbuilder.DeclarationTemplate
import co.touchlab.skie.kir.irbuilder.Namespace
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.resolve.descriptorUtil.module

abstract class BaseNamespace<D : DeclarationDescriptor> : Namespace<D> {

    private val templates = mutableListOf<DeclarationTemplate<*>>()

    context(DescriptorRegistrationScope)
    override fun addTemplate(declarationTemplate: DeclarationTemplate<*>) {
        templates.add(declarationTemplate)

        registerDescriptorProvider(declarationTemplate.descriptor)
    }

    override fun registerSymbols(symbolTable: SymbolTable) {
        templates.forEach {
            it.declareSymbol(symbolTable)
        }
    }

    context(DescriptorRegistrationScope)
    private fun registerDescriptorProvider(declarationDescriptor: DeclarationDescriptor) {
        addDescriptorIntoDescriptorHierarchy(declarationDescriptor)
        addDescriptorIntoDescriptorProvider(declarationDescriptor)
    }

    context(DescriptorRegistrationScope)
    private fun addDescriptorIntoDescriptorProvider(declarationDescriptor: DeclarationDescriptor) {
        registerExposedDescriptor(declarationDescriptor)
    }

    override fun generateIrDeclarations(pluginContext: IrPluginContext, symbolTable: SymbolTable) {
        val generatorContext = GeneratorContext(
            Psi2IrConfiguration(),
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
            it.generateIrDeclaration(namespaceIr, generatorContext)
        }
    }

    override fun generateIrBodies(pluginContext: IrPluginContext) {
        templates.forEach {
            it.generateIrBody(pluginContext)
        }
    }

    protected abstract fun addDescriptorIntoDescriptorHierarchy(declarationDescriptor: DeclarationDescriptor)

    protected abstract fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer
}