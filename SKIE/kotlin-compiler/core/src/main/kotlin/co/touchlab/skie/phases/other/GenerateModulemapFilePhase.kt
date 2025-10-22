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
            appendLine("framework module ${framework.frameworkName} {")
            appendLine("    umbrella header \"${framework.frameworkName}.h\"")

            if (SkieConfigurationFlag.Migration_WildcardExport.isEnabled) {
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

            if (generateSwiftModule && framework.swiftHeader.exists()) {
                appendLine()
                appendLine("module ${framework.frameworkName}.Swift {")
                appendLine("    header \"${framework.swiftHeader.name}\"")
                appendLine("    requires objc")
                appendLine("}")
            }
        }.toString()

    object ForSwiftCompilation : GenerateModulemapFilePhase(generateSwiftModule = false)

    object ForFramework : GenerateModulemapFilePhase(generateSwiftModule = true)
}
