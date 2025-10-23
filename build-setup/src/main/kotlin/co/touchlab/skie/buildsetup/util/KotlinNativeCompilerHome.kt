package co.touchlab.skie.buildsetup.util

import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File

private sealed interface BackupProperty<out T> {

    val name: String

    data class Value<T>(override val name: String, val value: T) : BackupProperty<T>

    data class NotDefined(override val name: String) : BackupProperty<Nothing>
}

private object KotlinNativeDownloaderProperties {

    const val main = "kotlin.native.version"
    const val deprecated = "org.jetbrains.kotlin.native.version"
    const val downloadFromMaven = "kotlin.native.distribution.downloadFromMaven"
}

fun Project.kotlinNativeCompilerHome(kotlinVersion: KotlinToolingVersion): File {
    val originalVersionProperty = backupProperty<String?>(getKotlinNativeVersionPropertyName())
    val originalDownloadFromMavenProperty = backupProperty<String>(KotlinNativeDownloaderProperties.downloadFromMaven)

    extra.set(KotlinNativeDownloaderProperties.main, kotlinVersion.toString())
    extra.set(KotlinNativeDownloaderProperties.downloadFromMaven, "true")

    val downloader = NativeCompilerDownloader(project)
    downloader.downloadIfNeeded()
    val compilerDirectory = downloader.compilerDirectory

    createProvisionOkFileIfNeeded(compilerDirectory)

    extra.set(KotlinNativeDownloaderProperties.main, null)

    restoreProperty(originalVersionProperty)
    restoreProperty(originalDownloadFromMavenProperty)

    return compilerDirectory
}

private fun createProvisionOkFileIfNeeded(compilerDirectory: File) {
    val provisionOkFile = compilerDirectory.resolve("provisioned.ok")

    if (!provisionOkFile.exists()) {
        provisionOkFile.writeText("")
    }
}

private fun Project.getKotlinNativeVersionPropertyName(): String =
    if (KotlinNativeDownloaderProperties.main in properties) KotlinNativeDownloaderProperties.main else KotlinNativeDownloaderProperties.deprecated

internal inline fun Project.withKotlinNativeCompilerEmbeddableDependency(kotlinVersion: KotlinToolingVersion, block: (Dependency) -> Unit) {
    val kotlinNativeCompilerEmbeddableFromHome: String? by project

    val useKonanFromHome = (kotlinNativeCompilerEmbeddableFromHome?.uppercase() == "CI" && System.getenv("CI") != null) ||
        kotlinNativeCompilerEmbeddableFromHome.toBoolean()

    val dependency = if (useKonanFromHome) {
        project.dependencies.create(
            files(
                kotlinNativeCompilerHome(kotlinVersion).resolve("konan/lib/kotlin-native-compiler-embeddable.jar"),
            ),
        )
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
