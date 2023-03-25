package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.execute
import co.touchlab.skie.framework.BuildConfig
import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.konan.target.TargetTriple
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class CompilerArgumentsProvider(
    val dependencies: List<String> = EnvDefaults.dependencies ?: emptyList(),
    val exportedDependencies: List<String> = EnvDefaults.exportedDependencies ?: emptyList(),
    val linkMode: LinkMode = EnvDefaults.linkMode ?: LinkMode.Static,
    val buildConfiguration: BuildConfiguration = EnvDefaults.buildConfiguration ?: BuildConfiguration.Debug,
    val target: Target = EnvDefaults.target ?: Target.current,
    val optIn: List<String> = emptyList(),
) {
    fun compile(
        sourcePaths: List<Path>,
        commonSourcePaths: List<Path>,
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
            optIn = this@CompilerArgumentsProvider.optIn.toTypedArray()

            freeArgs += sourcePaths.map { it.absolutePathString() }
            commonSources = commonSourcePaths.map { it.absolutePathString() }.toTypedArray()

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

        val sdk: String
            get() = when (this) {
                IOS_ARM64 -> "iphoneos"
                IOS_X64 -> "iphonesimulator"
                IOS_SIMULATOR_ARM64 -> "iphonesimulator"
                MACOS_ARM64 -> "macosx"
                MACOS_X64 -> "macosx"
            }

        val targetTriple: TargetTriple
            get() = when (this) {
                IOS_ARM64 -> TargetTriple("arm64", "apple", "ios13.0", null)
                IOS_X64 -> TargetTriple("x86_64", "apple", "ios13.0", null)
                IOS_SIMULATOR_ARM64 -> TargetTriple("arm64", "apple", "ios13.0", "simulator")
                MACOS_ARM64 -> TargetTriple("arm64", "apple", "macos10.15", null)
                MACOS_X64 -> TargetTriple("x86_64", "apple", "macos10.15", null)
            }

        companion object {
            val current: Target by lazy {
                val possibleTargets = mapOf(
                    "arm64" to MACOS_ARM64,
                    "x86_64" to MACOS_X64,
                )
                val systemName = "uname -m".execute().stdOut.trim()

                possibleTargets[systemName] ?: error("Unsupported architecture: $systemName")
            }
        }
    }

    enum class LinkMode {
        Dynamic,
        Static,
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
            val envValue = System.getenv(name)?.trim()?.takeIf { it.isNotBlank() } ?: return null
            return values.find { it.name.equals(envValue, ignoreCase = true) } ?: error("Unknown value for $name: $envValue")
        }
    }
}
