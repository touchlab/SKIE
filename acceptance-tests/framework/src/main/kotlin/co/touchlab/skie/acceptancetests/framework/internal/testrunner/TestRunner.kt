package co.touchlab.skie.acceptancetests.framework.internal.testrunner

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.skie.acceptancetests.framework.TestNode
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.PluginConfigurationGenerator
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftCodeEnhancer
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftProgramRunner
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.util.CreatedFilesDescriptionFilter
import co.touchlab.skie.configuration.Configuration
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

internal class TestRunner(private val tempFileSystemFactory: TempFileSystemFactory) {

    @OptIn(ExperimentalTime::class)
    fun runTest(test: TestNode.Test): TestResultWithLogs {
        val tempFileSystem = tempFileSystemFactory.create(test)
        val testLogger = TestLogger()

        val measuredTest = measureTimedValue {
            with(tempFileSystem) {
                with(testLogger) {
                    IntermediateResult.Value(test.kotlinFiles)
                        .map { withJvmInlineAnnotation(it) }
                        .flatMap { compileKotlin(it, test.compilerArgumentsProvider) }
                        .zip { generateConfiguration(test.configFiles) }
                        .flatMap { linkKotlin(it.first, Configuration.deserialize(it.second.readText()), test.compilerArgumentsProvider) }
                        .pairWith { enhanceSwiftCode(test.swiftCode) }
                        .flatMap { compileSwift(it.first, it.second) }
                        .finalize { runSwift(it) }
                        .also { testLogger.prependTestInfo(test, tempFileSystem) }
                }
            }
        }
        val testResult = measuredTest.value

        val testResultWithLogs = testResult.withLogsAndDuration(testLogger, measuredTest.duration)
        testLogger.prependLine(testResultLine(test, testResultWithLogs))

        writeResult(test, testResultWithLogs)
        reportResult(test, testResultWithLogs)

        return testResultWithLogs
    }

    context(TempFileSystem)
    private fun withJvmInlineAnnotation(
        kotlinFiles: List<Path>,
    ): List<Path> {
        val packageRegex = Regex("package (.*)\\n")

        val kotlinDirectory = createDirectory("kotlin")

        val jvmInlineFiles = kotlinFiles
            .mapNotNull { packageRegex.find(it.readText())?.groupValues?.getOrNull(1) }
            .distinct()
            .mapIndexed { index, packageName ->
                kotlinDirectory.resolve("JvmInline_$index.kt").also {
                    it.writeText(
                        """
                            package $packageName
                            annotation class JvmInline
                        """.trimIndent()
                    )
                }
            }

        return kotlinFiles + jvmInlineFiles
    }

    context(TempFileSystem, TestLogger)
    private fun compileKotlin(
        kotlinFiles: List<Path>,
        compilerArgumentsProvider: CompilerArgumentsProvider,
    ): IntermediateResult<Path> =
        KotlinTestCompiler(this@TempFileSystem, this@TestLogger).compile(kotlinFiles, compilerArgumentsProvider)

    context(TempFileSystem)
    private fun generateConfiguration(
        configFiles: List<Path>,
    ): IntermediateResult<Path> =
        PluginConfigurationGenerator(this@TempFileSystem).generate(configFiles)

    context(TempFileSystem, TestLogger)
    private fun linkKotlin(
        klib: Path,
        configuration: Configuration,
        compilerArgumentsProvider: CompilerArgumentsProvider,
    ): IntermediateResult<Path> =
        KotlinTestLinker(this@TempFileSystem, this@TestLogger).link(klib, configuration, compilerArgumentsProvider)

    context(TempFileSystem)
    private fun enhanceSwiftCode(swiftCode: String): Path =
        SwiftCodeEnhancer(this@TempFileSystem).enhance(swiftCode)

    context(TempFileSystem, TestLogger)
    private fun compileSwift(
        kotlinFramework: Path,
        swiftFile: Path,
    ): IntermediateResult<Path> =
        SwiftTestCompiler(this@TempFileSystem, this@TestLogger).compile(kotlinFramework, swiftFile)

    context(TestLogger)
    private fun runSwift(binary: Path): TestResult =
        SwiftProgramRunner(this@TestLogger).runProgram(binary)

    private fun writeResult(test: TestNode.Test, result: TestResultWithLogs) {
        val resultAsText = test.expectedResult.hasSucceededAsString(result)

        test.resultPath.writeText(resultAsText)
        test.logPath.writeText(result.logs)
    }

    private fun reportResult(test: TestNode.Test, result: TestResultWithLogs) {
        if (test.expectedResult.hasSucceeded(result)) {
            print("\u001b[32m")
        } else {
            print("\u001b[31m")
        }

        print(testResultLine(test, result))

        println("\u001b[0m")
    }

    private fun testResultLine(test: TestNode.Test, result: TestResultWithLogs): String =
        "${test.fullName}: ${test.expectedResult.hasSucceededAsString(result)} (took ${result.duration.toString(DurationUnit.SECONDS, 2)})"


    private fun TestLogger.prependTestInfo(test: TestNode.Test, tempFileSystem: TempFileSystem) {
        val createdFilesDescription = tempFileSystem.describeCreatedFiles(CreatedFilesDescriptionFilter)
        this.prependSection("Created files", createdFilesDescription)

        val testFilesDescription = test.describeTestFiles()
        this.prependSection("Test files", testFilesDescription)

        this.prependLine(
            """
                Test name: ${test.fullName}
                To run only this test add env variable: acceptanceTest=${test.fullName}
            """.trimIndent()
        )
    }

    private fun TestNode.Test.describeTestFiles(): String =
        (listOf(this.path) + this.kotlinFiles).joinToString("\n") { it.absolutePathString() }

    private fun TestResult.withLogsAndDuration(testLogger: TestLogger, duration: Duration): TestResultWithLogs =
        TestResultWithLogs(
            this,
            duration,
            testLogger.toString(),
        )

    private fun ExpectedTestResult.hasSucceededAsString(result: TestResultWithLogs): String =
        if (this.hasSucceeded(result)) ExpectedTestResult.SUCCESS else ExpectedTestResult.FAILURE
}
