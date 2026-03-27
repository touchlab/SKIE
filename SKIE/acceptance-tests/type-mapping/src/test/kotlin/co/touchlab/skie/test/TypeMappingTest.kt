package co.touchlab.skie.test

import co.touchlab.skie.acceptance_tests_framework.BuildConfig
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.testDispatcher
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.kotlingenerator.KotlinGenerator
import co.touchlab.skie.kotlingenerator.ir.KotlinType
import co.touchlab.skie.type_mapping.TestBuildConfig
import io.kotest.assertions.fail
import io.kotest.engine.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

class TypeMappingTest {

    @Test
    fun runTest() {
        System.setProperty("konan.home", BuildConfig.KONAN_HOME)
        val tempDirectory = Path(TestBuildConfig.BUILD).resolve("test-temp")
        tempDirectory.toFile().deleteRecursively()
        tempDirectory.toFile().mkdirs()

        val compilerArgumentsProvider = CompilerArgumentsProvider(
            dependencies = TestBuildConfig.DEPENDENCIES.toList(),
            exportedDependencies = TestBuildConfig.EXPORTED_DEPENDENCIES.toList(),
// TODO Debug configuration doesn't work currently
            buildConfiguration = CompilerArgumentsProvider.BuildConfiguration.Release,
            optIn = listOf(
                "kotlinx.cinterop.ExperimentalForeignApi",
            ),
        )

        val splitTypes = TestedType.ALL.sortedBy { it.getSafeName() }.chunked(1000)

        val onlyIndices = setOf<Int>(
            // *(0..31).toList().toTypedArray()
//             9
        )

        val testCompletionTracking = AtomicInteger(0)
        val testsToRun = splitTypes
            .mapIndexed { index, testedTypes -> index to testedTypes }
            .filter {
                @Suppress("KotlinConstantConditions")
                onlyIndices.isEmpty() || onlyIndices.contains(it.first)
            }

        val failures = runBlocking {
            withContext(testDispatcher()) {
                testsToRun
                    .map { (index, types) ->
                        async {
                            val testTime = measureTimedValue {
                                runTestForTypes(
                                    types = types,
                                    tempDirectory = tempDirectory.resolve("test-$index"),
                                    compilerArgumentsProvider = compilerArgumentsProvider,
                                )
                            }

                            val result = testTime.value
                            println(
                                "[${if (result is TestResult.Success) "PASS" else "FAIL"}] Finished test $index (${testCompletionTracking.incrementAndGet()}/${testsToRun.size}) in ${
                                    testTime.duration.toString(
                                        DurationUnit.SECONDS,
                                        2,
                                    )
                                } seconds",
                            )
                            index to result
                        }
                    }
                    .awaitAll()
                    .filter { it.second !is TestResult.Success }
            }
        }

        if (failures.isNotEmpty()) {
            failures.forEach { (_, result) ->
                println(result.actualErrorMessage)
            }
            println("To run only failed tests:")
            println(failures.joinToString(", ") { "${it.first}" })
            fail("${failures.size} tests failed.")
        }
    }

    private fun runTestForTypes(
        types: List<KotlinType>,
        tempDirectory: Path,
        compilerArgumentsProvider: CompilerArgumentsProvider,
    ): TestResult {
        val tempFileSystem = TempFileSystem(tempDirectory)
        val tempSourceFile = tempDirectory.resolve("KotlinFile.kt")

        val kotlinFile = KotlinTestFileProvider.getTestFile(types)

        val kotlinCode = KotlinGenerator.generate(kotlinFile)

        tempSourceFile.writeText(kotlinCode)

        val testLogger = TestLogger()

        val skieConfiguration = CompilerSkieConfigurationData(
            groups = listOf(
                CompilerSkieConfigurationData.Group(
                    target = "",
                    overridesAnnotations = false,
                    items = mapOf(
                        "TestConfigurationKeys.EnableVerifyFrameworkHeaderPhase" to "false",
                    ),
                ),
            ),
            enabledConfigurationFlags = setOf(
                SkieConfigurationFlag.Debug_VerifyDescriptorProviderConsistency,
                SkieConfigurationFlag.Build_ConcurrentSkieCompilation,
                SkieConfigurationFlag.Build_ParallelSkieCompilation,
            ),
        )

        return IntermediateResult.Value(listOf(tempSourceFile))
            .flatMap {
                val compiler = KotlinTestCompiler(tempFileSystem, testLogger)
                compiler.compile(
                    listOf(tempSourceFile),
                    compilerArgumentsProvider,
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
            .finalize {
                TestResult.Success
            }
            .also {
                tempDirectory.resolve("run.log").writeText(testLogger.toString())
            }
            .also {
                if (it is TestResult.Success) {
                    tempFileSystem.deleteCreatedFiles()
                }
            }
    }
}
