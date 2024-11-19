plugins {
    id("dev.root")
    kotlin("multiplatform") version "2.1.0-RC2" apply false
}

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("co.touchlab.skie:gradle-plugin")
    }
}
