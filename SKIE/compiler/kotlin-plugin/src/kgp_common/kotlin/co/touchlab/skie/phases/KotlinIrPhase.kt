package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

interface KotlinIrPhase : ForegroundPhase<KotlinIrPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilderImpl

        val moduleFragment: IrModuleFragment

        val pluginContext: IrPluginContext

        val skieSymbolTable: SkieSymbolTable

        val allModules: Map<String, IrModuleFragment>

        val irBuiltIns: IrBuiltIns
            get() = pluginContext.irBuiltIns
    }
}
