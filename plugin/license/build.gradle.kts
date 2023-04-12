plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(libs.java.jwt)
    implementation(projects.util)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
