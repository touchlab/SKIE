package co.touchlab.skie.api.phases.util

import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCBlockPointerType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCClassType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCGenericTypeUsage
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCIdType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCInstanceType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCMetaClassType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCNullableReferenceType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCPointerType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCPrimitiveType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCProtocolType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCRawType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCVoidType
import org.jetbrains.kotlin.backend.konan.objcexport.objcNullableAttribute
import org.jetbrains.kotlin.backend.konan.objcexport.objcNullableResultAttribute

class ObjCTypeRenderer {

    private val typedefsMap = mutableMapOf<String, String>()
    private val mutableMappedClasses = mutableSetOf<String>()
    private val mutableMappedProtocols = mutableSetOf<String>()

    val typedefs: List<Mapping>
        get() = typedefsMap.map { Mapping(it.key, it.value) }

    val mappedClasses: Set<String> by ::mutableMappedClasses

    val mappedProtocols: Set<String> by ::mutableMappedProtocols

    fun render(type: ObjCType, reservedIdentifiers: List<String>): String =
        type.render("", false, reservedIdentifiers.toSet())

    private fun ObjCType.render(attrsAndName: String, hasNullableAttribute: Boolean, reservedIdentifiers: Set<String>): String =
        when (this) {
            is ObjCPointerType -> {
                val nullabilityAttribute = if (nullable) objcNullableAttribute else objcNonnullAttribute

                pointee.render("* ${nullabilityAttribute.withAttrsAndName(attrsAndName)}", true, reservedIdentifiers)
            }
            is ObjCNullableReferenceType -> {
                val attribute = if (isNullableResult) objcNullableResultAttribute else objcNullableAttribute

                nonNullType.render(" $attribute".withAttrsAndName(attrsAndName), true, reservedIdentifiers)
            }
            is ObjCBlockPointerType ->
                returnType.render(buildString {
                    append("(^")
                    append(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
                    append(")(")
                    if (parameterTypes.isEmpty()) append("void".asTypeDefIfNeeded(reservedIdentifiers))
                    parameterTypes.joinTo(this) { it.render("", false, reservedIdentifiers) }
                    append(')')
                }, false, reservedIdentifiers)
            is ObjCProtocolType -> {
                mutableMappedProtocols.add(protocolName)

                if ("id" in reservedIdentifiers || protocolName in reservedIdentifiers) {
                    render().asTypeDef().withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
                } else {
                    render().withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
                }
            }
            is ObjCGenericTypeUsage -> render(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
            is ObjCClassType -> {
                mutableMappedClasses.add(className)

                buildString {
                    append(className.asTypeDefIfNeeded(reservedIdentifiers))
                    if (typeArguments.isNotEmpty()) {
                        append("<")
                        typeArguments.joinTo(this) { it.render("", true, reservedIdentifiers) }
                        append(">")
                    }
                    append(" *")
                    append(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
                }
            }
            is ObjCPrimitiveType, is ObjCRawType, ObjCVoidType -> {
                render().asTypeDefIfNeeded(reservedIdentifiers).withAttrsAndName(attrsAndName)
            }
            ObjCIdType, ObjCInstanceType, ObjCMetaClassType -> {
                render().asTypeDefIfNeeded(reservedIdentifiers)
                    .withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
            }
        }

    private fun String.withAttrsAndName(attrsAndName: String) =
        if (attrsAndName.isEmpty()) this else "$this ${attrsAndName.trimStart()}"

    private fun String.withPrependedExplicitNullability(hasNullableAttribute: Boolean) =
        if (hasNullableAttribute) this else "$objcNonnullAttribute $this"

    private fun String.asTypeDefIfNeeded(reservedIdentifiers: Set<String>): String =
        if (this in reservedIdentifiers) {
            this.asTypeDef()
        } else {
            this
        }

    private fun String.asTypeDef(): String =
        typedefsMap.getOrPut(this) { "Skie__TypeDef__${typedefsMap.size}__" + this.toValidSwiftIdentifier() }

    private val objcNonnullAttribute = "_Nonnull"

    data class Mapping(val from: String, val to: String)
}

