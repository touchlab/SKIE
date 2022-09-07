package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.spec.KobjcTransforms
import co.touchlab.swiftpack.spec.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.KotlinFunctionReference
import co.touchlab.swiftpack.spec.KotlinPackageReference
import co.touchlab.swiftpack.spec.KotlinPropertyReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.MemberParentReference
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.io.File

val MemberParentReference.fqName: FqName
    get() = when (this) {
        is KotlinPackageReference -> if (packageName.isBlank()) {
            FqName.ROOT
        } else {
            FqName(packageName)
        }
        is KotlinTypeReference -> container.fqName.child(Name.identifier(typeName))
    }

class TransformResolver(
    private val namer: ObjCExportNamer,
    private val kotlinNameResolver: KotlinNameResolver,
    private val transforms: KobjcTransforms,
) {

    constructor(
        namer: ObjCExportNamer,
        kotlinNameResolver: KotlinNameResolver,
        swiftPackModules: List<NamespacedSwiftPackModule>,
    ): this(namer, kotlinNameResolver, swiftPackModules.map { it.module.kobjcTransforms }.merge())

    val types: Map<KotlinTypeReference, ResolvedTypeTransform>
    val classes: Map<KotlinTypeReference, ResolvedTypeTransform>
    val protocols: Map<KotlinTypeReference, ResolvedTypeTransform>
    val fileClasses: Map<FileInPackage, ResolvedTypeTransform>
    val allClasses: List<ResolvedTypeTransform>

    private val properties: Map<KotlinPropertyReference, ResolvedPropertyTransform>
    private val functions: Map<KotlinFunctionReference, ResolvedFunctionTransform>
    private val enumEntries: Map<KotlinEnumEntryReference, ResolvedEnumEntryTransform>

    init {
        val allTypes: Map<KotlinTypeReference, ResolvedTypeTransform> = transforms.types.mapValues { (_, transform) ->
            val classDescriptor = kotlinNameResolver.resolveClass(transform.reference)
            val name = namer.getClassOrProtocolName(classDescriptor)
            val properties = transform.properties.mapValues { (_, transform) ->
                val propertyDescriptor = kotlinNameResolver.resolveProperty(transform.reference)
                val name = namer.getPropertyName(propertyDescriptor)
                ResolvedPropertyTransform(
                    reference = transform.reference,
                    descriptor = propertyDescriptor,
                    name = name,
                    newSwiftName = transform.rename ?: transform.hide.ifTrue { "__${name}" },
                    hide = transform.hide,
                    remove = transform.remove,
                    isStatic = false,
                )
            }
            val methods = transform.methods.mapValues { (_, transform) ->
                val methodDescriptor = kotlinNameResolver.resolveFunction(transform.reference)
                val selector = namer.getSelector(methodDescriptor)
                val swiftName = namer.getSwiftName(methodDescriptor)
                ResolvedFunctionTransform(
                    reference = transform.reference,
                    descriptor = methodDescriptor,
                    selector = selector,
                    newSwiftSelector = transform.rename ?: transform.hide.ifTrue { "__${swiftName}" },
                    hide = transform.hide,
                    remove = transform.remove,
                    isStatic = false,
                )
            }
            val enumEntries = transform.enumEntries.mapValues { (_, transform) ->
                val enumEntryDescriptor = kotlinNameResolver.resolveEnumEntry(transform.reference)
                val name = namer.getEnumEntrySelector(enumEntryDescriptor)
                ResolvedEnumEntryTransform(
                    reference = transform.reference,
                    descriptor = enumEntryDescriptor,
                    name = name,
                    newSwiftName = transform.rename ?: transform.hide.ifTrue { "__${name}" },
                    hide = transform.hide,
                    remove = transform.remove,
                )
            }

            ResolvedTypeTransform(
                isProtocol = classDescriptor.kind == ClassKind.INTERFACE,
                classOrProtocolName = name,
                newSwiftName = transform.rename ?: transform.hide.ifTrue { "__${name.swiftName}" },
                bridgedName = transform.bridge,
                hide = transform.hide,
                remove = transform.remove,
                properties = properties,
                methods = methods,
                enumEntries = enumEntries,
            )
        }

        val globalProperties = transforms.properties.map { (_, transform) ->
            val propertyDescriptor = kotlinNameResolver.resolveProperty(transform.reference)
            val name = namer.getPropertyName(propertyDescriptor)
            ResolvedPropertyTransform(
                reference = transform.reference,
                descriptor = propertyDescriptor,
                name = name,
                newSwiftName = transform.rename ?: transform.hide.ifTrue { "__$name" },
                hide = transform.hide,
                remove = transform.remove,
                isStatic = true,
            )
        }.groupBy { it.descriptor.findSourceFileInPackage() }

        val globalFunctions = transforms.functions.map { (_, transform) ->
            val functionDescriptor = kotlinNameResolver.resolveFunction(transform.reference)
            val selector = namer.getSelector(functionDescriptor)
            ResolvedFunctionTransform(
                reference = transform.reference,
                descriptor = functionDescriptor,
                selector = selector,
                newSwiftSelector = transform.rename ?: transform.hide.ifTrue { "__$selector" },
                hide = transform.hide,
                remove = transform.remove,
                isStatic = true,
            )
        }.groupBy { it.descriptor.findSourceFileInPackage() }

        val files = transforms.files.mapKeys { (_, transform) ->
            kotlinNameResolver.findFile(transform.reference)
        }

        fileClasses = (files.keys.filterNotNull().toSet() + globalProperties.keys + globalFunctions.keys).associateWith { file ->
            val fileTransform = files[file]

            val name = namer.getFileClassName(file.file)
            ResolvedTypeTransform(
                isProtocol = false,
                classOrProtocolName = name,
                newSwiftName = fileTransform?.rename ?: fileTransform?.hide?.ifTrue { "__${name.swiftName}" },
                bridgedName = fileTransform?.bridge,
                hide = fileTransform?.hide ?: false,
                remove = fileTransform?.remove ?: false,
                properties = globalProperties[file]?.associateBy { it.reference } ?: emptyMap(),
                methods = globalFunctions[file]?.associateBy { it.reference } ?: emptyMap(),
                enumEntries = emptyMap(),
            )
        }

        types = allTypes
        classes = allTypes.filterValues { !it.isProtocol }
        protocols = allTypes.filterValues { it.isProtocol }
        allClasses = classes.values + fileClasses.values

        properties = (types.values + fileClasses.values).flatMap { it.properties.values }.associateBy { it.reference }
        functions = (types.values + fileClasses.values).flatMap { it.methods.values }.associateBy { it.reference }
        enumEntries = types.values.flatMap { it.enumEntries.values }.associateBy { it.reference }
    }

    fun findTypeTransform(type: KotlinTypeReference): ResolvedTypeTransform? = types[type]

    fun findPropertyTransform(reference: KotlinPropertyReference): ResolvedPropertyTransform? = properties[reference]

    fun findFunctionTransform(reference: KotlinFunctionReference): ResolvedFunctionTransform? = functions[reference]

    fun findEnumEntryTransform(reference: KotlinEnumEntryReference): ResolvedEnumEntryTransform? = enumEntries[reference]

    fun findFileTransform(file: FileInPackage): ResolvedTypeTransform? = fileClasses[file]
}

