package co.touchlab.skie.test.template

import java.io.File

data class TemplateFile(
    val relativePath: String,
    val file: File,
    val kind: Kind,
) {
    enum class Kind(val extension: String) {
        Kotlin(".kt"),
        BundledSwift(".swift"),
        Swift(".swift"),
    }
}
