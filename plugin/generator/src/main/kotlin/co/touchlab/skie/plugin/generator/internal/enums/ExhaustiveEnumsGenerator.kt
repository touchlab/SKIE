@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.enums

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.EnumInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.getAllExposedMembers
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.function.reference
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.reference
import co.touchlab.skie.plugin.api.model.type.translation.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.SwiftTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridgedOrStableSpec
import co.touchlab.skie.plugin.api.model.type.packageName
import co.touchlab.skie.plugin.api.model.type.simpleName
import co.touchlab.skie.plugin.api.model.type.stableSpec
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
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.backend.konan.objcexport.doesThrow
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseProperties
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
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
    private val descriptorProvider: NativeDescriptorProvider,
    private val reporter: Reporter,
) : BaseGenerator(skieContext, namespaceProvider, configuration) {

    override val isActive: Boolean = true

    override fun execute() {
        descriptorProvider.exposedClasses
            .filter(::shouldGenerateExhaustiveEnums)
            .forEach {
                generate(it)
            }
    }

    private fun generate(declaration: ClassDescriptor) {
        if (declaration.enumEntries.isEmpty()) {
            reporter.report(
                Reporter.Severity.Warning,
                "Enum ${declaration.name} has no entries, no Swift enum will be generated. To silence this warning, add @EnumInterop.Disabled above the enum declaration.",
                declaration,
            )
            return
        }

        module.configure {
            declaration.swiftModel.visibility = SwiftModelVisibility.Replaced

            val original = declaration.swiftModel.original
            declaration.swiftModel.bridge = SwiftTypeSwiftModel(
                containingType = original.containingType,
                identifier = declaration.swiftModel.original.identifier,
                isHashable = declaration.enumEntries.isNotEmpty(),
            )
        }

        module.generateCode(declaration) {
            val extensionName = declaration.swiftModel.bridge!!.packageName
            val declarationName = declaration.swiftModel.bridge!!.identifier

            val enumDeclaration = TypeSpec.enumBuilder(declarationName)
                .apply {
                    addAttribute("frozen")
                    addModifiers(Modifier.PUBLIC)
                    if (declaration.enumEntries.isNotEmpty()) {
                        addSuperType(STRING)
                        addSuperType(DeclaredTypeName.typeName("Swift.Hashable"))
                    }

                    declaration.enumEntries.forEach {
                        addEnumCase(it.enumEntrySwiftModel.identifier)
                    }

                    addNestedClassTypeAliases(declaration)

                    addPassthroughForProperties(declaration)

                    addPassthroughForFunctions(declaration)

                    addCompanionObjectPropertyIfNeeded(declaration)

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

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addNestedClassTypeAliases(declaration: ClassDescriptor) {
        declaration.nestedClasses.forEach {
            addType(
                TypeAliasSpec.builder(it.name.asString(), it.stableSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
        }
    }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addPassthroughForProperties(
        declaration: ClassDescriptor,
    ) {
        descriptorProvider.getAllExposedMembers(declaration)
            .filterIsInstance<PropertyDescriptor>()
            // TODO Add support for Converted properties
            .mapNotNull { descriptor -> (descriptor.swiftModel as? KotlinRegularPropertySwiftModel)?.let { descriptor to it } }
            .forEach { (property, swiftModel) ->
                val propertyTypeModel = swiftModel.type
                addProperty(
                    PropertySpec.builder(
                        property.name.asString(),
                        propertyTypeModel.stableSpec,
                    )
                        .addModifiers(Modifier.PUBLIC)
                        .getter(
                            FunctionSpec.getterBuilder()
                                .addStatement(
                                    "return %L(self as _ObjectiveCType).%N",
                                    if (descriptorProvider.mapper.doesThrow(property.getter!!)) "try " else "",
                                    swiftModel.reference,
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
                                            // TODO: This might be a setter parameter, we need to investigate
                                            propertyTypeModel.stableSpec,
                                        )
                                        .addStatement(
                                            "%L(self as _ObjectiveCType).%N = value",
                                            if (descriptorProvider.mapper.doesThrow(property.setter!!)) "try " else "",
                                            swiftModel.reference,
                                        )
                                        .build()
                                )
                            }
                        }
                        .build()
                )
            }
    }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addPassthroughForFunctions(
        declaration: ClassDescriptor,
    ) {
        descriptorProvider.getAllExposedMembers(declaration)
            .filterIsInstance<FunctionDescriptor>()
            // TODO Solve together with interfaces
            .filter { it.name.asString() != "compareTo" }
            .filter {
                    !DescriptorUtils.isMethodOfAny(it)
            }
            .forEach { function ->
                if (function.isSuspend) {
                    return@forEach
                }

                val swiftModel = function.swiftModel
                addFunction(
                    FunctionSpec.builder(swiftModel.identifier)
                        .addModifiers(Modifier.PUBLIC)
                        .apply {
                            returns(swiftModel.returnType.stableSpec)
                            function.valueParameters.forEach { parameter ->
                                val parameterSwiftModel = parameter.swiftModel
                                val parameterType = parameterSwiftModel.type

                                addParameter(
                                    ParameterSpec.builder(
                                        parameterSwiftModel.argumentLabel,
                                        parameterSwiftModel.parameterName,
                                        parameterType.stableSpec,
                                    ).build()
                                )
                            }

                            throws(descriptorProvider.mapper.doesThrow(function))

                            addStatement(
                                "return %L(self as _ObjectiveCType).%N(%L)",
                                if (descriptorProvider.mapper.doesThrow(function)) "try " else "",
                                function.swiftModel.reference,
                                function.valueParameters.map {
                                    CodeBlock.of("%N", it.swiftModel.parameterName)
                                }.joinToCode(", "),
                            )
                        }
                        .build()
                )
            }
    }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addCompanionObjectPropertyIfNeeded(
        declaration: ClassDescriptor,
    ) {
        val companion = declaration.companionObjectDescriptor ?: return

        addProperty(
            PropertySpec.builder(
                "companion",
                companion.swiftModel.bridgedOrStableSpec,
            )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .getter(
                    FunctionSpec.getterBuilder()
                        .addStatement("return _ObjectiveCType.companion")
                        .build()
                )
                .build()
        )
    }

    private val ClassDescriptor.enumEntries: List<ClassDescriptor>
        get() = DescriptorUtils.getAllDescriptors(this.unsubstitutedInnerClassesScope)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.ENUM_ENTRY }

    private val ClassDescriptor.nestedClasses: List<ClassDescriptor>
        get() = unsubstitutedInnerClassesScope.getDescriptorsFiltered(DescriptorKindFilter.CLASSIFIERS)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT }

    private fun shouldGenerateExhaustiveEnums(declaration: ClassDescriptor): Boolean {
        return declaration.kind.isEnumClass && declaration.isEnumInteropEnabled && !declaration.belongsToSkieRuntime
    }

    private val ClassDescriptor.isEnumInteropEnabled: Boolean
        get() = getConfiguration(EnumInterop.Enabled)
}
