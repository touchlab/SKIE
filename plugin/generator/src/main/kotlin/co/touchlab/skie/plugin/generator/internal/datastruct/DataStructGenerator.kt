package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.DataStruct
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.util.typeAliasSpec
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileMemberSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class DataStructGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    private val reporter: Reporter,
) : BaseGenerator(skieContext, namespaceProvider, configuration) {

    class DataStructHelpers(val helpers: MutableSet<FileMemberSpec> = mutableSetOf())

    override val isActive: Boolean = true

    override fun execute(descriptorProvider: DescriptorProvider): Unit = with(descriptorProvider) {
        exportedClassDescriptors
            .filter {
                it.getConfiguration(DataStruct.Enabled) && it.isData && !it.belongsToSkieRuntime
            }
            .forEach {
                generate(it)
            }
    }

    context(DescriptorProvider)
        private fun generate(declaration: ClassDescriptor) {
        val primaryConstructor = declaration.unsubstitutedPrimaryConstructor ?: return

        module.generateCode(declaration) {
            val parametersWithMappings = primaryConstructor.valueParameters.mapNotNull { parameter ->
                val backingProperty = with(declaration) { parameter.backingProperty }
                val mapping = DataStructTypeMapper.supportedBuiltins.firstNotNullOfOrNull {
                    it.provideMapping(backingProperty, parameter)
                } ?: run {
                    reporter.report(Reporter.Severity.Error, "Unsupported parameter type ${parameter.type}.", parameter)
                    return@mapNotNull null
                }

                parameter to mapping
            }

            if (parametersWithMappings.count() != primaryConstructor.valueParameters.count()) {
                reporter.report(
                    Reporter.Severity.Error,
                    "Class ${declaration.name} couldn't be converted to Swift struct as it has unsupported property types.",
                    declaration,
                )
                return@generateCode
            }

            val bridge = TypeSpec.structBuilder("Bridge")
                .addProperties(
                    parametersWithMappings.map { (parameter, mapping) ->
                        PropertySpec.varBuilder(
                            name = parameter.name.asString(),
                            type = mapping.swiftTypeName,
                            Modifier.PUBLIC,
                        ).build()
                    }
                )
                .addFunction(
                    FunctionSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameters(
                            parametersWithMappings.map { (parameter, mapping) ->
                                ParameterSpec.builder(
                                    parameterName = parameter.name.asString(),
                                    type = mapping.swiftTypeName,
                                ).build()
                            }
                        )
                        .apply {
                            parametersWithMappings.map { (parameter, _) ->
                                addStatement("self.%N = %N", parameter.name.asString(), parameter.name.asString())
                            }
                        }
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("unbridged", declaration.typeAliasSpec, Modifier.PUBLIC)
                        .getter(
                            FunctionSpec.getterBuilder()
                                .addCode(
                                    "%T(%L)\n", declaration.typeAliasSpec,
                                    parametersWithMappings.map { (parameter, mapping) ->
                                        CodeBlock.of(
                                            "%N: %L",
                                            parameter.name.asString(),
                                            mapping.swiftToKotlinMapping ?: parameter.name.asString()
                                        )
                                    }.joinToCode()
                                )
                                .build()
                        )
                        .build()
                )
                .build()

            addExtension(
                ExtensionSpec.builder(declaration.typeAliasSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .addProperty(
                        PropertySpec.builder("bridged", declaration.typeAliasSpec.nestedType("Bridge"))
                            .getter(
                                FunctionSpec.getterBuilder()
                                    .addCode(
                                        "%T(%L)\n", declaration.typeAliasSpec.nestedType("Bridge"),
                                        parametersWithMappings.map { (parameter, mapping) ->
                                            CodeBlock.of(
                                                "%N: %L",
                                                parameter.name.asString(),
                                                mapping.kotlinToSwiftMapping ?: parameter.name.asString()
                                            )
                                        }.joinToCode()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addType(bridge)
                    .build()
            )

            parametersWithMappings.forEach { (_, mapping) ->
                mapping.additionalSingletonDeclarations.forEach { helper ->
                    module.file("DataStructHelpers") {
                        val helpersTag = tags.getOrPut(DataStructHelpers::class) { DataStructHelpers() } as DataStructHelpers
                        if (!helpersTag.helpers.contains(helper)) {
                            helpersTag.helpers.add(helper)
                            addMember(helper)
                        }
                    }
                }
            }
        }
    }

    context(ClassDescriptor)
        private val ValueParameterDescriptor.backingProperty: PropertyDescriptor
        get() {
            return unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.VARIABLES) {
                it == this@backingProperty.name
            }.single() as PropertyDescriptor
        }
}
