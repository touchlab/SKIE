package co.touchlab.skie.test.suite.gradle.artifact

import co.touchlab.skie.test.*
import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.OnlyDebug
import co.touchlab.skie.test.annotation.filter.OnlyDynamic
import co.touchlab.skie.test.annotation.filter.OnlyFor
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.runner.SkieTestRunnerConfiguration
import co.touchlab.skie.test.template.Template
import co.touchlab.skie.test.template.TemplateFile
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.trait.TestUtilsTrait
import co.touchlab.skie.test.trait.gradle.BuildGradleFile
import co.touchlab.skie.test.trait.gradle.GradleBuildFileBuilderTrait
import co.touchlab.skie.test.util.*
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.style.funSpec
import io.kotest.core.spec.style.scopes.ContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.datatest.withData
import io.kotest.engine.spec.tempdir
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.test.assertEquals

fun xcFrameworkTests(
    kotlinVersion: KotlinVersion,
    target: KotlinTarget.Native.Darwin,
    linkMode: LinkMode,
    configuration: BuildConfiguration
) = funSpec {

}

class KotlinArtifactDsl_XCFramework_Spec: BaseSpec({
    context("test context") {
        withMatrix { kotlinVersion: KotlinVersion, target: KotlinTarget.Native.Darwin, linkMode: LinkMode, configuration: BuildConfiguration ->
            rootBuildFile(kotlinVersion) {
                kotlin {
                    target(target)
                }

                +"""
                    kotlinArtifacts {
                        Native.XCFramework {
                            targets(${target.id})
                            modes(${configuration.toString().uppercase()})
                            isStatic = ${linkMode.isStatic}
                        }
                    }
                """.trimIndent()
            }

            copyToCommonMain(Templates.basic)

            runGradle()

            buildSwift(target, Templates.basic)

            if (target is KotlinTarget.Native.MacOS) {
                runSwift()
            }
        }
    }
})

abstract class BaseSpec(body: BaseSpec.() -> Unit): DslDrivenSpec(), FunSpecRootScope, TestUtilsTrait, GradleBuildFileBuilderTrait {
    init {
        beforeTest {
            createSettingsFile()
            createGradlePropertiesFile()
        }
        body()
    }

    suspend inline fun <reified P1> ContainerScope.withMatrix(crossinline test: suspend ContainerScope.(p1: P1) -> Unit) {
        withData(matrixAxis<P1>()) { p1 ->
            test(p1)
        }
    }
    suspend inline fun <reified P1, reified P2> ContainerScope.withMatrix(crossinline test: suspend ContainerScope.(p1: P1, p2: P2) -> Unit) {
        withMatrix<P1> { p1 ->
            withData(matrixAxis<P2>()) { p2 ->
                test(p1, p2)
            }
        }
    }

    suspend inline fun <reified P1, reified P2, reified P3> ContainerScope.withMatrix(crossinline test: suspend ContainerScope.(p1: P1, p2: P2, p3: P3) -> Unit) {
        withMatrix<P1, P2> { p1, p2 ->
            withData(matrixAxis<P3>()) { p3 ->
                test(p1, p2, p3)
            }
        }
    }

    suspend inline fun <reified P1, reified P2, reified P3, reified P4> ContainerScope.withMatrix(crossinline test: suspend ContainerScope.(p1: P1, p2: P2, p3: P3, p4: P4) -> Unit) {
        withMatrix<P1, P2, P3> { p1, p2, p3 ->
            withData(matrixAxis<P4>()) { p4 ->
                test(p1, p2, p3, p4)
            }
        }
    }

    @PublishedApi
    internal inline fun <reified T> matrixAxis(): List<T> {
        TODO()
    }

    suspend fun ContainerScope.withKotlinVersion(test: suspend ContainerScope.(KotlinVersion) -> Unit) {
        withData(
            nameFn = { it.value },
            ts = SkieTestRunnerConfiguration.kotlinVersions,
            test = test,
        )
    }

    suspend fun ContainerScope.withTarget(test: suspend ContainerScope.(KotlinTarget) -> Unit) {
        withData(
            ts = SkieTestRunnerConfiguration.targets.targets,
            test = test,
        )
    }

    suspend fun ContainerScope.withDarwinTarget(test: suspend ContainerScope.(KotlinTarget.Native.Darwin) -> Unit) {
        withData(
            ts = SkieTestRunnerConfiguration.targets.targets.filterIsInstance<KotlinTarget.Native.Darwin>(),
            test = test
        )
    }

    suspend fun ContainerScope.withLinkMode(test: suspend ContainerScope.(LinkMode) -> Unit) {
        withData(
            ts = SkieTestRunnerConfiguration.linkModes,
            test = test,
        )
    }

    suspend fun ContainerScope.withConfiguration(test:  ContainerScope.(BuildConfiguration) -> Unit) {
        withData(
            ts = SkieTestRunnerConfiguration.configurations,
            test = test,
        )
    }

//     @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    var testProjectDir: File = tempdir()

    val settingsFile: File
        get() = File(testProjectDir, "settings.gradle.kts")
    val rootBuildFile: BuildGradleFile
        get() = BuildGradleFile(File(testProjectDir, "build.gradle.kts"))
    val gradlePropertiesFile: File
        get() = File(testProjectDir, "gradle.properties")

