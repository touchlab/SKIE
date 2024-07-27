@file:OptIn(ExternalKotlinTargetApi::class)

import co.touchlab.skie.buildsetup.tasks.BuildNestedGradle
import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.darwinPlatformDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinSoftwareComponentWithCoordinatesAndPublication
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinCompilation.Delegate
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinCompilationDescriptor
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinTargetDescriptor
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createExternalKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetAttribute
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetType
import org.jetbrains.kotlin.gradle.targets.js.toAttribute
import org.jetbrains.kotlin.gradle.utils.named
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("skie.base")
//     kotlin("multiplatform")
//     id("skie.runtime.kotlin")
//     id("skie.publishable")
//     `maven-publish`
//     `java-base`
}

// skiePublishing {
//     name = "SKIE Runtime - Kotlin"
//     description = "Kotlin Multiplatform part of the SKIE runtime. It's used to facilitate certain features of SKIE."
//     publishSources = true
//     publishJavadoc = true
// }

// DOESN'T WORK FOR NATIVE ....
// class IncludeBuildExternalTarget(delegate: Delegate): DecoratedExternalKotlinTarget(delegate) {
//
// }
//
// class IncludeBuildExternalCompilation(delegate: Delegate): DecoratedExternalKotlinCompilation(delegate) {
//
// }
//
// kotlin {
//     kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
//         val pathSafeKotlinVersionName = kotlinToolingVersion.primaryVersion.toString().replace('.', '_')
//         val supportedDarwinTargets = darwinPlatformDimension().components
//             .filter { it.sinceKotlinVersion?.let { kotlinToolingVersion.primaryVersion >= KotlinToolingVersion(it) } ?: true }
//             .filter { it.untilKotlinVersionExclusive?.let { kotlinToolingVersion.primaryVersion < KotlinToolingVersion(it) } ?: true }
//
//         supportedDarwinTargets.forEach { darwinPlatformComponent ->
//             val target = createExternalKotlinTarget<IncludeBuildExternalTarget> {
//                 targetName = "${darwinPlatformComponent}__kgp_$pathSafeKotlinVersionName"
//                 platformType = KotlinPlatformType.native
//                 targetFactory = ExternalKotlinTargetDescriptor.TargetFactory(::IncludeBuildExternalTarget)
//
//                 configure { target ->
//                     (target as KotlinTarget).attributes {
//                         attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
//                         attribute(KotlinNativeTarget.konanTargetAttribute, darwinPlatformComponent.kotlinNativeTarget)
//                     }
//                 }
//             }
//
//             val mainCompilation = target.createCompilation {
//                 compilationName = KotlinCompilation.MAIN_COMPILATION_NAME
//                 compilationFactory = ExternalKotlinCompilationDescriptor.CompilationFactory(::IncludeBuildExternalCompilation)
//                 defaultSourceSet = sourceSets.maybeCreate(compilationName)
//             }
//         }
//     }
// }

// kotlin {
//     sourceSets.commonMain {
//         dependencies {
//             implementation(libs.kotlinx.coroutines.core.legacy)
//         }
//     }
//
//     // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
//     // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
//     // This solution is far from ideal due to current project setup limitations - refactor this code as part of the build logic rewrite
//     sourceSets.configureEach {
//         val nameSegments = name.split("kgp_")
//         if (nameSegments.size == 2) {
//             val kgpVersionSegment = nameSegments[1]
//             dependencies {
//                 if (kgpVersionSegment.startsWith("1.8.0")) {
//                     implementation(libs.kotlinx.coroutines.core.legacy)
//                 } else {
//                     implementation(libs.kotlinx.coroutines.core)
//                 }
//             }
//         }
//     }
// }

// KotlinCompilerVersion.registerIn(dependencies, "2.0.0")

