package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.TestResultReporter
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.plugin.libraries.dependencies.kotlin.KotlinDependencyProvider
import co.touchlab.skie.plugin.libraries.dependencies.kotlin.SkieRuntimeDependencyProvider
import co.touchlab.skie.plugin.libraries.dependencies.swift.ExpectedMissingSymbolsProvider
import co.touchlab.skie.plugin.libraries.dependencies.swift.SwiftFrameworkDependencyProvider
import co.touchlab.skie.plugin.libraries.dependencies.swift.SwiftLibraryDependencyProvider
import co.touchlab.skie.plugin.libraries.library.Artifacts
import co.touchlab.skie.plugin.libraries.lockfile.LockfileUpdater
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.toPath
import kotlin.io.path.writeText
import kotlin.time.Duration
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

class SingleLibraryTestRunner(
    private val skieConfigurationData: CompilerSkieConfigurationData?,
    private val kotlinDependencyProvider: KotlinDependencyProvider,
    private val lockfileUpdater: LockfileUpdater?,
) {

    private val isDependencyResolutionEnabled = "skipDependencyResolution" !in TestProperties
    private val isKotlinCompilationEnabled = isDependencyResolutionEnabled && "skipKotlinCompilation" !in TestProperties
    private val isSwiftCompilationEnabled = isKotlinCompilationEnabled && "skipSwiftCompilation" !in TestProperties

    private val skippedPathResult = IntermediateResult.Value(Path("SKIPPED"))

    fun runTest(test: ExternalLibraryTest, testResultReporter: TestResultReporter): TestResultWithLogs {
        val tempFileSystem = TempFileSystem(test.outputPath)

        val sourceFiles = generateKotlinSourceFiles(tempFileSystem)

        val swiftMainFile = generateSwiftSourceFile()

        val testLogger = TestLogger()

        val kotlinArtifacts = if (isDependencyResolutionEnabled) {
            kotlinDependencyProvider.resolveArtifacts(test.library.component, test.library.dependencies)
        } else {
            Result.failure(IllegalStateException("Dependency resolution is disabled."))
        }

        val measuredTest = measureTimedValue {
            runCompilation(test, tempFileSystem, testLogger, sourceFiles, swiftMainFile, kotlinArtifacts)
        }

        return processTestResult(test, measuredTest, testLogger, testResultReporter, tempFileSystem, kotlinArtifacts)
    }

    private fun runCompilation(
        test: ExternalLibraryTest,
        tempFileSystem: TempFileSystem,
        testLogger: TestLogger,
        sourceFiles: List<Path>,
        swiftMainFile: Path,
        kotlinArtifacts: Result<Artifacts>,
    ): TestResult {
        val headerValidationSwiftFile = tempFileSystem.createFile("HeaderValidation.swift")

        val testSpecificSkieConfiguration = createTestSpecificSkieConfiguration(headerValidationSwiftFile)

        return kotlinArtifacts
            .fold(
                onSuccess = { IntermediateResult.Value(it) },
                onFailure = {
                    if (isDependencyResolutionEnabled) {
                        IntermediateResult.Error(TestResult.TestInitializationError(it))
                    } else {
                        IntermediateResult.Value(Artifacts.empty)
                    }
                },
            )
            .map {
                SkieRuntimeDependencyProvider.withSkieRuntimeIfRequired(it, testSpecificSkieConfiguration)
            }
            .map { artifacts ->
                CompilerArgumentsProvider(
                    dependencies = artifacts.all.map { it.path.absolutePathString() },
                    exportedDependencies = artifacts.exported.map { it.path.absolutePathString() },
                    target = CompilerArgumentsProvider.Target.IOS_ARM64,
                )
            }
            .flatMap { compilerArgumentsProvider ->
                val compiler = KotlinTestCompiler(tempFileSystem, testLogger)

                val compilerResult = if (isKotlinCompilationEnabled) {
                    compiler.compile(
                        kotlinFiles = sourceFiles,
                        compilerArgumentsProvider = compilerArgumentsProvider,
                    )
                } else {
                    skippedPathResult
                }

                compilerResult.pairWith { compilerArgumentsProvider }
            }
            .flatMap { (primaryKlib, compilerArgumentsProvider) ->
                val linker = KotlinTestLinker(tempFileSystem, testLogger)

                val linkerResult = if (isKotlinCompilationEnabled) {
                    linker.link(
                        primaryKlib,
                        testSpecificSkieConfiguration,
                        compilerArgumentsProvider,
                    )
                } else {
                    skippedPathResult
                }

                linkerResult.pairWith { compilerArgumentsProvider }
            }
            .flatMap { (kotlinFramework, compilerArgumentsProvider) ->
                val additionalSwiftArguments = getSwiftLinkerArguments(test)

                val swiftCompiler = SwiftTestCompiler(tempFileSystem, testLogger, compilerArgumentsProvider.target, additionalSwiftArguments)

                if (isSwiftCompilationEnabled) {
                    swiftCompiler.compile(kotlinFramework, listOf(swiftMainFile, headerValidationSwiftFile))
                } else {
                    skippedPathResult
                }
            }
            .finalize {
                TestResult.Success
            }
    }

    private fun createTestSpecificSkieConfiguration(headerValidationSwiftFile: Path): CompilerSkieConfigurationData? =
        skieConfigurationData?.let {
            createBaseConfiguration(headerValidationSwiftFile) + skieConfigurationData
        }

    private fun createBaseConfiguration(headerValidationSwiftFile: Path): CompilerSkieConfigurationData =
        CompilerSkieConfigurationData(
            groups = listOf(
                CompilerSkieConfigurationData.Group(
                    target = "",
                    overridesAnnotations = false,
                    items = mapOf(
                        "TestConfigurationKeys.VerifyFrameworkHeaderPhaseSwiftFilePath" to headerValidationSwiftFile.absolutePathString(),
                    ),
                ),
            ),
        )

    private fun processTestResult(
        test: ExternalLibraryTest,
        measuredTest: TimedValue<TestResult>,
        testLogger: TestLogger,
        testResultReporter: TestResultReporter,
        tempFileSystem: TempFileSystem,
        kotlinArtifacts: Result<Artifacts>,
    ): TestResultWithLogs {
        val testResultWithLogs = getTestResultWithLogs(test, measuredTest, testLogger, testResultReporter)

        testResultReporter.reportResult(test, testResultWithLogs)

        writeResult(test, testResultWithLogs)

        deleteTemporaryFiles(test, testResultWithLogs, tempFileSystem)

        updateLockfile(test, testResultWithLogs, kotlinArtifacts)

        return testResultWithLogs
    }

    private fun getSwiftLinkerArguments(test: ExternalLibraryTest): List<String> {
        val libraries = SwiftLibraryDependencyProvider.getSystemLibraries(test.library.component)
            .flatMap { listOf("-Xlinker", "-l${it.name}") }

        val frameworks = SwiftFrameworkDependencyProvider.getSystemFrameworks(test.library.component)
            .flatMap { listOf("-framework", it.name) }

        val symbols = ExpectedMissingSymbolsProvider.missingSymbols.flatMap {
            listOf("-Xlinker", "-U", "-Xlinker", it)
        }

        return libraries + frameworks + symbols
    }

    private fun getTestResultWithLogs(
        test: ExternalLibraryTest,
        measuredTest: TimedValue<TestResult>,
        testLogger: TestLogger,
        testResultReporter: TestResultReporter,
    ): TestResultWithLogs {
        val testResult = measuredTest.value

        val initialTestResultWithLogs = testResult.withLogsAndDuration(testLogger, measuredTest.duration)

        testLogger.prependLine(testResultReporter.testResultLine(test, initialTestResultWithLogs))

        testLogger.prependTestInfo(test)

        return testResult.withLogsAndDuration(testLogger, measuredTest.duration)
    }

    private fun writeResult(test: ExternalLibraryTest, result: TestResultWithLogs) {
        val resultAsText = test.expectedResult.hasSucceededAsString(result)

        test.resultPath.writeText(resultAsText)
        test.durationPath.writeText(result.duration.toIsoString())
        test.logPath.writeText(result.logs)
    }

    private fun TestLogger.prependTestInfo(test: ExternalLibraryTest) {
        prependLine(
            """
                Test: ${test.library.component} [${test.library.index}]
                To run only this test add env variable: libraryTest=${test.library.component.coordinate.replace(".", "\\.")}
            """.trimIndent(),
        )
    }

    private fun TestResult.withLogsAndDuration(testLogger: TestLogger, duration: Duration): TestResultWithLogs =
        TestResultWithLogs(
            this,
            duration,
            testLogger.toString(),
        )

    private fun updateLockfile(test: ExternalLibraryTest, result: TestResultWithLogs, kotlinArtifacts: Result<Artifacts>) {
        lockfileUpdater?.add(test, result, kotlinArtifacts)
    }

    private fun ExpectedTestResult.hasSucceededAsString(result: TestResultWithLogs): String =
        if (this.hasSucceeded(result)) ExpectedTestResult.SUCCESS else ExpectedTestResult.FAILURE

    private fun generateKotlinSourceFiles(tempFileSystem: TempFileSystem): List<Path> = listOf(
        tempFileSystem.createFile("Experimental.kt").apply {
            writeText(
                """
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
            """.trimIndent(),
            )
        },
    )

    private fun generateSwiftSourceFile(): Path =
        javaClass.classLoader.getResource("main.swift")!!.toURI().toPath()

    private fun deleteTemporaryFiles(test: ExternalLibraryTest, result: TestResultWithLogs, tempFileSystem: TempFileSystem) {
        if (test.expectedResult.hasSucceeded(result)) {
            tempFileSystem.deleteCreatedFiles()
        }
    }
}
