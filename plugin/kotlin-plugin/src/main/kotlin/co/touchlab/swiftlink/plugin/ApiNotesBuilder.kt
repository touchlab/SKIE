@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.swiftlink.plugin

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import co.touchlab.swiftlink.plugin.reflection.reflectors.mapper
import java.io.File

internal class ApiNotesBuilder(
    private val accumulator: TransformAccumulator,
    private val namer: ObjCExportNamer,
    private val moduleName: String,
) {

    fun save(directory: File, enableBridging: Boolean) {
        fun typeNotes(target: TransformAccumulator.TypeTransformTarget, transform: TransformAccumulator.ObjcClassTransformScope): YamlBuilder = YamlBuilder().apply {
            val descriptorName = when (target) {
                is TransformAccumulator.TypeTransformTarget.Class -> namer.getClassOrProtocolName(target.descriptor)
                is TransformAccumulator.TypeTransformTarget.File -> namer.getFileClassName(target.file)
            }

            +"- Name: \"${descriptorName.objCName}\""

            indented {
                if (enableBridging) {
                    transform.bridge?.let { +"SwiftBridge: ${moduleName}.${it.typeAliasName}" }
                }
                transform.isHidden.ifTrue { +"SwiftPrivate: true" }
                transform.newSwiftName?.let { +"SwiftName: ${it.qualifiedName}" }
                transform.isRemoved.ifTrue { +"Availability: nonswift" }

                if (transform.properties.isNotEmpty()) {
                    +"Properties:"
                    transform.properties.forEach { (property, propertyTransform) ->
                        +"- Name: ${namer.getPropertyName(property)}"
                        indented {
                            +"PropertyKind: ${if (namer.mapper.isTopLevel(property)) "Class" else "Instance"}"
                            propertyTransform.rename?.let { +"SwiftName: $it" }
                            propertyTransform.isHidden.ifTrue { +"SwiftPrivate: true" }
                            propertyTransform.isRemoved.ifTrue { +"Availability: nonswift" }
                        }
                    }
                }


                if (transform.methods.isNotEmpty()) {
                    +"Methods:"
                    transform.methods.forEach { (method, methodTransform) ->
                        +"- Selector: \"${namer.getSelector(method)}\""
                        indented {
                            +"MethodKind: ${if (namer.mapper.isTopLevel(method)) "Class" else "Instance"}"
                            methodTransform.newSwiftName?.let { +"SwiftName: \"${it.qualifiedName}\"" }
                            methodTransform.isHidden.ifTrue { +"SwiftPrivate: true" }
                            methodTransform.isRemoved.ifTrue { +"Availability: nonswift" }
                        }
                    }
                }
            }
        }

        accumulator.close()

        val notesByTypes = accumulator.typeTransforms.mapValues { (descriptor, transform) ->
            typeNotes(descriptor, transform)
        }

        val classNotes = notesByTypes.filter { (key, _) -> key !is TransformAccumulator.TypeTransformTarget.Class || !key.descriptor.kind.isInterface }
        val protocolNotes = notesByTypes.filter { (key, _) -> key is TransformAccumulator.TypeTransformTarget.Class && key.descriptor.kind.isInterface }

        val builder = YamlBuilder()
        with(builder) {
            +"Name: \"$moduleName\""

            if (classNotes.isNotEmpty()) {
                +"Classes:"
            }

            classNotes.forEach { (_, notes) ->
                append(notes)
            }

            if (protocolNotes.isNotEmpty()) {
                +"Protocols:"
            }

            protocolNotes.forEach { (_, notes) ->
                append(notes)
            }
        }

        directory.resolve("${moduleName}.apinotes").writeText(builder.storage.toString())
    }

    private class YamlBuilder(val storage: StringBuilder = StringBuilder()) {
        operator fun String.unaryPlus() {
            storage.appendLine(this)
        }

        fun append(builder: YamlBuilder) {
            storage.append(builder.storage)
        }

        fun indented(perform: YamlBuilder.() -> Unit) {
            val builder = YamlBuilder()
            builder.perform()
            builder.storage.lines().forEach { storage.appendLine("  $it") }
        }
    }
}
