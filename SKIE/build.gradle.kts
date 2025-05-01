plugins {
    id("skie.root")
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.nexusPublish)
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0" apply false
}

nexusPublishing {
    this.repositories {
        sonatype()
    }

    transitionCheckOptions {
        this.maxRetries = 120
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.4.0")
        enableExperimentalRules.set(true)
        verbose.set(true)
        filter {
            exclude { it.file.path.contains("build/") }
        }
    }

    /*
    afterEvaluate {
        tasks.named("check") {
            dependsOn(tasks.getByName("ktlintCheck"))
        }
    }*/
}
