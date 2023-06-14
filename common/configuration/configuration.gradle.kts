plugins {
    id("skie.common")
    id("skie.publishable")
}

dependencies {
    api(projects.common.configuration.configurationAnnotations)
    api(projects.common.configuration.configurationImpl)
    api(projects.common.configuration.configurationGradle)
}
