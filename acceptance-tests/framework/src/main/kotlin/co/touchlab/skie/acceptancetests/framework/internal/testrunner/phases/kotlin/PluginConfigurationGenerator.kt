package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestResultBuilder
import co.touchlab.skie.configuration.Configuration
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class PluginConfigurationGenerator(
    private val tempFileSystem: TempFileSystem,
    private val testResultBuilder: TestResultBuilder,
) {

    fun generate(configFiles: List<Path>): IntermediateResult<Path> {
        val mergedConfigFile = mergeConfigFiles(configFiles)

        logConfiguration(configFiles, mergedConfigFile)

        return IntermediateResult.Value(mergedConfigFile)
    }

    private fun mergeConfigFiles(configFiles: List<Path>): Path {
        val configuration = Configuration {
            from(configFiles)
        }

        val mergedConfigFile = tempFileSystem.createFile("config.json")

        mergedConfigFile.writeText(configuration.serialize())

        return mergedConfigFile
    }

    private fun logConfiguration(configFiles: List<Path>, mergedConfigFile: Path) {
        if (configFiles.isNotEmpty()) {
            testResultBuilder.appendLog("Configuration", mergedConfigFile.readText())
        }
    }
}
