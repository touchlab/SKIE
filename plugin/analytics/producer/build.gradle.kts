plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.apache.compress)
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
