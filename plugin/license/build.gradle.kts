plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(libs.jjwt.api)
    implementation(libs.jjwt.impl)
    implementation(libs.jjwt.jackson)
    implementation(projects.util)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
