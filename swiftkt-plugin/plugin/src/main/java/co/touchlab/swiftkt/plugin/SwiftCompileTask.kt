package co.touchlab.swiftkt.plugin

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
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

    // TODO: @get:Incremental ?
    // TODO: @get:PathSensitive(PathSensitivity.NAME_ONLY) ?
    @get:InputFiles
    abstract val sourceFiles: Property<ConfigurableFileCollection>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val workDir by lazy { outputDir.dir("work") }
    private val backupDir by lazy { outputDir.dir("backup") }

    @TaskAction
    fun sampleAction(): Unit = with(project) {
        logger.info("Compiling Swift sources")

        // Delete work dir
        workDir.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }

        val darwinTarget = kotlinFramework.darwinTarget
        val baseName = kotlinFramework.baseName
        val sdk = darwinTarget.sdk
        val targetTriple = darwinTarget.targetTriple
        val sourceFramework = backupDir.get().dir(kotlinFramework.outputFile.name).asFile
        val targetFramework = kotlinFramework.outputFile
        val relativeExecutablePath = when (darwinTarget.konanTarget.family) {
            Family.OSX -> "Versions/A/$baseName"
            else -> baseName
        }
        val sourceExecutable = File(sourceFramework, relativeExecutablePath)
        val targetExecutable = File(targetFramework, relativeExecutablePath)

        when (kotlinFramework.linkTask.state.outcome) {
            TaskExecutionOutcome.EXECUTED -> {
                sourceFramework.deleteRecursively()
                kotlinFramework.outputFile.copyRecursively(sourceFramework)
                targetExecutable.delete()
                File(targetFramework, "/Headers/$baseName.h").appendText("\n#import \"$baseName-Swift.h\"")
            }
            TaskExecutionOutcome.UP_TO_DATE, TaskExecutionOutcome.SKIPPED -> {
                check(sourceFramework.exists()) {
                    "Framework backup doesn't exist and can't be created. Clean the build directory and try running again."
                }
            }
            TaskExecutionOutcome.FROM_CACHE -> TODO("Cached link phase not supported!")
            TaskExecutionOutcome.NO_SOURCE -> TODO("Link phase without sources is not supported!")
            null -> error("Swikt task can't run before link task has completed!")
        }

        val (swiftcBitcodeArg, ldBitcodeArgs) = when (kotlinFramework.embedBitcode) {
            BitcodeEmbeddingMode.DISABLE -> null to emptyList()
            BitcodeEmbeddingMode.BITCODE -> "-embed-bitcode" to listOf("-bitcode_bundle")
            BitcodeEmbeddingMode.MARKER -> "-embed-bitcode-marker" to listOf("-bitcode_bundle", "-bitcode_process_mode", "marker")
        }
        val swiftcBuildTypeArgs = when (kotlinFramework.buildType) {
            NativeBuildType.DEBUG -> emptyList()
            NativeBuildType.RELEASE -> listOf("-O", "-whole-module-optimization")
        }

        File(targetFramework, "Modules/$baseName.swiftmodule").mkdirs()
        copy {
            it.from(File(sourceFramework, "Headers/$baseName.h"))
            it.from(File(sourceFramework, "Modules/module.modulemap")) {
                it.filter { originalLine ->
                    originalLine.replace("\"$baseName.h\"", "\"../$baseName.h\"")
                }
            }
            it.into(workDir)
        }

        val otoolOutput = "/usr/bin/xcrun --sdk $sdk otool -l $sourceExecutable"
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
            Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR ->
                error("Unsupported target: ${kotlinFramework.target.konanTarget}")
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
            "$targetFramework/Modules/$baseName.swiftmodule/$targetTriple.swiftinterface",
            "-emit-module-path",
            "$targetFramework/Modules/$baseName.swiftmodule/$targetTriple.swiftmodule",
            "-emit-objc-header-path",
            "$targetFramework/Headers/$baseName-Swift.h",
            swiftcBitcodeArg,
            swiftcBuildTypeArgs,
            "-emit-library",
            "-static",
            "-enable-library-evolution",
            "-g",
            "-gdwarf-types",
            "-sdk",
            sdkPath,
            "-target",
            targetTriple.withOSVersion(targetVersion),
            sourceFiles.get().files.map { it.absolutePath },
        )

        if (kotlinFramework.isStatic) {
            xcrun(
                "--sdk", versionedSdk,
                "libtool",
                "-v",
                "-static",
                "-syslibroot", sdkPath,
                "-o", targetExecutable,
                sourceExecutable,
                workDir.get().file("lib$baseName.a").asFile.absolutePath,
            )
        } else {
            xcrun(
                "--sdk", versionedSdk,
                "ld",
                "-demangle",
                "-dylib",
                "-arch", targetTriple.architecture,
                "-platform_version", targetTriple.withoutVendorAndArch(), targetVersion, sdkVersion,
                "-all_load",
                "-lSystem", "-lobjc", "-lc++", "-framework", "Foundation",
                Xcode.current.swiftLibraryPaths(darwinTarget.sdk).flatMap { listOf("-L", it) },
                "-syslibroot", sdkPath,
                "-install_name", "@rpath/$baseName.framework/$relativeExecutablePath",
                "-dead_strip".takeIf { kotlinFramework.optimized },
                "-rpath", "/usr/lib/swift",
                ldBitcodeArgs,
                "-o", targetExecutable,
                sourceExecutable,
                darwinTarget.compilerRtLibrary(),
                workDir.get().file("lib$baseName.a").asFile.absolutePath,
            )

            if (!kotlinFramework.debuggable) {
                xcrun(
                    "--sdk", versionedSdk,
                    "dsymutil",
                    "-o", File(targetFramework.parentFile, "${targetFramework.name}.dSYM"),
                    targetExecutable,
                )
                if (kotlinFramework.optimized) {
                    xcrun(
                        "--sdk", versionedSdk,
                        "strip",
                        "-S",
                        targetExecutable,
                    )
                }
            }
        }

        val allFamilyTripletsButActive = DarwinTarget.allTargets.values.filter {
            it.konanTarget.family == kotlinFramework.target.konanTarget.family
        }.map { it.targetTriple } - targetTriple
        for (tripletToFake in allFamilyTripletsButActive) {
            logger.info("Faking triplet: $tripletToFake")
            copy {
                it.from("$targetFramework/Modules/$baseName.swiftmodule/") {
                    it.include("$targetTriple.*")
                }
                it.rename { it.replace(targetTriple.toString(), tripletToFake.toString()) }
                it.into("$targetFramework/Modules/$baseName.swiftmodule")
            }
        }
    }

    private fun Project.xcrun(vararg args: Any?) = exec {
        it.workingDir(workDir)
        it.executable = "/usr/bin/xcrun"
        it.args(args.flatMap {
            when (it) {
                is Iterable<*> -> it
                null -> emptyList()
                else -> listOf(it)
            }
        })
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

    companion object {
        val allTargets = listOf(
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

val Framework.darwinTarget: DarwinTarget
    get() = target.konanTarget.darwinTarget

val FrameworkDescriptor.darwinTarget: DarwinTarget
    get() = target.darwinTarget

val KonanTarget.darwinTarget: DarwinTarget
    get() = DarwinTarget.allTargets[this] ?: error("Unknown konan target: $this")

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
                    attributes = loadCommandAttribute.findAll(loadCommandMatch.groupValues[2])
                        .map { it.groupValues[1] to it.groupValues[2] }.toMap()
                )
            }.toList()
        }
    }
}
