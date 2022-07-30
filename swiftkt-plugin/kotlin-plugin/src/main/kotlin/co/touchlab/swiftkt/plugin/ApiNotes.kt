package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.KobjcTransform
import java.io.File

class ApiNotes(
    private val moduleName: String,
    private val transforms: List<KobjcTransform>,
    private val objCExportNamerSwiftNameProvider: ObjCExportNamerSwiftNameProvider,
) {

    fun save(directory: File) {
        val classes = transforms.map { transform ->
            when (transform) {
                is KobjcTransform.HideType -> {
                    """
                    - Name: ${objCExportNamerSwiftNameProvider.getObjCName(transform.typeName)}
                      SwiftPrivate: true
                    """.trimIndent()
                }
                is KobjcTransform.RenameType -> {
                    val name = objCExportNamerSwiftNameProvider.getClassOrProtocolName(transform.typeName)
                    """
                    - Name: ${name.objCName}
                      SwiftName: ${name.swiftName}
                    """.trimIndent()
                }
            }
        }.joinToString("\n")

        val apiNotes = """
            Name: $moduleName
            Classes:
        """.trimIndent() + "\n" + classes

        directory.resolve("${moduleName}.apinotes").writeText(apiNotes)
    }

}
