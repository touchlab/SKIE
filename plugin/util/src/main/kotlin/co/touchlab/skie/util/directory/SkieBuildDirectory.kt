package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.structure.Directory
import co.touchlab.skie.util.directory.structure.PermanentDirectory
import co.touchlab.skie.util.directory.structure.RootDirectory
import co.touchlab.skie.util.directory.structure.TemporaryDirectory
import java.io.File

class SkieBuildDirectory(
    rootDirectory: File,
) : RootDirectory(rootDirectory) {

    val debug: Debug = Debug(this)

    val swift: Swift = Swift(this)

    val skieConfiguration: File = directory.resolve("configuration.json")

    val license: File = directory.resolve("license.json")

    class Debug(parent: Directory) : TemporaryDirectory(parent, "debug") {

        val analytics: Analytics = Analytics(this)

        val logs: Logs = Logs(this)

        val dumps: Dumps = Dumps(this)

        class Analytics(parent: Directory) : TemporaryDirectory(parent, "analytics")

        class Logs(parent: Directory) : TemporaryDirectory(parent, "logs") {

            val swiftc: File = directory.resolve("swiftc.log")

            fun apiFile(baseName: String): File = directory.resolve("$baseName.log")
        }

        class Dumps(parent: Directory) : TemporaryDirectory(parent, "dumps") {

            fun apiFile(baseName: String): File = directory.resolve("$baseName.swift")
        }
    }

    class Swift(parent: Directory) : PermanentDirectory(parent, "swift") {

        val allSwiftFiles: List<File>
            get() = directory.walkTopDown()
                .filter { it.extension == "swift" }
                .toList()

        val generated: Generated = Generated(this)

        val custom: Custom = Custom(this)

        class Generated(parent: Directory) : TemporaryDirectory(parent, "generated") {

            fun swiftFile(baseName: String): File = directory.resolve("$baseName.swift")
        }

        class Custom(parent: Directory) : PermanentDirectory(parent, "custom")
    }
}

