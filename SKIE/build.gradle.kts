plugins {
    id("skie.root")
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.nexusPublish)
}

nexusPublishing {
    this.repositories {
        sonatype()
    }

    transitionCheckOptions {
        this.maxRetries = 120
    }
}
