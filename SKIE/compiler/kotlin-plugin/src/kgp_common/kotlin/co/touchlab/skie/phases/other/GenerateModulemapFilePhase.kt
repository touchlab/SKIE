package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.GeneratedBySkieComment

sealed class GenerateModulemapFilePhase(private val generateSwiftModule: Boolean) : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val content = getModulemapContent()

        framework.modulemapFile.writeText(content)
    }

    private fun SirPhase.Context.getModulemapContent(): String =
        StringBuilder().apply {
            appendLine("// $GeneratedBySkieComment")
            appendLine()
            appendLine("framework module ${framework.moduleName} {")
            appendLine("    umbrella header \"${framework.moduleName}.h\"")

            if (SkieConfigurationFlag.Migration_WildcardExport in skieConfiguration.enabledConfigurationFlags) {
                appendLine()
                appendLine("    export *")
                appendLine("    module * { export * }")
            }

            sirProvider.allUsedExternalModules.let { externalModules ->
                if (externalModules.isNotEmpty()) {
                    appendLine()
                    externalModules.forEach {
                        appendLine("    use ${it.name}")
                    }
                }
            }

            appendLine("}")

            if (generateSwiftModule) {
                appendLine()
                appendLine("module ${framework.moduleName}.Swift {")
                appendLine("    header \"${framework.swiftHeader.name}\"")
                appendLine("    requires objc")
                appendLine("}")
            }
        }.toString()

    object ForSwiftCompilation : GenerateModulemapFilePhase(generateSwiftModule = false)

    object ForFramework : GenerateModulemapFilePhase(generateSwiftModule = true)
}
