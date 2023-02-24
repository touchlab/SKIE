package co.touchlab.skie.api.apinotes.builder

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

fun ObjCType.renderWithExplicitNullability(): String =
    renderWithExplicitNullability("", false)

fun ObjCType.renderWithExplicitNullability(attrsAndName: String, hasNullableAttribute: Boolean): String =
    when (this) {
        is ObjCPointerType -> {
            val nullabilityAttribute = if (nullable) objcNullableAttribute else objcNonnullAttribute

            pointee.renderWithExplicitNullability("* ${nullabilityAttribute.withAttrsAndName(attrsAndName)}", true)
        }
        is ObjCPrimitiveType -> render(attrsAndName)
        is ObjCRawType -> render(attrsAndName)
        ObjCVoidType -> render(attrsAndName)
        is ObjCNullableReferenceType -> {
            val attribute = if (isNullableResult) objcNullableResultAttribute else objcNullableAttribute

            nonNullType.renderWithExplicitNullability(" $attribute".withAttrsAndName(attrsAndName), true)
        }
        is ObjCBlockPointerType ->
            returnType.renderWithExplicitNullability("", false) + buildString {
                append("(^")
                append(attrsAndName.withExplicitNullability(hasNullableAttribute))
                append(")(")
                if (parameterTypes.isEmpty()) append("void")
                parameterTypes.joinTo(this) { it.renderWithExplicitNullability("", false) }
                append(')')
            }
        is ObjCProtocolType -> render(attrsAndName.withExplicitNullability(hasNullableAttribute))
        ObjCIdType -> render(attrsAndName.withExplicitNullability(hasNullableAttribute))
        is ObjCGenericTypeUsage -> render(attrsAndName.withExplicitNullability(hasNullableAttribute))
        is ObjCClassType -> render(attrsAndName.withExplicitNullability(hasNullableAttribute))
        ObjCInstanceType -> render(attrsAndName.withExplicitNullability(hasNullableAttribute))
        ObjCMetaClassType -> render(attrsAndName.withExplicitNullability(hasNullableAttribute))
    }

private fun String.withAttrsAndName(attrsAndName: String) =
    if (attrsAndName.isEmpty()) this else "$this ${attrsAndName.trimStart()}"

private fun String.withExplicitNullability(hasNullableAttribute: Boolean) =
    if (hasNullableAttribute) this else "$this $objcNonnullAttribute"

private const val objcNonnullAttribute = "_Nonnull"
