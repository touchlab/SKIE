package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.util.withKotlinNativeCompilerEmbeddableDependency
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.ConfigureSourceSetScope
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import co.touchlab.skie.gradle.version.target.SourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

abstract class SkieCompiler : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<OptInExperimentalCompilerApi>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(kotlinToolingVersionDimension()) { target ->
                jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.primaryVersion

                generateKotlinCompilerVersionEnum(this, sourceSet, kotlinVersion, project)

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    withKotlinNativeCompilerEmbeddableDependency(kotlinVersion, isTarget = sourceSet.isTarget) {
                        weak(it)
                    }
                }
            }
        }
    }

    private fun MultiDimensionTargetExtension.generateKotlinCompilerVersionEnum(
        configureSourceSetScope: ConfigureSourceSetScope,
        sourceSet: SourceSet,
        kotlinVersion: KotlinToolingVersion,
        project: Project,
    ) {
        if (configureSourceSetScope.compilation.isMain) {
            val enumCode = if (sourceSet.isRoot) {
                getKotlinCompilerVersionEnumCode()
            } else if (sourceSet.isTarget) {
                getActualCurrentKotlinCompilerVersionPropertyCode(kotlinVersion)
            } else {
                null
            }

            enumCode?.let {
                configureSourceSetScope.addKotlinCode("KotlinCompilerVersion.kt", it, project)
            }
        }
    }

    private fun MultiDimensionTargetExtension.getKotlinCompilerVersionEnumCode(): String {
        val allKotlinVersions = targetConfigurer.allTargets.map { it.kotlinToolingVersion.primaryVersion }.distinct().sorted()

        return StringBuilder().apply {
            appendLine("package co.touchlab.skie.util")
            appendLine()
            appendLine("enum class KotlinCompilerVersion {")
            appendLine()

            append("    ")
            append(allKotlinVersions.joinToString { it.toIdentifier() })
            appendLine(";")
            appendLine()
            appendLine("    companion object")
            appendLine("}")

            appendLine()
            appendLine("expect val KotlinCompilerVersion.Companion.current: KotlinCompilerVersion")
        }.toString()
    }

    private fun getActualCurrentKotlinCompilerVersionPropertyCode(currentKotlinVersion: KotlinToolingVersion): String =
        StringBuilder().apply {
            appendLine("package co.touchlab.skie.util")
            appendLine()
            appendLine("actual val KotlinCompilerVersion.Companion.current: KotlinCompilerVersion")
            appendLine("    get() = KotlinCompilerVersion.${currentKotlinVersion.toIdentifier()}")
        }.toString()

    private fun KotlinToolingVersion.toIdentifier(): String = "`" + toString().replace(".", "_") + "`"

    private fun ConfigureSourceSetScope.addKotlinCode(fileName: String, code: String, project: Project) {
        val generatedDirectory = project.layout.buildDirectory.dir("generated/sources/skie/${kotlinSourceSet.name}").get().asFile

        generatedDirectory.mkdirs()

        val file = generatedDirectory.resolve(fileName)

        file.writeText(code)

        kotlinSourceSet.kotlin {
            srcDir(generatedDirectory)
        }
    }
}
