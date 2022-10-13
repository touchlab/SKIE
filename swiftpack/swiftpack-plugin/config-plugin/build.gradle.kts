
plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.buildconfig)
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(libs.auto.service)
    kapt(libs.auto.service)

    api(projects.swiftpackApi)
    implementation(projects.swiftpackPluginApi)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.map { configuration ->
        configuration.filter { !it.name.contains("kotlin-stdlib") }.map { if (it.isDirectory) it else zipTree(it) }
    })
}
