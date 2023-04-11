plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.apache.compress)
    api(libs.bugsnag)
    implementation(projects.analytics.analyticsConfiguration)
    implementation(projects.license)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
