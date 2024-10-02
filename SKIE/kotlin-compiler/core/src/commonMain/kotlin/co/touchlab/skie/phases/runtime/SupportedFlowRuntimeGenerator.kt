package co.touchlab.skie.phases.runtime

import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.bridging.CustomMembersPassthroughGenerator
import co.touchlab.skie.phases.bridging.CustomPassthroughDeclaration
import co.touchlab.skie.phases.bridging.ObjCBridgeableGenerator
import co.touchlab.skie.phases.runtime.declarations.SkieSwiftFlowInternalProtocol
import co.touchlab.skie.phases.runtime.declarations.SkieSwiftFlowProtocol
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirModality
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.getTypeParameter
import co.touchlab.skie.sir.element.toConformanceBound
import co.touchlab.skie.sir.element.toEqualityBound
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.toExistential
import co.touchlab.skie.sir.type.toNullable
import io.outfoxx.swiftpoet.CodeBlock

object SupportedFlowRuntimeGenerator {

    context(SirPhase.Context)
    fun generate(skieSwiftFlowIterator: SirClass) {
        val skieSwiftFlowProtocol = generateSkieSwiftFlowProtocol(skieSwiftFlowIterator)
        val skieSwiftFlowInternalProtocol = generateSkieSwiftFlowInternalProtocol()

        val classesForVariants = SupportedFlow.allVariants.associateWith { flowVariant ->
            createSwiftFlowClass(flowVariant, skieSwiftFlowProtocol, skieSwiftFlowInternalProtocol)
        }

        classesForVariants.forEach { (flowVariant, sirClass) ->
            sirClass.addSwiftFlowMembers(flowVariant, skieSwiftFlowIterator, skieSwiftFlowInternalProtocol)
        }

        // Needs to happen after `createSwiftFlowClass` for StateFlow is run.
        generateBridgeSubscriptionCountWorkaroundFunctions()
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowProtocol(skieSwiftFlowIterator: SirClass): SkieSwiftFlowProtocol {
        namespaceProvider.getSkieNamespaceFile("SkieSwiftFlowProtocol").apply {
            SirClass(
                baseName = "SkieSwiftFlowProtocol",
                kind = SirClass.Kind.Protocol,
                superTypes = listOf(sirBuiltins._Concurrency.AsyncSequence.defaultType, sirBuiltins.Swift.AnyObject.defaultType),
            ).apply {
                val elementTypeParameter = SirTypeParameter(
                    name = "Element",
                    isPrimaryAssociatedType = true,
                )

                SirConditionalConstraint(
                    sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("AsyncIterator"),
                    bounds = listOf(
                        skieSwiftFlowIterator.toType(
                            elementTypeParameter.toTypeParameterUsage(),
                        ).toEqualityBound(),
                    )
                )

                return SkieSwiftFlowProtocol(
                    self = this,
                )
            }
        }
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowInternalProtocol(): SkieSwiftFlowInternalProtocol {
        // This has currently no use-case, but is kept as it might help with SwiftUI extensions.
        namespaceProvider.getSkieNamespaceFile("SkieSwiftFlowInternalProtocol").apply {
            SirClass(
                baseName = "SkieSwiftFlowInternalProtocol",
                kind = SirClass.Kind.Protocol,
                visibility = SirVisibility.Internal,
            ).apply {
                SirTypeParameter(
                    name = "Element",
                    isPrimaryAssociatedType = true,
                )

                val delegateTypeParameter = SirTypeParameter(
                    name = "Delegate",
                    bounds = listOf(
                        kirProvider.getClassByFqName("kotlinx.coroutines.flow.Flow").primarySirClass.defaultType.toConformanceBound(),
                    ),
                )

                val delegateProperty = SirProperty(
                    identifier = "delegate",
                    type = delegateTypeParameter.toTypeParameterUsage(),
                ).apply {
                    SirGetter()
                }

                return SkieSwiftFlowInternalProtocol(
                    self = this,
                    delegateProperty = delegateProperty,
                )
            }
        }
    }

    context(SirPhase.Context)
    private fun generateBridgeSubscriptionCountWorkaroundFunctions() {
        // If Flow-interop is disabled globally (or even just for `kotlinx.coroutines.core`),
        // `subscriptionCount` property falls back to Kotlin's `StateFlow` protocol.
        // These two functions make sure our Flow runtime code still compiles as expected.
        namespaceProvider.getSkieNamespaceFile("bridgeSubscriptionCount").apply {
            val skieSwiftStateFlowOfInt = sirProvider.getClassByFqName(SirFqName(sirProvider.skieModule, "SkieSwiftStateFlow"))
                .toType(kirBuiltins.nsNumberDeclarationsByFqName["kotlin.Int"]!!.originalSirClass.defaultType)

            SirSimpleFunction(
                identifier = "bridgeSubscriptionCount",
                visibility = SirVisibility.Internal,
                returnType = skieSwiftStateFlowOfInt,
            ).apply {
                SirValueParameter(
                    label = "_",
                    name = "subscriptionCount",
                    type = skieSwiftStateFlowOfInt,
                )

                bodyBuilder.add {
                    +"return subscriptionCount"
                }
            }

            SirSimpleFunction(
                identifier = "bridgeSubscriptionCount",
                visibility = SirVisibility.Internal,
                returnType = skieSwiftStateFlowOfInt,
            ).apply {
                SirValueParameter(
                    label = "_",
                    name = "subscriptionCount",
                    type = kirProvider.getClassByFqName("kotlinx.coroutines.flow.StateFlow").primarySirClass.defaultType.toExistential(),
                )

                bodyBuilder.add {
                    addStatement("return %T(internal: subscriptionCount)", skieSwiftStateFlowOfInt.evaluate().swiftPoetTypeName)
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun createSwiftFlowClass(
        flowVariant: SupportedFlow.Variant,
        skieSwiftFlowProtocol: SkieSwiftFlowProtocol,
        skieSwiftFlowInternalProtocol: SkieSwiftFlowInternalProtocol,
    ): SirClass {
        return namespaceProvider.getSkieNamespaceFile(flowVariant.swiftSimpleName).run {
            SirClass(
                baseName = flowVariant.swiftSimpleName,
                superTypes = listOf(
                    skieSwiftFlowProtocol.self.defaultType,
                    skieSwiftFlowInternalProtocol.self.defaultType,
                    sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
                ),
                modality = SirModality.Final,
            )
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addSwiftFlowMembers(
        flowVariant: SupportedFlow.Variant,
        skieSwiftFlowIterator: SirClass,
        skieSwiftFlowInternalProtocol: SkieSwiftFlowInternalProtocol,
    ) {
        val flowKirClass = flowVariant.getCoroutinesKirClass()
        val flowClass = flowKirClass.originalSirClass

        val tParameter = SirTypeParameter("T")
        val elementType = tParameter.toTypeParameterUsage().toNullable(flowVariant is SupportedFlow.Variant.Optional)

        val asyncIteratorAlias = SirTypeAlias("AsyncIterator") {
            skieSwiftFlowIterator.toType(elementType)
        }

        val elementTypeAlias = SirTypeAlias("Element") {
            elementType
        }

        addDelegateProperty(flowClass, skieSwiftFlowInternalProtocol)

        addInternalConstructor(flowClass)

        addPassthroughMembers(flowVariant, elementTypeAlias)

        addObjCBridgeableImplementation(flowVariant.getKotlinKirClass().originalSirClass)

        addMakeAsyncIteratorFunction(asyncIteratorAlias)
    }

    private fun SirClass.addDelegateProperty(
        flowClass: SirClass,
        skieSwiftFlowInternalProtocol: SkieSwiftFlowInternalProtocol,
    ) {
        SirProperty(
            identifier = "delegate",
            type = flowClass.defaultType,
            visibility = SirVisibility.Internal,
        ).apply {
            addOverride(skieSwiftFlowInternalProtocol.delegateProperty)
        }
    }

    private fun SirClass.addInternalConstructor(
        flowClass: SirClass,
    ) {
        SirConstructor(
            visibility = SirVisibility.Internal,
        ).apply {
            SirValueParameter(
                name = "flow",
                label = "internal",
                type = flowClass.defaultType,
            )

            bodyBuilder.add {
                addCode("delegate = flow")
            }
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addPassthroughMembers(
        flowVariant: SupportedFlow.Variant,
        elementTypeAlias: SirTypeAlias,
    ) {
        CustomMembersPassthroughGenerator.generatePassthroughForDeclarations(
            targetBridge = this,
            declarations = flowVariant.kind.passthroughDeclarations(elementTypeAlias.type),
            delegateAccessor = CodeBlock.of("delegate"),
        )
    }

    context(SirPhase.Context)
    private fun SirClass.addObjCBridgeableImplementation(bridgedClass: SirClass) {
        ObjCBridgeableGenerator.addObjcBridgeableImplementation(
            target = this,
            bridgedType = bridgedClass.toType(sirBuiltins.Swift.AnyObject.defaultType),
            bridgeToObjectiveC = {
                addStatement("return ${bridgedClass.fqName}(delegate)")
            },
            bridgeFromObjectiveC = {
                addStatement("return .init(internal: source)")
            },
        )
    }

    private fun SirClass.addMakeAsyncIteratorFunction(asyncIteratorAlias: SirTypeAlias) {
        SirSimpleFunction(
            identifier = "makeAsyncIterator",
            returnType = asyncIteratorAlias.type,
        ).apply {
            bodyBuilder.add {
                addCode("return SkieSwiftFlowIterator(flow: delegate)")
            }
        }
    }

    context(SirPhase.Context)
    private fun SupportedFlow.passthroughDeclarations(elementType: SirType): List<CustomPassthroughDeclaration> {
        val directParents = when (this) {
            SupportedFlow.Flow -> emptyList()
            SupportedFlow.SharedFlow -> listOf(SupportedFlow.Flow)
            SupportedFlow.MutableSharedFlow -> listOf(SupportedFlow.SharedFlow)
            SupportedFlow.StateFlow -> listOf(SupportedFlow.SharedFlow)
            SupportedFlow.MutableStateFlow -> listOf(SupportedFlow.StateFlow, SupportedFlow.MutableSharedFlow)
        }
        val parentDeclarations = directParents.flatMap {
            it.passthroughDeclarations(elementType)
        }.distinct()

        val declarations = when (this) {
            SupportedFlow.Flow -> emptyList()
            SupportedFlow.SharedFlow -> listOf(
                CustomPassthroughDeclaration.Property(
                    identifier = "replayCache",
                    type = sirBuiltins.Swift.Array.toType(elementType),
                    transformGetter = {
                        CodeBlock.of("%L as! [%T]", it, elementType.evaluate().swiftPoetTypeName)
                    },
                ),
            )
            SupportedFlow.MutableSharedFlow -> listOf(
                CustomPassthroughDeclaration.Property(
                    identifier = "subscriptionCount",
                    type = sirProvider.getClassByFqName(SirFqName(sirProvider.skieModule, "SkieSwiftStateFlow"))
                        .toType(kirBuiltins.nsNumberDeclarationsByFqName["kotlin.Int"]!!.originalSirClass.defaultType),
                    transformGetter = {
                        CodeBlock.of("bridgeSubscriptionCount(%L)", it)
                    },
                ),
                CustomPassthroughDeclaration.SimpleFunction(
                    identifier = "emit",
                    returnType = sirBuiltins.Swift.Void.defaultType,
                    isAsync = true,
                    throws = true,
                    valueParameters = listOf(
                        CustomPassthroughDeclaration.SimpleFunction.ValueParameter(
                            name = "value",
                            type = elementType,
                        ),
                    ),
                ),
                CustomPassthroughDeclaration.SimpleFunction(
                    identifier = "tryEmit",
                    returnType = sirBuiltins.Swift.Bool.defaultType,
                    valueParameters = listOf(
                        CustomPassthroughDeclaration.SimpleFunction.ValueParameter(
                            name = "value",
                            type = elementType,
                        ),
                    ),
                ),
                CustomPassthroughDeclaration.SimpleFunction(
                    identifier = "resetReplayCache",
                    returnType = sirBuiltins.Swift.Void.defaultType,
                ),
            )
            SupportedFlow.StateFlow -> listOf(
                CustomPassthroughDeclaration.Property(
                    identifier = "value",
                    type = elementType,
                    transformGetter = {
                        CodeBlock.of("%L as! %T", it, elementType.evaluate().swiftPoetTypeName)
                    },
                ),
            )
            SupportedFlow.MutableStateFlow -> listOf(
                CustomPassthroughDeclaration.Property(
                    identifier = "value",
                    type = elementType,
                    transformGetter = {
                        CodeBlock.of("%L as! %T", it, elementType.evaluate().swiftPoetTypeName)
                    },
                    setter = CustomPassthroughDeclaration.Property.Setter.SimpleFunction(
                        identifier = "setValue",
                    ),
                ),
                CustomPassthroughDeclaration.SimpleFunction(
                    identifier = "compareAndSet",
                    returnType = sirBuiltins.Swift.Bool.defaultType,
                    valueParameters = listOf(
                        CustomPassthroughDeclaration.SimpleFunction.ValueParameter(
                            name = "expect",
                            type = elementType,
                        ),
                        CustomPassthroughDeclaration.SimpleFunction.ValueParameter(
                            name = "update",
                            type = elementType,
                        ),
                    ),
                ),
            )
        }

        return (declarations + parentDeclarations).distinctBy { declaration ->
            when (declaration) {
                is CustomPassthroughDeclaration.Property -> declaration.identifier
                is CustomPassthroughDeclaration.SimpleFunction -> declaration.identifier
            }
        }
    }
}
