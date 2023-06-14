package co.touchlab.skie.gradle

import co.touchlab.skie.gradle.version.DependencyMatrix
import co.touchlab.skie.gradle.version.SourceSetScope
import co.touchlab.skie.gradle.version.gradleApiVersions
import co.touchlab.skie.gradle.version.setupSourceSets
import org.gradle.api.*
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.artifacts.ivyservice.projectmodule.ProjectPublicationRegistry
import org.gradle.api.internal.plugins.PluginDescriptor
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.api.tasks.Copy
import org.gradle.initialization.buildsrc.GradlePluginApiVersionAttributeConfigurationAction
import org.gradle.internal.Describables
import org.gradle.internal.DisplayName
import org.gradle.internal.component.external.model.DefaultImmutableCapability
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.get
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugin.devel.tasks.GeneratePluginDescriptors
import org.gradle.plugin.devel.tasks.ValidatePlugins
import org.gradle.plugin.use.PluginId
import org.gradle.plugin.use.internal.DefaultPluginId
import org.gradle.plugin.use.resolve.internal.ArtifactRepositoriesPluginResolver
import org.gradle.plugin.use.resolve.internal.local.PluginPublication
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.util.concurrent.Callable

abstract class SKIEGradlePluginPlugin: Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
        val extension = createExtension()

        afterEvaluate {
            kotlin.targets.withType<KotlinJvmTarget>().configureEach {
                val pluginCapability = DefaultImmutableCapability(project.group.toString(), project.name, project.version.toString())
                configurations[apiElementsConfigurationName].outgoing.capability(pluginCapability)
                configurations[runtimeElementsConfigurationName].outgoing.capability(pluginCapability)

                configureJarTaskOfTarget(this, extension)
                configurePluginValidationsForTarget(this, extension)
            }

            configureTestKit(extension)
            configurePublishing()
            registerPlugins(extension)

            configureDescriptorGeneration(extension, kotlin)

            configurePluginValidationsAll()
            validatePluginDeclarations(extension)
            configureDependencyGradlePluginResolution()
        }
    }

    private val KotlinJvmTarget.main: KotlinJvmCompilation
        get() = compilations.getByName("main")

    private fun Project.createExtension(): SKIEGradlePluginDevelopmentExtension {
        return extensions.create<SKIEGradlePluginDevelopmentExtension>(
            extensionName,
            project,
        )
    }

    private fun Project.configureJarTaskOfTarget(target: KotlinJvmTarget, extension: SKIEGradlePluginDevelopmentExtension) {
        tasks.named<Jar>(target.artifactsTaskName) {
            val descriptors = mutableListOf<PluginDescriptor>()
            val classes = mutableSetOf<String>()
            val pluginDescriptorCollector = PluginDescriptorCollectorAction(descriptors)
            val classManifestCollector = ClassManifestCollectorAction(classes)
            val pluginsProvider: Provider<Collection<PluginDeclaration>> = provider { extension.plugins.asMap.values }
            val pluginValidationAction = PluginValidationAction(pluginsProvider, descriptors, classes)

            filesMatching(Patterns.pluginDescriptors, pluginDescriptorCollector)
            filesMatching(Patterns.classes, classManifestCollector)
            appendParallelSafeAction(pluginValidationAction)
        }
    }

    private fun Project.configureTestKit(extension: SKIEGradlePluginDevelopmentExtension) {
        Log.warn("TODO: configureTestKit")
    }

    private fun Project.configurePublishing() {
        pluginManager.withPlugin("maven-publish") {
            pluginManager.apply(SKIEMavenPluginPublishPlugin::class)
        }
    }

    private fun Project.registerPlugins(extension: SKIEGradlePluginDevelopmentExtension) {
        val projectInternal = this as ProjectInternal
        val registry = projectInternal.services.get<ProjectPublicationRegistry>()
        extension.plugins.all {
            registry.registerPublication(projectInternal, LocalPluginPublication(this))
        }
    }

    private fun Project.configureDescriptorGeneration(
        extension: SKIEGradlePluginDevelopmentExtension,
        kotlin: KotlinMultiplatformExtension,
    ) {
        val generatePluginDescriptors = tasks.register<GeneratePluginDescriptors>(Tasks.PluginDescriptors.name) {
            group = Tasks.group
            description = Tasks.PluginDescriptors.description
            declarations.set(extension.plugins)
            outputDirectory.set(layout.buildDirectory.dir(name))
        }

        kotlin.targets.withType<KotlinJvmTarget>().configureEach {
            compilations.named("main") {
                tasks.named<Copy>(processResourcesTaskName) {
                    val copyPluginDescriptors = rootSpec.addChild()
                    copyPluginDescriptors.into("META-INF/gradle-plugins")
                    copyPluginDescriptors.from(generatePluginDescriptors)
                }
            }
        }
    }

    private fun Project.validatePluginDeclarations(extension: SKIEGradlePluginDevelopmentExtension) {
        afterEvaluate {
            extension.plugins.forEach { declaration ->
                if (declaration.id == null) {
                    throw Errors.declarationMissingId(declaration.name)
                }
                if (declaration.implementationClass == null) {
                    throw Errors.declarationMissingImplementation(declaration.name)
                }
            }
        }
    }

    private fun Project.configurePluginValidationsForTarget(target: KotlinJvmTarget, extension: SKIEGradlePluginDevelopmentExtension) {
        val validatorTask = tasks.register<ValidatePlugins>(Tasks.ValidatePlugins.name(target.name)) {
            group = Tasks.group
            description = Tasks.ValidatePlugins.description

            outputFile.set(layout.buildDirectory.file("reports/plugin-development/${target.name}/validation-report.txt"))

            classes.setFrom(Callable<FileCollection> { target.main.output.classesDirs })
            classpath.setFrom(Callable<FileCollection> { target.main.compileDependencyFiles })

            enableStricterValidation.set(true)
            launcher.convention(toolchainLauncher())
        }
    }

    private fun Project.configurePluginValidationsAll() {
        tasks.register(Tasks.ValidatePlugins.name) {
            group = Tasks.group
            description = Tasks.ValidatePlugins.description

            dependsOn(tasks.withType<ValidatePlugins>())
        }
    }

    private fun Project.toolchainLauncher(): Provider<JavaLauncher> {
        val extension = extensions.getByType<JavaPluginExtension>()
        val service = extensions.getByType<JavaToolchainService>()

        return service.launcherFor(extension.toolchain)
    }

    private fun Project.configureDependencyGradlePluginResolution() {
        GradlePluginApiVersionAttributeConfigurationAction().execute(this as ProjectInternal)
    }

    class PluginDescriptorCollectorAction(
        private val descriptors: MutableList<PluginDescriptor>,
    ): Action<FileCopyDetails> {
        override fun execute(fileCopyDetails: FileCopyDetails) {
            try {
                val descriptor = PluginDescriptor(fileCopyDetails.file.toURI().toURL())
                if (descriptor.implementationClassName != null) {
                    descriptors.add(descriptor)
                }
            } catch (e: MalformedURLException) {
                // Not sure under what scenario (if any) this would occur,
                // but there's no sense in collecting the descriptor if it does.
                return
            }
        }
    }

    class ClassManifestCollectorAction(
        val classes: MutableSet<String>,
    ): Action<FileCopyDetails> {
        override fun execute(fileCopyDetails: FileCopyDetails) {
            classes.add(fileCopyDetails.relativePath.toString())
        }
    }

    class PluginValidationAction(
        private val plugins: Provider<Collection<PluginDeclaration>>,
        private val descriptors: Collection<PluginDescriptor>,
        private val classes: Set<String>,
    ): Action<Task> {
        override fun execute(task: Task) {
            if (descriptors.isNotEmpty()) {
                val pluginFileNames = mutableSetOf<String>()
                descriptors.forEach { descriptor ->
                    val descriptorUri = try {
                        descriptor.propertiesFileUrl.toURI()
                    } catch (e: URISyntaxException) {
                        // Do nothing since the only side effect is that we wouldn't
                        // be able to log the plugin descriptor file name.  Shouldn't
                        // be a reasonable scenario where this occurs since these
                        // descriptors should be generated from real files.
                        null
                    }
                    val pluginFileName = descriptorUri?.let { File(it).name } ?: "UNKNOWN"
                    pluginFileNames.add(pluginFileName)
                    val pluginImplementation = descriptor.implementationClassName
                    if (pluginImplementation.isNullOrBlank()) {
                        Log.invalidDescriptorWarning(task, pluginFileName)
                    } else if (!hasFullyQualifiedClass(pluginImplementation)) {
                        Log.badImplClassWarning(task, pluginFileName, pluginImplementation)
                    }
                }

                plugins.get().forEach { declaration ->
                    if (!pluginFileNames.contains("${declaration.id}.properties")) {
                        Log.declaredPluginMissingWarning(task, declaration.name, declaration.id)
                    }
                }
            } else {
                Log.noDescriptorWarning(task)
            }
        }

        private fun hasFullyQualifiedClass(fqClass: String): Boolean {
            return classes.contains(fqClass.replace("\\.".toRegex(), "/") + ".class")
        }
    }

    class LocalPluginPublication(private val pluginDeclaration: PluginDeclaration): PluginPublication {
        override fun getDisplayName(): DisplayName = Describables.withTypeAndName("plugin", pluginDeclaration.name)

        override fun getPluginId(): PluginId = DefaultPluginId.of(pluginDeclaration.id)
    }

    companion object {
        const val extensionName = "gradlePlugin"
        const val gradlePluginsDirectory = "gradle-plugins"
    }

    object Tasks {
        val group = "Plugin development"

        object PluginDescriptors {
            val name = "pluginDescriptors"
            val description = "Generates plugin descriptors from plugin declarations."
        }

        object ValidatePlugins {
            val name = "validatePlugins"
            fun name(variantName: String) = "validate${variantName.capitalize()}Plugins"

            val description = "Validates the plugin by checking parameter annotations on task and artifact transform types etc.";
        }

        val pluginUnderTestMetadata = "pluginUnderTestMetadata"
    }

    object Patterns {
        const val pluginDescriptors = "META-INF/$gradlePluginsDirectory/*.properties"
        const val classes = "**/*.class"
    }

    object Log: Logger by Logging.getLogger(SKIEGradlePluginPlugin::class.java) {
        fun noDescriptorWarning(task: Task) {
            warn("${task.path}: No valid plugin descriptors were found in META-INF/$gradlePluginsDirectory")
        }

        fun invalidDescriptorWarning(task: Task, pluginFileName: String) {
            warn("${task.path}: A plugin descriptor was found for ${pluginFileName} but it was invalid.")
        }

        fun badImplClassWarning(task: Task, pluginFileName: String, pluginImplementation: String) {
            warn("${task.path}: A valid plugin descriptor was found for $pluginFileName but the implementation class $pluginImplementation was not found in the jar.")
        }

        fun declaredPluginMissingWarning(task: Task, pluginName: String, pluginId: String) {
            warn("${task.path}: Could not find plugin descriptor of $pluginName at META-INF/$gradlePluginsDirectory/$pluginId.properties")
        }

//         const val BAD_IMPL_CLASS_WARNING_MESSAGE = "%s: A valid plugin descriptor was found for %s but the implementation class %s was not found in the jar."
//         const val INVALID_DESCRIPTOR_WARNING_MESSAGE = "%s: A plugin descriptor was found for %s but it was invalid."
//         const val NO_DESCRIPTOR_WARNING_MESSAGE =
//         const val DECLARED_PLUGIN_MISSING_MESSAGE =
//             "%s: Could not find plugin descriptor of %s at META-INF/" + JavaGradlePluginPlugin.GRADLE_PLUGINS + "/%s.properties"
//         const val DECLARATION_MISSING_ID_MESSAGE = "Missing id for %s"
//         const val DECLARATION_MISSING_IMPLEMENTATION_MESSAGE = "Missing implementationClass for %s"
    }

    object Errors {
        fun declarationMissingId(declarationName: String): Throwable {
            return IllegalArgumentException("Missing id for $declarationName")
        }

        fun declarationMissingImplementation(declarationName: String): Throwable {
            return IllegalArgumentException("Missing implementationClass for $declarationName")
        }
    }
}

