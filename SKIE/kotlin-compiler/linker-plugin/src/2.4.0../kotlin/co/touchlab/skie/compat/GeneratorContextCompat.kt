@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * Kotlin 2.4.0 made the [GeneratorContext] primary constructor private and added a `compilerConfiguration` parameter to
 * the public one.
 */
internal fun skieCreateGeneratorContext(
    compilerConfiguration: CompilerConfiguration,
    moduleDescriptor: ModuleDescriptor,
    bindingContext: BindingContext,
    languageVersionSettings: LanguageVersionSettings,
    symbolTable: SymbolTable,
    typeTranslator: TypeTranslator,
    irBuiltIns: IrBuiltIns,
): GeneratorContext =
    GeneratorContext(
        Psi2IrConfiguration(),
        compilerConfiguration,
        moduleDescriptor,
        bindingContext,
        languageVersionSettings,
        symbolTable,
        GeneratorExtensions(),
        typeTranslator,
        irBuiltIns,
    )
