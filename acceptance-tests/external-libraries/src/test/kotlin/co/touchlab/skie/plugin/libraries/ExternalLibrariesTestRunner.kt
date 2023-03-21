package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import co.touchlab.skie.acceptancetests.framework.testStream
import co.touchlab.skie.configuration.Configuration
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import kotlin.io.path.toPath
import kotlin.io.path.writeText
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue


class ExternalLibrariesTestRunner(
    private val testTmpDir: File,
    private val testFilter: TestFilter,
    private val skieConfiguration: Configuration?,
    private val scopeSuffix: String,
) {
    fun runTests(scope: FunSpec, tests: List<ExternalLibraryTest>) {
        val channel = Channel<Map<ExternalLibraryTest, TestResultWithLogs>>()
        scope.concurrency = 2

        scope.test("Evaluation${scopeSuffix}") {
            val testCompletionTracking = AtomicInteger(0)
            val filteredTests = tests
                .filter { testFilter.shouldBeEvaluated(it) }
            val results = filteredTests
                .testStream()
                .map {
                    val result = runTest(it) { "${testCompletionTracking.incrementAndGet()}/${filteredTests.size}" }
                    it to result
                }
                .collect(Collectors.toList())
                .toMap()

            channel.send(results)
            channel.close()
        }

        scope.context("Results${scopeSuffix}") {
            val testResult = channel.receive()
            val resultProcessor = ExternalLibrariesTestResultProcessor(testTmpDir = testTmpDir)
            resultProcessor.processResult(this, testResult)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun runTest(test: ExternalLibraryTest, positionProvider: () -> String): TestResultWithLogs {
        val tempDirectory = test.outputPath
        tempDirectory.toFile().deleteRecursively()
        tempDirectory.toFile().mkdirs()

        val tempFileSystem = TempFileSystem(tempDirectory)
        val sourceFiles = produceSourceFilesIn(tempDirectory)

        val testLogger = TestLogger()

        val compilerArgumentsProvider = CompilerArgumentsProvider(
            dependencies = test.input.files,
            exportedDependencies = test.input.exportedFiles,
            target = CompilerArgumentsProvider.Target.IOS_ARM64,
        )
        val swiftMainFile = javaClass.classLoader.getResource("main.swift")!!.toURI().toPath()

        val measuredTest = measureTimedValue {
            IntermediateResult.Value(Unit)
                .flatMap {
                    val compiler = KotlinTestCompiler(tempFileSystem, testLogger)
                    compiler.compile(
                        kotlinFiles = sourceFiles,
                        compilerArgumentsProvider = compilerArgumentsProvider,
                    )
                }
                .flatMap {
                    val linker = KotlinTestLinker(tempFileSystem, testLogger)
                    linker.link(
                        it,
                        skieConfiguration,
                        compilerArgumentsProvider,
                    )
                }
                .flatMap {
                    val swiftCompiler = SwiftTestCompiler(tempFileSystem, testLogger, compilerArgumentsProvider.target)
                    swiftCompiler.compile(it, swiftMainFile)
                }
                .finalize {
                    TestResult.Success
                }
        }
        val testResult = measuredTest.value

        testLogger.prependTestInfo(test)

        val position = positionProvider()
        val testResultWithLogs = testResult.withLogsAndDuration(testLogger, measuredTest.duration)
        testLogger.prependLine(testResultLine(test, testResultWithLogs, position))

        writeResult(test, testResultWithLogs)
        reportResult(test, testResultWithLogs, position)
        return testResultWithLogs
    }

    private fun writeResult(test: ExternalLibraryTest, result: TestResultWithLogs) {
        val resultAsText = test.expectedResult.hasSucceededAsString(result)

        test.resultPath.writeText(resultAsText)
        test.durationPath.writeText(result.duration.toIsoString())
        test.logPath.writeText(result.logs)
    }

    private fun reportResult(test: ExternalLibraryTest, result: TestResultWithLogs, position: String) {
        val color = if (test.expectedResult.hasSucceeded(result)) {
            "\u001b[32m"
        } else {
            "\u001b[31m"
        }
        val colorReset = "\u001b[0m"

        println(color + testResultLine(test, result, position) + colorReset)
    }

    private fun testResultLine(test: ExternalLibraryTest, result: TestResultWithLogs, position: String): String {
        return "${test.fullName}: ${test.expectedResult.hasSucceededAsString(result)} (${position}, took ${result.duration.toString(DurationUnit.SECONDS, 2)})"
    }

    private fun TestLogger.prependTestInfo(test: ExternalLibraryTest) {
        prependLine(
            """
                Test: ${test.library} [${test.index}]
                To run only this test add env variable: libraryTest=${test.library.replace(".", "\\.")}
            """.trimIndent()
        )
    }

    private fun TestResult.withLogsAndDuration(testLogger: TestLogger, duration: Duration): TestResultWithLogs =
        TestResultWithLogs(
            this,
            duration,
            testLogger.toString(),
        )

    private fun ExpectedTestResult.hasSucceededAsString(result: TestResultWithLogs): String =
        if (this.hasSucceeded(result)) ExpectedTestResult.SUCCESS else ExpectedTestResult.FAILURE

    private fun produceSourceFilesIn(tempDirectory: Path): List<Path> = listOf(
        tempDirectory.resolve("Experimental.kt").apply {
            writeText("""
                package kotlin

                import kotlin.annotation.AnnotationRetention.SOURCE
                import kotlin.annotation.AnnotationRetention.BINARY
                import kotlin.annotation.AnnotationTarget.*
                import kotlin.reflect.KClass

                @Target(ANNOTATION_CLASS)
                @Retention(BINARY)
                public annotation class Experimental(val level: Level = Level.ERROR) {
                    public enum class Level {
                        WARNING,
                        ERROR,
                    }
                }

                @Target(
                    CLASS, PROPERTY, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, EXPRESSION, FILE, TYPEALIAS
                )
                @Retention(SOURCE)
                public annotation class UseExperimental(
                    vararg val markerClass: KClass<out Annotation>
                )
            """.trimIndent())
        },
    )
}
