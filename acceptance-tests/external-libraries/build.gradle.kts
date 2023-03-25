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
    idea
}

skieJvm {
    areContextReceiversEnabled.set(true)
}

val stdlibTestsSourceSet = sourceSets.create("stdlibTests") {
    idea.module.testSources.from(kotlin.srcDirs)
    idea.module.testSources.from(java.srcDirs)
    idea.module.testResources.from(resources.srcDirs)
}

val compareTestsSourceSet = sourceSets.create("compareTests") {
    idea.module.testSources.from(kotlin.srcDirs)
    idea.module.testSources.from(java.srcDirs)
    idea.module.testResources.from(resources.srcDirs)
}

val acceptanceTestDependencies: Configuration = configurations.create("acceptanceTestDependencies") {
    isCanBeConsumed = false
    isCanBeResolved = true

    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

    attributes {
        attribute(
            KotlinPlatformType.attribute,
            KotlinPlatformType.native
        )
        attribute(KotlinNativeTarget.konanTargetAttribute, KonanTarget.IOS_ARM64.name)
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
    }
}

val stdlibTestsImplementation by configurations.getting
val compareTestsImplementation by configurations.getting

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

    stdlibTestsImplementation("co.touchlab.skie:configuration-api")
    stdlibTestsImplementation("co.touchlab.skie:generator")
    stdlibTestsImplementation("co.touchlab.skie:kotlin-plugin")
    stdlibTestsImplementation("co.touchlab.skie:api")
    stdlibTestsImplementation("co.touchlab.skie:spi")
    stdlibTestsImplementation(libs.kotlin.native.compiler.embeddable)
    stdlibTestsImplementation(libs.kotlinPoet)
    stdlibTestsImplementation(libs.bundles.testing.jvm)
    stdlibTestsImplementation(projects.acceptanceTests.framework)

    compareTestsImplementation(libs.bundles.testing.jvm)
    compareTestsImplementation(projects.acceptanceTests.framework)


    // Workaround for these dependencies not being built before running tests
    acceptanceTestDependencies("co.touchlab.skie:configuration-annotations")
    acceptanceTestDependencies("co.touchlab.skie:kotlin")
}

val librariesToTestLockfile = layout.projectDirectory.file("libraries.lock")
val generatedTestResourcesDir = layout.buildDirectory.dir("generated-test-resources")

sourceSets.test {
    resources.srcDir(generatedTestResourcesDir)
}

val updateExternalLibrariesLockfile = tasks.register<ExternalLibrariesTask>("updateExternalLibrariesLockfile") {
    group = "verification"
    description = "Loads external libraries for iosArm64"

    acceptableKotlinPrefixes.addAll("1.4", "1.5", "1.6", "1.7", "1.8")
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

fun Test.configureExternalLibraryTest(isSkieEnabled: Boolean) {
    dependsOn(acceptanceTestDependencies.buildDependencies)
    dependsOn(prepareTestClasspaths)

    val dirSuffix = if (isSkieEnabled) {
        ""
    } else {
        "-no-skie"
    }
    if (!isSkieEnabled) {
        systemProperty("disableSkie", "")
    }
    systemProperty("testTmpDir", layout.buildDirectory.dir("external-libraries-tests${dirSuffix}").get().asFile.absolutePath)

    maxHeapSize = "12g"

    testLogging {
        showStandardStreams = true
    }
}

val pureTest = tasks.register<Test>("pureTest") {
    description = "Runs library tests without SKIE"
    group = "verification"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform()

    configureExternalLibraryTest(isSkieEnabled = false)
}

tasks.test {
    // We don't want to run them in parallel, because they are both heavily parallelized internally
    // Also, running SKIE tests after pure tests in case the VM needs to warm up.
    mustRunAfter(pureTest)

    configureExternalLibraryTest(isSkieEnabled = true)
}

tasks.register<Test>("comparePureAndSkie") {
    mustRunAfter(tasks.test, pureTest)

    systemProperty("pureTestDir", layout.buildDirectory.dir("external-libraries-tests-no-skie").get().asFile.absolutePath)
    systemProperty("skieTestDir", layout.buildDirectory.dir("external-libraries-tests").get().asFile.absolutePath)
    systemProperty("csvOutputDir", layout.buildDirectory.dir("reports/comparison").get().asFile.absolutePath)

    description = "Compares library tests with and without SKIE"
    group = "verification"

    testClassesDirs = compareTestsSourceSet.output.classesDirs
    classpath = compareTestsSourceSet.runtimeClasspath

    useJUnitPlatform()
}

val stdlibTest = tasks.register<Test>("stdlibTest") {
    description = "Runs SKIE for stdlib symbols"
    group = "verification"

    testClassesDirs = stdlibTestsSourceSet.output.classesDirs
    classpath = stdlibTestsSourceSet.runtimeClasspath

    systemProperty("testTmpDir", layout.buildDirectory.dir("stdlib-tests").get().asFile.absolutePath)

    useJUnitPlatform()
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
