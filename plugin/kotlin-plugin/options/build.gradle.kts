import co.touchlab.swiftgen.gradle.kotlinNativeCompilerEmbeddable

plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(projects.api)
    compileOnly(files(kotlinNativeCompilerEmbeddable()))
    // compileOnly(strippedKotlinNativeCompilerEmbeddable())
}
