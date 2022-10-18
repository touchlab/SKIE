plugins {
    alias(libs.plugins.kotlin.jvm) apply false

    id("gradle-src-classpath-loader")
}

allprojects {
    group = "co.touchlab.skie.dev-support"
}
