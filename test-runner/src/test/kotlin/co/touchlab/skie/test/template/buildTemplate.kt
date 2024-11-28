package co.touchlab.skie.test.template

import java.io.File

fun buildTemplate(name: String, builder: TemplateBuilderScope.() -> Unit): Template {
    val files = mutableListOf<TemplateFile>()
    val scope = object: TemplateBuilderScope {

        private fun template(fileName: String, kind: TemplateFile.Kind) {
            val sourceRelativePath = "templates/${name}/${fileName}${kind.extension}"
            val url = checkNotNull(Templates::class.java.getResource("/$sourceRelativePath")) {
                "Couldn't find template for $fileName (at /$sourceRelativePath)"
            }
            val targetRelativePath = when (kind) {
                TemplateFile.Kind.Kotlin -> sourceRelativePath
                TemplateFile.Kind.Swift -> "templates.$name.$fileName${kind.extension}"
                TemplateFile.Kind.BundledSwift -> "$fileName${kind.extension}"
            }

            files += TemplateFile(
                relativePath = targetRelativePath,
                file = File(url.toURI()),
                kind = kind,
            )
        }

        override fun kotlin(name: String) = template(name, TemplateFile.Kind.Kotlin)

        override fun swift(name: String) = template(name, TemplateFile.Kind.Swift)

        override fun bundledSwift(name: String) = template(name, TemplateFile.Kind.BundledSwift)
    }

    scope.builder()

    return Template(
        name = name,
        files = files,
    )
}
