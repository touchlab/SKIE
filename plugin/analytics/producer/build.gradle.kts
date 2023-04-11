plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.apache.compress)
    api(libs.bugsnag)
    implementation(projects.analytics.analyticsConfiguration)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
