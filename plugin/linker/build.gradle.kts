import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable

plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(extractedKotlinNativeCompilerEmbeddable())

    implementation(projects.api)
    implementation(projects.spi)
}
