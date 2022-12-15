@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.io.File

internal class ApiNotesBuilder(
    private val accumulator: TransformAccumulator,
    private val namer: ObjCExportNamer,
    private val moduleName: String,
) {

    fun save(directory: File, enableBridging: Boolean) {
        val notesByTypes = accumulator.typeTransforms.mapValues { (descriptor, transform) ->
            typeNotes(descriptor, transform, enableBridging)
        }

        val builder = YamlBuilder()
        with(builder) {
            +"Name: \"$moduleName\""

            val classNotes = notesByTypes
                .filter { (key, _) -> key !is TransformAccumulator.TypeTransformTarget.Class || !key.descriptor.kind.isInterface }
            if (classNotes.isNotEmpty()) {
                +"Classes:"
            }

            classNotes.forEach { (_, notes) ->
                append(notes)
            }

            val protocolNotes = notesByTypes
                .filter { (key, _) -> key is TransformAccumulator.TypeTransformTarget.Class && key.descriptor.kind.isInterface }
            if (protocolNotes.isNotEmpty()) {
                +"Protocols:"
            }

            protocolNotes.forEach { (_, notes) ->
                append(notes)
            }
        }

        directory.resolve("${moduleName}.apinotes").writeText(builder.storage.toString())
    }

    private fun typeNotes(
        target: TransformAccumulator.TypeTransformTarget,
        transform: TransformAccumulator.ObjcClassTransformScope,
        enableBridging: Boolean,
    ): YamlBuilder = YamlBuilder().apply {
        +"- Name: \"${target.descriptorName.objCName}\""

        indented {
            if (enableBridging) {
                transform.bridge?.let { +"SwiftBridge: ${moduleName}.${it.typeAliasName}" }
            }
            transform.isHidden.ifTrue { +"SwiftPrivate: true" }
            transform.newSwiftName?.let { +"SwiftName: ${it.qualifiedName}" }
            transform.isRemoved.ifTrue { +"Availability: nonswift" }

            addPropertiesIfNeeded(transform)

            addMethodsIfNeeded(transform)
        }
    }

    context(YamlBuilder)
    private fun addPropertiesIfNeeded(transform: TransformAccumulator.ObjcClassTransformScope) {
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
    }

    context(YamlBuilder)
    private fun addMethodsIfNeeded(transform: TransformAccumulator.ObjcClassTransformScope) {
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

    private val TransformAccumulator.TypeTransformTarget.descriptorName: ObjCExportNamer.ClassOrProtocolName
        get() = when (this) {
            is TransformAccumulator.TypeTransformTarget.Class -> namer.getClassOrProtocolName(descriptor)
            is TransformAccumulator.TypeTransformTarget.File -> namer.getFileClassName(file)
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
