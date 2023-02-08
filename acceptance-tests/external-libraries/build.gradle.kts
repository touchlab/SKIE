import co.touchlab.skie.gradle.test.ExternalLibrariesTask
import groovy.json.JsonOutput
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget
import co.touchlab.skie.gradle.util.kotlinNativeCompilerHome

plugins {
    id("skie-jvm")
    id("skie-buildconfig")
    alias(libs.plugins.kotlin.plugin.serialization)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}

dependencies {
    testImplementation(projects.acceptanceTests.framework)
    testImplementation("co.touchlab.skie:configuration-annotations")
    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlin.native.compiler.embeddable)
    testImplementation(libs.kotlinx.serialization.json)

    testImplementation("co.touchlab.skie:configuration-api")
    testImplementation("co.touchlab.skie:generator")
    testImplementation("co.touchlab.skie:kotlin-plugin")
    testImplementation("co.touchlab.skie:api")
    testImplementation("co.touchlab.skie:spi")
}

val librariesToTestLockfile = layout.projectDirectory.file("libraries.lock")
val generatedTestResourcesDir = layout.buildDirectory.dir("generated-test-resources")

sourceSets.test {
    resources.srcDir(generatedTestResourcesDir)
}

val updateExternalLibrariesLockfile = tasks.register<ExternalLibrariesTask>("updateExternalLibrariesLockfile") {
    group = "verification"
    description = "Loads external libraries for iosArm64"

    acceptableKotlinPrefixes.addAll("1.4", "1.5", "1.6", "1.7")
    platformSuffix.set("-iosarm64")

    mavenSearchCache.set(layout.buildDirectory.file("tmp/maven-search-cache.json"))
    librariesToTestFile.set(librariesToTestLockfile)
}

val prepareTestClasspaths = tasks.register("prepareTestClasspaths") {
    doLast {
        val allLibraries = librariesToTestLockfile.asFile.readLines()
        val json = allLibraries.associateWith { library ->
            val configuration = configurations.create("testClasspath-${library.replace(':', '-')}").apply {
                isCanBeConsumed = false
                isCanBeResolved = true

                exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
                    attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment::class.java, "non-jvm"))
                    attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                    attribute(KotlinNativeTarget.konanTargetAttribute, KonanTarget.IOS_ARM64.name)
                    // Remove?
                    attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "org.jetbrains.kotlin.klib")
                }
            }

            dependencies {
                configuration("co.touchlab.skie:configuration-annotations")
                configuration("co.touchlab.skie:kotlin")
                configuration("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                    version {
                        strictly("1.6.4")
                    }
                }
                configuration(library)
            }

            val exportedFiles = configuration.resolvedConfiguration.firstLevelModuleDependencies.flatMap {
                it.moduleArtifacts
            }
            val allFiles = configuration.resolvedConfiguration.resolvedArtifacts

            if (exportedFiles.isEmpty()) {
                logger.warn("Exported files for $library: ${exportedFiles.map { it.file.name }} - shouldn't be empty!")
            }
            if (allFiles.isEmpty()) {
                logger.warn("All files for $library: ${allFiles.map { it.file.name }} - shouldn't be empty!")
            }

            mapOf(
                "files" to allFiles.map { it.file.absolutePath },
                "exported-files" to exportedFiles.map { it.file.absolutePath },
            )
        }

         generatedTestResourcesDir.get().file("test-input.json").asFile.apply {
             parentFile.mkdirs()
             writeText(JsonOutput.toJson(json))
         }
    }
}

tasks.test {
    systemProperty("testTmpDir", layout.buildDirectory.dir("external-libraries-tests").get().asFile.absolutePath)

    maxHeapSize = "24g"
    dependsOn(prepareTestClasspaths)
}


buildConfig {
    fun Collection<File>.toListString(): String =
        this.joinToString(", ") { "\"${it.absolutePath}\"" }

    fun Collection<String>.toListString(): String =
        this.joinToString(", ") { "\"$it\"" }

    // val resolvedDependencies = acceptanceTestDependencies.resolve() + acceptanceTestExportedDependencies.resolve()
    // val exportedDependencies = acceptanceTestDependencies.filter { it.path.contains("plugin/runtime/kotlin") }.toList() +
    //     acceptanceTestExportedDependencies.resolve()

    buildConfigField(
        type = "String",
        name = "BUILD",
        value = "\"${layout.buildDirectory.get().asFile.absolutePath}\"",
    )

    // buildConfigField(
    //     type = "co.touchlab.skie.acceptancetests.util.StringArray",
    //     name = "DEPENDENCIES",
    //     value = "arrayOf(${resolvedDependencies.toListString()})",
    // )
    //
    // buildConfigField(
    //     type = "co.touchlab.skie.acceptancetests.util.StringArray",
    //     name = "EXPORTED_DEPENDENCIES",
    //     value = "arrayOf(${exportedDependencies.toListString()})",
    // )

    buildConfigField(
        type = "String",
        name = "KONAN_HOME",
        value = "\"${kotlinNativeCompilerHome.path}\"",
    )
    buildConfigField(
        type = "String",
        name = "TEST_RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/test/resources").asFile.absolutePath}\"",
    )
}
