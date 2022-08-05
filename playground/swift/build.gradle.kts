import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.configurationcache.extensions.capitalized
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.listDirectoryEntries

val architecture = when (
    val arch = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()
) {
    "arm64" -> "macosArm64"
    "x86_64" -> "macosX64"
    else -> error("Unsupported architecture: $arch")
}

val compileSwift = tasks.register<Exec>("compileSwift") {
    val frameworkDirectory = layout.projectDirectory.dir("../kotlin/build/bin/$architecture/releaseFramework")
    val mainFile = layout.buildDirectory.file("main").get().asFile

    group = "build"
    dependsOn(":playground:kotlin:linkReleaseFramework${architecture.capitalized()}")

    inputs.dir(frameworkDirectory)
    outputs.file(mainFile)

    doFirst {
        mkdir(layout.buildDirectory)
    }
    doFirst {
        val generatedSwiftDirectory = layout.projectDirectory.dir("../kotlin/build/generated/swiftpack-expanded/releaseFramework/$architecture").asFile

        val generatedSwift = generatedSwiftDirectory.listFiles()!!.joinToString("\n") {
            "------ ${it.name} ------\n" + it.readText()
        }

        println("---------------- Generated Swift ----------------")
        print(generatedSwift)

        println("---------------- Swift compilation ----------------")
    }
    commandLine(
        "swiftc",
        "main.swift",
        "-F",
        frameworkDirectory.asFile.absolutePath,
        "-o",
        mainFile.absolutePath,
    )
}

val clean = tasks.register<Delete>("clean") {
    group = "build"
    delete("build")
    dependsOn(":playground:kotlin:clean")
}

tasks.register<Exec>("runSwift") {
    group = "build"
    dependsOn(clean)
    dependsOn(compileSwift).mustRunAfter(clean)
    doFirst {
        println("---------------- Program output ----------------")
    }
    commandLine("build/main")
}
