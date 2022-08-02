package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.KobjcTransforms
import java.io.File

class ApiNotes(
    private val moduleName: String,
    private val transforms: KobjcTransforms,
    private val objCExportNamerSwiftNameProvider: ObjCExportNamerSwiftNameProvider,
) {

    fun save(directory: File) {
        val classes = transforms.types.values.map { transform ->
            val name = objCExportNamerSwiftNameProvider.getClassOrProtocolName(transform.type)
            val config = when {
                transform.bridge != null -> "SwiftBridge: ${transform.bridge}"
                transform.hide -> "SwiftPrivate: true"
                transform.rename != null -> "SwiftName: ${name.swiftName}"
                transform.remove -> "Availability: nonswift"
                else -> ""
            }

            """
                |- Name: ${name.objCName}
                |  $config
            """.trimMargin()

            // TODO: Add support for properties and methods
        }.joinToString("\n")

        val apiNotes = """
            Name: $moduleName
            Classes:
        """.trimIndent() + "\n" + classes

        directory.resolve("${moduleName}.apinotes").writeText(apiNotes)
    }

}
