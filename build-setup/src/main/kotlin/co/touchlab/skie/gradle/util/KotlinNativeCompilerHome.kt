package co.touchlab.skie.gradle.util

import co.touchlab.skie.gradle.version.KotlinToolingVersionComponent
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.konan.CompilerVersion
import java.io.File

private data class BackupProperty<T>(
    val name: String,
    val value: T
)

private object KotlinNativeVersionPropertyName {
    const val main = "kotlin.native.version"
    const val deprecated = "org.jetbrains.kotlin.native.version"
}

fun Project.kotlinNativeCompilerHome(kotlinVersion: KotlinToolingVersionComponent): File {
    return NativeCompilerDownloader(project, CompilerVersion.fromString(kotlinVersion.value))
        .also { downloader ->
            val backupProperty = backupProperty<String?>(KotlinNativeVersionPropertyName.main) ?: backupProperty(KotlinNativeVersionPropertyName.deprecated)
            extra.set(KotlinNativeVersionPropertyName.main, kotlinVersion.value)
            downloader.downloadIfNeeded()
            extra.set(KotlinNativeVersionPropertyName.main, null)
            backupProperty?.let { restoreProperty(it) }
        }
        .compilerDirectory
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
