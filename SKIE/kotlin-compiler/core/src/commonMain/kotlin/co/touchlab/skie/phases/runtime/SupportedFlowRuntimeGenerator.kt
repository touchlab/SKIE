package co.touchlab.skie.phases.runtime

import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.bridging.CustomMembersPassthroughGenerator
import co.touchlab.skie.phases.bridging.CustomPassthroughDeclaration
import co.touchlab.skie.phases.bridging.ObjCBridgeableGenerator
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirModality
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.toNullable
import io.outfoxx.swiftpoet.CodeBlock

object SupportedFlowRuntimeGenerator {

    context(SirPhase.Context)
    fun generate(skieSwiftFlowIterator: SirClass) {
        generateBridgeSubscriptionCountWorkaroundFunctions()

        generateSkieSwiftFlowProtocol()
        generateSkieSwiftFlowInternalProtocol()

        val classesForVariants = SupportedFlow.allVariants.associateWith { flowVariant ->
            createSwiftFlowClass(flowVariant)
        }

        classesForVariants.forEach { (flowVariant, sirClass) ->
            sirClass.addSwiftFlowMembers(flowVariant, skieSwiftFlowIterator)
        }
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowProtocol() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieSwiftFlowProtocol").content = """
            public protocol SkieSwiftFlowProtocol<Element>: _Concurrency.AsyncSequence where AsyncIterator == SkieSwiftFlowIterator<Element> { }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowInternalProtocol() {
        // This has currently no use-case, but is kept as it might help with SwiftUI extensions.
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieSwiftFlowInternalProtocol").content = """
            internal protocol SkieSwiftFlowInternalProtocol<Element> {
                associatedtype Element
                associatedtype Delegate: Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.Flow.__Kotlin

                var delegate: Delegate { get }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateBridgeSubscriptionCountWorkaroundFunctions() {
        // If Flow-interop is disabled globally (or even just for `kotlinx.coroutines.core`),
        // `subscriptionCount` property falls back to Kotlin's `StateFlow` protocol.
        // These two functions make sure our Flow runtime code still compiles as expected.
        namespaceProvider.getSkieNamespaceWrittenSourceFile("bridgeSubscriptionCount").content = """
            internal func bridgeSubscriptionCount(_ subscriptionCount: SkieSwiftStateFlow<KotlinInt>) -> SkieSwiftStateFlow<KotlinInt> {
                return subscriptionCount
            }

            internal func bridgeSubscriptionCount(_ subscriptionCount: any Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.StateFlow.__Kotlin) -> SkieSwiftStateFlow<KotlinInt> {
                return SkieSwiftStateFlow(internal: subscriptionCount)
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun createSwiftFlowClass(flowVariant: SupportedFlow.Variant): SirClass {
        return namespaceProvider.getSkieNamespaceFile(flowVariant.swiftSimpleName).run {
            SirClass(
                baseName = flowVariant.swiftSimpleName,
                superTypes = listOf(
                    sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType,
                    sirBuiltins.Skie.SkieSwiftFlowInternalProtocol.defaultType,
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

        addDelegateProperty(flowClass)

        addInternalConstructor(flowClass)

        addPassthroughMembers(flowVariant, elementTypeAlias)

        addObjCBridgeableImplementation(flowVariant.getKotlinKirClass().originalSirClass)

        addMakeAsyncIteratorFunction(asyncIteratorAlias)
    }

    private fun SirClass.addDelegateProperty(
        flowClass: SirClass,
    ) {
        SirProperty(
            identifier = "delegate",
            type = flowClass.defaultType,
            visibility = SirVisibility.Internal,
        )
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
