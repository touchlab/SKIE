plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(projects.util)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
