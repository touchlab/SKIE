package co.touchlab.swikt.tests

import co.touchlab.swikt.plugin.DarwinTarget
import co.touchlab.swikt.plugin.darwinTarget
import co.touchlab.swikt.plugin.isSimulator
import org.apache.tools.ant.util.TeeOutputStream
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.get
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.setProperty
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodInstallTask
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

    @get:Input
    val onlySdks = project.objects.setProperty<String>()

    @get:Input
    val onlyArchs = project.objects.setProperty<Architecture>()

    @get:Input
    val onlyBuildType = project.objects.setProperty<NativeBuildType>()

    @get:Input
    val onlyLinkType = project.objects.setProperty<String>()

    init {
        group = "verification"
    }

    fun configure() {
        outputs.upToDateWhen { false }

        dependsOn(*project.subprojects.map { it.tasks.withType<PodInstallTask>() }.toTypedArray())

        project.the<KotlinMultiplatformExtension>().apply {

        }

        val onlySdks: String? by project
        val onlyArchs: String? by project
        val onlyBuildType: String? by project
        val onlyLinkType: String? by project

        onlySdks?.split(',')?.map { it.trim() }?.let(this.onlySdks::set)
        onlyArchs?.split(',')?.map { Architecture.valueOf(it.trim()) }?.let(this.onlyArchs::set)
        onlyBuildType?.split(',')?.map { NativeBuildType.valueOf(it.trim()) }?.let(this.onlyBuildType::set)
        onlyLinkType?.split(',')?.map { it.trim() }?.let(this.onlyLinkType::set)
    }

    @TaskAction
    fun runAllTests(): Unit = with(project) {
        val workDir = File(buildDir, "test-suite").also { it.mkdirs() }

        val allFrameworks = subprojects
            .flatMap { it.the<KotlinMultiplatformExtension>().targets }
            .mapNotNull { it as? KotlinNativeTarget }
            .filter { it.konanTarget.family.isAppleFamily }
            .flatMap { it.binaries }
            .mapNotNull { it as? Framework }

        val executions: List<TestExecution> = allFrameworks.map { framework ->
            val darwinTarget = framework.darwinTarget
            val schemeLinkType = if (framework.isStatic) "Static" else "Dynamic"

            fun exec(result: TestResult): TestExecution {
                return TestExecution(
                    linkType = schemeLinkType,
                    sdk = darwinTarget.sdk,
                    buildType = framework.buildType,
                    architecture = framework.target.konanTarget.architecture,
                    result = result,
                )
            }

            onlySdks.get().apply {
                if (isNotEmpty() && !contains(darwinTarget.sdk)) {
                    return@map exec(TestResult.Skipped("SDK ${darwinTarget.sdk} skipped."))
                }
            }

            onlyArchs.get().apply {
                if (isNotEmpty() && !contains(darwinTarget.konanTarget.architecture)) {
                    return@map exec(TestResult.Skipped("Arch ${darwinTarget.konanTarget.architecture} skipped."))
                }
            }

            onlyBuildType.get().apply {
                if (isNotEmpty() && !contains(framework.buildType)) {
                    return@map exec(TestResult.Skipped("BuildType ${framework.buildType} skipped."))
                }
            }

            onlyLinkType.get().apply {
                if (isNotEmpty() && !contains(schemeLinkType)) {
                    return@map exec(TestResult.Skipped("LinkType $schemeLinkType skipped."))
                }
            }

            val archiveSchemePlatform = when (framework.target.konanTarget.family) {
                Family.IOS -> "iOS"
                Family.TVOS -> "tvOS"
                Family.OSX -> "macOS"
                Family.WATCHOS -> "watchOS_WatchKit_App"
                Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR ->
                    error("Unsupported family: ${framework.target.konanTarget.family}")
            }
            val testSchemePlatform = when (framework.target.konanTarget.family) {
                Family.IOS, Family.TVOS, Family.OSX, Family.WATCHOS -> archiveSchemePlatform
                Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR ->
                    error("Unsupported family: ${framework.target.konanTarget.family}")
            }
            val archiveScheme = listOf("SwiktExample", archiveSchemePlatform, schemeLinkType).joinToString("_")
            val testScheme = listOf("SwiktExample", testSchemePlatform, schemeLinkType).joinToString("_")
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
            val shouldRunTests = when (framework.target.konanTarget.family) {
                Family.IOS -> darwinTarget.targetTriple.isSimulator
                Family.TVOS -> darwinTarget.targetTriple.isSimulator
                Family.OSX -> true
                Family.WATCHOS -> darwinTarget.targetTriple.isSimulator
                Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR ->
                    error("Unsupported family: ${framework.target.konanTarget.family}")
            }
            val shouldRunArchive = !darwinTarget.targetTriple.isSimulator && framework.buildType == NativeBuildType.RELEASE
            val platformWorkDir = File(workDir, "${schemeLinkType.toLowerCase()}/${framework.target.targetName}/${configuration.toLowerCase()}")
                .also { it.mkdirs() }

            val buildLogFile = File(platformWorkDir, "xcodebuild_build.log")

            logger.lifecycle("Building: scheme=$testScheme, sdk=${darwinTarget.sdk}, configuration=$configuration, arch=$arch")
            xcbeautify(buildLogFile) { outputStream ->
                try {
                    xcrun(
                        outputStream,
                        "-sdk", darwinTarget.sdk,
                        "xcodebuild", "-workspace", "SwiktExample.xcworkspace/",
                        "-scheme", testScheme,
                        "-sdk", darwinTarget.sdk,
                        "-configuration", configuration,
                        "-arch", arch,
                        "clean", "build-for-testing",
                    )
                } catch (t: Throwable) {
                    logger.error("Building for test failed. See the log at $buildLogFile")
                    return@map exec(TestResult.Failure.TestsFailed(t))
                }
            }

            if (shouldRunTests) {
                val testLogFile = File(platformWorkDir, "xcodebuild_test.log")

                withSimulatorIfNeeded(darwinTarget) { destination ->
                    logger.lifecycle("Testing: scheme=$testScheme, sdk=${darwinTarget.sdk}, configuration=$configuration, arch=$arch, destination=$destination")
                    xcbeautify(testLogFile) { outputStream ->
                        try {
                            xcrun(
                                outputStream,
                                "-sdk", darwinTarget.sdk,
                                "xcodebuild", "-workspace", "SwiktExample.xcworkspace/",
                                "-scheme", testScheme,
                                "-sdk", darwinTarget.sdk,
                                "-configuration", configuration,
                                "-destination", destination,
                                "test-without-building",
                            )
                        } catch (t: Throwable) {
                            logger.error("Testing failed. See the log at $testLogFile")
                            return@map exec(TestResult.Failure.TestsFailed(t))
                        }
                    }
                }
            }

            if (shouldRunArchive) {
                val archiveLogFile = File(platformWorkDir, "xcodebuild_archive.log")
                val archiveFile = File(platformWorkDir, "SwiftKtExample.xcarchive")
                logger.lifecycle("Archiving: scheme=$archiveScheme, sdk=${darwinTarget.sdk}, configuration=$configuration, arch=$arch")

                xcbeautify(archiveLogFile) { outputStream ->
                    try {
                        xcrun(
                            outputStream,
                            "-sdk", darwinTarget.sdk,
                            "xcodebuild", "-workspace", "SwiktExample.xcworkspace/",
                            "-scheme", archiveScheme,
                            "-sdk", darwinTarget.sdk,
                            "-configuration", configuration,
                            "-arch", arch,
                            "-archivePath", archiveFile,
                            "archive",
                        )
                    } catch (t: Throwable) {
                        logger.error("Archiving failed. See the log at $archiveLogFile")
                        return@map exec(TestResult.Failure.ArchiveFailed(t))
                    }
                }

                logger.lifecycle("Archiving succeeded. Now validating archive at $archiveFile")

                // xcrun altool --validate-app -f archiveFile
            }

            exec(TestResult.Success())
        }

        val testStatus = services.get<StyledTextOutputFactory>().create("test-status", LogLevel.QUIET)
        val failures = executions.mapNotNull { it.result as? TestResult.Failure }
        val skippedCount = executions.count { it.result is TestResult.Skipped }
        val successCount = executions.count() - skippedCount - failures.count()
        testStatus.style(StyledTextOutput.Style.Header).text("TESTS COMPLETED. ")
        val exception = if (failures.isNotEmpty()) {
            testStatus.style(StyledTextOutput.Style.FailureHeader).text("${failures.count()} failed, ")
            TestSuiteException("${failures.count()} out of ${executions.count() - skippedCount} tests failed ($skippedCount tests skipped).").also { e ->
                failures.forEach {
                    e.addSuppressed(it.cause)
                }
            }
        } else {
            null
        }

        if (skippedCount > 0) {
            testStatus.style(StyledTextOutput.Style.Header).text("$skippedCount skipped, ")
        }

        testStatus.style(StyledTextOutput.Style.SuccessHeader).text("$successCount succeeded.").println()
        val sortedExecutions = executions.sortedWith(
            compareBy(
                {
                    when (it.result) {
                        is TestResult.Failure -> 0
                        is TestResult.Success -> 1
                        is TestResult.Skipped -> 2
                    }
                },
                { it.linkType },
                { it.sdk },
            )
        )

        sortedExecutions.forEach { execution ->
            val (state, style, headerStyle) = when (execution.result) {
                is TestResult.Success -> Triple("SUCCESS", StyledTextOutput.Style.Success, StyledTextOutput.Style.SuccessHeader)
                is TestResult.Skipped -> Triple("SKIPPED", StyledTextOutput.Style.Normal, StyledTextOutput.Style.Header)
                is TestResult.Failure -> Triple("FAILURE", StyledTextOutput.Style.Failure, StyledTextOutput.Style.FailureHeader)
            }
            testStatus
                .style(headerStyle).text("[$state] ")
                .style(style).text("${execution.linkType} | ${execution.sdk} | ${execution.buildType} | ${execution.architecture}")
                .println()
        }

        if (exception != null) {
            throw exception
        }
    }

    private inline fun <T> Project.xcbeautify(logFile: File, block: (stream: OutputStream) -> T): T {
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
            return block(outputStream)
        } finally {
            outputStream.close()
            xcbeautify?.waitForFinish()
        }
    }

    private inline fun <T> Project.withSimulatorIfNeeded(darwinTarget: DarwinTarget, block: (destination: String) -> T): T {
        val (createdSimulatorIds, destination) = when (darwinTarget.konanTarget.family) {
            Family.OSX -> emptyList<String>() to "platform=OS X,arch=${darwinTarget.targetTriple.architecture}"
            Family.IOS -> {
                val simulatorId = listOf("/usr/bin/xcrun", "simctl", "create", "swiftkt_tests_${darwinTarget.sdk}", "iPhone 13 Pro")
                    .let(ProcessGroovyMethods::execute)
                    .let(ProcessGroovyMethods::getText).trim()

                listOf(simulatorId) to "platform=iOS Simulator,id=$simulatorId"
            }
            Family.TVOS -> {
                val simulatorId = listOf("/usr/bin/xcrun", "simctl", "create", "swiftkt_tests_${darwinTarget.sdk}", "Apple TV 4K (2nd generation)")
                    .let(ProcessGroovyMethods::execute)
                    .let(ProcessGroovyMethods::getText).trim()
                listOf(simulatorId) to "platform=tvOS Simulator,id=$simulatorId"
            }
            Family.WATCHOS -> {
                val phoneSimulatorId = listOf("/usr/bin/xcrun", "simctl", "create", "swiftkt_tests_${darwinTarget.sdk}_phone", "iPhone 13 Pro")
                    .let(ProcessGroovyMethods::execute)
                    .let(ProcessGroovyMethods::getText).trim()
                val watchSimulatorId = listOf("/usr/bin/xcrun", "simctl", "create", "swiftkt_tests_${darwinTarget.sdk}_watch", "Apple Watch Series 7 - 45mm")
                    .let(ProcessGroovyMethods::execute)
                    .let(ProcessGroovyMethods::getText).trim()
                xcrun(System.out, "simctl", "pair", watchSimulatorId, phoneSimulatorId)
                listOf(phoneSimulatorId, watchSimulatorId) to "platform=iOS Simulator,id=$phoneSimulatorId"
            }
            Family.LINUX, Family.MINGW, Family.ANDROID, Family.WASM, Family.ZEPHYR ->
                error("Unsupported family: ${darwinTarget.konanTarget.family}")
        }
        try {
            return block(destination)
        } finally {
            createdSimulatorIds.mapNotNull { simulatorId ->
                try {
                    xcrun(System.out, "simctl", "delete", simulatorId)
                    null
                } catch (t: Throwable) {
                    t
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

    data class TestExecution(
        val linkType: String,
        val sdk: String,
        val buildType: NativeBuildType,
        val architecture: Architecture,
        val result: TestResult,
    )
    sealed class TestResult {
        class Success(): TestResult()

        class Skipped(val reason: String): TestResult()

        sealed class Failure: TestResult() {
            abstract val cause: Throwable

            class TestsFailed(override val cause: Throwable): Failure()

            class ArchiveFailed(override val cause: Throwable): Failure()
        }
    }
}

class TestSuiteException(override val message: String?): RuntimeException(message)
