package co.touchlab.swiftgen.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File

fun Project.extractedKotlinNativeCompilerEmbeddable(): FileCollection {
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

fun Project.kotlinNativeCompilerEmbeddable(): File =
    NativeCompilerDownloader(project)
        .also { it.downloadIfNeeded() }
        .compilerDirectory
        .resolve("konan/lib/kotlin-native-compiler-embeddable.jar")
