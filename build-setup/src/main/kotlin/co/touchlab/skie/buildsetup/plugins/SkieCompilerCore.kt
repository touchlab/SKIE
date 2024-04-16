package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.toIdentifier
import co.touchlab.skie.gradle.util.generateKotlinCode
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

abstract class SkieCompilerCore : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<SkieBase>()
        apply<KotlinMultiplatformPluginWrapper>()
        apply<OptInExperimentalCompilerApi>()

        val primaryKotlinVersions = kotlinToolingVersionDimension().components.map { it.primaryVersion }.distinct().sorted()

        val minimumKotlinVersion = primaryKotlinVersions.min()

        extensions.configure<KotlinMultiplatformExtension> {
            jvm()

            sourceSets {
                val commonMain by getting {
                    dependencies {
                        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${minimumKotlinVersion}")
                    }

                    generateKotlinCompilerVersionEnum(primaryKotlinVersions, project)
                }

                val commonTest by getting {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-stdlib:$minimumKotlinVersion")
                    }
                }
            }
        }
    }

    private fun KotlinSourceSet.generateKotlinCompilerVersionEnum(
        primaryKotlinVersions: List<KotlinToolingVersion>,
        project: Project,
    ) {
        val kotlinVersionsEnum = getKotlinCompilerVersionEnumCode(primaryKotlinVersions)

        generateKotlinCode("KotlinCompilerVersion.kt", kotlinVersionsEnum, project)
    }

    private fun getKotlinCompilerVersionEnumCode(primaryKotlinVersions: List<KotlinToolingVersion>): String =
        StringBuilder().apply {
            appendLine("package co.touchlab.skie.util")
            appendLine()
            appendLine("enum class KotlinCompilerVersion {")
            appendLine()

            append("    ")
            append(primaryKotlinVersions.joinToString { it.toIdentifier() })
            appendLine(";")
            appendLine()
            appendLine("    companion object")
            appendLine("}")
        }.toString()
}
