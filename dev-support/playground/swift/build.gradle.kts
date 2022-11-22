import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.configurationcache.extensions.capitalized

val architecture = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()

val architectureKotlinName = when (architecture) {
    "arm64" -> "macosArm64"
    "x86_64" -> "macosX64"
    else -> error("Unsupported architecture: $architecture")
}

val createSwiftMain by tasks.registering {
    val mainFile = layout.projectDirectory.file("main.swift")

    outputs.files(mainFile)

    onlyIf { !mainFile.asFile.exists() }

    doFirst {
        if (!mainFile.asFile.exists()) {
            val newContent = """
                import Foundation
                import Kotlin

                @main
                struct Main {
                    static func main() async {
                    }
                }
            """.trimIndent() + "\n"

            mainFile.asFile.writeText(newContent)
        }
    }
}

val build by tasks.registering(Exec::class) {
    group = "build"

    val linkTask = tasks.getByPath(":playground:kotlin:framework:linkDebugFramework${architectureKotlinName.capitalized()}")

    inputs.files(linkTask.outputs)
    inputs.files(createSwiftMain.map { it.outputs })

    val output = layout.buildDirectory.file("main").get().asFile
    outputs.file(output)

    doFirst {
        mkdir(layout.buildDirectory)
    }
    doFirst {
        println("---------------- Swift compilation ----------------")
    }
    doFirst {
        commandLine(
            "arch",
            "-$architecture",
            "swiftc",
            createSwiftMain.get().outputs.files.first().absolutePath,
            "-F",
            linkTask.outputs.files.first().absolutePath,
            "-o",
            output.absolutePath,
            // Workaround for https://github.com/apple/swift/issues/55127
            "-parse-as-library",
            // Workaround for missing symbol when compiling with Coroutines for MacosArm64
            "-Xlinker",
            "-dead_strip",
        )
    }
}

tasks.register<Exec>("run") {
    group = "build"

    inputs.files(build.map { it.outputs })

    doFirst {
        println("---------------- Program output ----------------")
    }

    doFirst {
        commandLine(build.get().outputs.files.first().absolutePath)
    }
}

tasks.register<Delete>("clean") {
    delete("build")
}
