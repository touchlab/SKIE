package co.touchlab.skie.acceptancetests.framework.internal.testrunner

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.skie.acceptancetests.framework.TestNode
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.TestResultReporter
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.hasSucceededAsString
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.PluginConfigurationGenerator
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftCodeEnhancer
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftProgramRunner
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.util.CreatedFilesDescriptionFilter
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration
import kotlin.time.measureTimedValue

internal class TestRunner(
    private val tempFileSystemFactory: TempFileSystemFactory,
) {

    fun runTest(test: TestNode.Test, testResultReporter: TestResultReporter): TestResultWithLogs {
        val tempFileSystem = tempFileSystemFactory.create(test)
        val testLogger = TestLogger()

        val headerValidationSwiftFile = tempFileSystem.createFile("HeaderValidation.swift")

        val measuredTest = measureTimedValue {
            with(tempFileSystem) {
                with(testLogger) {
                    IntermediateResult.Value(test.kotlinFiles)
                        .map { withJvmInlineAnnotation(it) }
                        .flatMap { compileKotlin(it, test.compilerArgumentsProvider) }
                        .zip { generateConfiguration(createBaseConfiguration(headerValidationSwiftFile), test.configFiles) }
                        .flatMap { linkKotlin(it.first, CompilerSkieConfigurationData.deserialize(it.second.readText()), test.compilerArgumentsProvider) }
                        .pairWith { enhanceSwiftCode(test.swiftCode) }
                        .flatMap { compileSwift(it.first, listOf(it.second, headerValidationSwiftFile), test.compilerArgumentsProvider) }
                        .finalize { runSwift(it) }
                        .also { testLogger.prependTestInfo(test) }
                }
            }
        }

        val testResult = measuredTest.value

        val initialTestResultWithLogs = testResult.withLogsAndDuration(testLogger, measuredTest.duration)

        testLogger.prependLine(testResultReporter.testResultLine(test, initialTestResultWithLogs))

        testLogger.appendTestInfo(tempFileSystem)

        val finalTestResultWithLogs = testResult.withLogsAndDuration(testLogger, measuredTest.duration)

        testResultReporter.reportResult(test, finalTestResultWithLogs)

        writeResult(test, finalTestResultWithLogs)

        deleteTemporaryFiles(test, finalTestResultWithLogs, tempFileSystem)

        return finalTestResultWithLogs
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
                        """.trimIndent(),
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
        baseConfiguration: CompilerSkieConfigurationData,
        configFiles: List<Path>,
    ): IntermediateResult<Path> =
        PluginConfigurationGenerator(this@TempFileSystem).generate(baseConfiguration, configFiles)

    context(TempFileSystem, TestLogger)
    private fun linkKotlin(
        klib: Path,
        skieConfigurationData: CompilerSkieConfigurationData,
        compilerArgumentsProvider: CompilerArgumentsProvider,
    ): IntermediateResult<Path> =
        KotlinTestLinker(this@TempFileSystem, this@TestLogger).link(klib, skieConfigurationData, compilerArgumentsProvider)

    context(TempFileSystem)
    private fun enhanceSwiftCode(swiftCode: String): Path =
        SwiftCodeEnhancer(this@TempFileSystem).enhance(swiftCode)

    context(TempFileSystem, TestLogger)
    private fun compileSwift(
        kotlinFramework: Path,
        swiftFiles: List<Path>,
        compilerArgumentsProvider: CompilerArgumentsProvider,
    ): IntermediateResult<Path> =
        SwiftTestCompiler(this@TempFileSystem, this@TestLogger, compilerArgumentsProvider.target).compile(kotlinFramework, swiftFiles)

    context(TestLogger)
    private fun runSwift(binary: Path): TestResult =
        SwiftProgramRunner(this@TestLogger).runProgram(binary)

    private fun writeResult(test: TestNode.Test, result: TestResultWithLogs) {
        val resultAsText = result.hasSucceededAsString(test)

        test.resultPath.writeText(resultAsText)

        test.logPath.writeText(result.logs)
    }

    private fun TestLogger.prependTestInfo(test: TestNode.Test) {
        val testFilesDescription = test.describeTestFiles()
        this.prependSection("Test files", testFilesDescription)

        this.prependLine(
            """
                Test name: ${test.fullName}
                To run only this test add env variable: acceptanceTest=${test.fullName}
            """.trimIndent(),
        )
    }

    private fun TestLogger.appendTestInfo(tempFileSystem: TempFileSystem) {
        val createdFilesDescription = tempFileSystem.describeCreatedFiles(CreatedFilesDescriptionFilter)

        this.appendSection("Created files", createdFilesDescription)
    }

    private fun TestNode.Test.describeTestFiles(): String =
        (listOf(this.path) + this.kotlinFiles).joinToString("\n") { it.absolutePathString() }

    private fun TestResult.withLogsAndDuration(testLogger: TestLogger, duration: Duration): TestResultWithLogs =
        TestResultWithLogs(
            this,
            duration,
            testLogger.toString(),
        )

    private fun deleteTemporaryFiles(test: TestNode.Test, result: TestResultWithLogs, tempFileSystem: TempFileSystem) {
        if (test.expectedResult.hasSucceeded(result)) {
            tempFileSystem.deleteCreatedFiles()
        }
    }
}
