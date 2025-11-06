@file:OptIn(InternalKotlinGradlePluginApi::class)
@file:Suppress("DEPRECATION", "invisible_reference", "invisible_member")

package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.main.tasks.NativeDistributionCommonizerTask
import co.touchlab.skie.buildsetup.util.KotlinCompilerRunnerBuildService
import co.touchlab.skie.buildsetup.util.getKotlinNativeCompilerEmbeddableDependency
import co.touchlab.skie.buildsetup.util.kotlinNativeCompilerHome
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.targets.native.internal.commonizerTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.utils.Future
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as GradleDslKotlinVersion

abstract class UtilityMinimumTargetKotlinVersionPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val minimumVersion = SupportedKotlinVersionProvider.getMinimumSupportedKotlinVersion(project)
        val klibCompilerVersion = SupportedKotlinVersionProvider.getKlibCompilerVersion(project)
        val kotlinNativeCompilerHome = kotlinNativeCompilerHome(klibCompilerVersion)

        setMinimumTargetKotlinVersion(project, minimumVersion)

        val kotlinCompilerRunnerProvider = registerKotlinCompilerRunnerService(kotlinNativeCompilerHome, klibCompilerVersion)

        configureCompilerForKlibCompilation(klibCompilerVersion, kotlinCompilerRunnerProvider, kotlinNativeCompilerHome)
    }

    private fun Project.configureCompilerForKlibCompilation(
        klibCompilerVersion: KotlinToolingVersion,
        kotlinCompilerRunnerProvider: Provider<KotlinCompilerRunnerBuildService>,
        kotlinNativeCompilerHome: File,
    ) {
        plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
            configureCommonizer(klibCompilerVersion, kotlinNativeCompilerHome)

            configureCompilerClasspathForKlibCompilation(klibCompilerVersion)

            configureMetadataCompiler(kotlinCompilerRunnerProvider)

            configureJsCompiler(kotlinCompilerRunnerProvider)

            configureNativeCompiler(kotlinCompilerRunnerProvider, kotlinNativeCompilerHome, klibCompilerVersion)
        }
    }

    private fun Project.configureCommonizer(klibCompilerVersion: KotlinToolingVersion, kotlinNativeCompilerHome: File) {
        val commonizerConfiguration = project.configurations.detachedConfiguration(
            dependencies.create("org.jetbrains.kotlin:kotlin-klib-commonizer:$klibCompilerVersion"),
        )

        val commonizerTargets = project.objects.listProperty<Future<CommonizerTarget?>>()

        val commonizerTask = tasks.register<NativeDistributionCommonizerTask>("commonizeNativeDistributionForCustomKlibCompilation") {
            val commonizerTargets = commonizerTargets.map { futures -> futures.map { it.getOrThrow() }.filterIsInstance<SharedCommonizerTarget>() }

            commonizerClasspath.from(commonizerConfiguration)
            this.commonizerTargets.addAll(commonizerTargets)
            this.kotlinNativeCompilerHome.set(kotlinNativeCompilerHome)
            outputDirectory.set(kotlinNativeCompilerHome.resolve("klib/commonized/$klibCompilerVersion"))
        }

        extensions.configure<KotlinMultiplatformExtension> {
            targets.configureEach {
                compilations.configureEach {
                    commonizerTargets.add(commonizerTarget)
                }
            }
        }

        tasks.named { it == "commonizeNativeDistribution" }.configureEach {
            dependsOn(commonizerTask)
        }
    }

    private fun Project.registerKotlinCompilerRunnerService(
        kotlinNativeCompilerHome: File,
        klibCompilerVersion: KotlinToolingVersion,
    ): Provider<KotlinCompilerRunnerBuildService> {
        val trove4j = kotlinNativeCompilerHome.resolve("konan/lib/trove4j.jar")

        val dependencies = listOfNotNull(
            getKotlinNativeCompilerEmbeddableDependency(klibCompilerVersion),
            // Removed in Kotlin 2.2.0
            trove4j.takeIf { it.exists() }?.let { dependencies.create(files(it)) },
        )

        val kotlinNativeCompilerConfiguration = project.configurations.detachedConfiguration(*dependencies.toTypedArray())

        return gradle.sharedServices.registerIfAbsent(
            "kotlinCompilerRunner",
            KotlinCompilerRunnerBuildService::class.java,
        ) {
            parameters.classpath.from(kotlinNativeCompilerConfiguration.resolve())
        }
    }

    private fun Project.configureCompilerClasspathForKlibCompilation(klibCompilerVersion: KotlinToolingVersion) {
        configurations
            .named {
                it in listOf(
                    "kotlinCompilerPluginClasspathJsMain",
                    "kotlinCompilerPluginClasspathMetadataCommonMain",
                    "kotlinCompilerPluginClasspathMetadataMain",
                    "kotlinCompilerPluginClasspathMetadataWebMain",
                    "kotlinCompilerPluginClasspathWasmJsMain",
                    "kotlinCompilerPluginClasspathWasmWasiMain",
                )
            }
            .configureEach {
                resolutionStrategy {
                    force("org.jetbrains.kotlin:kotlin-compiler-embeddable:$klibCompilerVersion")
                    force("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$klibCompilerVersion")
                }
            }
    }

    private fun Project.configureMetadataCompiler(kotlinCompilerRunnerProvider: Provider<KotlinCompilerRunnerBuildService>) {
        // Renamed in 2.1.0 to KotlinMetadataCompiler
        configureCompiler<KotlinCompileCommon>("org.jetbrains.kotlin.cli.metadata.K2MetadataCompiler", kotlinCompilerRunnerProvider)
    }

    private fun Project.configureJsCompiler(kotlinCompilerRunnerProvider: Provider<KotlinCompilerRunnerBuildService>) {
        configureCompiler<Kotlin2JsCompile>("org.jetbrains.kotlin.cli.js.K2JSCompiler", kotlinCompilerRunnerProvider)
    }

    private fun Project.configureNativeCompiler(
        kotlinCompilerRunnerProvider: Provider<KotlinCompilerRunnerBuildService>,
        kotlinNativeCompilerHome: File,
        klibCompilerVersion: KotlinToolingVersion,
    ) {
        configureCompiler<KotlinNativeCompile>("org.jetbrains.kotlin.cli.bc.K2Native", kotlinCompilerRunnerProvider) { arguments ->
            val (libraries, remainingArguments) = parseLibraries(arguments)

            val libraryArguments = relocateKonanLibraries(libraries, kotlinNativeCompilerHome, klibCompilerVersion)
                .flatMap { listOf("-library", it) }

            System.setProperty("konan.home", kotlinNativeCompilerHome.absolutePath)

            return@configureCompiler remainingArguments + libraryArguments
        }
    }

    private inline fun <reified COMPILE : AbstractKotlinCompileTool<*>> Project.configureCompiler(
        compilerClassFqName: String,
        kotlinCompilerRunnerProvider: Provider<KotlinCompilerRunnerBuildService>,
        crossinline processArguments: (List<String>) -> List<String> = { it },
    ) {
        tasks.withType<COMPILE>().configureEach {
            actions.clear()

            usesService(kotlinCompilerRunnerProvider)

            doLast {
                val arguments = createCompilerArguments(KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.default)

                val cliArguments = ArgumentUtils.convertArgumentsToStringList(arguments).let(processArguments).toTypedArray()

                kotlinCompilerRunnerProvider.get().compile(compilerClassFqName, cliArguments)
            }
        }
    }

    companion object {

        fun setMinimumTargetKotlinVersion(project: Project, version: KotlinToolingVersion) {
            val minimumKotlinVersion = GradleDslKotlinVersion.fromVersion("${version.major}.${version.minor}")

            project.plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
                project.extensions.configure<KotlinMultiplatformExtension> {
                    compilerOptions {
                        apiVersion.set(minimumKotlinVersion)
                        languageVersion.set(minimumKotlinVersion)
                    }

                    coreLibrariesVersion = version.toString()
                }
            }

            project.plugins.withType<KotlinPluginWrapper>().configureEach {
                project.extensions.configure<KotlinJvmProjectExtension> {
                    compilerOptions {
                        apiVersion.set(minimumKotlinVersion)
                        languageVersion.set(minimumKotlinVersion)
                    }

                    coreLibrariesVersion = version.toString()
                }
            }
        }

        private fun parseLibraries(arguments: List<String>): Pair<List<String>, List<String>> {
            var isLibrary = false

            val libraries = mutableListOf<String>()
            val remainingArguments = mutableListOf<String>()

            arguments.forEach {
                if (it == "-library") {
                    isLibrary = true
                } else if (isLibrary) {
                    libraries.add(it)
                    isLibrary = false
                } else {
                    remainingArguments.add(it)
                }
            }

            return libraries to remainingArguments
        }

        private fun relocateKonanLibraries(libraries: List<String>, kotlinNativeCompilerHome: File, klibCompilerVersion: KotlinToolingVersion): List<String> {
            val konanHomePrefix = kotlinNativeCompilerHome.absolutePath.substringBeforeLast("/kotlin-native") + "/kotlin-native"
            val commonizedDirectory = kotlinNativeCompilerHome.absolutePath + "/klib/commonized/"

            return libraries
                .map { library ->
                    if (library.startsWith(konanHomePrefix)) {
                        val relativePathWithoutVersion = library.removePrefix(konanHomePrefix).substringAfter("/")

                        "${kotlinNativeCompilerHome.absolutePath}/$relativePathWithoutVersion"
                    } else {
                        library
                    }
                }
                .mapNotNull { library ->
                    if (library.startsWith(commonizedDirectory)) {
                        val relativePathWithoutVersion = library.removePrefix(commonizedDirectory).substringAfter("/")

                        "$commonizedDirectory$klibCompilerVersion/$relativePathWithoutVersion".takeIf { Path(it).exists() }
                    } else {
                        library
                    }
                }
        }
    }
}