// abstract class BuildNestedGradle: DefaultTask() {
//
//     @get:InputDirectory
//     abstract val projectDir: DirectoryProperty
//
//     @get:Input
//     abstract val tasks: ListProperty<String>
//
//     @get:Input
//     abstract val kotlinVersion: Property<String>
//
//     @get:Input
//     abstract val archivePrefix: Property<String>
//
//     init {
// //         outputs.file(
// //             zip(
// //                 kotlinVersion,
// //                 darwinPlatform,
// //                 projectDir,
// //                 archivePrefix,
// //             ) { kotlinVersion, darwinPlatform, projectDir, arhivePrefix ->
// //                 projectDir.file("build/classes/kotlin/${darwinPlatform.value}/main/klib/${archivePrefix}__kgp_${kotlinVersion.replace('.', '_')}.klib")
// //             }
// //         )
//     }
//
//     fun <A, B, C, D, RESULT> zip(
//         aProperty: Property<A>,
//         bProperty: Property<B>,
//         cProperty: Property<C>,
//         dProperty: Property<D>,
//         combiner: (a: A, b: B, c: C, d: D) -> RESULT,
//     ): Provider<RESULT> {
//         return aProperty.zip(bProperty) { a, b -> a to b }
//             .zip(cProperty.zip(dProperty) { c, d -> c to d }) { (a, b), (c, d) ->
//                 combiner(a, b, c, d)
//             }
//     }
//
//     @TaskAction
//     fun buildNestedGradle() {
//         val connection = GradleConnector.newConnector()
//             .forProjectDirectory(projectDir.get().asFile)
//             .connect()
//
//         connection.newBuild()
//             .forTasks(*tasks.get().toTypedArray())
//             .withSystemProperties(
//                 mapOf(
//                     "kotlinVersion" to kotlinVersion.get(),
//                 )
//             )
//             .setStandardOutput(System.out)
//             .setStandardError(System.err)
//             .run()
//     }
//
// }

// abstract class SoftwareComponentFactoryAccessor @Inject constructor(
//     val softwareComponentFactory: SoftwareComponentFactory,
// )
// val softwareComponentFactoryAccessor = objects.newInstance<SoftwareComponentFactoryAccessor>()
//
// // val rootSoftwareComponent = KotlinSoftwareComponentWithCoordinatesAndPublication(project, "kotlin", emptyList())
// // components.create()
//
// val rootSourcesConfiguration = configurations.create("kotlinMultiplatformSourcesElements") {
//     attributes {
//         attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_SOURCES))
//         attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
//         attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
//         attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
//     }
// }
//
// val sourcesJar = tasks.register<Jar>("skieRuntimeSourcesJar") {
//     archiveClassifier = "sources"
// }
//
// artifacts.add(rootSourcesConfiguration.name, sourcesJar.map { it.archiveFile }) {
//     builtBy(sourcesJar)
// }
//
// val rootSoftwareComponent = softwareComponentFactoryAccessor.softwareComponentFactory.adhoc("kotlin")
// components.add(rootSoftwareComponent)
// rootSoftwareComponent.addVariantsFromConfiguration(rootSourcesConfiguration) { }
//
// val rootPublication = publishing.publications.create<MavenPublication>("kotlinMultiplatform") {
//     from(rootSoftwareComponent)
//     (this as MavenPublicationInternal).publishWithOriginalFileName()
// //     artifactId = "runtime-kotlin"
//
//
// //     artifact(buildKotlinToolingMetadataTask.map { it.outputFile }) {
// //
// //     }
//
// //
//
// //
// //     val javadocJar = tasks.register<Jar>("skieRuntimeJavadocJar") {
// //         archiveClassifier = "javadoc"
// //     }
// //     artifact(javadocJar)
// }



// kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
//     val pathSafeKotlinVersionName = kotlinToolingVersion.primaryVersion.toString().replace('.', '_')
//     val supportedDarwinTargets = darwinPlatformDimension().components
//         .filter { it.sinceKotlinVersion?.let { kotlinToolingVersion.primaryVersion >= KotlinToolingVersion(it) } ?: true }
//         .filter { it.untilKotlinVersionExclusive?.let { kotlinToolingVersion.primaryVersion < KotlinToolingVersion(it) } ?: true }
//
//     val copyProjectTask = tasks.register<Sync>("copyProject__kgp_${kotlinToolingVersion.primaryVersion}") {
//         description = "Copies implementation for Kotlin ${kotlinToolingVersion.primaryVersion}."
//
//         from(layout.projectDirectory.dir("impl")) {
//             include("src/**", "build.gradle.kts", "gradle.properties", "settings.gradle.kts")
//             filter(
//                 ReplaceTokens::class,
//                 "tokens" to mapOf(
//                     "targetKotlinVersion" to kotlinToolingVersion.primaryVersion.toString(),
//                     "targets" to supportedDarwinTargets.joinToString("\n") { "$it()" },
//                     // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
//                     // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
//                     "dependencies" to if (kotlinToolingVersion.value == "1.8.0") {
//                         "implementation(libs.kotlinx.coroutines.core.legacy)"
//                     } else {
//                         "implementation(libs.kotlinx.coroutines.core)"
//                     }
//                 )
//             )
//         }
//         into(layout.buildDirectory.dir("impl_$pathSafeKotlinVersionName"))
//     }
//
//     val buildTask = tasks.register<GradleBuild>("buildProject__kgp_${kotlinToolingVersion.primaryVersion}") {
//         group = "build"
//
//         dependsOn(copyProjectTask)
//
//         setDir(layout.buildDirectory.dir("impl_$pathSafeKotlinVersionName"))
//
// //         archivePrefix = "runtime-kotlin"
// //         projectDir =
// //         kotlinVersion = kotlinToolingVersion.primaryVersion.toString()
//         tasks = supportedDarwinTargets.flatMap { darwinPlatformComponent ->
//             val uppercaseTargetName = darwinPlatformComponent.value.replaceFirstChar { it.uppercase() }
//             listOf(
//                 "generateMetadataFileFor${uppercaseTargetName}Publication",
//                 "generatePomFileFor${uppercaseTargetName}Publication",
//             )
//         } + listOf(
//             "generateMetadataFileForKotlinMultiplatformPublication",
//             "generatePomFileForKotlinMultiplatformPublication",
//         )
//     }
//
// //     val component = softwareComponentFactoryAccessor.softwareComponentFactory.adhoc("skieRuntime")
// //     components.add(component)
//
//
// //     val buildTask = tasks.register<BuildNestedGradle>("buildKotlin__kgp_${kotlinToolingVersion.primaryVersion}") {
// //         group = "build"
// //
// //         archivePrefix = "runtime-kotlin"
// //         projectDir = layout.projectDirectory.dir("impl")
// //         kotlinVersion = kotlinToolingVersion.primaryVersion.toString()
// //         tasks = darwinPlatformDimension().components.flatMap { darwinPlatformComponent ->
// //             val uppercaseTargetName = darwinPlatformComponent.value.replaceFirstChar { it.uppercase() }
// //             listOf(
// //                 "generateMetadataFileFor${uppercaseTargetName}Publication",
// //                 "generatePomFileFor${uppercaseTargetName}Publication",
// //             )
// //         }
// //     }
//
//     supportedDarwinTargets.map { darwinPlatformComponent ->
//         val componentName = "${darwinPlatformComponent.value}__kgp_${kotlinToolingVersion.primaryVersion}"
//
// //         val configuration = configurations.create(componentName) {
// //             isCanBeConsumed = true
// //             isCanBeResolved = false
// //
// //             attributes {
// //                 attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
// //                 attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
// //                 attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
// //                 attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
// //                 attribute(KotlinNativeTarget.konanTargetAttribute, darwinPlatformComponent.kotlinNativeTarget)
// //                 attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.primaryVersion.toString()))
// //             }
// //         }
//
// //         val component = softwareComponentFactoryAccessor.softwareComponentFactory.adhoc(componentName)
// //         component.addVariantsFromConfiguration()
//
// //         val componentPublication = publishing.publications.create<MavenPublication>(componentName).apply {
// //             from(component)
// //             (this as MavenPublicationInternal).publishWithOriginalFileName()
// //             artifactId = componentName
// //
// //             // TODO: Rewrite poms?
// //         }
//
//         // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
//         // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
// //         dependencies {
// //             configuration(
// //                 if (kotlinToolingVersion.value == "1.8.0") {
// //                     libs.kotlinx.coroutines.core.legacy
// //                 } else {
// //                     libs.kotlinx.coroutines.core
// //                 }
// //             )
// //         }
//
//         val artifactName = "runtime-kotlin__kgp_${pathSafeKotlinVersionName}"
//         val artifactClassifierPrefix = "${darwinPlatformComponent.value}-kgp_${pathSafeKotlinVersionName}"
//         val klibPath = "impl_$pathSafeKotlinVersionName/build/classes/kotlin/${darwinPlatformComponent.value}/main/klib/skie-kotlin-runtime.klib"
//         val extraArchiveBaseName = "impl/build/libs/skie-kotlin-runtime__kgp_${pathSafeKotlinVersionName}-${darwinPlatformComponent.value.lowercase()}"
//
//         artifacts.add(configuration.name, layout.buildDirectory.file(klibPath)) {
//             builtBy(buildTask)
//             name = artifactName
//             classifier = artifactClassifierPrefix
//         }
// //         if (kotlinToolingVersion.value == "1.8.0") {
// //             val sourcesJar = tasks.register<Jar>("${darwinPlatformComponent.value}__kgp_${pathSafeKotlinVersionName}SourcesJar") {
// //                 archiveClassifier = "$artifactClassifierPrefix-sources"
// //                 archiveBaseName = "${archiveBaseName.orNull ?: project.name}-${darwinPlatformComponent.value}-kgp_${pathSafeKotlinVersionName}"
// //             }
// //             artifacts.add(configuration.name, sourcesJar.map { it.archiveFile }) {
// //                 builtBy(sourcesJar)
// //                 classifier = "$artifactClassifierPrefix-sources"
// //                 name = artifactName
// //             }
// //         } else {
// //             artifacts.add(configuration.name, layout.projectDirectory.file("$extraArchiveBaseName-sources.jar")) {
// //                 builtBy(buildTask)
// //                 classifier = "$artifactClassifierPrefix-sources"
// //                 name = artifactName
// //             }
// //         }
// //
// //         val javadocJar = tasks.register<Jar>("${darwinPlatformComponent.value}__kgp_${pathSafeKotlinVersionName}JavadocJar") {
// //             archiveClassifier = "$artifactClassifierPrefix-javadoc"
// //             archiveBaseName = "${archiveBaseName.orNull ?: project.name}-${darwinPlatformComponent.value}-kgp_${pathSafeKotlinVersionName}"
// //         }
// //         artifacts.add(configuration.name, javadocJar.map { it.archiveFile }) {
// //             builtBy(javadocJar)
// //             classifier = "$artifactClassifierPrefix-javadoc"
// //             name = artifactName
// //
// //         }
// //
// //         artifacts.add(configuration.name, layout.projectDirectory.file("$extraArchiveBaseName-metadata.jar")) {
// //             builtBy(buildTask)
// //             classifier = "$artifactClassifierPrefix-metadata"
// //             name = artifactName
// //         }
//
//         component.addVariantsFromConfiguration(configuration) {
//             mapToMavenScope("runtime")
//         }
//
//         val platformModuleDependencyProvider = provider {
//             (project.dependencies.create("co.touchlab.skie:${componentName}:${version}") as ModuleDependency).apply {
//                 capabilities {
//                     requireCapability(
//                         ComputedCapability(
//                             groupProvider = provider { group.toString() },
//                             nameValue = componentName,
//                             versionProvider = provider { version.toString() },
//                             suffix = "",
//                         )
//                     )
//                 }
//             }
//         }
//
//         val publishedConfiguration = configurations.create("${configuration.name}-published") {
//             isCanBeConsumed = false
//             isCanBeResolved = false
//
//             configuration.outgoing.capability(
//                 ComputedCapability(
//                     groupProvider = provider { group.toString() },
//                     nameValue = componentName,
//                     versionProvider = provider { version.toString() },
//                     suffix = "",
//                 )
//             )
//             dependencies.addLater(platformModuleDependencyProvider)
//             copyAttributes(configuration.attributes, this.attributes)
//             rootSoftwareComponent.addVariantsFromConfiguration(this) { }
//         }
//     }
// }

