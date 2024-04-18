package co.touchlab.skie.gradle.util

import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.kotlinNativePrebuiltRepositoryBaseUrl
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.konan.CompilerVersion
import java.io.File

private sealed interface BackupProperty<out T> {

    val name: String

    data class Value<T>(override val name: String, val value: T) : BackupProperty<T>

    data class NotDefined(override val name: String) : BackupProperty<Nothing>
}

private object KotlinNativeDownloaderProperties {

    const val main = "kotlin.native.version"
    const val deprecated = "org.jetbrains.kotlin.native.version"
    const val base_repository_url = "kotlin.native.distribution.baseDownloadUrl"
}

fun Project.kotlinNativeCompilerHome(kotlinVersion: KotlinToolingVersion): File {
    return NativeCompilerDownloader(project, CompilerVersion.fromString(kotlinVersion.toString()))
        .also { downloader ->
            val originalVersionProperty = backupProperty<String?>(getKotlinNativeVersionPropertyName())
            val originalBaseRepositoryUrlProperty = backupProperty<String>(KotlinNativeDownloaderProperties.base_repository_url)

            extra.set(KotlinNativeDownloaderProperties.main, kotlinVersion.toString())
            extra.set(KotlinNativeDownloaderProperties.base_repository_url, kotlinVersion.kotlinNativePrebuiltRepositoryBaseUrl)

            downloader.downloadIfNeeded()

            extra.set(KotlinNativeDownloaderProperties.main, null)

            restoreProperty(originalVersionProperty)
            restoreProperty(originalBaseRepositoryUrlProperty)
        }
        .compilerDirectory
}

private fun Project.getKotlinNativeVersionPropertyName(): String =
    if (KotlinNativeDownloaderProperties.main in properties) KotlinNativeDownloaderProperties.main else KotlinNativeDownloaderProperties.deprecated

internal inline fun Project.withKotlinNativeCompilerEmbeddableDependency(kotlinVersion: KotlinToolingVersion, isTarget: Boolean, block: (Dependency) -> Unit) {
    val kotlinNativeCompilerEmbeddableFromHome: String? by project
    val dependency = if (kotlinNativeCompilerEmbeddableFromHome.toBoolean()) {
        if (isTarget) {
            project.dependencies.create(
                files(
                    kotlinNativeCompilerHome(kotlinVersion).resolve("konan/lib/kotlin-native.jar"),
                ),
            )
        } else {
            return
        }
    } else {
        project.dependencies.create("org.jetbrains.kotlin:kotlin-native-compiler-embeddable:$kotlinVersion")
    }
    block(dependency)
}

@Suppress("UNCHECKED_CAST")
private fun <T> Project.backupProperty(name: String): BackupProperty<T> =
    if (name in properties) {
        BackupProperty.Value(name, properties[name] as T)
    } else {
        BackupProperty.NotDefined(name)
    }

private fun <T> Project.restoreProperty(property: BackupProperty<T>) {
    when (property) {
        is BackupProperty.Value -> extra.set(property.name, property.value)
        is BackupProperty.NotDefined -> extra.set(property.name, null)
    }
}
