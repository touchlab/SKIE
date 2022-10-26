import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    api(libs.swiftPoet)

    compileOnly(strippedKotlinNativeCompilerEmbeddable())
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
    }
}

fun strippedKotlinNativeCompilerEmbeddable(): FileCollection {
    val targetFile = layout.buildDirectory.file("tmp/kotlin-native-stripped").map {
        val file = it.asFile
        if (!file.exists()) {
            val tree = zipTree(
                org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader(project).also {
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
