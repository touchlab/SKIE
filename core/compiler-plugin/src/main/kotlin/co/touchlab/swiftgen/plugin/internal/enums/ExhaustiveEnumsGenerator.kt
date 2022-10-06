package co.touchlab.swiftgen.plugin.internal.enums

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftpack.spec.module.ApiTransform
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass
import org.jetbrains.kotlin.ir.expressions.typeParametersCount
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class ExhaustiveEnumsGenerator(
    swiftFileBuilderFactory: SwiftFileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    private val reporter: Reporter,
) : BaseGenerator(swiftFileBuilderFactory, namespaceProvider, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider): Unit = with(descriptorProvider) {
        descriptorProvider.classDescriptors
            .filter { it.getConfiguration(ConfigurationKeys.ExperimentalFeatures.Enabled) }
            .forEach {
                generate(it)
            }
    }

    private fun DescriptorProvider.generate(declaration: ClassDescriptor) {
        if (!shouldGenerateExhaustiveEnums(declaration)) {
            return
        }

        generateCode(declaration) {
            with(swiftPackModuleBuilder) {
                val declarationReference = declaration.classReference()
                val declaredCases = declaration.enumEntries.map { it.enumEntryReference() }
                val enumDeclaration = TypeSpec.enumBuilder(declaration.name.asString())
                    .apply {
                        addAttribute("frozen")
                        addModifiers(Modifier.PUBLIC)
                        addSuperType(DeclaredTypeName("Swift", "_ObjectiveCBridgeable"))

                        declaredCases.forEach {
                            addEnumCase(it.swiftTemplateVariable().name)
                        }

                        declaration.nestedClasses.forEach {
                            addType(
                                TypeAliasSpec.builder(it.name.asString(), it.classReference().swiftTemplateVariable())
                                    .addModifiers(Modifier.PUBLIC)
                                    .build()
                            )
                        }

                        declaration.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                            .filterIsInstance<PropertyDescriptor>()
                            .filter { mapper.isBaseProperty(it) && mapper.isObjCProperty(it) }
                            .forEach { property ->
                                addProperty(
                                    PropertySpec.builder(property.name.asString(), property.type.reference().swiftTemplateVariable())
                                        .addModifiers(Modifier.PUBLIC)
                                        .getter(
                                            FunctionSpec.getterBuilder()
                                                .addStatement(
                                                    "return %L(self as _ObjectiveCType).%N",
                                                    if (mapper.doesThrow(property.getter!!)) "try " else "",
                                                    property.reference().swiftTemplateVariable(),
                                                )
                                                .build()
                                        )
                                        .apply {
                                            if (property.isVar) {
                                                setter(
                                                    FunctionSpec.setterBuilder()
                                                        .addModifiers(Modifier.NONMUTATING)
                                                        .addParameter("value", property.type.reference().swiftTemplateVariable())
                                                        .addStatement(
                                                            "%L(self as _ObjectiveCType).%N = value",
                                                            if (mapper.doesThrow(property.setter!!)) "try " else "",
                                                            property.reference().swiftTemplateVariable(),
                                                        )
                                                        .build()
                                                )
                                            }
                                        }
                                        .build()
                                )
                            }

                        declaration.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
                            .filterIsInstance<FunctionDescriptor>()
                            .filter { mapper.isBaseMethod(it) }
                            .forEach { function ->
                                if (function.isSuspend) {
                                    reporter.report(
                                        Reporter.Severity.Warning,
                                        "Exhaustive enums do not support bridging of suspend functions, skipping.",
                                        function
                                    )
                                    return@forEach
                                }

                                if (function.typeParametersCount > 0) {
                                    reporter.report(
                                        Reporter.Severity.Warning,
                                        "Exhaustive enums do not support bridging of generic functions, skipping.",
                                        function
                                    )
                                    return@forEach
                                }

                                addFunction(
                                    FunctionSpec.builder(function.name.asString())
                                        .addModifiers(Modifier.PUBLIC)
                                        .apply {
                                            function.returnType?.let { returnType ->
                                                returns(returnType.reference().swiftTemplateVariable())
                                            }
                                            function.valueParameters.forEach { parameter ->
                                                addParameter(
                                                    parameter.name.asString(),
                                                    parameter.type.reference().swiftTemplateVariable(),
                                                )
                                            }

                                            throws(mapper.doesThrow(function))
                                        }
                                        .addStatement(
                                            "return %L(self as _ObjectiveCType).%N(%L)",
                                            if (mapper.doesThrow(function)) "try " else "",
                                            function.reference().swiftTemplateVariable(),
                                            function.valueParameters.map { CodeBlock.of("%N", it.name.asString()) }.joinToCode(", "),
                                        )
                                        .build()
                                )
                            }

                        addType(
                            TypeAliasSpec.builder("_ObjectiveCType", declarationReference.swiftTemplateVariable())
                                .addModifiers(Modifier.PUBLIC)
                                .build()
                        )

                        addFunction(
                            FunctionSpec.builder("_bridgeToObjectiveC")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(declarationReference.swiftTemplateVariable())
                                .addCode(
                                    CodeBlock.builder()
                                        .beginControlFlow("switch", "self")
                                        .add(
                                            declaredCases.map {
                                                CodeBlock.of(
                                                    "case .%N: return %T.%N",
                                                    it.swiftTemplateVariable(),
                                                    declarationReference.swiftTemplateVariable(),
                                                    it.swiftTemplateVariable()
                                                )
                                            }.joinToCode("\n")
                                        )
                                        .endControlFlow("switch")
                                        .build()
                                )
                                .build()
                        )

                        addFunction(
                            FunctionSpec.builder("_forceBridgeFromObjectiveC")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter("_", "source", declarationReference.swiftTemplateVariable())
                                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                                .addStatement("result = fromObjectiveC(source)")
                                .build()
                        )

                        addFunction(
                            FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter("_", "source", declarationReference.swiftTemplateVariable())
                                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                                .addStatement("result = fromObjectiveC(source)")
                                .addStatement("return true")
                                .returns(BOOL)
                                .build()
                        )

                        addFunction(
                            FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter("_", "source", declarationReference.swiftTemplateVariable().makeOptional())
                                .addStatement("return fromObjectiveC(source)")
                                .returns(SelfTypeName.INSTANCE)
                                .build()
                        )

                        addFunction(
                            FunctionSpec.builder("fromObjectiveC")
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .addParameter("_", "source", declarationReference.swiftTemplateVariable().makeOptional())
                                .addCode(
                                    CodeBlock.builder()
                                        .beginControlFlow("switch", "source")
                                        .add(
                                            declaredCases.map {
                                                CodeBlock.of(
                                                    "case .%N?: return .%N",
                                                    it.swiftTemplateVariable(),
                                                    it.swiftTemplateVariable()
                                                )
                                            }.joinToCode("\n", suffix = "\n")
                                        )
                                        .addStatement("default: fatalError(\"Couldn't map value of \\(String(describing: source)) to ${declaration.name.asString()}\")")
                                        .endControlFlow("switch")
                                        .build()
                                )
                                .returns(SelfTypeName.INSTANCE)
                                .build()
                        )
                    }
                    .build()

                val bridge = when (val parent = declaration.containingDeclaration) {
                    is ClassDescriptor -> {
                        addExtension(
                            ExtensionSpec.builder(parent.classReference().swiftTemplateVariable())
                                .addType(enumDeclaration)
                                .build()
                        )
                        ApiTransform.TypeTransform.Bridge.Relative(parent.classReference().id, enumDeclaration.name)
                    }
                    is PackageFragmentDescriptor -> {
                        addType(enumDeclaration)
                        ApiTransform.TypeTransform.Bridge.Absolute(enumDeclaration.name)
                    }
                    else -> error("Unexpected parent type: $parent")
                }

                declarationReference.applyTransform {
                    hide()
                    bridge(bridge)
                }
            }
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
        return declaration.kind.isEnumClass && declaration.isEnumInteropEnabled
    }

    private val ClassDescriptor.isEnumInteropEnabled: Boolean
        get() = getConfiguration(ConfigurationKeys.EnumInterop.Enabled)
}
