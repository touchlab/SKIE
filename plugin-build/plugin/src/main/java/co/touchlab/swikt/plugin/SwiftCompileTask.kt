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

        val frameworkFile = kotlinFramework.outputFile
        val frameworkPath = frameworkFile.absolutePath
        val baseName = kotlinFramework.baseName
        val (targetTriple, sdk) = darwinTripletsAndSdks[kotlinFramework.target.konanTarget] ?: return@with

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

        val otoolOutput = "/usr/bin/xcrun --sdk $sdk otool -l $frameworkPath/$baseName"
            .let(ProcessGroovyMethods::execute)
            .let(ProcessGroovyMethods::getText)
            .trim()

        val targetVersion = minosRegex.find(otoolOutput)?.groupValues?.getOrNull(1) ?: error("Couldn't determine target version using otool!")
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

        logger.warn("KF Static? ${kotlinFramework.isStatic}")
        if (kotlinFramework.isStatic) {
            xcrun(
                "--sdk", versionedSdk,
                "libtool",
                "-v",
                "-static",
                "-syslibroot", sdkPath,
                "-o", "${frameworkPath}/${baseName}_merged",
                "${frameworkPath}/${baseName}",
                outputDir.get().file("lib${baseName}.a").asFile.absolutePath,
            )
        } else {
            xcrun(
                "--sdk", versionedSdk,
                "ld",
                "-dylib",
                "-arch", "arm64",
                "-platform_version", "ios-simulator", targetVersion, sdkVersion,
                "-ObjC",
                "-syslibroot", sdkPath,
                "-undefined", "dynamic_lookup",
                "-install_name", "@rpath/$baseName.framework/$baseName",
                "-o", "${frameworkPath}/${baseName}_merged",
                "${frameworkPath}/${baseName}",
                outputDir.get().file("lib${baseName}.a").asFile.absolutePath,
            )
        }
        delete("${frameworkPath}/${baseName}")
        File("${frameworkPath}/${baseName}_merged").renameTo(File("${frameworkPath}/${baseName}"))
        File("${frameworkPath}/Headers/${baseName}.h").appendText("\n#import \"${baseName}-Swift.h\"")

        val allFamilyTripletsButActive = darwinTripletsAndSdks.filter { it.key.family == kotlinFramework.target.konanTarget.family }.values.map { it.first } - targetTriple
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

    private fun Project.xcrun(vararg args: Any) = exec {
        it.workingDir(outputDir)
        it.executable = "/usr/bin/xcrun"
        it.args(*args)
    }

    companion object {
        val minosRegex = """minos\s+([\d.]+)""".toRegex()
        val sdkRegex = """sdk\s+([\d.]+)""".toRegex()

        val darwinTripletsAndSdks = mapOf(
            KonanTarget.IOS_ARM32 to (TargetTriple.fromString("armv7-apple-ios") to "iphoneos"),
            KonanTarget.IOS_ARM64 to (TargetTriple.fromString("arm64-apple-ios") to "iphoneos"),
            KonanTarget.IOS_X64 to (TargetTriple.fromString("x86_64-apple-ios-simulator") to "iphonesimulator"),
            KonanTarget.IOS_SIMULATOR_ARM64 to (TargetTriple.fromString("arm64-apple-ios-simulator") to "iphonesimulator"),
            KonanTarget.WATCHOS_ARM32 to (TargetTriple.fromString("armv7k-apple-watchos") to "watchos"),
            KonanTarget.WATCHOS_ARM64 to (TargetTriple.fromString("arm64_32-apple-watchos") to "watchos"),
            KonanTarget.WATCHOS_X86 to (TargetTriple.fromString("i386-apple-watchos-simulator") to "watchsimulator"),
            KonanTarget.WATCHOS_X64 to (TargetTriple.fromString("x86_64-apple-watchos-simulator") to "watchsimulator"),
            KonanTarget.WATCHOS_SIMULATOR_ARM64 to (TargetTriple.fromString("arm64-apple-watchos-simulator") to "watchsimulator"),
            KonanTarget.TVOS_ARM64 to (TargetTriple.fromString("arm64-apple-tvos") to "appletvos"),
            KonanTarget.TVOS_X64 to (TargetTriple.fromString("x86_64-apple-tvos-simulator") to "appletvsimulator"),
            KonanTarget.TVOS_SIMULATOR_ARM64 to (TargetTriple.fromString("arm64-apple-tvos-simulator") to "appletvsimulator"),
            KonanTarget.MACOS_X64 to (TargetTriple.fromString("x86_64-apple-macos") to "macosx"),
            KonanTarget.MACOS_ARM64 to (TargetTriple.fromString("arm64-apple-macos") to "macosx"),
        )
    }
}
