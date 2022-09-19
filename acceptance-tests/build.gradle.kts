plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

val acceptanceTestsDirectory: File = layout.projectDirectory.dir("src/test/resources").asFile

buildConfig {
    packageName(project.group.toString())
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${acceptanceTestsDirectory.absolutePath}\"",
    )

    buildConfigField(
        type = "String",
        name = "BUILD",
        value = "\"${layout.buildDirectory.get().asFile.absolutePath}\"",
    )
}

dependencies {
    testImplementation(project(":acceptance-tests:framework"))
    testImplementation("co.touchlab.swiftgen:api")
}

tasks.test {
    useJUnitPlatform()

    maxHeapSize = "16g"
}

tasks.register("reformatPackagesInAcceptanceTests") {
    doLast {
        acceptanceTestsDirectory.resolve("tests")
            .walkTopDown()
            .filter { it.extension == "kt" }
            .forEach {
                reformatPackage(it)
            }
    }
}

fun reformatPackage(file: File) {
    val lines = file.readLines()

    val indexOfLineWithPackageDeclaration = lines.indexOfFirst { it.trimStart().startsWith("package ") }

    val modifiedLines = lines.mapIndexed { index, line ->
        if (index == indexOfLineWithPackageDeclaration) {
            file.correctPackageDeclaration()
        } else {
            line
        }
    }

    val modifiedText = modifiedLines.joinToString(System.lineSeparator())

    file.writeText(modifiedText)
}

fun File.correctPackageDeclaration(): String {
    val pathComponents = this.getRelativePathComponents(acceptanceTestsDirectory)

    val packageName = pathComponents.joinToString(".") { "`$it`" }

    return "package $packageName"
}

fun File.getRelativePathComponents(base: File): List<String> {
    require(this.toPath().startsWith(base.toPath()) && this != base) {
        "File $this must be located inside ${base}."
    }

    val pathComponents = mutableListOf<String>()

    var currentDirectory = this.parentFile
    while (currentDirectory != base) {
        pathComponents.add(currentDirectory.name)

        currentDirectory = currentDirectory.parentFile
    }

    return pathComponents.reversed()
}