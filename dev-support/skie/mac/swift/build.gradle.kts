import co.touchlab.skie.buildsetup.util.MacOsCpuArchitecture

val architecture = MacOsCpuArchitecture.getCurrent(project).get()

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

val buildDebug by tasks.registering(Exec::class) {
    configureBuild("debug")
}

val buildRelease by tasks.registering(Exec::class) {
    configureBuild("release")
}

fun String.capitalized(): String =
    replaceFirstChar { it.uppercase() }

fun Exec.configureBuild(mode: String) {
    group = "build"

    val linkTask = tasks.getByPath(":skie-mac-framework:link${mode.capitalized()}Framework${architecture.kotlinGradleName.capitalized()}")
//     val linkTask = tasks.getByPath(":skie-mac-framework:assembleKotlin${mode.capitalized()}Framework${architecture.kotlinGradleName.capitalized()}")

    linkTask.enabled = "swiftOnly" !in System.getenv()

    inputs.files(linkTask.outputs)
    inputs.files(createSwiftMain.map { it.outputs })

    val outputDirectory = layout.buildDirectory.dir(mode)
    val output = outputDirectory.map { it.file("main") }
    outputs.file(output)

    doFirst {
        outputDirectory.get().asFile.mkdirs()
    }
    doFirst {
        println("---------------- Swift compilation ----------------")
    }
    doFirst {
        commandLine(
            "arch",
            "-${architecture.systemName}",
            "swiftc",
            createSwiftMain.get().outputs.files.first().absolutePath,
            "-F",
            linkTask.outputs.files.first().absolutePath,
            "-o",
            output.get().asFile.absolutePath,
            // Workaround for https://github.com/apple/swift/issues/55127
            "-parse-as-library",
            // Workaround for missing symbol when compiling with Coroutines for MacosArm64
            "-Xlinker",
            "-dead_strip",
        )
    }
}

tasks.register<Exec>("runDebug") {
    configureRun(buildDebug)
}

tasks.register<Exec>("runRelease") {
    configureRun(buildRelease)
}

fun Exec.configureRun(build: TaskProvider<Exec>) {
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
