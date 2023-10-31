package co.touchlab.skie.gradle.util

import co.touchlab.skie.gradle.KotlinToolingVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.konan.CompilerVersion
import java.io.File

private data class BackupProperty<T>(
    val name: String,
    val value: T,
)

private object KotlinNativeVersionPropertyName {

    const val main = "kotlin.native.version"
    const val deprecated = "org.jetbrains.kotlin.native.version"
}

fun Project.kotlinNativeCompilerHome(kotlinVersion: KotlinToolingVersion): File {
    return NativeCompilerDownloader(project, CompilerVersion.fromString(kotlinVersion.toString()))
        .also { downloader ->
            val backupProperty = backupProperty<String?>(KotlinNativeVersionPropertyName.main) ?: backupProperty(KotlinNativeVersionPropertyName.deprecated)
            extra.set(KotlinNativeVersionPropertyName.main, kotlinVersion.toString())
            downloader.downloadIfNeeded()
            extra.set(KotlinNativeVersionPropertyName.main, null)
            backupProperty?.let { restoreProperty(it) }
        }
        .compilerDirectory
}

internal inline fun Project.withKotlinNativeCompilerEmbeddableDependency(kotlinVersion: KotlinToolingVersion, isTarget: Boolean, block: (Dependency) -> Unit) {
    val kotlinNativeCompilerEmbeddableFromHome: String? by project
    val dependency = if (kotlinNativeCompilerEmbeddableFromHome.toBoolean()) {
        if (isTarget) {
            project.dependencies.create(
                files(
                    kotlinNativeCompilerHome(kotlinVersion).resolve("konan/lib/kotlin-native.jar")
                )
            )
        } else {
            return
        }
    } else {
        project.dependencies.create("org.jetbrains.kotlin:kotlin-native-compiler-embeddable:$kotlinVersion")
    }
    block(dependency)
}

private fun <T> Project.backupProperty(name: String): BackupProperty<T>? {
    return if (properties.containsKey(name)) {
        BackupProperty(name, properties[name] as T)
    } else {
        null
    }
}

private fun <T> Project.restoreProperty(property: BackupProperty<T>) {
    extra.set(property.name, property.value)
}
