package co.touchlab.skie.plugin

import co.touchlab.skie.gradle.KotlinCompilerVersion
// import co.touchlab.skie.plugin.shim.ShimTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.util.GradleVersion
import java.net.URLClassLoader

abstract class SkieLoaderPlugin: Plugin<Project> {
    override fun apply(project: Project) = with(project) {
//         val shimConfiguration = project.configurations.detachedConfiguration(
//             project.dependencies.module("co.touchlab.skie:kotlin-gradle-plugin-shim-impl:1.0.0-SNAPSHOT")
//         ).apply {
//             this.isCanBeConsumed = false
//             this.isCanBeResolved = true
//
//             attributes {
//                 attribute(KotlinCompilerVersion.attribute, objects.named(KotlinCompilerVersion::class.java, "1.8.20"))
//                 if (GradleVersion.current() >= GradleVersion.version("7.0")) {
//                     attribute(
//                         GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
//                         objects.named(GradlePluginApiVersion::class.java, GradleVersion.current().version)
//                     )
//                 }
//             }
//         }
//
//         shimConfiguration.resolvedConfiguration.rethrowFailure()
//
//         val shimClassLoader = URLClassLoader(
//             "shimClassLoader",
//             shimConfiguration.files.map { it.toURI().toURL() }.toTypedArray(),
//             buildscript.classLoader,
//         )
//
//
//         println("SCL: $shimClassLoader")
//         val shimTest: ShimTest = shimClassLoader.loadClass("co.touchlab.skie.plugin.shim.ShimTestImpl").getDeclaredConstructor().newInstance() as ShimTest
//         println("ShimTest ${shimTest.hello}")

        // Apply SkieGradlePlugin
    }
}