abstract class SKIEMavenPluginPublishPlugin: Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        afterEvaluate {
            extensions.configure<PublishingExtension> {
                val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
                kotlin.metadata {
                    mavenPublication {
                        val metadataPublication = this
                        val pluginDevelopment = extensions.getByType<SKIEGradlePluginDevelopmentExtension>()
                        pluginDevelopment.plugins.forEach { declaration ->
                            createMavenMarkerPublication(declaration, metadataPublication, publications)
                        }
                    }
                }
            }
        }
    }

    private fun Project.createMavenMarkerPublication(
        declaration: PluginDeclaration,
        coordinates: MavenPublication,
        publications: PublicationContainer,
    ) {
        val pluginId = declaration.id
        val pluginGroupId = coordinates.groupId
        val pluginArtifactId = coordinates.artifactId
        val pluginVersion = coordinates.version

        val publication = publications.create<MavenPublication>("${declaration.name}PluginMarkerMaven") as MavenPublicationInternal
        publication.apply {
            isAlias = true
            artifactId = pluginId + ArtifactRepositoriesPluginResolver.PLUGIN_MARKER_SUFFIX
            groupId = pluginId
            pom.withXml {
                val root = asElement()
                val document = root.ownerDocument
                val dependencies = root.appendChild(document.createElement("dependencies"))
                val dependency = dependencies.appendChild(document.createElement("dependency"))
                val groupId = dependency.appendChild(document.createElement("groupId"))
                groupId.textContent = pluginGroupId
                val artifactId = dependency.appendChild(document.createElement("artifactId"))
                artifactId.textContent = pluginArtifactId
                val version = dependency.appendChild(document.createElement("version"))
                version.textContent = pluginVersion
            }
            pom.name.set(declaration.displayName)
            pom.description.set(declaration.description)
        }
    }
}

