package co.touchlab.swikt.plugin

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import javax.inject.Inject

// TODO: is it @CacheableTask ?
abstract class SwiftCompileTask
@Inject constructor(
    @Internal
    val kotlinFramework: Framework,
): DefaultTask() {

    init {
        description = "Swift compilation task"
        group = BasePlugin.BUILD_GROUP
    }

    @get:Internal
    val linkTaskName: String
        get() = kotlinFramework.linkTaskName

    @get:Internal
    val linkTask: KotlinNativeLink
        get() = kotlinFramework.linkTask

    @get:Internal
    val linkTaskProvider: Provider<out KotlinNativeLink>
        get() = kotlinFramework.linkTaskProvider

    // TODO: @get:Incremental ?
    // TODO: @get:PathSensitive(PathSensitivity.NAME_ONLY) ?
    @get:InputFiles
    abstract val sourceFiles: Property<ConfigurableFileCollection>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun sampleAction(): Unit = with(project) {
        logger.info("Compiling Swift sources")

        val darwinTarget = darwinTargets[kotlinFramework.target.konanTarget] ?: return@with
        val baseName = kotlinFramework.baseName
        val sdk = darwinTarget.sdk
        val targetTriple = darwinTarget.targetTriple
        val frameworkFile = kotlinFramework.outputFile
        val frameworkPath = frameworkFile.absolutePath
        val relativeExecutablePath = when (darwinTarget.konanTarget.family) {
            Family.OSX -> "Versions/A/$baseName"
            else -> baseName
        }
        val executableFile = File(frameworkFile, relativeExecutablePath)

        File(frameworkFile, "Modules/${baseName}.swiftmodule").mkdirs()
        copy {
            it.from(File(frameworkFile, "Headers/${baseName}.h"))
            it.from(File(frameworkFile, "Modules/module.modulemap")) {
                it.filter { originalLine ->
                    originalLine.replace("\"${baseName}.h\"", "\"../${baseName}.h\"")
                }
            }
            it.into(outputDir)
        }

        val otoolOutput = "/usr/bin/xcrun --sdk $sdk otool -l $executableFile"
            .let(ProcessGroovyMethods::execute)
            .let(ProcessGroovyMethods::getText)
            .trim()
        val loadCommands = LoadCommand.parseOtoolOutput(otoolOutput)

        val targetVersion = when (kotlinFramework.target.konanTarget.family) {
            Family.OSX -> loadCommands.firstOrNull { it.cmd == "LC_VERSION_MIN_MACOSX" }?.get("version")
                ?: loadCommands.firstOrNull { it.cmd == "LC_BUILD_VERSION" }?.get("minos")
            Family.IOS -> loadCommands.firstOrNull { it.cmd == "LC_VERSION_MIN_IPHONEOS" }?.get("version")
                ?: loadCommands.firstOrNull { it.cmd == "LC_BUILD_VERSION" }?.get("minos")
            Family.TVOS -> loadCommands.firstOrNull { it.cmd == "LC_VERSION_MIN_TVOS" }?.get("version")
                ?: loadCommands.firstOrNull { it.cmd == "LC_BUILD_VERSION" }?.get("minos")
            Family.WATCHOS -> loadCommands.firstOrNull { it.cmd == "LC_BUILD_VERSION" }?.get("minos")
            Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR -> error("Unsupported target: ${kotlinFramework.target.konanTarget}")
        } ?: error("Couldn't determine target version using otool! otool output: $otoolOutput")
        // TODO[TK]: SDK version from otool is not available on my mac (it returns 15.0, but my xcrun can't find it). Unsure what's correct.
        // val sdkVersion = sdkRegex.find(otoolOutput)?.groupValues?.getOrNull(1) ?: error("Couldn't determine sdk version using otool!")

        val sdkVersion = "/usr/bin/xcrun --sdk $sdk --show-sdk-platform-version"
            .let(ProcessGroovyMethods::execute)
            .let(ProcessGroovyMethods::getText)
            .trim()
        val versionedSdk = sdk + sdkVersion
        val sdkPath = "/usr/bin/xcrun --sdk $versionedSdk --show-sdk-path"
            .let(ProcessGroovyMethods::execute)
            .let(ProcessGroovyMethods::getText)
            .trim()

        xcrun(
            "--sdk", versionedSdk,
            "swiftc",
            "-v",
            "-module-name",
            baseName,
            "-import-underlying-module",
            "-I",
            ".",
            "-emit-module-interface-path",
            "$frameworkPath/Modules/${baseName}.swiftmodule/$targetTriple.swiftinterface",
            "-emit-module-path",
            "$frameworkPath/Modules/${baseName}.swiftmodule/$targetTriple.swiftmodule",
            "-emit-objc-header-path",
            "$frameworkPath/Headers/${baseName}-Swift.h",
            "-emit-library",
            "-static",
            "-enable-library-evolution",
            "-sdk",
            sdkPath,
            "-target",
            targetTriple.withOSVersion(targetVersion),
            "-embed-bitcode-marker",
            *sourceFiles.get().files.map { it.absolutePath }.toTypedArray(),
        )

        if (kotlinFramework.isStatic) {
            xcrun(
                "--sdk", versionedSdk,
                "libtool",
                "-v",
                "-static",
                "-syslibroot", sdkPath,
                "-o", "${frameworkPath}/${baseName}_merged",
                executableFile,
                outputDir.get().file("lib${baseName}.a").asFile.absolutePath,
            )
        } else {
            xcrun(
                "--sdk", versionedSdk,
                "ld",
                "-dylib",
                "-arch", targetTriple.architecture,
                "-platform_version", targetTriple.withoutVendorAndArch(), targetVersion, sdkVersion,
                "-ObjC",
                "-syslibroot", sdkPath,
                "-undefined", "dynamic_lookup",
                "-install_name", "@rpath/$baseName.framework/$relativeExecutablePath",
                "-o", "${frameworkPath}/${baseName}_merged",
                executableFile,
                darwinTarget.compilerRtLibrary(),
                outputDir.get().file("lib${baseName}.a").asFile.absolutePath,
            )
        }
        executableFile.delete()
        File("${frameworkPath}/${baseName}_merged").renameTo(executableFile)
        File("${frameworkPath}/Headers/${baseName}.h").appendText("\n#import \"${baseName}-Swift.h\"")

        val allFamilyTripletsButActive = darwinTargets.values.filter { it.konanTarget.family == kotlinFramework.target.konanTarget.family }.map { it.targetTriple } - targetTriple
        for (tripletToFake in allFamilyTripletsButActive) {
            println("Faking triplet: $tripletToFake")
            copy {
                it.from("${frameworkPath}/Modules/${baseName}.swiftmodule/") {
                    it.include("${targetTriple}.*")
                }
                it.rename { it.replace(targetTriple.toString(), tripletToFake.toString()) }
                it.into("${frameworkPath}/Modules/${baseName}.swiftmodule")
            }
        }
    }

    private fun Project.xcrun(vararg args: Any?) = exec {
        it.workingDir(outputDir)
        it.executable = "/usr/bin/xcrun"
        it.args(args.mapNotNull { it })
    }

    private val compilerRtDir: String? by lazy {
        val dir = org.jetbrains.kotlin.konan.file.File("${Xcode.current.toolchain}/usr/lib/clang/").listFiles.firstOrNull()?.absolutePath
        if (dir != null) "$dir/lib/darwin/" else null
    }

    private fun DarwinTarget.compilerRtLibrary(): String? {
        return provideCompilerRtLibrary("")
    }

    private fun DarwinTarget.provideCompilerRtLibrary(libraryName: String, isDynamic: Boolean = false): String? {
        val prefix = when (konanTarget.family) {
            Family.IOS -> "ios"
            Family.WATCHOS -> "watchos"
            Family.TVOS -> "tvos"
            Family.OSX -> "osx"
            else -> error("Target $konanTarget is unsupported")
        }
        // TODO: remove after `minimalXcodeVersion` will be 12.
        // Separate libclang_rt version for simulator appeared in Xcode 12.
        val compilerRtForSimulatorExists = Xcode.current.version.substringBefore('.').toInt() >= 12
        val suffix = if ((libraryName.isNotEmpty() || compilerRtForSimulatorExists) && targetTriple.isSimulator) {
            "sim"
        } else {
            ""
        }

        val dir = compilerRtDir
        val mangledLibraryName = if (libraryName.isEmpty()) "" else "${libraryName}_"
        val extension = if (isDynamic) "_dynamic.dylib" else ".a"

        return if (dir != null) "$dir/libclang_rt.$mangledLibraryName$prefix$suffix$extension" else null
    }

    companion object {
        val darwinTargets = listOf(
            DarwinTarget(KonanTarget.IOS_ARM32, "armv7-apple-ios", "iphoneos"),
            DarwinTarget(KonanTarget.IOS_ARM64, "arm64-apple-ios", "iphoneos"),
            DarwinTarget(KonanTarget.IOS_X64, "x86_64-apple-ios-simulator", "iphonesimulator"),
            DarwinTarget(KonanTarget.IOS_SIMULATOR_ARM64, "arm64-apple-ios-simulator", "iphonesimulator"),
            DarwinTarget(KonanTarget.WATCHOS_ARM32, "armv7k-apple-watchos", "watchos"),
            DarwinTarget(KonanTarget.WATCHOS_ARM64, "arm64_32-apple-watchos", "watchos"),
            DarwinTarget(KonanTarget.WATCHOS_X86, "i386-apple-watchos-simulator", "watchsimulator"),
            DarwinTarget(KonanTarget.WATCHOS_X64, "x86_64-apple-watchos-simulator", "watchsimulator"),
            DarwinTarget(KonanTarget.WATCHOS_SIMULATOR_ARM64, "arm64-apple-watchos-simulator", "watchsimulator"),
            DarwinTarget(KonanTarget.TVOS_ARM64, "arm64-apple-tvos", "appletvos"),
            DarwinTarget(KonanTarget.TVOS_X64, "x86_64-apple-tvos-simulator", "appletvsimulator"),
            DarwinTarget(KonanTarget.TVOS_SIMULATOR_ARM64, "arm64-apple-tvos-simulator", "appletvsimulator"),
            DarwinTarget(KonanTarget.MACOS_X64, "x86_64-apple-macos", "macosx"),
            DarwinTarget(KonanTarget.MACOS_ARM64, "arm64-apple-macos", "macosx"),
        ).associateBy { it.konanTarget }
    }
}

data class DarwinTarget(
    val konanTarget: KonanTarget,
    val targetTriple: TargetTriple,
    val sdk: String,
) {
    constructor(
        konanTarget: KonanTarget,
        targetTripleString: String,
        sdk: String,
    ): this(konanTarget, TargetTriple.fromString(targetTripleString), sdk)
}

data class LoadCommand(
    val index: Int,
    val attributes: Map<String, String>,
) {

    val cmd: String?
        get() = attributes["cmd"]

    operator fun get(name: String): String? {
        return attributes[name]
    }
    companion object {
        private val loadCommand = "Load command (\\d+)((?:\\s\\s+(?:\\w+)\\s(?:.*))+)".toRegex()
        private val loadCommandAttribute = "(\\w+)\\s(.*)".toRegex()

        fun parseOtoolOutput(otoolOutput: String): List<LoadCommand> {
            return loadCommand.findAll(otoolOutput).map { loadCommandMatch ->
                LoadCommand(
                    index = loadCommandMatch.groupValues[1].toInt(),
                    attributes = loadCommandAttribute.findAll(loadCommandMatch.groupValues[2]).map { it.groupValues[1] to it.groupValues[2] }.toMap()
                )
            }.toList()
        }
    }
}
