import co.touchlab.skie.PublishSkieToTempMavenTask

plugins {
    kotlin("jvm") version "2.2.21"
    alias(libs.plugins.buildconfig)
}

dependencies {
    implementation(kotlin("test"))
    implementation(gradleTestKit())
    implementation(libs.bundles.testing.jvm)
}

val smokeTestRepository = layout.buildDirectory.dir("smokeTestRepo")

val publishSkieToTempMaven by tasks.registering(PublishSkieToTempMavenTask::class) {
    skieSources = rootDir.resolve("../SKIE")
    tempRepository = smokeTestRepository
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.1")

            dependencies {
                implementation("org.junit.platform:junit-platform-suite:1.10.2")
                implementation("org.junit.platform:junit-platform-reporting:1.10.2")
            }

            targets.all {
                testTask.configure {
                    minHeapSize = "1024m"
                    maxHeapSize = "4024m"
                    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

                    inputs.files(publishSkieToTempMaven)

                    systemProperty("smokeTestRepository", smokeTestRepository.get().asFile.absolutePath)
                    systemProperty("junit.platform.reporting.open.xml.enabled", "true")
                    systemProperty("junit.platform.reporting.output.dir", reports.junitXml.outputLocation.get().asFile.absolutePath)

                    listOf(
                        "testLevel",
                        "testTypes",
                        "matrix.targets",
                        "matrix.configurations",
                        "matrix.linkModes",
                        "matrix.kotlinVersions",
                        "matrix.gradleVersions",
                    ).forEach { property ->
                        val propertyValue = findProperty(property) ?: return@forEach
                        systemProperty(property, propertyValue)
                    }

                    testLogging {
                        events("passed", "skipped", "failed")
                        showStandardStreams = true
                    }
                }
            }
        }
    }
}

buildConfig {
    useKotlinOutput {
        internalVisibility = false
    }

    val primarySupportedKotlinVersions = getPrimarySupportedKotlinVersions().joinToString(",")

    buildConfigField("String", "PRIMARY_SUPPORTED_KOTLIN_VERSIONS", "\"${primarySupportedKotlinVersions}\"")
}

private fun getPrimarySupportedKotlinVersions(): List<String> {
    val rawKotlinVersions = project.property("versionSupport.kotlin") as String

    val enabledVersions = getEnabledVersions()

    val rawKotlinVersionsWithoutBrackets = removeBrackets(rawKotlinVersions)

    return rawKotlinVersionsWithoutBrackets
        .split(",")
        .map { extractPrimaryVersion(it.trim()) }
        .filter { enabledVersions == null || it.first in enabledVersions }
        .map { it.second }
}

private fun removeBrackets(string: String): String {
    val result = StringBuilder()

    var isInBracket = false

    string.forEach { char ->
        when (char) {
            '(' -> isInBracket = true
            ')' -> isInBracket = false
            else -> {
                if (!isInBracket) {
                    result.append(char)
                }
            }
        }
    }

    return result.toString()
}

private fun getEnabledVersions(): List<String>? =
    project.findProperty("versionSupport.kotlin.enabledVersions")
        ?.toString()
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.takeIf { it.isNotEmpty() }

private fun extractPrimaryVersion(rawVersion: String): Pair<String, String> =
    if (rawVersion.contains("[")) {
        rawVersion.substringBefore("[") to rawVersion.substringAfter("[").substringBefore("]")
    } else {
        rawVersion to rawVersion
    }