// private class ComputedCapability(
//     val groupProvider: Provider<String>,
//     val nameValue: String,
//     val versionProvider: Provider<String>,
//     val suffix: String?
// ) : Capability {
//     override fun getGroup(): String = groupProvider.get()
//
//     override fun getName(): String = nameValue + suffix?.let { "..$it" }.orEmpty()
//
//     override fun getVersion(): String? = versionProvider.get()
//
//     fun notation(): String = "$group:$name:$version"
// }
//
// private fun copyAttributes(from: AttributeContainer, to: AttributeContainer, keys: Iterable<Attribute<*>> = from.keySet()) {
//     // capture type argument T
//     fun <T : Any> copyOneAttribute(from: AttributeContainer, to: AttributeContainer, key: Attribute<T>) {
//         val value = checkNotNull(from.getAttribute(key))
//         to.attribute(key, value)
//     }
//     for (key in keys) {
//         copyOneAttribute(from, to, key)
//     }
// }

// val buildKotlinIosArm64__kgp_2_0_0 = tasks.register<GradleBuild>("buildKotlinIosArm64__kgp_2.0.0") {
//     setDir(layout.projectDirectory.dir("impl"))
//
//     // TODO: Which task should we run?
//     /*
//     > Task :runtime:runtime-kotlin:compileKotlinIosArm32__kgp_1.9.0
//     > Task :runtime:runtime-kotlin:iosArm32__kgp_1.9.0SourcesJar
//     > Task :runtime:runtime-kotlin:generatePomFileForIosArm32__kgp_1.9.0Publication
//      */
//     tasks = listOf(
//         "iosArm64MainKlibrary",
//     )
// }

// val iosArm64__kgp_2_0_0 = configurations.create("iosArm64__kgp_2.0.0") {
//     isCanBeConsumed = true
//     isCanBeResolved = false
//
//     attributes {
//         attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
//         attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
//         attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
//         attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
//         attribute(KotlinNativeTarget.konanTargetAttribute, "ios_arm64")
//         attribute(KotlinCompilerVersion.attribute, objects.named("2.0.0"))
//     }
// }

