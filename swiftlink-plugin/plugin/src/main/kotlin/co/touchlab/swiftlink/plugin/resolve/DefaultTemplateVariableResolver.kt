package co.touchlab.swiftlink.plugin.resolve

import co.touchlab.swiftlink.plugin.transform.ApiTransformResolver
import co.touchlab.swiftlink.plugin.transform.ResolvedApiTransform
import co.touchlab.swiftlink.plugin.transform.parent
import co.touchlab.swiftpack.spec.module.SwiftTemplateVariable
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeParameterReference
import co.touchlab.swiftpack.spi.TemplateVariableResolver
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.types.typeUtil.isBoolean
import org.jetbrains.kotlin.types.typeUtil.isByte
import org.jetbrains.kotlin.types.typeUtil.isDouble
import org.jetbrains.kotlin.types.typeUtil.isFloat
import org.jetbrains.kotlin.types.typeUtil.isInt
import org.jetbrains.kotlin.types.typeUtil.isLong
import org.jetbrains.kotlin.types.typeUtil.isShort
import org.jetbrains.kotlin.types.typeUtil.isUnit

class DefaultTemplateVariableResolver(
    private val moduleName: String,
    private val namer: ObjCExportNamer,
    private val symbolResolver: KotlinSymbolResolver,
    private val transformResolver: ApiTransformResolver,
    templateVariables: Collection<SwiftTemplateVariable<*>>,
): TemplateVariableResolver {

    private val templateVariables = templateVariables.associateBy { it.name }

    override fun resolve(variableName: SwiftTemplateVariable.Name): String {
        return when (val variable = templateVariables[variableName]) {
            is SwiftTemplateVariable.EnumEntryReference -> resolveEnumEntry(variable)
            is SwiftTemplateVariable.FunctionReference -> resolveFunction(variable)
            is SwiftTemplateVariable.PropertyReference -> resolveProperty(variable)
            is SwiftTemplateVariable.TypeReference -> when (val type = variable.type) {
                is KotlinClassReference.Id -> resolveClass(type)
                is KotlinTypeParameterReference.Id -> resolveTypeParameter(type)
            }
            null -> error("Unknown template variable: $variableName!")
        }
    }

    private fun resolveEnumEntry(variable: SwiftTemplateVariable.EnumEntryReference): String {
        val enumEntry = symbolResolver.resolveEnumEntry(variable.enumEntry)
        return namer.getEnumEntrySelector(enumEntry)
    }

    private fun resolveTypeParameter(type: KotlinTypeParameterReference.Id): String {
        val typeParameter = symbolResolver.resolveTypeParameter(type)
        TODO()
    }

    private fun resolveClass(type: KotlinClassReference.Id): String {
        val classDescriptor = symbolResolver.resolveClass(type)
        return with (classDescriptor.defaultType) {
            when {
                isBoolean() -> "Swift.Bool"
                isByte() -> "Swift.Int8"
                isShort() -> "Swift.Int16"
                isInt() -> "Swift.Int32"
                isLong() -> "Swift.Int64"
                isFloat() -> "Swift.Float32"
                isDouble() -> "Swift.Float64"
                KotlinBuiltIns.isString(this) -> "Swift.String"
                isUnit() -> "Swift.Void"
                else -> {
                    val transform = transformResolver.findTypeTransform(classDescriptor)
                    val finalName = transform?.newSwiftName?.newQualifiedName() ?: namer.getClassOrProtocolName(classDescriptor).swiftName
                    "$moduleName.$finalName"
                }
            }
        }
    }

    private fun resolveProperty(variable: SwiftTemplateVariable.PropertyReference): String {
        val property = symbolResolver.resolveProperty(variable.property)
        val transform = transformResolver.findPropertyTransform(property)
        return getParentPrefix(property) + (transform?.newSwiftName ?: namer.getPropertyName(property))
    }

    private fun resolveFunction(variable: SwiftTemplateVariable.FunctionReference): String {
        val function = symbolResolver.resolveFunction(variable.function)
        val transform = transformResolver.findFunctionTransform(function)
        val functionSelector = transform?.newSwiftSelector ?: namer.getSwiftName(function)
        val swiftFunctionReference = if (function.valueParameters.isEmpty()) {
            functionSelector.dropLast(2)
        } else {
            functionSelector
        }
        return getParentPrefix(function) + swiftFunctionReference
    }

    private fun resolveFileClass(fileInPackage: ResolvedApiTransform.Target.File): String {
        val transform = transformResolver.findFileClassTransform(fileInPackage)
        return transform?.newSwiftName?.newQualifiedName() ?: namer.getFileClassName(fileInPackage.file).swiftName
    }

    private fun getParentPrefix(descriptor: CallableMemberDescriptor): String {
        return when (val parent = descriptor.parent) {
            is ResolvedApiTransform.Target.File -> resolveFileClass(parent) + "."
            is ResolvedApiTransform.Target.Type -> ""
        }
    }
}
