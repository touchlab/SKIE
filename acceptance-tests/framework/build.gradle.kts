import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable
import co.touchlab.skie.gradle.util.kotlinNativeCompilerEmbeddable

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

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(kotlinNativeCompilerEmbeddable())

    implementation("co.touchlab.skie:configuration-api")
    implementation("co.touchlab.skie:generator")
    implementation("co.touchlab.skie:kotlin-plugin")
}
