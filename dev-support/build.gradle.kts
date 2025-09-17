plugins {
    id("dev.root")
    kotlin("multiplatform") version "2.2.20" apply false
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
