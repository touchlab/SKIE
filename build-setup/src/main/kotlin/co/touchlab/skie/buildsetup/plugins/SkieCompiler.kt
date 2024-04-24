package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.toIdentifier
import co.touchlab.skie.gradle.util.generateKotlinCode
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

                generateKotlinCompilerVersionEnum(sourceSet, sourceSet.kotlinToolingVersion.name, project)

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    withKotlinNativeCompilerEmbeddableDependency(kotlinVersion, isTarget = sourceSet.isTarget) {
                        weak(it)
                    }
                }
            }
        }
    }

    private fun ConfigureSourceSetScope.generateKotlinCompilerVersionEnum(
        sourceSet: SourceSet,
        kotlinVersion: KotlinToolingVersion,
        project: Project,
    ) {
        if (this.compilation.isMain) {
            val enumCode = if (sourceSet.isRoot) {
                getExpectCurrentKotlinCompilerVersionPropertyCode()
            } else if (sourceSet.isTarget) {
                getActualCurrentKotlinCompilerVersionPropertyCode(kotlinVersion)
            } else {
                null
            }

            enumCode?.let {
                this.kotlinSourceSet.generateKotlinCode("KotlinCompilerVersion.kt", it, project)
            }
        }
    }

    private fun getExpectCurrentKotlinCompilerVersionPropertyCode(): String =
        StringBuilder().apply {
            appendLine("package co.touchlab.skie.util")
            appendLine()
            appendLine("expect val KotlinCompilerVersion.Companion.current: KotlinCompilerVersion")
        }.toString()

    private fun getActualCurrentKotlinCompilerVersionPropertyCode(currentKotlinVersion: KotlinToolingVersion): String =
        StringBuilder().apply {
            appendLine("package co.touchlab.skie.util")
            appendLine()
            appendLine("actual val KotlinCompilerVersion.Companion.current: KotlinCompilerVersion")
            appendLine("    get() = KotlinCompilerVersion.${currentKotlinVersion.toIdentifier()}")
        }.toString()
}
