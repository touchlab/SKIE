plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.apache.compress)
    api(libs.bugsnag)
    implementation(projects.analytics.analyticsConfiguration)
    implementation(projects.util)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
