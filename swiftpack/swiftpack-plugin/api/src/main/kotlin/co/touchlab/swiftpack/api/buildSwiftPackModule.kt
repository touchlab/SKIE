package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.module.SwiftPackModule
import co.touchlab.swiftpack.spec.module.SwiftPackModule.Companion.write

fun buildSwiftPackModule(
    moduleName: String = "main",
    writeToOutputDir: Boolean = true,
    block: SwiftPackModuleBuilder.() -> Unit
): SwiftPackModule {
    val context = SwiftPackModuleBuilder(moduleName)
    context.block()
    val template = context.build()
    if (writeToOutputDir) {
        val outputDir = checkNotNull(SwiftPackModuleBuilder.Config.outputDir) {
            "Output directory not configured! Either apply the SwiftPack Gradle plugin, set the SwiftTemplateBuilder.Config.outputDir, or pass false as the first parameter of buildSwiftTemplate."
        }
        outputDir.mkdirs()
        template.write(outputDir.resolve("$moduleName.swiftpack"))
    }
    return template
}