/**
 * Configuration options for the [org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin].
 *
 *
 * Below is a full configuration example. Since all properties have sensible defaults,
 * typically only selected properties will be configured.
 *
 * <pre class='autoTested'>
 * plugins {
 * id 'java-gradle-plugin'
 * }
 *
 * sourceSets {
 * customMain
 * functionalTest
 * }
 *
 * gradlePlugin {
 * pluginSourceSet project.sourceSets.customMain
 * testSourceSets project.sourceSets.functionalTest
 * plugins {
 * helloPlugin {
 * id  = 'org.example.hello'
 * implementationClass = 'org.example.HelloPlugin'
 * }
 * }
 * }
</pre> *
 *
 * @see org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
 *
 * @since 2.13
 */
abstract class SKIEGradlePluginDevelopmentExtension(
    project: Project,
) {
    /**
     * Returns the property holding the URL for the plugin's website.
     *
     * @since 7.6
     */
    @get:Incubating
    val website: Property<String>

    /**
     * Returns the property holding the URL for the plugin's VCS repository.
     *
     * @since 7.6
     */
    @get:Incubating
    val vcsUrl: Property<String>

    /**
     * Returns the declared plugins.
     *
     * @return the declared plugins, never null
     */
    val plugins: NamedDomainObjectContainer<PluginDeclaration>

    init {
        plugins = project.container(PluginDeclaration::class.java)
        website = project.objects.property(String::class.java)
        vcsUrl = project.objects.property(String::class.java)
    }
}
