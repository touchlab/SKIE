package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.structure.Directory
import co.touchlab.skie.util.directory.structure.PermanentDirectory
import co.touchlab.skie.util.directory.structure.RootDirectory
import co.touchlab.skie.util.directory.structure.TemporaryDirectory
import java.io.File

class SkieBuildDirectory(
    rootDirectory: File,
) : RootDirectory(rootDirectory) {

    val cache: Cache = Cache(this)

    val debug: Debug = Debug(this)

    val swift: Swift = Swift(this)

    val temp: Temp = Temp(this)

    val swiftCompiler: SwiftCompiler = SwiftCompiler(this)

    val skieConfiguration: File = directory.resolve("configuration.json")

    val license: File = directory.resolve("license.json")

    class Cache(parent: Directory) : PermanentDirectory(parent, "cache") {

        val swiftModules: SwiftModules = SwiftModules(this)

        val cacheableKotlinFramework: CacheableKotlinFramework = CacheableKotlinFramework(this)

        class SwiftModules(parent: Directory) : PermanentDirectory(parent, "swift-module-cache")

        class CacheableKotlinFramework(parent: Directory) : PermanentDirectory(parent, "kotlin-framework") {

            fun framework(moduleName: String): File = directory.resolve("$moduleName.framework").also { it.mkdirs() }
        }
    }

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

        class Generated(parent: Directory) : PermanentDirectory(parent, "generated") {

            fun swiftFile(baseName: String): File = directory.resolve("$baseName.swift")
        }

        class Custom(parent: Directory) : PermanentDirectory(parent, "custom")
    }

    class SwiftCompiler(parent: Directory) : PermanentDirectory(parent, "swift-compiler") {

        val objectFiles: ObjectFiles = ObjectFiles(this)

        fun moduleHeader(moduleName: String): ModuleHeader = ModuleHeader(this, moduleName)

        class ObjectFiles(parent: Directory) : PermanentDirectory(parent, "object-files") {

            val all: List<File>
                get() = directory.walkTopDown()
                    .filter { it.extension == "o" }
                    .toList()
        }

        class ModuleHeader(parent: Directory, private val moduleName: String) : PermanentDirectory(parent, "headers") {

            val swiftModule: File = directory.resolve("${moduleName}.swiftmodule")

            val swiftInterface: File = directory.resolve("${moduleName}.swiftinterface")

            val privateSwiftInterface: File = directory.resolve("${moduleName}.private.swiftinterface")

            val swiftDoc: File = directory.resolve("${moduleName}.swiftdoc")

            val abiJson: File = directory.resolve("${moduleName}.abi.json")

            val swiftSourceInfo: File = directory.resolve("${moduleName}.swiftsourceinfo")

            val swiftHeader: File = directory.resolve("${moduleName}-Swift.h")
        }
    }

    class Temp(parent: Directory) : PermanentDirectory(parent, "temp") {

        val gradle: Gradle = Gradle(this)

        class Gradle(parent: Directory) : PermanentDirectory(parent, "gradle") {

            val mergedCustomSwift: MergedCustomSwift = MergedCustomSwift(this)

            class MergedCustomSwift(parent: Directory) : PermanentDirectory(parent, "merged-custom-swift")
        }
    }
}

