@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.enums

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.EnumInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.function.reference
import co.touchlab.skie.plugin.api.model.property.regular.reference
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.packageName
import co.touchlab.skie.plugin.api.model.type.simpleName
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.api.module.stableSpec
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.generator.internal.enums.ObjectiveCBridgeable.addObjcBridgeableImplementation
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.backend.konan.objcexport.doesThrow
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class ExhaustiveEnumsGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    private val reporter: Reporter,
) : BaseGenerator(skieContext, namespaceProvider, configuration) {

    override val isActive: Boolean = true

    override fun execute(descriptorProvider: NativeDescriptorProvider): Unit = with(descriptorProvider) {
        exportedClassDescriptors
            .filter(::shouldGenerateExhaustiveEnums)
            .forEach {
                generate(it)
            }
    }

    context(NativeDescriptorProvider)
    private fun generate(declaration: ClassDescriptor) {
        module.configure {
            declaration.swiftModel.visibility = SwiftModelVisibility.Replaced

            declaration.swiftModel.bridge = declaration.swiftModel.original
        }

        module.generateCode(declaration) {
            val extensionName = declaration.swiftModel.bridge!!.packageName
            val declarationName = declaration.swiftModel.bridge!!.identifier

            val enumDeclaration = TypeSpec.enumBuilder(declarationName)
                .apply {
                    addAttribute("frozen")
                    addModifiers(Modifier.PUBLIC)

                    declaration.enumEntries.forEach {
                        addEnumCase(it.swiftModel.simpleName)
                    }

                    addNestedClassTypeAliases(declaration)

                    addPassthroughForProperties(declaration)

                    addPassthroughForFunctions(declaration)

                    addObjcBridgeableImplementation(declaration)
                }
                .build()

            if (extensionName.isNotEmpty()) {
                addExtension(
                    ExtensionSpec.builder(DeclaredTypeName.qualifiedLocalTypeName(extensionName))
                        .addType(enumDeclaration)
                        .build()
                )
            } else {
                addType(enumDeclaration)
            }
        }
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addNestedClassTypeAliases(declaration: ClassDescriptor) {
        declaration.nestedClasses.forEach {
            addType(
                TypeAliasSpec.builder(it.name.asString(), it.stableSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
        }
    }

    context(NativeDescriptorProvider, SwiftPoetScope)
    private fun TypeSpec.Builder.addPassthroughForProperties(
        declaration: ClassDescriptor,
    ) {
        declaration.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
            .filterIsInstance<PropertyDescriptor>()
            .filter { mapper.isBaseProperty(it) && mapper.isObjCProperty(it) }
            .forEach { property ->
                addProperty(
                    PropertySpec.builder(
                        property.name.asString(),
                        property.type.spec(KotlinTypeSpecUsage.Default)
                    )
                        .addModifiers(Modifier.PUBLIC)
                        .getter(
                            FunctionSpec.getterBuilder()
                                .addStatement(
                                    "return %L(self as _ObjectiveCType).%N",
                                    if (mapper.doesThrow(property.getter!!)) "try " else "",
                                    property.regularPropertySwiftModel.reference,
                                )
                                .build()
                        )
                        .apply {
                            if (property.isVar) {
                                setter(
                                    FunctionSpec.setterBuilder()
                                        .addModifiers(Modifier.NONMUTATING)
                                        .addParameter(
                                            "value",
                                            property.type.spec(KotlinTypeSpecUsage.ParameterType)
                                        )
                                        .addStatement(
                                            "%L(self as _ObjectiveCType).%N = value",
                                            if (mapper.doesThrow(property.setter!!)) "try " else "",
                                            property.regularPropertySwiftModel.reference,
                                        )
                                        .build()
                                )
                            }
                        }
                        .build()
                )
            }
    }

    context(NativeDescriptorProvider, SwiftPoetScope)
    private fun TypeSpec.Builder.addPassthroughForFunctions(
        declaration: ClassDescriptor,
    ) {
        declaration.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<FunctionDescriptor>()
            .filter { mapper.isBaseMethod(it) }
            .forEach { function ->
                if (function.isSuspend) {
                    return@forEach
                }

                addFunction(
                    FunctionSpec.builder(function.swiftModel.identifier)
                        .addModifiers(Modifier.PUBLIC)
                        .apply {
                            function.returnType?.let { returnType ->
                                returns(returnType.spec(KotlinTypeSpecUsage.ReturnType))
                            }
                            function.valueParameters.forEach { parameter ->
                                val parameterTypeSpec = parameter.type.spec(KotlinTypeSpecUsage.ParameterType)
                                addParameter(
                                    ParameterSpec.builder(
                                        parameter.name.asString(),
                                        parameterTypeSpec,
                                    ).build()
                                )
                            }

                            throws(mapper.doesThrow(function))
                        }
                        .addStatement(
                            "return %L(self as _ObjectiveCType).%N(%L)",
                            if (mapper.doesThrow(function)) "try " else "",
                            function.swiftModel.reference,
                            function.valueParameters.map { CodeBlock.of("%N", it.name.asString()) }.joinToCode(", "),
                        )
                        .build()
                )
            }
    }

    private val ClassDescriptor.enumEntries: List<ClassDescriptor>
        get() = DescriptorUtils.getAllDescriptors(this.unsubstitutedInnerClassesScope)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.ENUM_ENTRY }

    private val ClassDescriptor.nestedClasses: List<ClassDescriptor>
        get() = unsubstitutedInnerClassesScope.getDescriptorsFiltered(DescriptorKindFilter.CLASSIFIERS)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.CLASS }

    private fun shouldGenerateExhaustiveEnums(declaration: ClassDescriptor): Boolean {
        return declaration.kind.isEnumClass && declaration.isEnumInteropEnabled && !declaration.belongsToSkieRuntime
    }

    private val ClassDescriptor.isEnumInteropEnabled: Boolean
        get() = getConfiguration(EnumInterop.Enabled)
}
