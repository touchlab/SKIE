package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.compileOnly
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import co.touchlab.skie.gradle.version.ToolingVersions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

abstract class SkieGradle : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<SkieBase>()
//         apply<MultiDimensionTargetPlugin>()

        project.apply<KotlinMultiplatformPluginWrapper>()
        apply<DevGradleImplicitReceiver>()

        val gradleVersion = ToolingVersions.Gradle.`7․3`
        val kotlinVersion = gradleVersion.kotlinVersion.toString()
        val groovyVersion = gradleVersion.groovyVersion

        val weakDependencies = listOf(
            "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
            "dev.gradleplugins:gradle-api:${gradleVersion.gradleVersion.version}",
            "org.codehaus.groovy:groovy-json:$groovyVersion",
        )

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(libs.versions.java)

            jvm()

            sourceSets.commonMain {
                kotlin.srcDirs("src/main/kotlin")
            }

            weakDependencies.forEach { dependency ->
                sourceSets.commonMain {
                    dependencies {
                        compileOnly(dependency)
                    }
                }

                sourceSets.commonTest {
                    dependencies {
                        implementation(dependency)
                    }
                }
            }
        }

//         extensions.configure<MultiDimensionTargetExtension> {
//             dimensions(gradleApiVersionDimension()) { target ->
//                 jvm(target.name) {
//                     attributes {
//                         attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(target.gradleApiVersion.value))
//                         attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
//                     }
//                 }
//             }
//
//             configureSourceSet { sourceSet ->
//                 val gradleApiVersion = sourceSet.gradleApiVersion
//
//                 val gradleVersion = gradleApiVersion.value
//                 val kotlinVersion = gradleApiVersion.version.kotlinVersion.toString()
//                 val groovyVersion = sourceSet.gradleApiVersion.version.groovyVersion
//
//                 dependencies {
//                     weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
//                     weak("dev.gradleplugins:gradle-api:$gradleVersion")
//                     weak("org.codehaus.groovy:groovy-json:$groovyVersion")
//                 }
//             }
//         }
    }
}
