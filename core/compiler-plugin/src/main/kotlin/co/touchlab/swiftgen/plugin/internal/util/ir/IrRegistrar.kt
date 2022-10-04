package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.psi2ir.generators.SyntheticDeclarationsGenerator

internal class IrRegistrar constructor(
    private val moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
    private val symbolTable: SymbolTable,
) {

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private val generatorContext = GeneratorContext(
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

    private val syntheticDeclarationsGenerator = SyntheticDeclarationsGenerator(generatorContext)

    fun registerIr(packageBuilders: Collection<PackageBuilder>) {
        packageBuilders.forEach {
            registerIr(it)
        }
    }

    private fun registerIr(packageBuilder: PackageBuilder) {
        val file = packageBuilder.buildFile(moduleFragment)

        packageBuilder.descriptors.forEach { descriptor ->
            val declarationBuilder = packageBuilder.getBuilder(descriptor)

            registerIr(descriptor, declarationBuilder, file)
        }
    }

    private fun <D : DeclarationDescriptor, I : IrDeclaration, S : IrBindableSymbol<D, I>> registerIr(
        descriptor: D,
        declarationBuilder: DeclarationBuilder<D, I, S>,
        file: IrFile,
    ) {
        descriptor.accept(syntheticDeclarationsGenerator, file)

        val symbol = declarationBuilder.getSymbol(descriptor, symbolTable)

        val declarationIrBuilder = DeclarationIrBuilder(generatorContext, symbol, startOffset = 0, endOffset = 0)

        declarationBuilder.initializeIr(symbol.owner, symbolTable, declarationIrBuilder)
    }
}