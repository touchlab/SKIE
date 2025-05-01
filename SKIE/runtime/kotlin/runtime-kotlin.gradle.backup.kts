import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.darwinPlatformDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.tooling.GradleConnector
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinSoftwareComponentWithCoordinatesAndPublication
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages

plugins {
//     id("skie.runtime.kotlin")
    id("skie.publishable")
//     `maven-publish`
    `java-base`
}

skiePublishing {
    name = "SKIE Runtime - Kotlin"
    description = "Kotlin Multiplatform part of the SKIE runtime. It's used to facilitate certain features of SKIE."
    publishSources = true
    publishJavadoc = true
}

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

KotlinCompilerVersion.registerIn(dependencies, "2.0.0")

abstract class BuildNestedGradle : DefaultTask() {

    @get:InputDirectory
    abstract val projectDir: DirectoryProperty

    @get:Input
    abstract val tasks: ListProperty<String>

    @get:Input
    abstract val kotlinVersion: Property<String>

    @get:Input
    abstract val archivePrefix: Property<String>

    init {
//         outputs.file(
//             zip(
//                 kotlinVersion,
//                 darwinPlatform,
//                 projectDir,
//                 archivePrefix,
//             ) { kotlinVersion, darwinPlatform, projectDir, arhivePrefix ->
//                 projectDir.file("build/classes/kotlin/${darwinPlatform.value}/main/klib/${archivePrefix}__kgp_${kotlinVersion.replace('.', '_')}.klib")
//             }
//         )
    }

    fun <A, B, C, D, RESULT> zip(
        aProperty: Property<A>,
        bProperty: Property<B>,
        cProperty: Property<C>,
        dProperty: Property<D>,
        combiner: (a: A, b: B, c: C, d: D) -> RESULT,
    ): Provider<RESULT> = aProperty.zip(bProperty) { a, b -> a to b }
        .zip(cProperty.zip(dProperty) { c, d -> c to d }) { (a, b), (c, d) ->
            combiner(a, b, c, d)
        }

    @TaskAction
    fun buildNestedGradle() {
        val connection = GradleConnector.newConnector()
            .forProjectDirectory(projectDir.get().asFile)
            .connect()

        connection.newBuild()
            .forTasks(*tasks.get().toTypedArray())
            .withSystemProperties(
                mapOf(
                    "kotlinVersion" to kotlinVersion.get(),
                ),
            )
            .setStandardOutput(System.out)
            .setStandardError(System.err)
            .run()
    }
}

abstract class SoftwareComponentFactoryAccessor @Inject constructor(val softwareComponentFactory: SoftwareComponentFactory)
val softwareComponentFactoryAccessor = objects.newInstance<SoftwareComponentFactoryAccessor>()

val rootSoftwareComponent = KotlinSoftwareComponentWithCoordinatesAndPublication(project, "kotlin", emptyList())
components.create()

val component = softwareComponentFactoryAccessor.softwareComponentFactory.adhoc("skieRuntime")
components.add(component)

val publication = publishing.publications.create<MavenPublication>("skieRuntime") {
    artifactId = "runtime-kotlin"
    from(component)

    val sourcesJar = tasks.register<Jar>("skieRuntimeSourcesJar") {
        archiveClassifier = "sources"
    }
    artifact(sourcesJar)

    val javadocJar = tasks.register<Jar>("skieRuntimeJavadocJar") {
        archiveClassifier = "javadoc"
    }
    artifact(javadocJar)
}

kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
    val pathSafeKotlinVersionName = kotlinToolingVersion.primaryVersion.toString().replace('.', '_')
    val supportedDarwinTargets = darwinPlatformDimension().components
        .filter { it.sinceKotlinVersion?.let { kotlinToolingVersion.primaryVersion >= KotlinToolingVersion(it) } ?: true }
        .filter { it.untilKotlinVersionExclusive?.let { kotlinToolingVersion.primaryVersion < KotlinToolingVersion(it) } ?: true }

    val copyProjectTask = tasks.register<Sync>("copyProject__kgp_${kotlinToolingVersion.primaryVersion}") {
        description = "Copies implementation for Kotlin ${kotlinToolingVersion.primaryVersion}."

        from(layout.projectDirectory.dir("impl")) {
            include("src/**", "build.gradle.kts", "gradle.properties", "settings.gradle.kts")
            filter(
                ReplaceTokens::class,
                "tokens" to mapOf(
                    "targetKotlinVersion" to kotlinToolingVersion.primaryVersion.toString(),
                    "targets" to supportedDarwinTargets.joinToString("\n") { "$it()" },
                    // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
                    // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
                    "dependencies" to if (kotlinToolingVersion.value == "1.8.0") {
                        "implementation(libs.kotlinx.coroutines.core.legacy)"
                    } else {
                        "implementation(libs.kotlinx.coroutines.core)"
                    },
                ),
            )
        }
        into(layout.buildDirectory.dir("impl_$pathSafeKotlinVersionName"))
    }

    val buildTask = tasks.register<GradleBuild>("buildProject__kgp_${kotlinToolingVersion.primaryVersion}") {
        group = "build"

        dependsOn(copyProjectTask)

        setDir(layout.buildDirectory.dir("impl_$pathSafeKotlinVersionName"))

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

//     val buildTask = tasks.register<BuildNestedGradle>("buildKotlin__kgp_${kotlinToolingVersion.primaryVersion}") {
//         group = "build"
//
//         archivePrefix = "runtime-kotlin"
//         projectDir = layout.projectDirectory.dir("impl")
//         kotlinVersion = kotlinToolingVersion.primaryVersion.toString()
//         tasks = darwinPlatformDimension().components.flatMap { darwinPlatformComponent ->
//             val uppercaseTargetName = darwinPlatformComponent.value.replaceFirstChar { it.uppercase() }
//             listOf(
//                 "generateMetadataFileFor${uppercaseTargetName}Publication",
//                 "generatePomFileFor${uppercaseTargetName}Publication",
//             )
//         }
//     }

    supportedDarwinTargets.map { darwinPlatformComponent ->
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

        // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
        // For this reason we must use an older version of Coroutines for Kotlin 1.8.0
        dependencies {
            configuration(
                if (kotlinToolingVersion.value == "1.8.0") {
                    libs.kotlinx.coroutines.core.legacy
                } else {
                    libs.kotlinx.coroutines.core
                },
            )
        }

        val artifactName = "runtime-kotlin__kgp_$pathSafeKotlinVersionName"
        val artifactClassifierPrefix = "${darwinPlatformComponent.value}-kgp_$pathSafeKotlinVersionName"
        val klibPath = "impl_$pathSafeKotlinVersionName/build/classes/kotlin/${darwinPlatformComponent.value}/main/klib/skie-kotlin-runtime.klib"
        val extraArchiveBaseName = "impl/build/libs/skie-kotlin-runtime__kgp_$pathSafeKotlinVersionName-${darwinPlatformComponent.value.lowercase()}"

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

        component.addVariantsFromConfiguration(configuration) {
            mapToMavenScope("runtime")
        }
    }
}

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
//
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
//
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
