import co.touchlab.skie.gradle.util.kotlinNativeCompilerEmbeddableRuntime

plugins {
    id("skie-jvm")
    id("skie-buildconfig")
}

buildConfig {
    buildConfigField(
        type = "String",
        name = "CODE",
        value = "\"${layout.projectDirectory.dir("src/main/resources/code").asFile.absolutePath}\"",
    )
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

dependencies {
    compileOnly(libs.kotlin.native.compiler.embeddable)
    runtimeOnly(kotlinNativeCompilerEmbeddableRuntime())
}
