import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader

plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(kotlinNativeCompilerEmbeddable())
    runtimeOnly(
        files(
            NativeCompilerDownloader(project).also {
                it.downloadIfNeeded()
            }.compilerDirectory.resolve("konan/lib/kotlin-native-compiler-embeddable.jar").absolutePath
        )
    )

    implementation("co.touchlab.swiftgen:compiler-plugin")
    implementation(libs.swiftpack.api)
    implementation(libs.swiftpack.spi)
    implementation(libs.swiftkt.kotlin.plugin)
}

tasks.test {
    useJUnitPlatform()
}

fun kotlinNativeCompilerEmbeddable(): FileCollection {
    val targetFile = layout.buildDirectory.file("tmp/kotlin-native").map {
        val file = it.asFile
        if (!file.exists()) {
            val tree = zipTree(
                NativeCompilerDownloader(project).also {
                    it.downloadIfNeeded()
                }.compilerDirectory.resolve("konan/lib/kotlin-native-compiler-embeddable.jar")
            )

            copy {
                from(tree)
                into(file)
            }
        }

        it
    }

    return files(targetFile)
}