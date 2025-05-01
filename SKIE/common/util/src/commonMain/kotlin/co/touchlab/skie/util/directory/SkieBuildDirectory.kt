package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.structure.Directory
import co.touchlab.skie.util.directory.structure.PermanentDirectory
import co.touchlab.skie.util.directory.structure.RootDirectory
import co.touchlab.skie.util.directory.structure.TemporaryDirectory
import java.io.File

class SkieBuildDirectory(rootDirectory: File) : RootDirectory(rootDirectory) {

    val analytics: Analytics = Analytics(this)

    val cache: Cache = Cache(this)

    val debug: Debug = Debug(this)

    val swift: Swift = Swift(this)

    val temp: Temp = Temp(this)

    val swiftCompiler: SwiftCompiler = SwiftCompiler(this)

    val skieConfiguration: File = directory.resolve("configuration.json")

    class Analytics(parent: Directory) : TemporaryDirectory(parent, "analytics") {

        fun file(name: String): File = directory.resolve("$name.json")
    }

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

        val allNonGeneratedSwiftFiles: List<File>
            get() = directory.walkTopDown()
                .filter { it.extension == "swift" }
                .filterNot { it.toPath().startsWith(generated.path) }
                .toList()

        val generated: Generated = Generated(this)

        val bundled: Bundled = Bundled(this)

        class Generated(parent: Directory) : PermanentDirectory(parent, "generated")

        class Bundled(parent: Directory) : PermanentDirectory(parent, "bundled")
    }

    class SwiftCompiler(parent: Directory) : PermanentDirectory(parent, "swift-compiler") {

        val objectFiles: ObjectFiles = ObjectFiles(this)

        val module: Module = Module(this)

        fun moduleHeader(moduleName: String): ModuleHeader = ModuleHeader(this, moduleName)

        val fakeObjCFrameworks: FakeObjCFrameworks = FakeObjCFrameworks(this)

        val apiNotes: ApiNotes = ApiNotes(this)

        val config: Config = Config(this)

        class ObjectFiles(parent: Directory) : PermanentDirectory(parent, "object-files") {

            val allFiles: List<File>
                get() = directory.walkTopDown().toList()

            fun objectFile(sourceFileName: String): File = directory.resolve("$sourceFileName.o")

            fun swiftDependencies(sourceFileName: String): File = directory.resolve("$sourceFileName.swiftdeps")

            fun dependencies(sourceFileName: String): File = directory.resolve("$sourceFileName.d")

            fun partialSwiftModule(sourceFileName: String): File = directory.resolve("$sourceFileName~partial.swiftmodule")
        }

        class Module(parent: Directory) : PermanentDirectory(parent, "module") {

            fun swiftDependencies(moduleName: String): File = directory.resolve("$moduleName.swiftdeps")

            fun dependencies(moduleName: String): File = directory.resolve("$moduleName.d")
        }

        class ModuleHeader(parent: Directory, moduleName: String) : PermanentDirectory(parent, "headers") {

            init {
                // Has to be called manually because the instances are created dynamically
                directory.mkdirs()
            }

            val swiftModule: File = directory.resolve("$moduleName.swiftmodule")

            val swiftInterface: File = directory.resolve("$moduleName.swiftinterface")

            val privateSwiftInterface: File = directory.resolve("$moduleName.private.swiftinterface")

            val swiftDoc: File = directory.resolve("$moduleName.swiftdoc")

            val abiJson: File = directory.resolve("$moduleName.abi.json")

            val swiftSourceInfo: File = directory.resolve("$moduleName.swiftsourceinfo")

            val swiftHeader: File = directory.resolve("$moduleName-Swift.h")
        }

        class FakeObjCFrameworks(parent: Directory) : PermanentDirectory(parent, "fake-objc-frameworks") {

            private fun framework(moduleName: String): File = directory.resolve("$moduleName.framework").also { it.mkdirs() }

            private fun headers(moduleName: String): File = framework(moduleName).resolve("Headers").also { it.mkdirs() }

            fun header(moduleName: String): File = headers(moduleName).resolve("$moduleName.h")

            private fun modules(moduleName: String): File = framework(moduleName).resolve("Modules").also { it.mkdirs() }

            fun moduleMap(moduleName: String): File = modules(moduleName).resolve("module.modulemap")
        }

        class ApiNotes(parent: Directory) : PermanentDirectory(parent, "apinotes") {

            fun apiNotes(moduleName: String): File = directory.resolve("$moduleName.apinotes")
        }

        class Config(parent: Directory) : PermanentDirectory(parent, "config") {

            val outputFileMap: File = directory.resolve("OutputFileMap.json")

            fun swiftFileList(moduleName: String): File = directory.resolve("$moduleName.SwiftFileList")
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
