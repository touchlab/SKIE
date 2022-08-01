package co.touchlab.swiftgen.irinspector.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import java.nio.file.Path
import kotlin.io.path.writeText

class IrInspectorPluginExtension(private val outputPath: String): IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val irDump = moduleFragment.dump()

        Path.of(outputPath).writeText(irDump)
    }
}
