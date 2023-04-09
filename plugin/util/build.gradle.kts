plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
