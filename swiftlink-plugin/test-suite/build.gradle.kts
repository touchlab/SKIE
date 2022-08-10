plugins {
    `kotlin-dsl`
    kotlin("jvm")
    id("java-gradle-plugin")
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation(gradleApi())
    implementation(project(":plugin"))
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))

    testImplementation(libs.junit)
    testImplementation(kotlin("gradle-plugin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        create("co.touchlab.swiftlink.test-suite") {
            id = "co.touchlab.swiftlink.test-suite"
            implementationClass = "co.touchlab.swiftlink.tests.TestSuitePlugin"
        }
    }
}
