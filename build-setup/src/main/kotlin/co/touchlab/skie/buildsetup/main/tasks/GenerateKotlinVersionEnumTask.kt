package co.touchlab.skie.buildsetup.main.tasks

import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersion
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import co.touchlab.skie.buildsetup.util.version.toIdentifier
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

abstract class GenerateKotlinVersionEnumTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val makeEnumPublic: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val activeVersion: Property<SupportedKotlinVersion>

    @get:Input
    abstract val supportedKotlinVersions: ListProperty<SupportedKotlinVersion>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = "other"
    }

    @TaskAction
    fun execute() {
        val code = getKotlinCompilerVersionEnumCode(
            packageName = packageName.get(),
            makeEnumPublic = makeEnumPublic.get(),
            activeVersion = activeVersion.orNull,
            supportedVersions = supportedKotlinVersions.get(),
        )

        val outputFile = this@GenerateKotlinVersionEnumTask.outputDirectory.get().asFile.toPath().resolve("KotlinCompilerVersion.kt")

        outputFile.createParentDirectories()

        outputFile.writeText(code)
    }

    private fun getKotlinCompilerVersionEnumCode(
        packageName: String,
        makeEnumPublic: Boolean,
        activeVersion: SupportedKotlinVersion?,
        supportedVersions: List<SupportedKotlinVersion>,
    ): String =
        StringBuilder().apply {
            appendLine(
                """
                    |package $packageName
                    |
                    |${if (!makeEnumPublic) "internal " else ""}enum class KotlinCompilerVersion(
                    |   val versionName: String,
                    |   val compilerVersion: String,
                    |   val otherSupportedVersions: List<String>,
                    |   val isEnabled: Boolean,
                    |) {
                    |
                """.trimMargin(),
            )

            supportedVersions.forEach { version ->
                val name = version.name.toString().enquoted()
                val compilerVersion = version.compilerVersion.toString().enquoted()
                val otherSupportedVersions = "listOf(" + version.otherSupportedVersions.joinToString { it.toString().enquoted() } + ")"
                val isEnabled = version.isEnabled

                appendLine(
                    "    ${version.name.toIdentifier()}($name, $compilerVersion, $otherSupportedVersions, $isEnabled),",
                )
            }

            appendLine("    ;")

            appendLine(
                """
                    |
                    |    val supportedVersions: List<String> =
                    |        listOf(compilerVersion) + otherSupportedVersions
                """.trimMargin(),
            )

            if (activeVersion != null) {
                appendLine(
                    """
                    |
                    |    companion object {
                    |
                    |        val current: KotlinCompilerVersion = KotlinCompilerVersion.${activeVersion.name.toIdentifier()}
                    |    }
                """.trimMargin(),
                )
            }

            appendLine(
                """
                    |}
                """.trimMargin(),
            )
        }.toString()

    companion object {

        fun register(
            kotlinCompilation: KotlinCompilation<*>,
            packageName: String,
            makeEnumPublic: Boolean,
            activeVersion: SupportedKotlinVersion?,
        ) {
            val project = kotlinCompilation.project

            val supportedKotlinVersions = SupportedKotlinVersionProvider.getSupportedKotlinVersions(project)

            val outputDirectory = project.layout.buildDirectory
                .dir("generated/sources/skie/KotlinCompilerVersion/${kotlinCompilation.defaultSourceSet.name}")

            val task = project.tasks.register<GenerateKotlinVersionEnumTask>(
                "generateKotlinVersionEnum${kotlinCompilation.defaultSourceSet.name.capitalized()}",
            ) {
                this.packageName.set(packageName)
                this.makeEnumPublic.set(makeEnumPublic)
                this.activeVersion.set(activeVersion)
                this.supportedKotlinVersions.set(supportedKotlinVersions)
                this.outputDirectory.set(outputDirectory)
            }

            kotlinCompilation.defaultSourceSet.kotlin {
                srcDir(task.map { it.outputDirectory })
            }
        }
    }
}
