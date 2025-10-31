@file:Suppress("UnstableApiUsage")

package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.main.extensions.MultiKotlinVersionSupportExtension
import co.touchlab.skie.buildsetup.util.version.KotlinVersionAttribute
import co.touchlab.skie.buildsetup.util.version.KotlinVersionSet
import co.touchlab.skie.buildsetup.util.version.MultiKotlinVersionSupportCompilation
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersion
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

abstract class UtilityMultiKotlinVersionSupportPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinPluginWrapper>()

        val extension = registerExtension()

        registerSharedConfigurations(extension)

        val versionSets = getVersionSets()

        configureEnabledCompilations(extension, versionSets)
    }

    private fun Project.registerExtension(): MultiKotlinVersionSupportExtension =
        extensions.create("multiKotlinVersionSupport", MultiKotlinVersionSupportExtension::class.java)

    private fun Project.registerSharedConfigurations(extension: MultiKotlinVersionSupportExtension) {
        registerSharedConfiguration(extension.sharedApiConfigurationName)
        registerSharedConfiguration(extension.sharedImplementationConfigurationName)
        registerSharedConfiguration(extension.sharedCompileOnlyConfigurationName)
        registerSharedConfiguration(extension.sharedRuntimeOnlyConfigurationName)
    }

    private fun Project.registerSharedConfiguration(name: String) {
        configurations.register(name) {
            isCanBeDeclared = true
            isCanBeConsumed = false
            isCanBeResolved = false
        }
    }

    private fun Project.getVersionSets(): List<KotlinVersionSet> {
        val kotlinVersionSets = layout.projectDirectory.dir("src").asFile.toPath()
            .listDirectoryEntries()
            .filter { it.isDirectory() }
            .filter { it.name.startsWith("..") || it.name.first().isDigit() }
            .map { KotlinVersionSet.from(it) }

        val supportedVersionNames = SupportedKotlinVersionProvider.getSupportedKotlinVersions(project).map { it.name }
        val invalidSourceSets = kotlinVersionSets.filterNot { it.isValid(supportedVersionNames) }
        check(invalidSourceSets.isEmpty()) {
            "The following version source sets are invalid because they reference unsupported versions: " +
                invalidSourceSets.joinToString { it.path.name }
        }

        return kotlinVersionSets
    }

    private fun Project.configureEnabledCompilations(extension: MultiKotlinVersionSupportExtension, kotlinVersionSets: List<KotlinVersionSet>) {
        val enabledKotlinVersion = SupportedKotlinVersionProvider.getEnabledKotlinVersions(project)
        val primaryKotlinVersion = SupportedKotlinVersionProvider.getPrimaryKotlinVersion(project)
        val secondaryKotlinVersions = enabledKotlinVersion.filter { it.name != primaryKotlinVersion.name }

        configurePrimaryKotlinCompilation(extension, kotlinVersionSets, primaryKotlinVersion)

        secondaryKotlinVersions.forEach {
            configureSecondaryKotlinCompilation(extension, kotlinVersionSets, it)
        }
    }

    private fun Project.configurePrimaryKotlinCompilation(
        extension: MultiKotlinVersionSupportExtension,
        kotlinVersionSets: List<KotlinVersionSet>,
        primaryKotlinVersion: SupportedKotlinVersion,
    ) {
        val activeKotlinVersionSets = kotlinVersionSets.filter { it.isActive(primaryKotlinVersion.name) }

        extensions.configure<KotlinJvmProjectExtension> {
            val mainCompilation = target.compilations.getByName("main")

            configureKotlinCompilation(extension, activeKotlinVersionSets, mainCompilation)

            setKotlinVersionAttribute(mainCompilation.target.apiElementsConfigurationName, primaryKotlinVersion)
            setKotlinVersionAttribute(mainCompilation.target.runtimeElementsConfigurationName, primaryKotlinVersion)
            setKotlinVersionAttribute(mainCompilation.target.sourcesElementsConfigurationName, primaryKotlinVersion)

            val multiKotlinVersionSupportCompilation = MultiKotlinVersionSupportCompilation(primaryKotlinVersion, mainCompilation)
            extension.compilations.add(multiKotlinVersionSupportCompilation)
        }
    }

    private fun Project.setKotlinVersionAttribute(configurationName: String, supportedKotlinVersion: SupportedKotlinVersion) {
        project.configurations.configureEach {
            if (name == configurationName) {
                attributes {
                    attribute(KotlinVersionAttribute.attribute, project.objects.named(supportedKotlinVersion.name.toString()))
                }
            }
        }
    }

    private fun Project.configureSecondaryKotlinCompilation(
        extension: MultiKotlinVersionSupportExtension,
        kotlinVersionSets: List<KotlinVersionSet>,
        supportedKotlinVersion: SupportedKotlinVersion,
    ) {
        val activeKotlinVersionSets = kotlinVersionSets.filter { it.isActive(supportedKotlinVersion.name) }

        extensions.configure<KotlinJvmProjectExtension> {
            val compilationName = supportedKotlinVersion.name.toString().replace(".", "_")
            val compilation = target.compilations.create(compilationName)

            val mainSourceDirectory = layout.projectDirectory.dir("src/main").asFile
            compilation.defaultSourceSet.kotlin.srcDir(mainSourceDirectory.resolve("kotlin"))
            compilation.defaultSourceSet.resources.srcDir(mainSourceDirectory.resolve("resources"))

            configureKotlinCompilation(extension, activeKotlinVersionSets, compilation)
            configureOutgoingVariants(compilation, supportedKotlinVersion)

            val multiKotlinVersionSupportCompilation = MultiKotlinVersionSupportCompilation(supportedKotlinVersion, compilation)
            extension.compilations.add(multiKotlinVersionSupportCompilation)
        }
    }

    private fun KotlinJvmProjectExtension.configureOutgoingVariants(
        compilation: KotlinWithJavaCompilation<*, KotlinJvmCompilerOptions>,
        supportedKotlinVersion: SupportedKotlinVersion,
    ) {
        val jarTask = project.tasks.register<Jar>("${compilation.name}Jar") {
            archiveClassifier.set(supportedKotlinVersion.name.toString())

            from(compilation.output.allOutputs)
        }

        project.tasks.named("assemble").configure {
            dependsOn(jarTask)
        }

        val javaComponent = project.components["java"] as AdhocComponentWithVariants

        configureApiElementsVariant(compilation, supportedKotlinVersion, jarTask, javaComponent)
        configureRuntimeElementsVariant(compilation, supportedKotlinVersion, jarTask, javaComponent)
        configureSourceElementsVariant(compilation, supportedKotlinVersion, javaComponent)
    }

    private fun KotlinJvmProjectExtension.configureApiElementsVariant(
        compilation: KotlinWithJavaCompilation<*, KotlinJvmCompilerOptions>,
        supportedKotlinVersion: SupportedKotlinVersion,
        jarTask: TaskProvider<Jar>,
        javaComponent: AdhocComponentWithVariants,
    ) {
        val apiElements = registerElementsConfiguration(
            compilationName = "${compilation.name}ApiElements",
            referenceConfigurationName = target.apiElementsConfigurationName,
            supportedKotlinVersion = supportedKotlinVersion,
        )

        project.artifacts {
            add(apiElements.name, jarTask)
        }

        javaComponent.addVariantsFromConfiguration(apiElements.get()) {
        }
    }

    private fun KotlinJvmProjectExtension.configureRuntimeElementsVariant(
        compilation: KotlinWithJavaCompilation<*, KotlinJvmCompilerOptions>,
        supportedKotlinVersion: SupportedKotlinVersion,
        jarTask: TaskProvider<Jar>,
        javaComponent: AdhocComponentWithVariants,
    ) {
        val runtimeElements = registerElementsConfiguration(
            compilationName = "${compilation.name}RuntimeElements",
            referenceConfigurationName = target.runtimeElementsConfigurationName,
            supportedKotlinVersion = supportedKotlinVersion,
        )

        runtimeElements.configure {
            outgoing {
                artifact(jarTask)

                variants.create("classes") {
                    compilation.javaSourceSet.output.classesDirs.forEach {
                        artifact(it) {
                            type = ArtifactTypeDefinition.JVM_CLASS_DIRECTORY
                        }
                    }

                    attributes {
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.CLASSES))
                    }
                }

                variants.create("resources") {
                    compilation.javaSourceSet.output.resourcesDir?.let {
                        artifact(it) {
                            type = ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY
                        }
                    }

                    attributes {
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.RESOURCES))
                    }
                }
            }
        }

        javaComponent.addVariantsFromConfiguration(runtimeElements.get()) {
            if (configurationVariant.name == "classes" || configurationVariant.name == "resources") {
                skip()
            }
        }
    }

    private fun KotlinJvmProjectExtension.configureSourceElementsVariant(
        compilation: KotlinWithJavaCompilation<*, KotlinJvmCompilerOptions>,
        supportedKotlinVersion: SupportedKotlinVersion,
        javaComponent: AdhocComponentWithVariants,
    ) {
        val sourcesElements = registerElementsConfiguration(
            compilationName = "${compilation.name}SourcesElements",
            referenceConfigurationName = target.sourcesElementsConfigurationName,
            supportedKotlinVersion = supportedKotlinVersion,
        )

        val sourcesJarTask = project.tasks.register<Jar>("${compilation.name}SourcesJar") {
            archiveClassifier.set(supportedKotlinVersion.name.toString() + "-sources")

            from(compilation.defaultSourceSet.kotlin)
            from(compilation.defaultSourceSet.resources)
        }

        project.tasks.named("assemble").configure {
            dependsOn(sourcesJarTask)
        }

        project.artifacts {
            add(sourcesElements.name, sourcesJarTask)
        }

        javaComponent.addVariantsFromConfiguration(sourcesElements.get()) {
        }
    }

    private fun KotlinJvmProjectExtension.registerElementsConfiguration(
        compilationName: String,
        referenceConfigurationName: String,
        supportedKotlinVersion: SupportedKotlinVersion,
    ): NamedDomainObjectProvider<Configuration> {
        val elementsConfiguration = project.configurations.register(compilationName) {
            isCanBeDeclared = false
            isCanBeConsumed = true
            isCanBeResolved = false
        }

        project.afterEvaluate {
            elementsConfiguration.configure {
                attributes {
                    addAllLater(configurations[referenceConfigurationName].attributes)
                    attribute(KotlinVersionAttribute.attribute, objects.named(supportedKotlinVersion.name.toString()))
                }
            }
        }

        return elementsConfiguration
    }

    private fun Project.configureKotlinCompilation(
        extension: MultiKotlinVersionSupportExtension,
        activeKotlinVersionSets: List<KotlinVersionSet>,
        compilation: KotlinWithJavaCompilation<*, KotlinJvmCompilerOptions>,
    ) {
        activeKotlinVersionSets.forEach {
            compilation.defaultSourceSet.kotlin.srcDir(it.path.resolve("kotlin"))
            compilation.defaultSourceSet.resources.srcDir(it.path.resolve("resources"))
        }

        extendConfiguration(compilation.defaultSourceSet.apiConfigurationName, extension.sharedApiConfigurationName)
        extendConfiguration(compilation.defaultSourceSet.implementationConfigurationName, extension.sharedImplementationConfigurationName)
        extendConfiguration(compilation.defaultSourceSet.compileOnlyConfigurationName, extension.sharedCompileOnlyConfigurationName)
        extendConfiguration(compilation.defaultSourceSet.runtimeOnlyConfigurationName, extension.sharedRuntimeOnlyConfigurationName)
    }

    private fun Project.extendConfiguration(name: String, by: String) {
        configurations.named(name).configure {
            extendsFrom(configurations.named(by).get())
        }
    }
}

