import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    api(libs.swiftPoet)
    implementation(libs.kotlinx.serialization.json)

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
