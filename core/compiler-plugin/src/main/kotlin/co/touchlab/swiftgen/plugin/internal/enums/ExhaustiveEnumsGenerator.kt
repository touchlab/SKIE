package co.touchlab.swiftgen.plugin.internal.enums

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
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
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class ExhaustiveEnumsGenerator(
    swiftFileBuilderFactory: SwiftFileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
) : BaseGenerator(swiftFileBuilderFactory, namespaceProvider, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider): Unit = with(descriptorProvider) {
        descriptorProvider.classDescriptors.forEach {
            generate(it)
        }
    }

    private fun DescriptorProvider.generate(declaration: ClassDescriptor) {
        if (!shouldGenerateExhaustiveEnums(declaration)) {
            return
        }

        generateCode(declaration) {
            with(swiftPackModuleBuilder) {
                val declarationReference = declaration.reference()
                declarationReference.applyTransform {
                    hide()
                    bridge(declarationReference.typeName)
                }

                val declaredCases = declaration.enumEntries.map { it.enumEntryReference() }
                addType(
                    TypeSpec.enumBuilder(declarationReference.typeName)
                        .apply {
                            addAttribute("frozen")
                            addModifiers(Modifier.PUBLIC)
                            addSuperType(DeclaredTypeName("Swift", "_ObjectiveCBridgeable"))

                            declaredCases.forEach {
                                addEnumCase(it.swiftReference().name)
                            }

                            declaration.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
                                .filterIsInstance<PropertyDescriptor>()
                                .filter { mapper.isBaseProperty(it) }
                                .forEach { property ->
                                    addProperty(
                                        PropertySpec.builder(property.name.asString(), property.type.topLevelSwiftReference())
                                            .addModifiers(Modifier.PUBLIC)
                                            .getter(
                                                FunctionSpec.getterBuilder()
                                                    .addStatement(
                                                        "return %L(self as _ObjectiveCType).%N",
                                                        if (mapper.doesThrow(property.getter!!)) "try " else "",
                                                        property.reference().swiftReference(),
                                                    )
                                                    .build()
                                            )
                                            .apply {
                                                if (property.isVar) {
                                                    setter(
                                                        FunctionSpec.setterBuilder()
                                                            .addModifiers(Modifier.NONMUTATING)
                                                            .addParameter("value", property.type.topLevelSwiftReference())
                                                            .addStatement(
                                                                "%L(self as _ObjectiveCType).%N = value",
                                                                if (mapper.doesThrow(property.setter!!)) "try " else "",
                                                                property.reference().swiftReference(),
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
                                .filter { mapper.isBaseMethod(it) && !it.isSuspend }
                                .forEach { function ->
                                    addFunction(
                                        FunctionSpec.builder(function.name.asString())
                                            .addModifiers(Modifier.PUBLIC)
                                            .apply {
                                                function.returnType?.let { returnType ->
                                                    returns(returnType.topLevelSwiftReference())
                                                }
                                                function.valueParameters.forEach { parameter ->
                                                    addParameter(
                                                        parameter.name.asString(),
                                                        parameter.type.topLevelSwiftReference()
                                                    )
                                                }

                                                throws(mapper.doesThrow(function))
                                            }
                                            .addStatement(
                                                "return %L(self as _ObjectiveCType).%N(%L)",
                                                if (mapper.doesThrow(function)) "try " else "",
                                                function.reference().swiftReference(),
                                                function.valueParameters.map { CodeBlock.of("%N", it.name.asString()) }.joinToCode(", "),
                                            )
                                            .build()
                                    )
                                }

                            addType(
                                TypeAliasSpec.builder("_ObjectiveCType", declarationReference.swiftReference())
                                    .addModifiers(Modifier.PUBLIC)
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_bridgeToObjectiveC")
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(declarationReference.swiftReference())
                                    .addCode(
                                        CodeBlock.builder()
                                            .beginControlFlow("switch", "self")
                                            .add(
                                                declaredCases.map {
                                                    CodeBlock.of(
                                                        "case .%N: return %T.%N",
                                                        it.swiftReference(),
                                                        declarationReference.swiftReference(),
                                                        it.swiftReference()
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
                                    .addParameter("_", "source", declarationReference.swiftReference())
                                    .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                                    .addStatement("result = fromObjectiveC(source)")
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference())
                                    .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                                    .addStatement("result = fromObjectiveC(source)")
                                    .addStatement("return true")
                                    .returns(BOOL)
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference().makeOptional())
                                    .addStatement("return fromObjectiveC(source)")
                                    .returns(SelfTypeName.INSTANCE)
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("fromObjectiveC")
                                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference().makeOptional())
                                    .addCode(
                                        CodeBlock.builder()
                                            .beginControlFlow("switch", "source")
                                            .add(
                                                declaredCases.map {
                                                    CodeBlock.of("case .%N?: return .%N", it.swiftReference(), it.swiftReference())
                                                }.joinToCode("\n", suffix = "\n")
                                            )
                                            .addStatement("default: fatalError(\"Couldn't map value of \\(String(describing: source)) to ${declarationReference.typeName}\")")
                                            .endControlFlow("switch")
                                            .build()
                                    )
                                    .returns(SelfTypeName.INSTANCE)
                                    .build()
                            )
                        }
                        .build()
                )
            }
        }
    }

    private val ClassDescriptor.enumEntries: List<ClassDescriptor>
        get() = DescriptorUtils.getAllDescriptors(this.unsubstitutedInnerClassesScope)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.ENUM_ENTRY }

    private fun shouldGenerateExhaustiveEnums(declaration: ClassDescriptor): Boolean {
        return declaration.kind.isEnumClass
    }
}
