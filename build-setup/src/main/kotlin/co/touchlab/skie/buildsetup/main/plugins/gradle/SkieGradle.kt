package co.touchlab.skie.buildsetup.main.plugins.gradle

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityGradleImplicitReceiver
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.minGradleVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

abstract class SkieGradle : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<KotlinMultiplatformPluginWrapper>()
        apply<UtilityGradleImplicitReceiver>()

        configureKotlinJvm(project)
    }

    private fun Project.configureKotlinJvm(project: Project) {
        val minGradleVersion = project.minGradleVersion()

        val weakDependencies = listOf(
            "org.jetbrains.kotlin:kotlin-stdlib:${minGradleVersion.embeddedKotlin}",
            "dev.gradleplugins:gradle-api:${minGradleVersion.gradle}",
            "org.codehaus.groovy:groovy-json:${minGradleVersion.embeddedGroovy}",
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
    }
}
