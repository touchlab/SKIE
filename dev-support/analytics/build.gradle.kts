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

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation("co.touchlab.skie:analytics-api")
    implementation("co.touchlab.skie:producer")
    implementation("co.touchlab.skie:configuration-api")
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
