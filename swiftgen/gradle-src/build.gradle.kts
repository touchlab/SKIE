plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins.register("gradle-src-classpath-loader") {
        id = "gradle-src-classpath-loader"
        implementationClass = "co.touchlab.swiftgen.gradle.GradleSrcClasspathLoader"
    }
}
