import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable

plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    compileOnly(extractedKotlinNativeCompilerEmbeddable())

    implementation(projects.kotlinPlugin.options)
    implementation(projects.api)
    implementation(projects.configurationApi)
    implementation(projects.interceptor)
    implementation(projects.generator)
    implementation(projects.linker)
    implementation(projects.reflector)
    implementation(projects.spi)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
