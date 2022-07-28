import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    compileOnly(strippedKotlinNativeCompilerEmbeddable())

    implementation(libs.swiftpack.spi)

    compileOnly(libs.auto.service)
    kapt(libs.auto.service)
}


fun strippedKotlinNativeCompilerEmbeddable(): FileCollection {
    val targetFile = layout.buildDirectory.file("tmp/kotlin-native-stripped").map {
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
