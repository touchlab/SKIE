@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContextImpl
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SkieIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val linker = (pluginContext as? IrPluginContextImpl)?.linker as? KonanIrLinker ?: return

        SkieCompilerConfigurationKey.SkieScheduler.get(configuration).runIrPhases(moduleFragment, pluginContext, linker.modules)
    }
}
