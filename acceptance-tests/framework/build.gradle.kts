import co.touchlab.skie.gradle.util.kotlinNativeCompilerEmbeddableRuntime

plugins {
    id("skie-jvm")
    id("skie-buildconfig")
}

buildConfig {
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

skieJvm {
    areContextReceiversEnabled.set(true)
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(libs.kotlin.native.compiler.embeddable)
    runtimeOnly(kotlinNativeCompilerEmbeddableRuntime())

    implementation("co.touchlab.skie:configuration-api")
    implementation("co.touchlab.skie:generator")
    implementation("co.touchlab.skie:kotlin-plugin")
}