    @BeforeEach
    fun createSettingsFile() {
        this.settingsFile.invoke("""
            pluginManagement {
                repositories {
                    maven("${tempRepository.absolutePath}")
                    gradlePluginPortal()
                }
            }

            rootProject.name = "gradle-test"

            dependencyResolutionManagement {
                repositories {
                    maven("${tempRepository.absolutePath}")
                    mavenCentral()
                    google()
                }
            }
        """.trimIndent())
    }

    @BeforeEach
    fun createGradlePropertiesFile() {
        gradlePropertiesFile("""
            org.gradle.jvmargs=-XX:+UseParallelGC
        """.trimIndent())
    }

    fun runGradle(
        gradleVersion: String = "8.4",
        vararg arguments: String = arrayOf("build"),
        assertResult: ((BuildResult) -> Unit)? = {
            assertEquals(TaskOutcome.SUCCESS, it.task(":build")?.outcome)
        },
    ): BuildResult {
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withGradleVersion(gradleVersion)
            .withArguments(*arguments)
            .forwardOutput()
//             .withDebug(true)
            .build()

        assertResult?.invoke(result)

        return result
    }

    fun buildSwift(
        target: KotlinTarget.Native.Darwin,
        template: Template,
        frameworkParentPath: String = "build/out/framework/ios_simulator_arm64/debug",
        assertResult: ((CommandResult) -> Unit)? = {
            assertEquals(0, it.exitCode)
        },
    ): CommandResult {
        val command = buildList<String> {
            this += "/usr/bin/xcrun"
            this += listOf("-sdk", target.sdk)
            this += "swiftc"
            this += template.files.filter { it.kind == TemplateFile.Kind.Swift }.map { it.file.absolutePath }
            this += listOf("-F", frameworkParentPath)
            this += listOf("-o", "swift_executable")
            this += listOf("-target", target.triple)
            // Workaround for https://github.com/apple/swift/issues/55127
            this += "-parse-as-library"
            // Workaround for missing symbol when compiling with Coroutines for MacosArm64
            this += listOf("-Xlinker", "-dead_strip")
        }

        val result = command.joinToString(" ").execute(testProjectDir)
        println(result.stdOut)

        assertResult?.invoke(result)

        return result
    }

    fun runSwift() {

    }

    fun commonMain(fqdn: String): File {
        return testProjectDir.resolve("src/commonMain/kotlin").resolve(fqdn.split(".").joinToString("/") + ".kt").also {
            it.parentFile.mkdirs()
        }
    }

    fun copyToCommonMain(template: Template) {
        template.files.forEach { file ->
            val targetFile = testProjectDir
                .resolve("src/commonMain/kotlin")
                .resolve(file.relativePath)
            targetFile.parentFile.mkdirs()
            file.file.copyTo(targetFile)
        }
    }
}


@OnlyFor(
    targets = [RawKotlinTarget.iosSimulatorArm64],
)
@OnlyDebug
@OnlyDynamic
@Suppress("ClassName")
@Smoke
@GradleTests
class KotlinArtifactDsl_XCFramework_Tests: BaseGradleTests() {

    @MatrixTest
    fun `single target`(
        kotlinVersion: KotlinVersion,
        target: KotlinTarget.Native.Darwin,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                target(target)
            }

            +"""
                kotlinArtifacts {
                    Native.XCFramework {
                        targets(${target.id})
                        modes(${configuration.toString().uppercase()})
                        isStatic = ${linkMode.isStatic}
                    }
                }
            """.trimIndent()
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        buildSwift(target, Templates.basic, configuration)

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }

    @MatrixTest
    fun `all darwin targets and single xcframework artifact`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allDarwin()
            }

            +"""
                kotlinArtifacts {
                    Native.XCFramework {
                        targets(${KotlinTarget.Native.Darwin.targets.joinToString { it.id }})
                        modes(${configuration.toString().uppercase()})
                        isStatic = ${linkMode.isStatic}
                    }
                }
            """.trimIndent()
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        KotlinTarget.Native.Darwin.targets.forEach { target ->
            val frameworkParentName = when (target) {
                KotlinTarget.Native.Ios.SimulatorArm64, KotlinTarget.Native.Ios.X64 -> "ios-arm64_x86_64-simulator"
                KotlinTarget.Native.Ios.Arm64 -> "ios-arm64"
                KotlinTarget.Native.Tvos.SimulatorArm64, KotlinTarget.Native.Tvos.X64 -> "tvos-arm64_x86_64-simulator"
                KotlinTarget.Native.Tvos.Arm64 -> "tvos-arm64"
                KotlinTarget.Native.MacOS.Arm64, KotlinTarget.Native.MacOS.X64 -> "macos-arm64_x86_64"
            }

            buildSwift(
                target = target,
                frameworkParentPath = "build/out/xcframework/${configuration.name.lowercase()}/gradle_test.xcframework/$frameworkParentName",
                template = Templates.basic,
                configuration = configuration,
            )

            if (target is KotlinTarget.Native.MacOS) {
                runSwift()
            }
        }
    }
}