data class FileInPackage(val file: SourceFile, val packageDes: FqName)

fun CallableMemberDescriptor.findSourceFileInPackage(): FileInPackage {
    val packageName = this.containingPackage() ?: FqName.ROOT
    val file = findSourceFile()
    return FileInPackage(file, packageName)
}

class ResolvedTypeTransform(
    val isProtocol: Boolean,
    val classOrProtocolName: ObjCExportNamer.ClassOrProtocolName,
    val newSwiftName: String?,
    val bridgedName: String?,
    val hide: Boolean,
    val remove: Boolean,
    val properties: Map<KotlinPropertyReference, ResolvedPropertyTransform>,
    val methods: Map<KotlinFunctionReference, ResolvedFunctionTransform>,
    val enumEntries: Map<KotlinEnumEntryReference, ResolvedEnumEntryTransform>,
)

class ResolvedPropertyTransform(
    val reference: KotlinPropertyReference,
    val descriptor: PropertyDescriptor,
    val name: String,
    val newSwiftName: String?,
    val hide: Boolean,
    val remove: Boolean,
    val isStatic: Boolean,
)

class ResolvedFunctionTransform(
    val reference: KotlinFunctionReference,
    val descriptor: FunctionDescriptor,
    val selector: String,
    val newSwiftSelector: String?,
    val hide: Boolean,
    val remove: Boolean,
    val isStatic: Boolean,
)

class ResolvedEnumEntryTransform(
    val reference: KotlinEnumEntryReference,
    val descriptor: ClassDescriptor,
    val name: String,
    val newSwiftName: String?,
    val hide: Boolean,
    val remove: Boolean,
)

class ApiNotes(
    private val moduleName: String,
    private val transformResolver: TransformResolver,
) {

    fun save(directory: File, enableBridging: Boolean) {
        val builder = Builder()
        with(builder) {
            +"Name: \"$moduleName\""

            fun typeNotes(transform: ResolvedTypeTransform) {
                val name = transform.classOrProtocolName

                +"- Name: \"${name.objCName}\""

                indented {
                    if (enableBridging) {
                        transform.bridgedName?.let { +"SwiftBridge: $it" }
                    }
                    transform.hide.ifTrue { +"SwiftPrivate: true" }
                    transform.newSwiftName?.let { +"SwiftName: $it" }
                    transform.remove.ifTrue { +"Availability: nonswift" }

                    if (transform.properties.isNotEmpty()) {
                        +"Properties:"
                        transform.properties.values.forEach { propertyTransform ->
                            +"- Name: ${propertyTransform.name}"
                            indented {
                                +"PropertyKind: ${if (propertyTransform.isStatic) "Class" else "Instance"}"
                                propertyTransform.newSwiftName?.let { +"SwiftName: $it" }
                                propertyTransform.hide.ifTrue { +"SwiftPrivate: true" }
                                propertyTransform.remove.ifTrue { +"Availability: nonswift" }
                            }
                        }
                    }

                    if (transform.methods.isNotEmpty()) {
                        +"Methods:"
                        transform.methods.values.forEach { methodTransform ->
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


            if (transformResolver.allClasses.isNotEmpty()) {
                +"Classes:"
                transformResolver.allClasses.forEach { typeNotes(it) }
            }

            if (transformResolver.protocols.isNotEmpty()) {
                +"Protocols:"
                transformResolver.protocols.values.forEach { typeNotes(it) }
            }
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
