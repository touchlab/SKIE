plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    implementation(libs.swiftpack.api)
}
