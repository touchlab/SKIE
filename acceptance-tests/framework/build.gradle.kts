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
    buildConfigField(
        type = "String",
        name = "SWIFT_GEN_API",
        value = "\"${gradle.includedBuild("core").projectDir.resolve("api/src/commonMain/kotlin")}\"",
    )
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(files(kotlinNativeCompilerEmbeddable().absolutePath))

    implementation("co.touchlab.swiftgen:compiler-plugin")
    implementation("co.touchlab.swiftgen:configuration")
    implementation(libs.swiftpack.api)
    implementation(libs.swiftpack.spi)
    implementation(libs.swiftkt.kotlin.plugin)
}

tasks.test {
    useJUnitPlatform()
}

fun extractedKotlinNativeCompilerEmbeddable(): FileCollection {
    val targetFile = layout.buildDirectory.file("tmp/kotlin-native").map {
        val file = it.asFile
        if (!file.exists()) {
            val tree = zipTree(kotlinNativeCompilerEmbeddable())

            copy {
                from(tree)
                into(file)
            }
        }

        it
    }

    return files(targetFile)
}

fun kotlinNativeCompilerEmbeddable(): File =
    NativeCompilerDownloader(project)
        .also { it.downloadIfNeeded() }
        .compilerDirectory
        .resolve("konan/lib/kotlin-native-compiler-embeddable.jar")