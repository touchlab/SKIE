package co.touchlab.skie.util.directory

import co.touchlab.skie.util.TargetTriple
import java.io.File

class FrameworkLayout(val frameworkDirectory: File) {

    constructor(frameworkPath: String) : this(File(frameworkPath))

    val frameworkName: String by lazy { frameworkDirectory.name.removeSuffix(".framework") }

    val parentDir: File by lazy { frameworkDirectory.parentFile }

    val headersDir: File by lazy { frameworkDirectory.resolve("Headers") }
    val kotlinHeader: File by lazy { headersDir.resolve("$frameworkName.h") }
    val apiNotes: File by lazy { headersDir.resolve("$frameworkName.apinotes") }
    val swiftHeader: File by lazy { headersDir.resolve("$frameworkName-Swift.h") }

    val modulesDir: File by lazy { frameworkDirectory.resolve("Modules") }
    val swiftModuleParent: File by lazy { modulesDir.resolve("$frameworkName.swiftmodule").also { it.mkdirs() } }
    val modulemapFile: File by lazy { modulesDir.resolve("module.modulemap") }

    fun swiftModule(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftmodule")

    fun swiftInterface(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftinterface")

    fun privateSwiftInterface(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.private.swiftinterface")

    fun swiftDoc(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftdoc")

    fun abiJson(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.abi.json")

    fun swiftSourceInfo(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftsourceinfo")
}
