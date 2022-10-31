import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable
import co.touchlab.skie.gradle.util.kotlinNativeCompilerEmbeddable

plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(projects.api)
    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(kotlinNativeCompilerEmbeddable())
}
