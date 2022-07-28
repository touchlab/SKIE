import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation("co.touchlab.swiftgen:compiler-plugin")

    api(libs.bundles.testing.jvm)

    implementation(libs.compiler.testing)
    implementation(libs.coroutines)

    implementation(
        files(
            NativeCompilerDownloader(project).also {
                it.downloadIfNeeded()
            }.compilerDirectory.resolve("konan/lib/kotlin-native-compiler-embeddable.jar").absolutePath
        )
    )

    testImplementation(project(":acceptance-tests:framework"))
}

tasks.test {
    useJUnitPlatform()
}

// TODO Check for swiftc
