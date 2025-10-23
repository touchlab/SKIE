plugins {
    id("skie.common")
    id("utility.skie-publishable")
}

skiePublishing {
    name = "SKIE Configuration Declaration"
    description = "Configuration declarations for SKIE, used in Gradle plugin."
}

dependencies {
    api(projects.common.configuration.configurationApi)
    implementation(projects.common.configuration.configurationAnnotations)
}
