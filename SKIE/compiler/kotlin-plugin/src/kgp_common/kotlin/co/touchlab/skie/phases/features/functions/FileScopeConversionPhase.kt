package co.touchlab.skie.phases.features.functions

import co.touchlab.skie.configuration.FunctionInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.isAccessibleFromOtherModules
import co.touchlab.skie.sir.element.resolveAsDirectClassSirType
import co.touchlab.skie.sir.element.resolveAsDirectSirClass
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor

// WIP 2 Refactor after SwiftModels
object FileScopeConversionPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        with(ExtensionProvider(sirProvider, swiftModelProvider)) {
            exposedFiles
                .flatMap { it.allCallableMembers }
                .filter { it.isInteropEnabled }
                .forEach {
                    generateCallableDeclarationWrapper(it)
                }
        }
    }

    context(SirPhase.Context)
    private val KotlinCallableMemberSwiftModel.isInteropEnabled: Boolean
        get() = this.descriptor.getConfiguration(FunctionInterop.FileScopeConversion.Enabled)

    context(SirPhase.Context, ExtensionProvider)
    private fun generateCallableDeclarationWrapper(swiftModel: MutableKotlinCallableMemberSwiftModel) {
        when (swiftModel) {
            is MutableKotlinFunctionSwiftModel -> {
                generateFunctionWrapper(swiftModel)
                swiftModel.asyncSwiftModelOrNull?.let { generateFunctionWrapper(it) }
            }
            is MutableKotlinPropertySwiftModel -> generatePropertyWrapper(swiftModel)
        }
    }

    context(SirPhase.Context, ExtensionProvider)
    private fun generateFunctionWrapper(swiftModel: MutableKotlinFunctionSwiftModel) {
        if (swiftModel.descriptor.extensionReceiverParameter != null) {
            generateInterfaceExtensionWrapper(swiftModel)
        } else {
            generateGlobalFunctionWrapper(swiftModel)
        }
    }

    context(SirPhase.Context, ExtensionProvider)
    private fun generatePropertyWrapper(swiftModel: MutableKotlinPropertySwiftModel) {
        when (swiftModel) {
            is MutableKotlinRegularPropertySwiftModel -> generateGlobalPropertyWrapper(swiftModel)
            is MutableKotlinConvertedPropertySwiftModel -> generateInterfaceExtensionWrapper(swiftModel)
        }
    }

    context(SirPhase.Context, ExtensionProvider)
    private fun generateInterfaceExtensionWrapper(swiftModel: MutableKotlinFunctionSwiftModel) {
        val originalFunction = swiftModel.primarySirFunction

        if (!originalFunction.visibility.isAccessibleFromOtherModules) return

        getExtensions(swiftModel).forEach { extension ->
            SirFunction(
                identifier = originalFunction.identifier,
                parent = extension,
                visibility = originalFunction.visibility,
                returnType = originalFunction.returnType,
                attributes = originalFunction.attributes,
                modifiers = originalFunction.modifiers,
                isAsync = originalFunction.isAsync,
                throws = originalFunction.throws,
            ).apply {
                originalFunction.valueParameters.drop(1).map {
                    SirValueParameter(
                        label = it.label,
                        name = it.name,
                        type = it.type,
                    )
                }

                addFunctionDeclarationBodyWithErrorTypeHandling(swiftModel) {
                    addStatement(
                        "return %L%L%T.%N(%L)",
                        if (originalFunction.throws) "try " else "",
                        if (originalFunction.isAsync) "await " else "",
                        originalFunction.memberOwner!!.defaultType.toSwiftPoetTypeName(),
                        originalFunction.reference,
                        (listOf(CodeBlock.of("self")) + valueParameters.map { CodeBlock.of("%N", it.name) }).joinToCode(", "),
                    )
                }

                swiftModel.bridgedSirFunction = this
            }
        }
    }

    context(SirPhase.Context)
    private fun generateGlobalFunctionWrapper(swiftModel: MutableKotlinFunctionSwiftModel) {
        val originalFunction = swiftModel.primarySirFunction

        if (!originalFunction.visibility.isAccessibleFromOtherModules) return

        SirFunction(
            identifier = originalFunction.identifier,
            parent = sirProvider.getFile(swiftModel.owner!!),
            visibility = originalFunction.visibility,
            returnType = originalFunction.returnType,
            attributes = originalFunction.attributes,
            modifiers = originalFunction.modifiers,
            isAsync = originalFunction.isAsync,
            throws = originalFunction.throws,
        ).apply {
            originalFunction.valueParameters.map {
                SirValueParameter(
                    label = it.label,
                    name = it.name,
                    type = it.type,
                )
            }

            addFunctionDeclarationBodyWithErrorTypeHandling(swiftModel) {
                addStatement(
                    "return %L%L%T.%N(%L)",
                    if (originalFunction.throws) "try " else "",
                    if (originalFunction.isAsync) "await " else "",
                    originalFunction.memberOwner!!.defaultType.toSwiftPoetTypeName(),
                    originalFunction.reference,
                    valueParameters.map { CodeBlock.of("%N", it.name) }.joinToCode(", "),
                )
            }

            swiftModel.bridgedSirFunction = this
        }
    }

    context(SirPhase.Context, ExtensionProvider)
    private fun generateInterfaceExtensionWrapper(swiftModel: MutableKotlinConvertedPropertySwiftModel) {
        val originalGetter = swiftModel.getter.primarySirFunction
        val originalSetter = swiftModel.setter?.primarySirFunction?.takeIf { it.visibility.isAccessibleFromOtherModules }

        if (!originalGetter.visibility.isAccessibleFromOtherModules) return

        getExtensions(swiftModel).forEach { extension ->
            SirProperty(
                identifier = originalGetter.identifier,
                parent = extension,
                visibility = originalGetter.visibility,
                type = originalGetter.returnType,
            ).apply {
                SirGetter(
                    attributes = originalGetter.attributes,
                ).apply {
                    addFunctionDeclarationBodyWithErrorTypeHandling(swiftModel.getter) {
                        addStatement(
                            "return %T.%N(self)",
                            originalGetter.memberOwner!!.defaultType.toSwiftPoetTypeName(),
                            originalGetter.reference,
                        )
                    }
                }

                originalSetter?.let {
                    SirSetter(
                        attributes = originalSetter.attributes,
                        modifiers = originalSetter.modifiers,
                    ).apply {
                        addFunctionDeclarationBodyWithErrorTypeHandling(swiftModel.setter!!) {
                            addStatement(
                                "%T.%N(self, %N)",
                                originalSetter.memberOwner!!.defaultType.toSwiftPoetTypeName(),
                                originalSetter.reference,
                                parameterName,
                            )
                        }
                    }
                }

                // WIP 2 should be possible after removing SwiftModels
//                 swiftModel.getter.bridgedSirFunction = this
//                 swiftModel.setter?.bridgedSirFunction = this
            }
        }
    }

    context(SirPhase.Context)
    private fun generateGlobalPropertyWrapper(swiftModel: MutableKotlinRegularPropertySwiftModel) {
        val originalProperty = swiftModel.primarySirProperty

        if (!originalProperty.visibility.isAccessibleFromOtherModules) return

        SirProperty(
            identifier = originalProperty.identifier,
            parent = sirProvider.getFile(swiftModel.owner!!),
            visibility = originalProperty.visibility,
            type = originalProperty.type,
            attributes = originalProperty.attributes,
            modifiers = originalProperty.modifiers,
        ).apply {
            originalProperty.getter?.let { originalGetter ->
                SirGetter(
                    attributes = originalGetter.attributes,
                ).apply {
                    addFunctionDeclarationBodyWithErrorTypeHandling(swiftModel) {
                        addStatement(
                            "return %T.%N",
                            originalProperty.memberOwner!!.defaultType.toSwiftPoetTypeName(),
                            originalProperty.reference,
                        )
                    }
                }
            }

            originalProperty.setter?.let { originalSetter ->
                SirSetter(
                    attributes = originalSetter.attributes,
                    modifiers = originalSetter.modifiers,
                ).apply {
                    addFunctionDeclarationBodyWithErrorTypeHandling(swiftModel) {
                        addStatement(
                            "%T.%N = %N",
                            originalProperty.memberOwner!!.defaultType.toSwiftPoetTypeName(),
                            originalProperty.reference,
                            parameterName,
                        )
                    }
                }
            }

            swiftModel.bridgedSirProperty = this
        }
    }

    private class ExtensionProvider(
        private val sirProvider: SirProvider,
        private val swiftModelScope: SwiftModelScope,
    ) {

        private val cache: MutableMap<SirType, List<SirExtension>> = mutableMapOf()

        fun getExtensions(swiftModel: KotlinCallableMemberSwiftModel): List<SirExtension> {
            val parentType = when (swiftModel) {
                is MutableKotlinFunctionSwiftModel -> swiftModel.kotlinSirFunction.valueParameters.first().type
                is KotlinRegularPropertySwiftModel -> swiftModel.kotlinSirProperty.type
                is KotlinConvertedPropertySwiftModel -> swiftModel.getter.kotlinSirFunction.valueParameters.first().type
                else -> error("Unsupported callable member type: $swiftModel")
            }

            return getExtensions(swiftModel, parentType)
        }

        fun getExtensions(swiftModel: KotlinCallableMemberSwiftModel, parentType: SirType): List<SirExtension> =
            cache.getOrPut(parentType) {
                createExtension(swiftModel, parentType)
            }

        private fun createExtension(swiftModel: KotlinCallableMemberSwiftModel, parentType: SirType): List<SirExtension> {
            val file = with(swiftModelScope) {
                (swiftModel.descriptor.extensionReceiverParameter?.type?.constructor?.declarationDescriptor as? ClassDescriptor)
                    ?.swiftModelOrNull?.let { sirProvider.getFile(it) } ?: sirProvider.getFile(swiftModel.owner!!)
            }

            return when (parentType) {
                is DeclaredSirType -> createNonOptionalExtension(
                    file = file,
                    sirClass = parentType.resolveAsDirectSirClass() ?: return emptyList(),
                ).let(::listOfNotNull)
                is SpecialSirType.Any -> createNonOptionalExtension(
                    file = file,
                    sirClass = sirProvider.sirBuiltins.Foundation.NSObject,
                ).let(::listOfNotNull)
                is NullableSirType -> listOfNotNull(
                    createOptionalExtension(file, parentType),
                    // WIP This should be generated only if there is not a collision
                ) + getExtensions(swiftModel, parentType.type)
                else -> return emptyList()
            }
        }

        private fun createNonOptionalExtension(file: SirFile, sirClass: SirClass): SirExtension? =
            if (sirClass.kind != SirClass.Kind.Protocol) {
                // WIP Non protocol types are not supported yet
                null
            } else {
                SirExtension(
                    classDeclaration = sirClass,
                    parent = file,
                )
            }

        private fun createOptionalExtension(file: SirFile, nullableSirType: NullableSirType): SirExtension? {
            val constraintType = when (val type = nullableSirType.type) {
                is DeclaredSirType -> type.resolveAsDirectClassSirType() ?: return null
                SpecialSirType.Any -> sirProvider.sirBuiltins.Foundation.NSObject.defaultType
                else -> return null
            }

            return SirExtension(
                classDeclaration = sirProvider.sirBuiltins.Swift.Optional,
                parent = file,
            ).apply {
                SirConditionalConstraint(
                    typeParameter = sirProvider.sirBuiltins.Swift.Optional.typeParameters.first(),
                    bounds = listOf(constraintType),
                )
            }
        }
    }
}
