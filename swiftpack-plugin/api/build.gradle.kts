plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.swiftPoet)
    api(projects.swiftpackSpec)

    // compileOnly(kotlin("compiler-embeddable"))
    compileOnly(strippedKotlinNativeCompilerEmbeddable())

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation(libs.compileTesting)
    testImplementation(libs.compileTesting.ksp)
}

tasks.test {
    useJUnitPlatform()
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