// artifacts {
//     add("iosArm64__kgp_2.0.0", layout.projectDirectory.file("impl/build/classes/kotlin/iosArm64/main/klib/skie-kotlin-runtime__kgp_2_0_0.klib")) {
//         builtBy(buildKotlinIosArm64__kgp_2_0_0)
//     }
// }

// class RuntimeKotlinPlugin @Inject constructor(
//     private val softwareComponentFactory: SoftwareComponentFactory,
// ): Plugin<Project> {
//     override fun apply(target: Project) {
//         val adhocComponent = softwareComponentFactory.adhoc("kotlin")
//
//         components.add(adhocComponent)
//
//         adhocComponent.addVariantsFromConfiguration()
//     }
// }



// private fun createRootPublication(project: Project, publishing: PublishingExtension): MavenPublication {
// //     val rootSoftwareComponent =
//
//     return publishing.publications.create<MavenPublication>("kotlinMultiplatform") {
//         from(rootSoftwareComponent)
//         (this as MavenPublicationInternal).publishWithOriginalFileName()
//
//     }
// }

// private fun MavenPublication.addKotlinToolingMetadataArtifactIfNeeded(
//     project: Project,
//     buildKotlinToolingMetadataTask: Task,
//     artifactFile: Provider<File>,
// ) {
//     artifact() {
//
//     }
// }

