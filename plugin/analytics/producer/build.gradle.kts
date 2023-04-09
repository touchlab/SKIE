plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.apache.compress)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
