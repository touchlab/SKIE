// Usage ../../../gradlew :noop -Plibrary=module:name:version [-Pconstraints=module:name:version|module:name:version]

@file:Suppress("invisible_reference", "invisible_member")

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.utils.named

plugins {
    kotlin("multiplatform") version "1.9.23" apply false
    java
}

KotlinUsages.setupAttributesMatchingStrategy(dependencies.attributesSchema, false)

tasks.create("noop") {
}

fun String.isLowerVersionThan(other: String): Boolean {
    val lhsParts = this.substringBefore("-").split(".")
    val rhsParts = other.substringBefore("-").split(".")

    lhsParts.zip(rhsParts).forEach { (lhsSegment, rhsSegment) ->
        val lhsNumericSegment = lhsSegment.toIntOrNull() ?: return true
        val rhsNumericSegment = rhsSegment.toInt()

        if (lhsNumericSegment < rhsNumericSegment) {
            return true
        } else if (lhsNumericSegment > rhsNumericSegment) {
            return false
        }
    }

    return lhsParts.size < rhsParts.size
}

val libraryTestDependencies = configurations.create("libraryTestDependencies") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
        attribute(KotlinNativeTarget.konanTargetAttribute, "ios_arm64")
    }

    resolutionStrategy {
        dependencySubstitution {
            eachDependency {
                val version = requested.version

                if (
                    requested.module.toString().startsWith("org.jetbrains.kotlinx:kotlinx-coroutines-") &&
                    version != null &&
                    version.isNotBlank()
                ) {
                    val isNativeMt = version.contains("native-mt")
                    val isTooOld = version.isLowerVersionThan("1.6.4")

                    if (!version.startsWith("1")) {
                        println(requested)
                    }

                    if (isNativeMt || isTooOld) {
                        useVersion("1.6.4")
                    }
                }
            }
        }
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
}

dependencies {
    val library = property("library").toString()

    libraryTestDependencies(library + "!!") {
        exclude(group = "co.touchlab.skie", module = "runtime-kotlin")
    }

    constraints {
        if (hasProperty("constraints")) {
            property("constraints")
                .toString()
                .split("|")
                .forEach {
                    libraryTestDependencies(it + "!!")
                }
        }
    }
}

val klibsByCoordinates = libraryTestDependencies.resolvedConfiguration.resolvedArtifacts
    .filter { it.file.extension == "klib" }
    .map { it.moduleVersion.id.toString() to it.file.absolutePath }

val output = StringBuilder().apply {
    appendLine("<libraries-start>")
    klibsByCoordinates.forEach { (coordinates, klib) ->
        appendLine("$coordinates|$klib")
    }
    appendLine("<libraries-end>")
}.toString()

println(output)
