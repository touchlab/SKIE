plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.apache.compress)
    implementation(libs.bugsnag)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
