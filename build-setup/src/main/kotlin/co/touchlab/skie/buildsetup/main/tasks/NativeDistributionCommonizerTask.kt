package co.touchlab.skie.buildsetup.main.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.gradle.targets.native.internal.NativeDistributionCommonizerCache
import java.io.File
import java.net.URLClassLoader

abstract class NativeDistributionCommonizerTask : DefaultTask() {

    @get:Classpath
    abstract val commonizerClasspath: ConfigurableFileCollection

    @get:Internal
    abstract val kotlinNativeCompilerHome: RegularFileProperty

    @get:Input
    abstract val commonizerTargets: SetProperty<SharedCommonizerTarget>

    @get:OutputDirectory
    abstract val outputDirectory: RegularFileProperty

    init {
        group = "other"
    }

    @TaskAction
    fun execute() {
        val cache = NativeDistributionCommonizerCache(
            outputDirectory = outputDirectory.get().asFile,
            konanHome = kotlinNativeCompilerHome.get().asFile,
            logger = logger,
            isCachingEnabled = true,
        )

        cache.writeCacheForUncachedTargets(commonizerTargets.get()) { targets ->
            commonize(targets)
        }
    }

    private fun commonize(targets: Set<SharedCommonizerTarget>) {
        if (targets.isEmpty()) {
            return
        }

        val commonizerClasspathFiles = commonizerClasspath.files

        val classLoader = createClassLoader(commonizerClasspathFiles)

        val cliCommonizer = createCommonizerInstance(classLoader, commonizerClasspathFiles)

        val quietLogLevel = getQuietLogLevel(classLoader)

        val sharedCommonizerTargets = createCommonizerTargets(classLoader, targets)

        val commonizeNativeDistributionMethod = cliCommonizer.javaClass.getDeclaredMethod(
            "commonizeNativeDistribution",
            File::class.java,
            File::class.java,
            Set::class.java,
            quietLogLevel.javaClass,
            List::class.java,
        )

        commonizeNativeDistributionMethod.invoke(
            cliCommonizer,
            kotlinNativeCompilerHome.asFile.get(),
            outputDirectory.asFile.get(),
            sharedCommonizerTargets,
            quietLogLevel,
            emptyList<Object>(),
        )
    }

    private fun createClassLoader(commonizerClasspathFiles: Set<File>): URLClassLoader {
        val embeddableClasspath = commonizerClasspathFiles.map { it.toURI().toURL() }.toTypedArray()

        val classLoader = URLClassLoader(embeddableClasspath)
        return classLoader
    }

    private fun createCommonizerInstance(classLoader: URLClassLoader, commonizerClasspathFiles: Set<File>): Any {
        val cliCommonizerKtClass = classLoader.loadClass("org.jetbrains.kotlin.commonizer.CliCommonizerKt")

        val cliCommonizerFunction = cliCommonizerKtClass.getDeclaredMethod("CliCommonizer", Iterable::class.java)

        return cliCommonizerFunction.invoke(null, commonizerClasspathFiles)
    }

    private fun getQuietLogLevel(classLoader: URLClassLoader): Any {
        val commonizerLogLevelClass = classLoader.loadClass("org.jetbrains.kotlin.commonizer.CommonizerLogLevel")

        return commonizerLogLevelClass.enumConstants.first { it.toString() == "Quiet" }
    }

    private fun createCommonizerTargets(classLoader: URLClassLoader, targets: Set<SharedCommonizerTarget>): Set<Any> {
        val leafCommonizerTargetClass = classLoader.loadClass("org.jetbrains.kotlin.commonizer.LeafCommonizerTarget")
        val sharedCommonizerTargetClass = classLoader.loadClass("org.jetbrains.kotlin.commonizer.SharedCommonizerTarget")

        val leafCommonizerTargetConstructor = leafCommonizerTargetClass.getDeclaredConstructor(String::class.java)
        val sharedCommonizerTargetConstructor = sharedCommonizerTargetClass.getDeclaredConstructor(Set::class.java)

        return targets
            .map { sharedTarget ->
                val leafCommonizerTargets = sharedTarget.targets.map { leafCommonizerTargetConstructor.newInstance(it.name) }.toSet()

                sharedCommonizerTargetConstructor.newInstance(leafCommonizerTargets)
            }
            .toSet()
    }
}
