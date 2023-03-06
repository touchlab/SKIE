package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.framework.BuildConfig
import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.reflect.full.companionObject

class CompilerArgumentsProvider(
    private val dependencies: List<String> = EnvDefaults.dependencies ?: emptyList(),
    private val exportedDependencies: List<String> = EnvDefaults.exportedDependencies ?: emptyList(),
    private val linkMode: LinkMode = EnvDefaults.linkMode ?: LinkMode.Static,
    private val buildConfiguration: BuildConfiguration = EnvDefaults.buildConfiguration ?: BuildConfiguration.Debug,
    private val target: Target? = EnvDefaults.target,
) {
    fun compile(
        sourcePaths: List<Path>,
        outputFile: Path,
        tempDirectory: Path,
    ): K2NativeCompilerArguments {
        return K2NativeCompilerArguments().apply {
            debug = true
            enableAssertions = true

            memoryModel = "experimental"

            produce = "library"
            moduleName = "co.touchlab.swiftgen:kotlin"
            shortModuleName = "kotlin"

            multiPlatform = true
            noendorsedlibs = true

            libraries = dependencies.toTypedArray()

            freeArgs += sourcePaths.map { it.absolutePathString() }
            temporaryFilesDir = tempDirectory.absolutePathString()
            outputName = outputFile.absolutePathString()

            target = this@CompilerArgumentsProvider.target?.kotlinName
        }
    }

    fun link(
        klib: Path,
        tempDirectory: Path,
        outputFile: Path,
    ): K2NativeCompilerArguments {
        return K2NativeCompilerArguments().apply {
            includes = (includes ?: emptyArray()) + klib.absolutePathString()

            memoryModel = "experimental"

            produce = "framework"
            staticFramework = linkMode == LinkMode.Static

            temporaryFilesDir = tempDirectory.absolutePathString()
            outputName = outputFile.absolutePathString()
            bundleId = "Kotlin"

            multiPlatform = true
            noendorsedlibs = true

            pluginClasspaths = (pluginClasspaths ?: emptyArray()) + arrayOf(BuildConfig.RESOURCES)

            libraries = dependencies.toTypedArray()
            exportedLibraries = exportedDependencies.toTypedArray()

            target = this@CompilerArgumentsProvider.target?.kotlinName

            when (buildConfiguration) {
                BuildConfiguration.Debug -> {
                    debug = true
                    enableAssertions = true
                }
                BuildConfiguration.Release -> {
                    optimization = true
                }
            }
        }
    }

    enum class BuildConfiguration {
        Debug,
        Release,
    }

    enum class Target(val kotlinName: String) {
        IOS_ARM64("ios_arm64"),
        IOS_X64("ios_x64"),
        IOS_SIMULATOR_ARM64("ios_simulator_arm64"),
        MACOS_ARM64("macos_arm64"),
        MACOS_X64("macos_x64"),
        ;
    }

    enum class LinkMode {
        Dynamic,
        Static,
    }

    companion object {
        fun createPreferringEnvValues(
            dependencies: List<String>,
            exportedDependencies: List<String>,
            linkMode: LinkMode,
            buildConfiguration: BuildConfiguration,
            target: Target?,
        ): CompilerArgumentsProvider = CompilerArgumentsProvider(
            dependencies = EnvDefaults.dependencies ?: dependencies,
            exportedDependencies = EnvDefaults.exportedDependencies ?: exportedDependencies,
            linkMode = EnvDefaults.linkMode ?: linkMode,
            buildConfiguration = EnvDefaults.buildConfiguration ?: buildConfiguration,
            target = target,
        )
    }

    object EnvDefaults {
        val dependencies: List<String>? = listFromEnv("KOTLIN_DEPENDENCIES")
        val exportedDependencies: List<String>? = listFromEnv("KOTLIN_EXPORTED_DEPENDENCIES")
        val linkMode: LinkMode? = enumFromEnv("KOTLIN_LINK_MODE", LinkMode.values())
        val buildConfiguration: BuildConfiguration? = enumFromEnv("KOTLIN_BUILD_CONFIGURATION", BuildConfiguration.values())
        val target: Target? = enumFromEnv("KOTLIN_TARGET", Target.values())
        private fun listFromEnv(name: String): List<String>? {
            return System.getenv(name)?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() }
        }

        private fun <T: Enum<T>> enumFromEnv(name: String, values: Array<T>): T? {
            return System.getenv(name)?.let { value ->
                values.find { it.name.equals(value, ignoreCase = true) } ?: error("Unknown value for $name: $value")
            }
        }
    }
}
