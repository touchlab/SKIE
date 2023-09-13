package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

interface KotlinIrPhase : SkiePhase<KotlinIrPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilderImpl

        val moduleFragment: IrModuleFragment

        val pluginContext: IrPluginContext

        val allModules: Map<String, IrModuleFragment>
    }
}
