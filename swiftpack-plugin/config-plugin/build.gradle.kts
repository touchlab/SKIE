import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.shadow)
    alias(libs.plugins.buildconfig)
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(libs.auto.service)
    kapt(libs.auto.service)

    api(project(":swiftpack-api"))
}

tasks.shadowJar {
    relocate("org.jetbrains.kotlin.com.intellij", "com.intellij")
    mergeServiceFiles()

    archiveClassifier.set("shadow")
}