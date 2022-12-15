package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.DataStruct
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.types.KotlinType

class DataStructDeclarationChecker(
    override val configuration: Configuration,
) : DeclarationChecker, ConfigurationContainer {

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        val trace = context.trace

        when (descriptor) {
            is ConstructorDescriptor -> if (descriptor.isPrimary && descriptor.constructedClass.getConfiguration(DataStruct.Enabled)) {
                for ((index, parameter) in descriptor.valueParameters.withIndex()) {
                    if (!isSupportedBuiltin(parameter.type) && !isSupportedDataStruct(parameter.type)) {
                        val parameterDeclaration = (declaration as? KtPrimaryConstructor)?.valueParameters?.getOrNull(index) ?: declaration
                        trace.report(DataStructErrors.UNSUPPORTED_TYPE.on(parameterDeclaration, parameter.type, parameter))
                    }
                }
            }
            is ClassDescriptor -> if (!descriptor.isData && descriptor.getConfiguration(DataStruct.Enabled)) {
                trace.report(DataStructErrors.DATA_STRUCT_NOT_DATA_CLASS.on(declaration, descriptor))
            }
        }
    }

    private fun isSupportedBuiltin(type: KotlinType): Boolean {
        val supportedBuiltins = listOf<(KotlinType) -> Boolean>(
            KotlinBuiltIns::isBoolean,
            KotlinBuiltIns::isByte,
            KotlinBuiltIns::isShort,
            KotlinBuiltIns::isInt,
            KotlinBuiltIns::isLong,
            KotlinBuiltIns::isFloat,
            KotlinBuiltIns::isDouble,
            KotlinBuiltIns::isString,
        )

        return when {
            KotlinBuiltIns.isArray(type) -> {
                isSupportedBuiltin(type.arguments.single().type)
            }
            KotlinBuiltIns.isPrimitiveArray(type) -> {
                // TODO: Check the primitive type for support?
                true
            }
            KotlinBuiltIns.isListOrNullableList(type) -> {
                isSupportedBuiltin(type.arguments.single().type)
            }
            else -> {
                supportedBuiltins.any { it(type) }
            }
        }
    }

    private fun isSupportedDataStruct(type: KotlinType): Boolean = when {
        KotlinBuiltIns.isArray(type) -> {
            isSupportedDataStruct(type.arguments.single().type)
        }
        KotlinBuiltIns.isListOrNullableList(type) -> {
            isSupportedDataStruct(type.arguments.single().type)
        }
        else -> {
            type.constructor.declarationDescriptor?.getConfiguration(DataStruct.Enabled) ?: false
        }
    }
}
