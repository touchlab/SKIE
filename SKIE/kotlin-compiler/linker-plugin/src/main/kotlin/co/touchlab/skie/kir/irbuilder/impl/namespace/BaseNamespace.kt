package co.touchlab.skie.kir.irbuilder.impl.namespace

import co.touchlab.skie.compat.skieCreateGeneratorContext
import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationTemplate
import co.touchlab.skie.kir.irbuilder.Namespace
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.SymbolTablePhase
import co.touchlab.skie.phases.compilerConfiguration
import co.touchlab.skie.phases.pluginContext
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.resolve.descriptorUtil.module

abstract class BaseNamespace<D : DeclarationDescriptor> : Namespace<D> {

    private val templates = mutableListOf<DeclarationTemplate<*>>()

    context(MutableDescriptorProvider)
    override fun addTemplate(declarationTemplate: DeclarationTemplate<*>) {
        templates.add(declarationTemplate)

        registerDescriptorProvider(declarationTemplate)
    }

    context(SymbolTablePhase.Context)
    override fun registerSymbols() {
        templates.forEach {
            it.declareSymbol()
        }
    }

    context(MutableDescriptorProvider)
    private fun registerDescriptorProvider(declarationTemplate: DeclarationTemplate<*>) {
        addDescriptorIntoDescriptorHierarchy(declarationTemplate.descriptor)
        addDescriptorIntoDescriptorProvider(declarationTemplate)
    }

    context(MutableDescriptorProvider)
    private fun addDescriptorIntoDescriptorProvider(declarationTemplate: DeclarationTemplate<*>) {
        declarationTemplate.registerExposedDescriptor()
    }

    context(KotlinIrPhase.Context)
    override fun generateIrDeclarations() {
        val generatorContext = skieCreateGeneratorContext(
            compilerConfiguration = compilerConfiguration,
            moduleDescriptor = descriptor.module,
            bindingContext = pluginContext.bindingContext,
            languageVersionSettings = pluginContext.languageVersionSettings,
            symbolTable = skieSymbolTable.kotlinSymbolTable,
            typeTranslator = pluginContext.typeTranslator,
            irBuiltIns = pluginContext.irBuiltIns,
        )

        val namespaceIr = generateNamespaceIr()

        templates.forEach {
            it.generateIrDeclaration(namespaceIr, generatorContext)
        }
    }

    context(KotlinIrPhase.Context)
    override fun generateIrBodies() {
        templates.forEach {
            it.generateIrBody()
        }
    }

    protected abstract fun addDescriptorIntoDescriptorHierarchy(declarationDescriptor: DeclarationDescriptor)

    context(KotlinIrPhase.Context)
    protected abstract fun generateNamespaceIr(): IrDeclarationContainer
}