// private fun List<SkieRuntimeTarget>.createTargetPublications(project: Project, publishing: PublishingExtension) {
//     this.forEach { kotlinTarget ->
//
//     }
// }
//
// private fun SkieRuntimeTarget.createMavenPublications(publications: PublicationContainer) {
//
// }
//
// private fun SkieRuntimeTarget.setupApiElements(configuration: Configuration) {
//     configuration.setUsesPlatformOf(this)
//     configuration.attributes {
//         attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.producerApiUsage(project, platformType))
//         attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
//     }
// }
//
// private fun SkieRuntimeTarget.setupRuntimeElements(configuration: Configuration) {
//     configuration.setUsesPlatformOf(this)
//     configuration.attributes {
//         attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.producerRuntimeUsage(project, platformType))
//         attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
//     }
// }
//
// private fun SkieRuntimeTarget.setupSourcesElements(configuration: Configuration) {
//     configuration.configureSourcesPublicationAttributes(this)
// }
//
// private fun Configuration.configureSourcesPublicationAttributes(target: SkieRuntimeTarget) {
//     attributes {
//         // In order to be consistent with Java Gradle Plugin, set usage attribute for sources variant
//         // to be either JAVA_RUNTIME (for jvm) or KOTLIN_RUNTIME (for other targets)
//         // the latter isn't a strong requirement since there is no tooling that consume kotlin sources through gradle variants at the moment
//         // so consistency with Java Gradle Plugin seemed most desirable choice.
//         attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.producerRuntimeUsage(project, target.platformType))
//         attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
//         attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
//         // Bundling attribute is about component dependencies, external means that they are provided as separate components
//         // source variants doesn't have any dependencies (at least at the moment) so there is not much sense to use this attribute
//         // however for Java Gradle Plugin compatibility and in order to prevent weird Variant Resolution errors we include this attribute
//         attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
//     }
//     setUsesPlatformOf(target)
// }
//
// object KotlinUsages {
//     const val KOTLIN_API = "kotlin-api"
//     const val KOTLIN_RUNTIME = "kotlin-runtime"
//     const val KOTLIN_METADATA = "kotlin-metadata"
//     const val KOTLIN_SOURCES = "kotlin-sources"
//
//     private val jvmPlatformTypes: Set<KotlinPlatformType> = setOf(KotlinPlatformType.jvm, KotlinPlatformType.androidJvm)
//
//     internal fun producerApiUsage(project: Project, platformType: KotlinPlatformType) = project.objects.named(Usage::class.java,
//         when (platformType) {
//             in jvmPlatformTypes -> "java-api-jars"
//             else -> org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_API
//         }
//     )
//
//     internal fun producerRuntimeUsage(project: Project, platformType: KotlinPlatformType) = project.objects.named(Usage::class.java,
//         when (platformType) {
//             // This attribute is deprecated in Gradle and additionally to Usage attribute
//             // it implicitly adds `org.gradle.libraryelements=jar`
//             in jvmPlatformTypes -> "java-runtime-jars"
//             else -> org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_RUNTIME
//         }
//     )
// }
//
// fun <T: HasAttributes> T.setUsesPlatformOf(target: SkieRuntimeTarget): T {
//     attributes.attribute(KotlinPlatformType.attribute, target.platformType)
//
//     attributes.attribute(
//         TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
//         project.objects.named(
//             when (target.platformType) {
//                 KotlinPlatformType.jvm -> "standard-jvm"
//                 KotlinPlatformType.androidJvm -> "android"
//                 else -> "non-jvm"
//             }
//         )
//     )
//
//     when (target) {
//         is SkieRuntimeJsTarget -> {
//             attributes.attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.legacy)
//         }
//         is SkieRuntimeJsIrTarget -> if (target.platformType == KotlinPlatformType.js) {
//             attributes.attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
//         } else {
//             attributes.attribute(KotlinWasmTargetAttribute.wasmTargetAttribute, target.wasmTargetType!!.toAttribute())
//         }
//         is SkieRuntimeNativeTarget -> {
//             attributes.attribute(KotlinNativeTarget.konanTargetAttribute, target.konanTarget.name)
//         }
//         else -> {}
//     }
//
//     return this
// }
//
// sealed interface SkieRuntimeTarget {
//     val platformType: KotlinPlatformType
// }
//
// interface SkieRuntimeJsTarget: SkieRuntimeTarget
//
// interface SkieRuntimeJsIrTarget: SkieRuntimeTarget {
//     val wasmTargetType: KotlinWasmTargetType?
// }
//
// class SkieRuntimeNativeTarget(
//     val konanTarget: SkieRuntimeKonanTarget
// ): SkieRuntimeTarget {
//     override val platformType: KotlinPlatformType = KotlinPlatformType.native
// }
//
// sealed class SkieRuntimeKonanTarget(
//     val name: String,
//     val sinceKotlinVersion: String? = null,
//     val untilKotlinVersionExclusive: String? = null
// ) {
//     object ANDROID_X64: SkieRuntimeKonanTarget("android_x64")
//     object ANDROID_X86: SkieRuntimeKonanTarget("android_x86")
//     object ANDROID_ARM32: SkieRuntimeKonanTarget("android_arm32")
//     object ANDROID_ARM64: SkieRuntimeKonanTarget("android_arm64")
//     object IOS_ARM32: SkieRuntimeKonanTarget("ios_arm32", untilKotlinVersionExclusive = "2.0.0")
//     object IOS_ARM64: SkieRuntimeKonanTarget("ios_arm64")
//     object IOS_X64: SkieRuntimeKonanTarget("ios_x64")
//     object IOS_SIMULATOR_ARM64: SkieRuntimeKonanTarget("ios_simulator_arm64")
//     object WATCHOS_ARM32: SkieRuntimeKonanTarget("watchos_arm32")
//     object WATCHOS_ARM64: SkieRuntimeKonanTarget("watchos_arm64")
//     object WATCHOS_X86: SkieRuntimeKonanTarget("watchos_x86", untilKotlinVersionExclusive = "2.0.0")
//     object WATCHOS_X64: SkieRuntimeKonanTarget("watchos_x64")
//     object WATCHOS_SIMULATOR_ARM64: SkieRuntimeKonanTarget("watchos_simulator_arm64")
//     object WATCHOS_DEVICE_ARM64: SkieRuntimeKonanTarget("watchos_device_arm64", sinceKotlinVersion = "1.8.20")
//     object TVOS_ARM64: SkieRuntimeKonanTarget("tvos_arm64")
//     object TVOS_X64: SkieRuntimeKonanTarget("tvos_x64")
//     object TVOS_SIMULATOR_ARM64: SkieRuntimeKonanTarget("tvos_simulator_arm64")
//     object MACOS_X64: SkieRuntimeKonanTarget("macos_x64")
//     object MACOS_ARM64: SkieRuntimeKonanTarget("macos_arm64")
//     object LINUX_X64: SkieRuntimeKonanTarget("linux_x64")
//     object MINGW_X86: SkieRuntimeKonanTarget("mingw_x86", untilKotlinVersionExclusive = "2.0.0")
//     object MINGW_X64: SkieRuntimeKonanTarget("mingw_x64")
//     object LINUX_ARM64: SkieRuntimeKonanTarget("linux_arm64")
//     object LINUX_ARM32_HFP: SkieRuntimeKonanTarget("linux_arm32_hfp")
//     object LINUX_MIPS32: SkieRuntimeKonanTarget("linux_mips32", untilKotlinVersionExclusive = "2.0.0")
//     object LINUX_MIPSEL32: SkieRuntimeKonanTarget("linux_mipsel32", untilKotlinVersionExclusive = "2.0.0")
//     object WASM32: SkieRuntimeKonanTarget("wasm32", untilKotlinVersionExclusive = "2.0.0")
//     object ZEPHYR: SkieRuntimeKonanTarget("zephyr", untilKotlinVersionExclusive = "2.0.0")
// }
//
// val skieRuntimeDarwinTargets = listOf(
//     SkieRuntimeKonanTarget.IOS_ARM32,
//     SkieRuntimeKonanTarget.IOS_ARM64,
//     SkieRuntimeKonanTarget.IOS_X64,
//     SkieRuntimeKonanTarget.IOS_SIMULATOR_ARM64,
//     SkieRuntimeKonanTarget.WATCHOS_ARM32,
//     SkieRuntimeKonanTarget.WATCHOS_ARM64,
//     SkieRuntimeKonanTarget.WATCHOS_X86,
//     SkieRuntimeKonanTarget.WATCHOS_X64,
//     SkieRuntimeKonanTarget.WATCHOS_SIMULATOR_ARM64,
//     SkieRuntimeKonanTarget.WATCHOS_DEVICE_ARM64,
//     SkieRuntimeKonanTarget.TVOS_ARM64,
//     SkieRuntimeKonanTarget.TVOS_X64,
//     SkieRuntimeKonanTarget.TVOS_SIMULATOR_ARM64,
//     SkieRuntimeKonanTarget.MACOS_X64,
//     SkieRuntimeKonanTarget.MACOS_ARM64,
// ).map(::SkieRuntimeNativeTarget)
//
//
// class SkieRuntimeSoftwareComponentWithCoordinatesAndPublication(
//
// )
//
// abstract class SkieRuntimeSoftwareComponent(
//     private val project: Project,
//     private val name: String,
//     protected val skieRuntimeTargets: Iterable<SkieRuntimeTarget>,
// ): SoftwareComponentInternal, ComponentWithVariants {
//
//     override fun getName(): String = name
//
//     override fun getVariants(): MutableSet<out SoftwareComponent> {
//
//     }
//
// }

