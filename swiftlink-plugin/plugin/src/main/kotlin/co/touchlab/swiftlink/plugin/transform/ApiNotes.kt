package co.touchlab.swiftlink.plugin.transform

import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.io.File

class ApiNotes(
    private val moduleName: String,
    private val transformResolver: ApiTransformResolver,
) {

    fun save(directory: File, enableBridging: Boolean) {
        val builder = Builder()
        with(builder) {
            +"Name: \"$moduleName\""

            fun typeNotes(transform: ResolvedApiTransform.CallableMemberParentTransform) {
                val name = transform.classOrProtocolName

                +"- Name: \"${name.objCName}\""

                indented {
                    if (enableBridging) {
                        transform.bridgedName?.let { +"SwiftBridge: ${moduleName}.${it.resolve()}" }
                    }
                    transform.hide.ifTrue { +"SwiftPrivate: true" }
                    transform.newSwiftName?.let { +"SwiftName: ${it.newQualifiedName()}" }
                    transform.remove.ifTrue { +"Availability: nonswift" }

                    val propertyTransforms = transformResolver.findPropertyTransformsOfParent(transform.target)
                    if (propertyTransforms.isNotEmpty()) {
                        +"Properties:"
                        propertyTransforms.forEach { propertyTransform ->
                            +"- Name: ${propertyTransform.name}"
                            indented {
                                +"PropertyKind: ${if (propertyTransform.isStatic) "Class" else "Instance"}"
                                propertyTransform.newSwiftName?.let { +"SwiftName: $it" }
                                propertyTransform.hide.ifTrue { +"SwiftPrivate: true" }
                                propertyTransform.remove.ifTrue { +"Availability: nonswift" }
                            }
                        }
                    }

                    val methodTransforms = transformResolver.findFunctionTransformsOfParent(transform.target)
                    if (methodTransforms.isNotEmpty()) {
                        +"Methods:"
                        methodTransforms.forEach { methodTransform ->
                            +"- Selector: \"${methodTransform.selector}\""
                            indented {
                                +"MethodKind: ${if (methodTransform.isStatic) "Class" else "Instance"}"
                                methodTransform.newSwiftSelector?.let { +"SwiftName: \"$it\"" }
                                methodTransform.hide.ifTrue { +"SwiftPrivate: true" }
                                methodTransform.remove.ifTrue { +"Availability: nonswift" }
                            }
                        }
                    }
                }
            }

            if (transformResolver.classTransforms.isNotEmpty() || transformResolver.fileTransforms.isNotEmpty()) {
                +"Classes:"
            }

            transformResolver.classTransforms.forEach { typeNotes(it) }
            transformResolver.fileTransforms.forEach { typeNotes(it) }

            if (transformResolver.protocolTransforms.isNotEmpty()) {
                +"Protocols:"
            }

            transformResolver.protocolTransforms.forEach { typeNotes(it) }
        }

        directory.resolve("${moduleName}.apinotes").writeText(builder.storage.toString())
    }

    private class Builder(val storage: StringBuilder = StringBuilder()) {
        operator fun String.unaryPlus() {
            storage.appendLine(this)
        }

        fun indented(perform: Builder.() -> Unit) {
            val builder = Builder()
            builder.perform()
            builder.storage.lines().forEach { storage.appendLine("  $it") }
        }
    }
}
