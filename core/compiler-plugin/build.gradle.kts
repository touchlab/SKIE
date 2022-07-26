plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    `maven-publish`
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    implementation(libs.swiftpack.api)
}

tasks.shadowJar {
    relocate("org.jetbrains.kotlin.com.intellij", "com.intellij")
    mergeServiceFiles()

    archiveClassifier.set("shadow")
}
