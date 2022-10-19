plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

dependencies {
    implementation(projects.configurationApi)
    implementation(projects.generator.configurationAnnotations)
}

publishing {
    publications.all {
        (this as? MavenPublication)?.apply {
            artifactId = "generator-${artifactId}"
        }
    }
}