val smokeTestTmpRepositoryPath: String? by project
val publishTaskNames = listOfNotNull(
    "publishToMavenLocal" to listOf("publishToMavenLocal"),
    "publishToSonatype" to listOf("findSonatypeStagingRepository", "publishToSonatype"),
    if (smokeTestTmpRepositoryPath != null) {
        "publishAllPublicationsToSmokeTestTmpRepository" to listOf("publishAllPublicationsToSmokeTestTmpRepository")
    } else {
        null
    },
)

val publishTaskNamesWithTasks = publishTaskNames.associateWith { (publishTaskName, _) ->
    tasks.register(publishTaskName)
}

val kotlin_1_8_0 = KotlinToolingVersion("1.8.0")

kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
    val pathSafeKotlinVersionName = kotlinToolingVersion.primaryVersion.toString().replace('.', '_')
    val supportedDarwinTargets = darwinPlatformDimension().components
        .filter { it.sinceKotlinVersion?.let { kotlinToolingVersion.primaryVersion >= KotlinToolingVersion(it) } ?: true }
        .filter { it.untilKotlinVersionExclusive?.let { kotlinToolingVersion.primaryVersion < KotlinToolingVersion(it) } ?: true }

    val copyProjectTask = tasks.register<Copy>("copyProject__kgp_${kotlinToolingVersion.primaryVersion}") {
        description = "Copies implementation for Kotlin ${kotlinToolingVersion.primaryVersion}."

        from(layout.projectDirectory.dir("impl")) {
            include("src/**", "build.gradle.kts", "gradle.properties", "settings.gradle.kts")
            filter(
                ReplaceTokens::class,
                "tokens" to mapOf(
                    "targetKotlinVersion" to kotlinToolingVersion.primaryVersion.toString(),
                    "artifactIdSuffix" to "-${kotlinToolingVersion.primaryVersion}",
                    "targets" to supportedDarwinTargets.joinToString("\n") { "$it()" },
                    // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
                    // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
                    "dependencies" to if (kotlinToolingVersion.primaryVersion == kotlin_1_8_0) {
                        "implementation(libs.kotlinx.coroutines.core.legacy)"
                    } else {
                        "implementation(libs.kotlinx.coroutines.core)"
                    },
                    "artifactIdSuffix" to "-${kotlinToolingVersion.primaryVersion}",
                    "smokeTestTmpRepositoryConfiguration" to smokeTestTmpRepositoryPath?.let {
                        """
                            publishing {
                                repositories {
                                    maven {
                                        url = uri("$it")
                                        name = "smokeTestTmp"
                                    }
                                }
                            }
                        """.trimIndent()
                    }.orEmpty(),
                )
            )
        }
        into(layout.buildDirectory.dir("impl_${pathSafeKotlinVersionName}"))
    }

    val buildTask = tasks.register<GradleBuild>("buildProject__kgp_${pathSafeKotlinVersionName}") {
        group = "build"

        dependsOn(copyProjectTask)

        setDir(layout.buildDirectory.dir("impl_${pathSafeKotlinVersionName}"))

//         archivePrefix = "runtime-kotlin"
//         projectDir =
//         kotlinVersion = kotlinToolingVersion.primaryVersion.toString()
        tasks = supportedDarwinTargets.flatMap { darwinPlatformComponent ->
            val uppercaseTargetName = darwinPlatformComponent.value.replaceFirstChar { it.uppercase() }
            listOf(
                "generateMetadataFileFor${uppercaseTargetName}Publication",
                "generatePomFileFor${uppercaseTargetName}Publication",
            )
        } + listOf(
            "generateMetadataFileForKotlinMultiplatformPublication",
            "generatePomFileForKotlinMultiplatformPublication",
        )
    }

    publishTaskNamesWithTasks.forEach { (publishTaskNames, parentPublishTask) ->
        val (publishTaskName, publishTasks) = publishTaskNames
        val publishTask = tasks.register<BuildNestedGradle>("${publishTaskName}__kgp_${kotlinToolingVersion.primaryVersion}") {
            group = "publishing"

            dependsOn(copyProjectTask)

            projectDir.fileProvider(copyProjectTask.map { it.destinationDir })

            tasks.set(publishTasks)
        }

        parentPublishTask.configure {
            dependsOn(publishTask)
        }
    }

    supportedDarwinTargets.forEach { darwinPlatformComponent ->
        val configuration = configurations.create("${darwinPlatformComponent.value}__kgp_${kotlinToolingVersion.primaryVersion}") {
            isCanBeConsumed = true
            isCanBeResolved = false

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
                attribute(KotlinNativeTarget.konanTargetAttribute, darwinPlatformComponent.kotlinNativeTarget)
                attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.primaryVersion.toString()))
            }
        }

        dependencies {
            configuration(
                libs.kotlinx.coroutines.core
            )
        }

        val artifactName = "runtime-kotlin__kgp_$pathSafeKotlinVersionName"
        val artifactClassifierPrefix = "${darwinPlatformComponent.value}-kgp_${pathSafeKotlinVersionName}"
        val klibPath = "impl_${pathSafeKotlinVersionName}/build/classes/kotlin/${darwinPlatformComponent.value}/main/klib/runtime-kotlin-${kotlinToolingVersion.primaryVersion}.klib"
        val extraArchiveBaseName = "impl/build/libs/runtime-kotlin__kgp_${pathSafeKotlinVersionName}-${darwinPlatformComponent.value.lowercase()}"

        artifacts.add(configuration.name, layout.buildDirectory.file(klibPath)) {
            builtBy(buildTask)
            name = artifactName
            classifier = artifactClassifierPrefix
        }
