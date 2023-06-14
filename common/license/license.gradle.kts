plugins {
    id("skie.common")
    id("skie.publishable")
}

dependencies {
    implementation(projects.common.configuration)
    implementation(projects.common.util)

    implementation(libs.java.jwt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jackson.databind)

}
