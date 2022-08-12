plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    implementation(libs.swiftpack.api)
    implementation(project(":api"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}