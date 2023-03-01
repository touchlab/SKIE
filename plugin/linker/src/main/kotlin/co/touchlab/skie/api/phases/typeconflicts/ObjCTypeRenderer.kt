package co.touchlab.skie.api.phases.typeconflicts

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

    fun render(type: ObjCType): String =
        type.render("", false)

    private fun ObjCType.render(attrsAndName: String, hasNullableAttribute: Boolean): String =
        when (this) {
            is ObjCPointerType -> {
                val nullabilityAttribute = if (nullable) objcNullableAttribute else objcNonnullAttribute

                pointee.render("* ${nullabilityAttribute.withAttrsAndName(attrsAndName)}", true)
            }
            is ObjCPrimitiveType -> render().asTypeDef().withAttrsAndName(attrsAndName)
            is ObjCRawType -> render().asTypeDef().withAttrsAndName(attrsAndName)
            ObjCVoidType -> render().asTypeDef().withAttrsAndName(attrsAndName)
            is ObjCNullableReferenceType -> {
                val attribute = if (isNullableResult) objcNullableResultAttribute else objcNullableAttribute

                nonNullType.render(" $attribute".withAttrsAndName(attrsAndName), true)
            }
            is ObjCBlockPointerType ->
                returnType.render(buildString {
                    append("(^")
                    append(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
                    append(")(")
                    if (parameterTypes.isEmpty()) append("void".asTypeDef())
                    parameterTypes.joinTo(this) { it.render("", false) }
                    append(')')
                }, false)
            is ObjCProtocolType -> {
                mutableMappedProtocols.add(protocolName)

                render().asTypeDef().withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
            }
            ObjCIdType -> render().asTypeDef().withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
            is ObjCGenericTypeUsage -> render(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
            is ObjCClassType -> {
                mutableMappedClasses.add(className)

                buildString {
                    append(className.asTypeDef())
                    if (typeArguments.isNotEmpty()) {
                        append("<")
                        typeArguments.joinTo(this) { it.render("", true) }
                        append(">")
                    }
                    append(" *")
                    append(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
                }
            }
            ObjCInstanceType -> render().asTypeDef().withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
            ObjCMetaClassType -> render().asTypeDef().withAttrsAndName(attrsAndName.withPrependedExplicitNullability(hasNullableAttribute))
        }

    private fun String.withAttrsAndName(attrsAndName: String) =
        if (attrsAndName.isEmpty()) this else "$this ${attrsAndName.trimStart()}"

    private fun String.withPrependedExplicitNullability(hasNullableAttribute: Boolean) =
        if (hasNullableAttribute) this else "$objcNonnullAttribute $this"

    private fun String.asTypeDef(): String =
        typedefsMap.getOrPut(this) { "Skie__TypeDef__${typedefsMap.size}__" + this.toValidSwiftIdentifier() }

    private val objcNonnullAttribute = "_Nonnull"

    data class Mapping(val from: String, val to: String)
}

