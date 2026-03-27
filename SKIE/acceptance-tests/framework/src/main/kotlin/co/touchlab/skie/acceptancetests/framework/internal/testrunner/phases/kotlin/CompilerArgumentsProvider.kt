package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptance_tests_framework.BuildConfig
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.execute
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.konan.target.TargetTriple
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class CompilerArgumentsProvider(
    val dependencies: List<String> = TestDefaults.dependencies ?: emptyList(),
    val exportedDependencies: List<String> = TestDefaults.exportedDependencies ?: emptyList(),
    val linkMode: LinkMode = TestDefaults.linkMode ?: LinkMode.Static,
    val buildConfiguration: BuildConfiguration = TestDefaults.buildConfiguration ?: BuildConfiguration.Debug,
    val target: Target = TestDefaults.target ?: Target.current,
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
            moduleName = "co.touchlab.skie:kotlin"
            shortModuleName = "kotlin"

            multiPlatform = true
            noendorsedlibs = true

            libraries = dependencies.toTypedArray()
            optIn = this@CompilerArgumentsProvider.optIn.toTypedArray()

            freeArgs += sourcePaths.map { it.absolutePathString() }
            commonSources = commonSourcePaths.map { it.absolutePathString() }.toTypedArray()

            temporaryFilesDir = tempDirectory.absolutePathString()
            outputName = outputFile.absolutePathString()

            target = this@CompilerArgumentsProvider.target.kotlinName
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

            target = this@CompilerArgumentsProvider.target.kotlinName

            overrideKonanProperties = (overrideKonanProperties ?: arrayOf()) + arrayOf(
                "osVersionMin.$target=${getMinRequiredOsVersionForSwiftAsync(target!!)}",
                "osVersionMinSinceXcode15.$target=${getMinRequiredOsVersionForSwiftAsync(target!!)}",
            )

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
                val systemName = listOf("uname", "-m").execute().stdOut.trim()

                possibleTargets[systemName] ?: error("Unsupported architecture: $systemName")
            }
        }
    }

    enum class LinkMode {
        Dynamic,
        Static,
    }

    object TestDefaults {

        val dependencies: List<String>? = listFromTestProperty("KOTLIN_DEPENDENCIES")
        val exportedDependencies: List<String>? = listFromTestProperty("KOTLIN_EXPORTED_DEPENDENCIES")
        val linkMode: LinkMode? = enumFromTestProperty("KOTLIN_LINK_MODE", LinkMode.entries)
        val buildConfiguration: BuildConfiguration? = enumFromTestProperty("KOTLIN_BUILD_CONFIGURATION", BuildConfiguration.entries)
        val target: Target? = enumFromTestProperty("KOTLIN_TARGET", Target.entries)

        private fun listFromTestProperty(name: String): List<String>? {
            return TestProperties[name]?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() }
        }

        private fun <T : Enum<T>> enumFromTestProperty(name: String, values: List<T>): T? {
            val propertyValue = TestProperties[name]?.trim()?.takeIf { it.isNotBlank() } ?: return null

            return values.find { it.name.equals(propertyValue, ignoreCase = true) } ?: error("Unknown value for $name: $propertyValue")
        }
    }
}
