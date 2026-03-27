package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class PluginConfigurationGenerator(
    private val tempFileSystem: TempFileSystem,
) {

    fun generate(baseConfiguration: CompilerSkieConfigurationData, configFiles: List<Path>): IntermediateResult<Path> {
        val mergedConfigFile = mergeConfigFiles(baseConfiguration, configFiles)

        return IntermediateResult.Value(mergedConfigFile)
    }

    private fun mergeConfigFiles(baseConfiguration: CompilerSkieConfigurationData, configFiles: List<Path>): Path {
        val skieConfiguration = configFiles
            .map { CompilerSkieConfigurationData.deserialize(it.readText()) }
            .fold(baseConfiguration) { acc, skieConfiguration ->
                acc + skieConfiguration
            }

        val mergedConfigFile = tempFileSystem.createFile("config.json")

        mergedConfigFile.writeText(skieConfiguration.serialize())

        return mergedConfigFile
    }
}