//         if (kotlinToolingVersion.value == "1.8.0") {
//             val sourcesJar = tasks.register<Jar>("${darwinPlatformComponent.value}__kgp_${pathSafeKotlinVersionName}SourcesJar") {
//                 archiveClassifier = "$artifactClassifierPrefix-sources"
//                 archiveBaseName = "${archiveBaseName.orNull ?: project.name}-${darwinPlatformComponent.value}-kgp_${pathSafeKotlinVersionName}"
//             }
//             artifacts.add(configuration.name, sourcesJar.map { it.archiveFile }) {
//                 builtBy(sourcesJar)
//                 classifier = "$artifactClassifierPrefix-sources"
//                 name = artifactName
//             }
//         } else {
//             artifacts.add(configuration.name, layout.projectDirectory.file("$extraArchiveBaseName-sources.jar")) {
//                 builtBy(buildTask)
//                 classifier = "$artifactClassifierPrefix-sources"
//                 name = artifactName
//             }
//         }
//
//         val javadocJar = tasks.register<Jar>("${darwinPlatformComponent.value}__kgp_${pathSafeKotlinVersionName}JavadocJar") {
//             archiveClassifier = "$artifactClassifierPrefix-javadoc"
//             archiveBaseName = "${archiveBaseName.orNull ?: project.name}-${darwinPlatformComponent.value}-kgp_${pathSafeKotlinVersionName}"
//         }
//         artifacts.add(configuration.name, javadocJar.map { it.archiveFile }) {
//             builtBy(javadocJar)
//             classifier = "$artifactClassifierPrefix-javadoc"
//             name = artifactName
//
//         }
//
//         artifacts.add(configuration.name, layout.projectDirectory.file("$extraArchiveBaseName-metadata.jar")) {
//             builtBy(buildTask)
//             classifier = "$artifactClassifierPrefix-metadata"
//             name = artifactName
//         }
//
//     component.addVariantsFromConfiguration(configuration) {
//         mapToMavenScope("runtime")
//     }
    }
}
