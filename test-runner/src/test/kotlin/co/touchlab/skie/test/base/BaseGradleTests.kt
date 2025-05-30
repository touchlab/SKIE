package co.touchlab.skie.test.base

import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.template.Template
import co.touchlab.skie.test.template.TemplateFile
import co.touchlab.skie.test.trait.gradle.BuildGradleFile
import co.touchlab.skie.test.trait.TestUtilsTrait
import co.touchlab.skie.test.trait.gradle.GradleBuildFileBuilderTrait
import co.touchlab.skie.test.util.CommandResult
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.StringBuilderScope
import co.touchlab.skie.test.util.execute
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestReporter
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals

abstract class BaseGradleTests: TestUtilsTrait, GradleBuildFileBuilderTrait {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    lateinit var testProjectDir: File

    @Inject
    lateinit var reporter: TestReporter

    val settingsFile: File
        get() = File(testProjectDir, "settings.gradle.kts")
    val rootBuildFile: BuildGradleFile
        get() = BuildGradleFile(File(testProjectDir, "build.gradle.kts"))
    val gradlePropertiesFile: File
        get() = File(testProjectDir, "gradle.properties")

    @BeforeEach
    fun createSettingsFile() {
        settingsFile("""
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
        gradlePropertiesFile {
            +"org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseParallelGC\n"
            appendAdditionalGradleProperties()
        }
    }

    fun runGradle(
        // TODO Revert back to 8.4 once the issue with Kotlin 2.1.20 is resolved
        gradleVersion: String = "8.8",
        vararg arguments: String = arrayOf("build"),
        assertResult: ((BuildResult) -> Unit)? = {
            assertEquals(TaskOutcome.SUCCESS, it.task(":build")?.outcome)
        },
    ): BuildResult {
        val result: BuildResult = GradleRunner.create()
            .apply {
                withProjectDir(testProjectDir)
                withGradleVersion(gradleVersion)
                withArguments(*arguments, "--stacktrace", "--info")
                if (!isCI) {
                    forwardOutput()
                }
            }
            .build()

        assertResult?.invoke(result)

        return result
    }

    fun buildSwift(
        target: KotlinTarget.Native.Darwin,
        template: Template,
        frameworkParentPath: String,
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
            // Add the framework parent path as `rpath` so it gets loaded correctly when run
            this += listOf("-Xlinker", "-rpath", "-Xlinker", frameworkParentPath)
        }

        val result = command.joinToString(" ").execute(testProjectDir)
        if (!isCI) {
            println(result.stdOut)
        }

        assertResult?.invoke(result)

        return result
    }

    fun runSwift(
        assertResult: ((CommandResult) -> Unit)? = {
            assertEquals(0, it.exitCode)
        },
    ): CommandResult {
        val command = buildList {
            this += "./swift_executable"
        }

        val result = command.joinToString(" ").execute(testProjectDir)
        if (!isCI) {
            println(result.stdOut)
        }

        assertResult?.invoke(result)

        return result
    }

    fun commonMain(fqdn: String): File {
        return testProjectDir.resolve("src/commonMain/kotlin").resolve(fqdn.split(".").joinToString("/") + ".kt").also {
            it.parentFile.mkdirs()
        }
    }

    fun copyToCommonMain(template: Template) {
        template.files.forEach { file ->
            val targetFile = when (file.kind) {
                TemplateFile.Kind.Kotlin -> testProjectDir
                    .resolve("src/commonMain/kotlin")
                    .resolve(file.relativePath)
                TemplateFile.Kind.BundledSwift -> testProjectDir
                    .resolve("src/commonMain/swift")
                    .resolve(file.relativePath)
                TemplateFile.Kind.Swift -> return@forEach
            }

            targetFile.parentFile.mkdirs()
            file.file.copyTo(targetFile)
        }
    }

    fun builtFrameworkParentDir(
        target: KotlinTarget.Native.Darwin,
        configuration: BuildConfiguration,
        isArtifactDsl: Boolean,
    ): String {
        return if (isArtifactDsl) {
            "build/out/framework/${target.frameworkTarget}/${configuration.name.lowercase()}"
        } else {
            "build/bin/${target.id}/${configuration.name.lowercase()}Framework"
        }
    }

    protected open fun StringBuilderScope.appendAdditionalGradleProperties() { }
}
