plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    val kgpVersion = File("/Users/filip/Documents/work/Touchlab/projects/internal/SKIE/dev-support/build.gradle.kts")
        .readLines()
        .first { it.contains("kotlin(\"multiplatform\") version ") }
        .trim()
        .substringAfter("kotlin(\"multiplatform\") version ")
        .trim()
        .substringBefore(" ")
        .removePrefix("\"")
        .removeSuffix("\"")

    implementation("co.touchlab.skie:build-setup-shared")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kgpVersion")
}

gradlePlugin {
    plugins.register("dev.root") {
        id = "dev.root"
        implementationClass = "co.touchlab.skie.buildsetup.dev.plugins.dev.DevRootPlugin"
    }

    plugins.register("dev.multiplatform") {
        id = "dev.multiplatform"
        implementationClass = "co.touchlab.skie.buildsetup.dev.plugins.dev.DevMultiplatformPlugin"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
