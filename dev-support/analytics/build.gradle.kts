plugins {
    id("dev.jvm")
    id("dev.buildconfig")
    id("experimental.context-receivers")
    kotlin("plugin.serialization")
}

buildConfig {
    fun String.enquoted() = """"$this""""

    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = layout.projectDirectory.dir("src/main/resources").asFile.absolutePath.enquoted(),
    )
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation("co.touchlab.skie:analytics")
}
