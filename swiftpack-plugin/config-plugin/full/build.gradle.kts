plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    `maven-publish`
}

dependencies {
    api(project(":swiftpack-config-plugin"))
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            project.shadow.component(this)
        }
    }
}