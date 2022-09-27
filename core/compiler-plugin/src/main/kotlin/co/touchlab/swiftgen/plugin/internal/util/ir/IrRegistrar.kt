package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.psi2ir.generators.SyntheticDeclarationsGenerator

internal class IrRegistrar constructor(
    private val descriptorRegistrar: DescriptorRegistrar,
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
    private val symbolTable: SymbolTable,
) {

    private val file: IrFile
    private val generatorContext: GeneratorContext
    private val syntheticDeclarationsGenerator: SyntheticDeclarationsGenerator

    init {
        file = createFile()

        generatorContext = createGeneratorContext()

        syntheticDeclarationsGenerator = SyntheticDeclarationsGenerator(generatorContext)
    }

    private fun createFile(): IrFileImpl {
        val fqName = FqName("co.touchlab.swiftgen.generated")
        val fileSymbol = IrFileSymbolImpl(descriptorRegistrar.syntheticPackageDescriptor)

        val file = IrFileImpl(DummyIrFileEntry, fileSymbol, fqName, moduleFragment)

        moduleFragment.files.add(file)

        return file
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun createGeneratorContext() =
        GeneratorContext(
            Psi2IrConfiguration(ignoreErrors = false, allowUnboundSymbols = false),
            moduleFragment.descriptor,
            pluginContext.bindingContext,
            pluginContext.languageVersionSettings,
            symbolTable,
            GeneratorExtensions(),
            pluginContext.typeTranslator,
            pluginContext.irBuiltIns,
            null,
        )

    @Suppress("UNCHECKED_CAST")
    fun registerIr() {
        descriptorRegistrar.descriptorsWithIrTemplate.forEach { (descriptor, irInitializer) ->
            val castedIrInitializer = irInitializer as IrTemplate<
                    DeclarationDescriptor, IrDeclaration, IrBindableSymbol<DeclarationDescriptor, IrDeclaration>
                    >

            registerIr(descriptor, castedIrInitializer)
        }
    }

    private fun <D : DeclarationDescriptor, I : IrDeclaration, S : IrBindableSymbol<D, I>> registerIr(
        descriptor: D,
        irTemplate: IrTemplate<D, I, S>,
    ) {
        descriptor.accept(syntheticDeclarationsGenerator, file)

        val symbol = irTemplate.getSymbol(descriptor, symbolTable)

        val declarationBuilder = DeclarationIrBuilder(generatorContext, symbol)

        irTemplate.initializeIr(symbol.owner, symbolTable, declarationBuilder)
    }

    private object DummyIrFileEntry : IrFileEntry {

        override val maxOffset: Int = 0
        override val name: String = IrGenerator.generatedFileName

        override fun getColumnNumber(offset: Int): Int = 0

        override fun getLineNumber(offset: Int): Int = 0

        override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo = SourceRangeInfo(
            name, 0, 0, 0, -1, 0, 0,
        )
    }
}