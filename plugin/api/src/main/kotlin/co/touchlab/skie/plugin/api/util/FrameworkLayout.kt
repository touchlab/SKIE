package co.touchlab.skie.plugin.api.util

import org.jetbrains.kotlin.konan.target.TargetTriple
import java.io.File

class FrameworkLayout(val framework: File) {
    constructor(frameworkPath: String) : this(File(frameworkPath))

    val parentDir by lazy { framework.parentFile }
    val moduleName by lazy { framework.name.removeSuffix(".framework") }
    val headersDir by lazy { framework.resolve("Headers") }
    val kotlinHeader by lazy { headersDir.resolve("$moduleName.h") }
    val swiftHeader by lazy { headersDir.resolve("$moduleName-Swift.h") }
    val swiftModuleParent by lazy { framework.resolve("Modules").resolve("$moduleName.swiftmodule").also { it.mkdirs() } }
    val modulemapFile by lazy { framework.resolve("Modules/module.modulemap") }

    fun swiftModule(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftmodule")

    fun swiftInterface(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftinterface")

    fun privateSwiftInterface(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.private.swiftinterface")

    fun swiftDoc(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftdoc")

    fun abiJson(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.abi.json")

    fun swiftSourceInfo(targetTriple: TargetTriple): File = swiftModuleParent.resolve("$targetTriple.swiftsourceinfo")

    fun cleanSkie() {
        swiftHeader.delete()
        swiftModuleParent.deleteRecursively()
        swiftModuleParent.mkdirs()
    }
}
