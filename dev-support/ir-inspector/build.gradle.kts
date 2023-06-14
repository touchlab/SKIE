import co.touchlab.skie.gradle.util.kotlinNativeCompilerHome

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
