plugins {
    id("skie-jvm")
    id("skie-buildconfig")
}

val acceptanceTestsDirectory: File = layout.projectDirectory.dir("src/test/resources").asFile

buildConfig {
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

skieJvm {
    areContextReceiversEnabled.set(true)
}

dependencies {
    testImplementation(projects.acceptanceTests.framework)
    testImplementation("co.touchlab.skie:configuration-annotations")
}

tasks.test {
    maxHeapSize = "16g"
}

tasks.register<ReformatPackagesInAcceptanceTests>("reformatPackagesInAcceptanceTests") {
    directory = acceptanceTestsDirectory
}

abstract class ReformatPackagesInAcceptanceTests : DefaultTask() {

    @InputDirectory
    lateinit var directory: File

    @TaskAction
    fun execute() {
        directory.resolve("tests")
            .walkTopDown()
            .filter { it.extension == "kt" }
            .forEach {
                reformatPackage(it)
            }
    }

    private fun reformatPackage(file: File) {
        val lines = file.readLines()

        val indexOfLineWithPackageDeclaration = lines.indexOfFirst { it.trimStart().startsWith("package ") }

        val modifiedLines = lines.mapIndexed { index, line ->
            if (index == indexOfLineWithPackageDeclaration) {
                file.correctPackageDeclaration()
            } else {
                line
            }
        }

        val modifiedText = modifiedLines.joinToString(System.lineSeparator(), postfix = System.lineSeparator())

        file.writeText(modifiedText)
    }

    private fun File.correctPackageDeclaration(): String {
        val pathComponents = this.getRelativePathComponents(directory)

        val packageName = pathComponents.joinToString(".") { "`$it`" }

        return "package $packageName"
    }

    private fun File.getRelativePathComponents(base: File): List<String> {
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
}
