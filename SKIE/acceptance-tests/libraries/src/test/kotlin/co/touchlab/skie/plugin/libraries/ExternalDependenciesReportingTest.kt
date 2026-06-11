package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptance_tests_framework.BuildConfig
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

/**
 * Regression test for external-dependency reporting after the Kotlin 2.4.0 `UserVisibleIrModulesSupport` removal.
 *
 * On 2.4.0 SKIE reconstructs `descriptorProvider.externalDependencies` by deserializing the `-Xexternal-dependencies`
 * file the Kotlin Gradle plugin forwards to the native link. None of the existing harnesses pass that file, so the
 * broken `getExternalDependencies() = emptySet()` stub silently passed the whole suite. This test supplies a real
 * `-Xexternal-dependencies` file and relies on [co.touchlab.skie.acceptancetests.framework.internal.skie.VerifyExternalDependenciesAreReportedPhase]
 * (active only when the compiled module contains the marker class below) to crash the SKIE compilation if the external
 * dependencies are not reported.
 *
 * Pass: the SKIE link succeeds (external dependencies were reported). Fail (with the stub): the link errors.
 */
class ExternalDependenciesReportingTest : FunSpec({

    System.setProperty("konan.home", BuildConfig.KONAN_HOME)

    test("SKIE reports external dependencies from the -Xexternal-dependencies file") {
        val tempFileSystem = TempFileSystem(Files.createTempDirectory("skie-external-dependencies-test"))
        val testLogger = TestLogger()

        // The marker class activates VerifyExternalDependenciesAreReportedPhase for this compilation only.
        val sourceFile = tempFileSystem.createFile("VerifyExternalDependenciesAreReported.kt")
        sourceFile.writeText(
            """
            class VerifyExternalDependenciesAreReported {
                fun marker(): Int = 0
            }
            """.trimIndent(),
        )

        // A real -Xexternal-dependencies file in `ResolvedDependenciesSupport`'s format: one external module
        // (`com.example:foo:1.2.3`) depending on the source code module (index 0).
        val externalKlibPath = tempFileSystem.createFile("com.example.foo.klib").absolutePathString()
        val externalDependenciesFile = tempFileSystem.createFile("external.deps")
        externalDependenciesFile.writeText(
            buildString {
                appendLine("0 co.touchlab.skie:kotlin")
                appendLine("1 com.example:foo[1.2.3] #0[1.2.3]")
                appendLine("\t$externalKlibPath")
            },
        )

        val compilerArgumentsProvider = CompilerArgumentsProvider(
            externalDependencies = externalDependenciesFile.absolutePathString(),
        )

        val klib = when (val compileResult = KotlinTestCompiler(tempFileSystem, testLogger).compile(
            kotlinFiles = listOf(sourceFile),
            compilerArgumentsProvider = compilerArgumentsProvider,
        )) {
            is IntermediateResult.Value -> compileResult.value
            is IntermediateResult.Error -> fail("Kotlin compilation failed: ${compileResult.testResult}")
        }

        val linkResult = KotlinTestLinker(tempFileSystem, testLogger).link(
            klib = klib,
            skieConfigurationData = CompilerSkieConfigurationData(
                groups = listOf(
                    CompilerSkieConfigurationData.Group(
                        target = "",
                        overridesAnnotations = false,
                        items = mapOf("TestConfigurationKeys.EnableVerifyFrameworkHeaderPhase" to "false"),
                    ),
                ),
            ),
            compilerArgumentsProvider = compilerArgumentsProvider,
        )

        when (linkResult) {
            is IntermediateResult.Value -> Unit
            is IntermediateResult.Error -> fail(
                "SKIE link failed — external dependencies were not reported (getExternalDependencies returned empty?). " +
                    "Result: ${linkResult.testResult}",
            )
        }
    }
})
