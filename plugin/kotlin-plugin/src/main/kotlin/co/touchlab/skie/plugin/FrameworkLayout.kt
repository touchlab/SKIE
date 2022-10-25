package co.touchlab.skie.plugin

import java.io.File

class FrameworkLayout(val framework: File) {
    constructor(frameworkPath: String) : this(File(frameworkPath))

    val moduleName by lazy { framework.name.removeSuffix(".framework") }
    val headersDir by lazy { framework.resolve("Headers") }
    val kotlinHeader by lazy { headersDir.resolve("$moduleName.h") }
    val swiftHeader by lazy { headersDir.resolve("$moduleName-Swift.h") }
    val swiftModule by lazy { framework.resolve("Modules").resolve("$moduleName.swiftmodule").also { it.mkdirs() } }
    val modulemapFile by lazy { framework.resolve("Modules/module.modulemap") }
}
