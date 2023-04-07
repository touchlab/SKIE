package co.touchlab.skie.plugin.generator.internal.util

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal interface SkieCompilationPhase {

    val isActive: Boolean

    fun runObjcPhase() {
    }

    fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
    }
}
