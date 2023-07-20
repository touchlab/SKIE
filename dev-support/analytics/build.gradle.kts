plugins {
    id("dev.jvm")
    id("dev.buildconfig")
    id("experimental.context-receivers")
}

val analyticsDir = layout.buildDirectory.dir("analytics")

buildConfig {
    fun String.enquoted() = """"$this""""

    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = layout.projectDirectory.dir("src/main/resources").asFile.absolutePath.enquoted()
    )

    buildConfigField(
        type = "String",
        name = "ANALYTICS_DIR",
        value = analyticsDir.map { it.asFile.absolutePath.enquoted() },
    )

    buildConfigField(
        type = "String",
        name = "ANALYTICS_PRIVATE_KEY",
        value = System.getenv("ANALYTICS_PRIVATE_KEY").orEmpty().trim().enquoted()
    )

    buildConfigField(
        type = "String",
        name = "MIXPANEL_PROJECT",
        value = System.getenv("MIXPANEL_PROJECT").orEmpty().trim().enquoted(),
    )

    buildConfigField(
        type = "String",
        name = "MIXPANEL_USERNAME",
        value = System.getenv("MIXPANEL_USERNAME").orEmpty().trim().enquoted(),
    )

    buildConfigField(
        type = "String",
        name = "MIXPANEL_PASSWORD",
        value = System.getenv("MIXPANEL_PASSWORD").orEmpty().trim().enquoted(),
    )
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation("co.touchlab.skie:analytics")
    implementation("co.touchlab.skie:configuration")

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.java)
}

val jar by tasks.getting
tasks.create<JavaExec>("uploadAnalyticsToMixpanel") {
    group = "analytics"
    description = "Uploads analytics to Mixpanel"

    classpath(configurations.getByName("runtimeClasspath"), jar)
    mainClass.set("co.touchlab.skie.analytics.consumer.MixpanelReporterKt")

    doFirst {
        exec {
            environment(
                "AWS_ACCESS_KEY_ID" to System.getenv("AWS_TOUCHLAB_DEPLOY_ACCESS"),
                "AWS_SECRET_ACCESS_KEY" to System.getenv("AWS_TOUCHLAB_DEPLOY_SECRET"),
                "AWS_DEFAULT_REGION" to "us-east-1",
            )
            commandLine(
                "aws", "s3", "sync", "s3://skie-analytics/Backend-ENV-PROD/SKIE-ENV-Production/", analyticsDir.get().asFile.absolutePath,
                "--exclude", "*",
                "--include", "*.gradle.*.Production.*",
            )
        }
    }
}
