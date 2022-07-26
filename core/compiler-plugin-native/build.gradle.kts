plugins {
    java
    `maven-publish`
}

dependencies {
    implementation(project(":compiler-plugin", configuration = "shadow"))
}

tasks.jar {
    from(configurations.runtimeClasspath.map { config ->
        config.map {
            if (it.isDirectory || !it.exists()) it else zipTree(it)
        }
    })
}