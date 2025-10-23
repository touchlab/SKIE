package co.touchlab.skie.buildsetup.util.version

import co.touchlab.skie.buildsetup.util.generateKotlinCode
import co.touchlab.skie.buildsetup.util.enquoted
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

object KotlinCompilerVersionEnumGenerator {

    fun generate(project: Project, packageName: String, makeEnumPublic: Boolean) {
        project.plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
            project.extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        generateEnum(packageName, makeEnumPublic)
                    }
                }
            }
        }

        project.plugins.withType<KotlinPluginWrapper>().configureEach {
            project.extensions.configure<KotlinJvmProjectExtension> {
                sourceSets.named("main").configure {
                    generateEnum(packageName, makeEnumPublic)
                }
            }
        }
    }

    private fun KotlinSourceSet.generateEnum(packageName: String, makeEnumPublic: Boolean) {
        val kotlinVersionsEnum = getKotlinCompilerVersionEnumCode(
            packageName = packageName,
            makeEnumPublic = makeEnumPublic,
            activeVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).name,
            supportedVersions = KotlinToolingVersionProvider.getSupportedKotlinToolingVersions(project),
        )

        generateKotlinCode("KotlinCompilerVersion.kt", kotlinVersionsEnum, project)
    }

    private fun getKotlinCompilerVersionEnumCode(
        packageName: String,
        makeEnumPublic: Boolean,
        activeVersion: KotlinToolingVersion,
        supportedVersions: List<SupportedKotlinToolingVersion>,
    ): String =
        StringBuilder().apply {
            appendLine(
                """
                    |package $packageName
                    |
                    |${if (!makeEnumPublic) "internal " else ""}enum class KotlinCompilerVersion(
                    |   val versionName: String,
                    |   val primaryVersion: String,
                    |   val otherSupportedVersions: List<String>,
                    |) {
                    |
                """.trimMargin()
            )

            supportedVersions.forEach { version ->
                val name = version.name.toString().enquoted()
                val primaryVersion = version.primaryVersion.toString().enquoted()
                val otherSupportedVersions = "listOf(" + version.otherSupportedVersions.joinToString { it.toString().enquoted() } + ")"

                appendLine(
                    "    ${version.name.toIdentifier()}($name, $primaryVersion, $otherSupportedVersions),"
                )
            }

            appendLine("    ;")

            appendLine(
                """
                    |
                    |    val supportedVersions: List<String> =
                    |        listOf(primaryVersion) + otherSupportedVersions
                    |
                    |    companion object {
                    |
                    |        val current: KotlinCompilerVersion = KotlinCompilerVersion.${activeVersion.toIdentifier()}
                    |    }
                    |}
                """.trimMargin()
            )
        }.toString()
}
