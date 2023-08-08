import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File
import org.gradle.api.Project

plugins {
    application
    id("dev.jvm")
    id("dev.buildconfig")
}

buildConfig {
    buildConfigField(
        type = "String",
        name = "KONAN_HOME",
        value = "\"${kotlinNativeCompilerHome.path}\"",
    )
    buildConfigField(
        type = "String",
        name = "CODE",
        value = "\"${layout.projectDirectory.dir("src/main/resources/code").asFile.absolutePath}\"",
    )
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

application {
    mainClass.set("co.touchlab.skie.devsupport.irinspector.IrInspectorKt")
}

dependencies {
    implementation(libs.kotlin.native.compiler.embeddable)
}

val Project.kotlinNativeCompilerHome: File
    get() = NativeCompilerDownloader(project)
        .also { it.downloadIfNeeded() }
        .compilerDirectory
