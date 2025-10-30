plugins {
    id("skie.root")
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.nexusPublish)
}

nexusPublishing {
    repositories {
        sonatype()
    }

    transitionCheckOptions {
        maxRetries = 120
    }
}
