
import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.SourceSetScope
import co.touchlab.skie.gradle.version.kotlinToolingVersions
import co.touchlab.skie.gradle.version.setupSourceSets

plugins {
    kotlin("multiplatform")
}

group = "co.touchlab.skie"

KotlinCompilerVersion.registerIn(project)

kotlin {
    jvmToolchain(libs.versions.java)

    val kotlinVersions = project.kotlinToolingVersions()
    setupSourceSets(
        matrix = kotlinVersions,
        configureTarget = { cell ->
            attributes {
                attribute(KotlinCompilerVersion.attribute, objects.named(cell.toString()))
            }
        },
        configureSourceSet = {
            val kotlinVersion = target.baseValue.toString()

            addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
            addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))
            addWeakDependency("org.jetbrains.kotlin:kotlin-native-compiler-embeddable", configureVersion(kotlinVersion))
        },
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}
