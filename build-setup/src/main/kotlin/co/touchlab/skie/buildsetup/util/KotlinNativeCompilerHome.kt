package co.touchlab.skie.buildsetup.util

import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File

fun Project.kotlinNativeCompilerHome(kotlinVersion: KotlinToolingVersion): File {
    val downloader = CustomizableNativeCompilerDownloader(project, kotlinVersion.toString())
    downloader.downloadIfNeeded()
    val compilerDirectory = downloader.compilerDirectory

    createProvisionOkFileIfNeeded(compilerDirectory)

    return compilerDirectory
}

private fun createProvisionOkFileIfNeeded(compilerDirectory: File) {
    val provisionOkFile = compilerDirectory.resolve("provisioned.ok")

    if (!provisionOkFile.exists()) {
        provisionOkFile.writeText("")
    }
}

fun Project.getKotlinNativeCompilerEmbeddableDependency(kotlinVersion: KotlinToolingVersion): Dependency {
    val kotlinNativeCompilerEmbeddableFromHome: String? by project

    val useKonanFromHome = (kotlinNativeCompilerEmbeddableFromHome?.uppercase() == "CI" && System.getenv("CI") != null) ||
        kotlinNativeCompilerEmbeddableFromHome.toBoolean()

    return if (useKonanFromHome) {
        project.dependencies.create(
            files(
                kotlinNativeCompilerHome(kotlinVersion).resolve("konan/lib/kotlin-native-compiler-embeddable.jar"),
            ),
        )
    } else {
        project.dependencies.create("org.jetbrains.kotlin:kotlin-native-compiler-embeddable:$kotlinVersion")
    }
}
