package co.touchlab.swikt.tests

import co.touchlab.swikt.plugin.SwiftCompileTask
import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.the
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import java.io.File
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import javax.inject.Inject

abstract class TestSuitePlugin: Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        tasks.register("swiktTestScenarios", TestScenariosTask::class.java).configure {
            outputs.upToDateWhen { false }
        }

        tasks.register("integrationTests", IntegrationTestTask::class.java).configure {
            configure()
        }
    }
}

abstract class IntegrationTestTask @Inject constructor(
    private val execHandleFactory: ExecHandleFactory,
): DefaultTask() {

    init {
        group = "verification"


    }

    fun configure() {
        outputs.upToDateWhen { false }

        project.the<KotlinMultiplatformExtension>().apply {

        }
    }

    @TaskAction
    fun runAllTests(): Unit = with(project) {
        listOf("Dynamic", "Static")
        listOf("iOS", "macOS", "watchOS_WatchKit_App", "tvOS")

        val workDir = File(buildDir, "test-suite").also { it.mkdirs() }

        val allFrameworks = subprojects
            .flatMap { it.the<KotlinMultiplatformExtension>().targets }
            .mapNotNull { it as? KotlinNativeTarget }
            .filter { it.konanTarget.family.isAppleFamily }
            .flatMap { it.binaries }
            .mapNotNull { it as? Framework }

        val results = allFrameworks.map { framework ->
            val darwinTarget = SwiftCompileTask.darwinTargets[framework.target.konanTarget]
                ?: error("Unsupported target: ${framework.target.konanTarget}")

            val schemeBuildType = if (framework.isStatic) "Static" else "Dynamic"
            val schemePlatform = when (framework.target.konanTarget.family) {
                Family.IOS -> "iOS"
                Family.TVOS -> "tvOS"
                Family.OSX -> "macOS"
                Family.WATCHOS -> "watchOS_WatchKit_App"
                Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR ->
                    error("Unsupported family: ${framework.target.konanTarget.family}")
            }
            val scheme = listOf("SwiktExample", schemePlatform, schemeBuildType).joinToString("_")
            val configuration = when (framework.buildType) {
                NativeBuildType.DEBUG -> "Debug"
                NativeBuildType.RELEASE -> "Release"
            }
            val arch = when (framework.target.konanTarget.architecture) {
                Architecture.X64 -> "x86_64"
                Architecture.X86 -> "i386"
                Architecture.ARM64 -> "arm64"
                Architecture.ARM32 -> "armv7"
                Architecture.MIPS32, Architecture.MIPSEL32, Architecture.WASM32 ->
                    error("Architecture ${framework.target.konanTarget.architecture} not supported.")
            }
            val logDir = File(workDir, "${schemeBuildType.toLowerCase()}/${framework.target.targetName}/${configuration.toLowerCase()}")
                .also { it.mkdirs() }
            val logFile = File(logDir, "xcodebuild.log")
            logger.lifecycle("Archiving: scheme=$scheme, sdk=${darwinTarget.sdk}, configuration=$configuration, arch=$arch")

            val xcbeautifyExecutable = File(projectDir, "bin/xcbeautify")
            val (xcbeautify, outputStream) = if (xcbeautifyExecutable.exists()) {
                val pipedInputStream = PipedInputStream()
                val pipedOutputStream = PipedOutputStream(pipedInputStream)

                val xcbeautify = execHandleFactory.newExec().apply {
                    executable = xcbeautifyExecutable.absolutePath
                    standardInput = pipedInputStream
                    standardOutput = System.out
                    errorOutput = System.err
                }.build().start()

                val outputStream = TeeOutputStream(logFile.outputStream(), pipedOutputStream)
                xcbeautify to outputStream
            } else {
                null to logFile.outputStream()
            }

            try {
                xcrun(
                    outputStream,
                    "-sdk", darwinTarget.sdk,
                    "xcodebuild", "-workspace", "SwiktExample.xcworkspace/",
                    "-scheme", scheme,
                    "-sdk", darwinTarget.sdk,
                    "-configuration", configuration,
                    "-arch", arch,
                    "-allowProvisioningUpdates",
                    "archive",
                )
            } catch (t: Throwable) {
                logger.error("Archiving failed. See the log at $logFile.")
                // TODO: Do we want to throw right away or run the rest first?
                return@map TestResult.Failure.ArchiveFailed(t)
            } finally {
                outputStream.close()
                xcbeautify?.waitForFinish()
            }

            TestResult.Success()
        }

        val failures = results.mapNotNull { it as? TestResult.Failure }
        if (failures.isNotEmpty()) {
            throw TestSuiteException("${failures.count()} out of ${results.count()} tests failed.").also { e ->
                failures.forEach {
                    e.addSuppressed(it.cause)
                }
            }
        }
    }

    private fun Project.xcrun(output: OutputStream, vararg args: Any?) = exec {
        workingDir(File(projectDir, "app"))
        executable = "/usr/bin/xcrun"
        standardOutput = output
        errorOutput = output
        args(args.flatMap {
            when (it) {
                is Iterable<*> -> it
                null -> emptyList()
                else -> listOf(it)
            }
        })
    }

    sealed class TestResult {
        class Success(): TestResult()

        sealed class Failure: TestResult() {
            abstract val cause: Throwable

            class ArchiveFailed(override val cause: Throwable): Failure()
        }
    }
}

class TestSuiteException(override val message: String?): RuntimeException(message)
