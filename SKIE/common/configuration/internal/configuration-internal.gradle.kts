plugins {
    id("skie.common")
    id("utility.skie-publishable")
}

skiePublishing {
    name = "SKIE Internal Configuration Declarations"
    description = "Internal Configuration declarations for SKIE, used in Gradle plugin."
}

dependencies {
    api(projects.common.configuration.configurationApi)
}
