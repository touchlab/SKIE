plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

dependencies {
    compileOnly(libs.compiler.embeddable)
    implementation(libs.compiler.testing)

    testImplementation(libs.bundles.testing.jvm)
}

tasks.test {
    useJUnitPlatform()
}
