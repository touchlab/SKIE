import co.touchlab.skie.PublishSkieToTempMavenTask

plugins {
    kotlin("jvm") version "1.9.22"
    alias(libs.plugins.buildconfig)
}

dependencies {
    implementation(kotlin("test"))
    implementation(gradleTestKit())
    implementation(libs.kotlinPoet)
    implementation(libs.bundles.kotest)
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
    val rawKotlinVersions = project.property("versionSupport.kotlinTooling") as String

    val rawKotlinVersionsWithoutBrackets = removeBrackets(rawKotlinVersions)

    return rawKotlinVersionsWithoutBrackets.split(",").map { extractPrimaryVersion(it.trim()) }
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

private fun extractPrimaryVersion(rawVersion: String): String =
    if (rawVersion.contains("[")) {
        rawVersion.substringAfter("[").substringBefore("]")
    } else {
        rawVersion
    }
