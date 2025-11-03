@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import co.touchlab.skie.phases.KotlinIrPhase
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextBase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.resolve.BindingContext

class KotlinIrPhaseContext(
    mainSkieContext: MainSkieContext,
    val moduleFragment: IrModuleFragment,
    val pluginContext: CompatibleIrPluginContext,
) : KotlinIrPhase.Context, ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: KotlinIrPhaseContext = this

    val allModules: Map<String, IrModuleFragment> = pluginContext.linker.modules

    val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder

    val skieSymbolTable: SkieSymbolTable = SkieSymbolTable(pluginContext.symbolTable)

    class CompatibleIrPluginContext(
        val symbolTable: SymbolTable,
        val irBuiltIns: IrBuiltIns,
        val linker: KonanIrLinker,
        val bindingContext: BindingContext,
        val languageVersionSettings: LanguageVersionSettings,
        val typeTranslator: TypeTranslator,
    ) {

        val generatorContext: IrGeneratorContext = IrGeneratorContextBase(irBuiltIns)
    }
}
