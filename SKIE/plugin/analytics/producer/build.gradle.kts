plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.apache.compress)
    api(libs.bugsnag)
    implementation(projects.pluginAnalyticsAnalyticsConfiguration)
    implementation(projects.pluginUtil)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
