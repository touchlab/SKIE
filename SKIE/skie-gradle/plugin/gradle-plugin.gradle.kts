plugins {
    id("gradle.plugin")
    id("utility.skie-publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin"
    description = "Gradle plugin for configuring SKIE compiler plugin."
}

gradlePlugin {
    website = "https://skie.touchlab.co"
    vcsUrl = "https://github.com/touchlab/SKIE.git"

    plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SkieGradlePlugin"
            version = project.version

            description = "A Gradle plugin to add Swift into Kotlin/Native framework."
            tags = listOf(
                "swift",
                "kotlin",
                "native",
                "compiler",
            )
        }
    }
}

dependencies {
    api(projects.gradle.gradlePluginApi)
    implementation(projects.gradle.gradlePluginImpl)
}
