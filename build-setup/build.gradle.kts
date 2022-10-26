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
        implementationClass = "co.touchlab.skie.gradle.GradleSrcClasspathLoader"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
