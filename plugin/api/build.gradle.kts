import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable

plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
}

dependencies {
    api(libs.swiftPoet)

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
}

skieJvm {
    areContextReceiversEnabled.set(true)
}
