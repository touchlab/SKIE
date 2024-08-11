package co.touchlab.skie.phases.runtime

import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirTypeParameterParent
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.element.getTypeParameter
import co.touchlab.skie.sir.element.toConformanceBound
import co.touchlab.skie.sir.element.toEqualityBound
import co.touchlab.skie.sir.type.LambdaSirType
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.TupleSirType
import co.touchlab.skie.sir.type.toExistential
import co.touchlab.skie.sir.type.toNullable
import co.touchlab.skie.sir.type.toOpaque
import org.intellij.lang.annotations.Language

object SwiftUIFlowObservingGenerator {
    data class SkieSwiftFlowWithInitialValue(
        val self: SirClass,
        val flowProperty: SirProperty,
        val initialValueProperty: SirProperty,
        val elementTypeParameter: SirTypeParameter,
    )

    data class Observing(
        val self: SirClass,
        val valuesTypeParameter: SirTypeParameter,
        val initialContentTypeParameter: SirTypeParameter,
        val contentTypeParameter: SirTypeParameter,
    )

    private val availability = "available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *)"
    private val maxFlowsOverload = 5

    context(SirPhase.Context)
    fun generate() {
        generateCollectFlowModifiers()
        val skieSwiftFlowWithInitialValue = generateSkieSwiftFlowWithInitialValue()
        val skieSwiftFlowObserver = generateSkieSwiftFlowObserver()
        generateObserveSkieSwiftFlowsView(skieSwiftFlowObserver)
        val observingSirClass = generateObservingSwiftUIView()
        generateFlowObservingInitializers(observingSirClass)
        generateStateFlowObservingInitializers(observingSirClass, skieSwiftFlowWithInitialValue)
        generateAssertingSkieSwiftFlowValueUnwrap()
        generateSkieFlowDoesNotThrow()
    }

    context(SirPhase.Context)
    private fun generateCollectFlowModifiers() {
        namespaceProvider.getSkieNamespaceFile("SwiftUI.View+collect").apply {
            imports += "SwiftUI"

            SirExtension(
                classDeclaration = sirBuiltins.SwiftUI.View,
                attributes = listOf(availability),
            ).apply {
                SirSimpleFunction(
                    identifier = "collect",
                    returnType = sirBuiltins.SwiftUI.View.defaultType.toOpaque(),
                ).apply {
                    documentation = """
                        |A view modifier used to collect a SKIE-bridged Flow into a SwiftUI Binding.
                        |
                        |The flow is being collected using the `task` modifier,
                        |sharing the same lifecycle.
                        |
                        |In the following example we collect a `Flow<Int>` property `counter`
                        |from the `SharedViewModel` into a `@State` property in our view.
                        |
                        |```swift
                        |struct ExampleView: View {
                        |   let viewModel = SharedViewModel()
                        |
                        |   @State
                        |   var counter: KotlinInt = 0
                        |
                        |   var body: some View {
                        |       Text("Tick #\(counter)")
                        |           .collect(flow: viewModel.counter, into: ${'$'}counter)
                        |   }
                        |}
                        |```
                        |
                        |- parameter flow: A SKIE-bridged Flow you with to collect.
                        |- parameter binding: A binding to a property where each new value will be set to.
                    """.trimMargin()

                    val flowTypeParameter = SirTypeParameter(
                        name = "Flow",
                        bounds = listOf(
                            sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toConformanceBound(),
                        )
                    )

                    SirValueParameter(
                        name = "flow",
                        type = flowTypeParameter.toTypeParameterUsage(),
                    )

                    SirValueParameter(
                        label = "into",
                        name = "binding",
                        type = sirBuiltins.SwiftUI.Binding.toType(
                            flowTypeParameter.toTypeParameterUsage().typeParameter(
                                sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element"),
                            )
                        )
                    )

                    bodyBuilder.add {
                        addCode("collect(flow: flow) { newValue in%>")
                        addStatement("binding.wrappedValue = newValue")
                        addCode("%<}")
                    }
                }

                SirSimpleFunction(
                    identifier = "collect",
                    returnType = sirBuiltins.SwiftUI.View.defaultType.toOpaque(),
                ).apply {
                    documentation = """
                        |A view modifier used to collect a SKIE-bridged Flow into a SwiftUI Binding, transforming the value before assigning.
                        |
                        |The flow is being collected using the `task` modifier,
                        |sharing the same lifecycle.
                        |
                        |In the following example we collect a `Flow<Int>` property `counter`
                        |from the `SharedViewModel` into a `@State` property in our view.
                        |
                        |```swift
                        |struct ExampleView: View {
                        |   let viewModel = SharedViewModel()
                        |
                        |   @State
                        |   var counter: Int = 0
                        |
                        |   var body: some View {
                        |       Text("Tick #\(counter)")
                        |           .collect(flow: viewModel.counter, into: ${'$'}counter)
                        |   }
                        |}
                        |```
                        |
                        |- parameter flow: A SKIE-bridged Flow you with to collect.
                        |- parameter binding: A binding to a property where each new value will be set to.
                        |- parameter transform: An async closure to transform any value emitted by the flow into a one expected by the binding.
                        |                       Returning `nil` from this closure will reject the value.
                    """.trimMargin()

                    val flowTypeParameter = SirTypeParameter(
                        name = "Flow",
                        bounds = listOf(
                            sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toConformanceBound(),
                        )
                    )

                    val uTypeParameter = SirTypeParameter("U")

                    SirValueParameter(
                        name = "flow",
                        type = flowTypeParameter.toTypeParameterUsage(),
                    )

                    SirValueParameter(
                        label = "into",
                        name = "binding",
                        type = sirBuiltins.SwiftUI.Binding.toType(
                            uTypeParameter.toTypeParameterUsage()
                        ),
                    )

                    SirValueParameter(
                        name = "transform",
                        type = LambdaSirType(
                            valueParameterTypes = listOf(
                                flowTypeParameter.toTypeParameterUsage().typeParameter(
                                    sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element"),
                                )
                            ),
                            returnType = uTypeParameter.toTypeParameterUsage().toNullable(),
                            isEscaping = true,
                            isAsync = true,
                        )
                    )

                    bodyBuilder.add {
                        addCode("collect(flow: flow) { newValue in%>")
                        beginControlFlow("if", "let newTransformedValue = await transform(newValue)")
                        addStatement("binding.wrappedValue = newTransformedValue")
                        endControlFlow("if")
                        addCode("%<}")
                    }
                }

                SirSimpleFunction(
                    identifier = "collect",
                    returnType = sirBuiltins.SwiftUI.View.defaultType.toOpaque(),
                ).apply {
                    documentation = """
                        |A view modifier used to collect a SKIE-bridged Flow and perform a closere with each received value.
                        |
                        |The flow is being collected using the `task` modifier,
                        |sharing the same lifecycle.
                        |
                        |In the following example we collect a `Flow<Int>` property `counter`
                        |from the `SharedViewModel`, print the received value
                        |and add it to a `@State` property in our view.
                        |
                        |```swift
                        |struct ExampleView: View {
                        |   let viewModel = SharedViewModel()
                        |
                        |   @State
                        |   var sum: Int = 0
                        |
                        |   var body: some View {
                        |       Text("Sum \(sum)")
                        |           .collect(flow: viewModel.counter) { value in
                        |               print("Received \(value)")
                        |               sum = value
                        |           }
                        |   }
                        |}
                        |```
                        |
                        |- parameter flow: A SKIE-bridged Flow you with to collect.
                        |- parameter perform: An async closure to be invoked with each received value.
                    """.trimIndent()

                    val flowTypeParameter = SirTypeParameter(
                        name = "Flow",
                        bounds = listOf(
                            sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toConformanceBound(),
                        )
                    )

                    SirValueParameter(
                        name = "flow",
                        type = flowTypeParameter.toTypeParameterUsage(),
                    )

                    SirValueParameter(
                        name = "perform",
                        type = LambdaSirType(
                            valueParameterTypes = listOf(
                                flowTypeParameter.toTypeParameterUsage().typeParameter(
                                    sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element"),
                                )
                            ),
                            returnType = sirBuiltins.Swift.Void.defaultType,
                            isEscaping = true,
                            isAsync = true,
                        )
                    )

                    bodyBuilder.add {
                        addCode("self.task {%>")
                        beginControlFlow("do", "")
                        beginControlFlow("for", "try await item in flow")
                        addStatement("await perform(item)")
                        endControlFlow("for")
                        nextControlFlow("catch", "")
                        endControlFlow("do")
                        addCode("%<}")
                    }
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowWithInitialValue(): SkieSwiftFlowWithInitialValue {
        namespaceProvider.getSkieNamespaceFile("SkieSwiftFlowWithInitialValue").apply {
            val skieSwiftFlowWithInitialValue = SirClass(
                baseName = "SkieSwiftFlowWithInitialValue",
                kind = SirClass.Kind.Protocol,
            ).run {
                documentation = """
                    |A helper protocol uniting StateFlows with regular Flows with an assigned initial value using ``SkieSwiftFlow/withInitialValue``.
                """.trimMargin()

                val flowTypeParameter = SirTypeParameter(
                    name = "Flow",
                    bounds = listOf(
                        sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toConformanceBound(),
                    ),
                    isPrimaryAssociatedType = true,
                )

                val elementTypeParameter = SirTypeParameter(
                    name = "Element",
                    bounds = listOf(
                        flowTypeParameter.toTypeParameterUsage().typeParameter(
                            sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element"),
                        ).toEqualityBound(),
                    )
                )

                val flowProperty = SirProperty(
                    // TODO: Included to make sure visibility is not added, but should be handled automatically
                    visibility = SirVisibility.Internal,
                    identifier = "flow",
                    type = flowTypeParameter.toTypeParameterUsage(),
                    attributes = listOf("_spi(SKIE)"),
                    isAbstract = true,
                ).apply {
                    SirGetter()
                }

                val initialValueProperty = SirProperty(
                    // TODO: Included to make sure visibility is not added, but should be handled automatically
                    visibility = SirVisibility.Internal,
                    identifier = "initialValue",
                    type = elementTypeParameter.toTypeParameterUsage(),
                    attributes = listOf("_spi(SKIE)"),
                    isAbstract = true,
                ).apply {
                    SirGetter()
                }

                SkieSwiftFlowWithInitialValue(
                    self = this,
                    flowProperty = flowProperty,
                    initialValueProperty = initialValueProperty,
                    elementTypeParameter = elementTypeParameter,
                )
            }

            SirExtension(
                skieSwiftFlowWithInitialValue.self,
            ).apply {
                listOf(
                    skieSwiftFlowWithInitialValue.flowProperty,
                    skieSwiftFlowWithInitialValue.initialValueProperty,
                ).forEach { property ->
                    SirProperty(
                        property.identifier,
                        property.type,
                        visibility = SirVisibility.Internal,
                    ).apply {
                        addOverride(property)

                        SirGetter().apply {
                            bodyBuilder.add {
                                addStatement("""Swift.fatalError("SkieSwiftFlowWithInitialValue has to be conformed to with @_spi(SKIE) enabled and property '${property.identifier}' implemented")""")
                            }
                        }
                    }
                }
            }

            SirClass(
                baseName = "SkieSwiftFlowWithInitialValueImpl",
                kind = SirClass.Kind.Struct,
                visibility = SirVisibility.Internal,
                superTypes = listOf(
                    skieSwiftFlowWithInitialValue.self.defaultType,
                )
            ).apply {
                val flowTypeParameter = SirTypeParameter(
                    name = "Flow",
                    bounds = listOf(
                        sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toConformanceBound(),
                    )
                )

                SirProperty(
                    identifier = skieSwiftFlowWithInitialValue.flowProperty.identifier,
                    type = flowTypeParameter.toTypeParameterUsage(),
                ).apply {
                    addOverride(skieSwiftFlowWithInitialValue.flowProperty)
                }

                SirProperty(
                    identifier =  skieSwiftFlowWithInitialValue.initialValueProperty.identifier,
                    type = flowTypeParameter.toTypeParameterUsage().typeParameter(
                        sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element"),
                    ),
                ).apply {
                    addOverride(skieSwiftFlowWithInitialValue.initialValueProperty)
                }
            }

            val statefulFlowVariants = listOf(
                SupportedFlow.StateFlow,
                SupportedFlow.MutableStateFlow,
            ).flatMap { it.variants }
            statefulFlowVariants.forEach { variant ->
                val variantClass = variant.getSwiftClass()
                val tTypeParameter = variantClass.getTypeParameter("T")
                SirExtension(
                    classDeclaration = variantClass,
                    superTypes = listOf(
                        skieSwiftFlowWithInitialValue.self.defaultType,
                    ),
                ).apply {
                    SirProperty(
                        attributes = skieSwiftFlowWithInitialValue.flowProperty.attributes,
                        identifier = skieSwiftFlowWithInitialValue.flowProperty.identifier,
                        type = variantClass.toType(tTypeParameter.toTypeParameterUsage())
                    ).apply {
                        addOverride(skieSwiftFlowWithInitialValue.flowProperty)

                        SirGetter().apply {
                            bodyBuilder.add {
                                addStatement("self")
                            }
                        }
                    }

                    SirProperty(
                        attributes = skieSwiftFlowWithInitialValue.initialValueProperty.attributes,
                        identifier = skieSwiftFlowWithInitialValue.initialValueProperty.identifier,
                        type = skieSwiftFlowWithInitialValue.elementTypeParameter.toTypeParameterUsage(),
                    ).apply {
                        addOverride(skieSwiftFlowWithInitialValue.initialValueProperty)

                        SirGetter().apply {
                            bodyBuilder.add {
                                addStatement("value")
                            }
                        }
                    }
                }
            }

            val statelessFlowVariants = listOf(
                SupportedFlow.Flow,
                SupportedFlow.SharedFlow,
                SupportedFlow.MutableSharedFlow,
            ).flatMap { it.variants }
            statelessFlowVariants.forEach { variant ->
                val variantClass = variant.getSwiftClass()
                val tTypeParameter = variantClass.getTypeParameter("T")
                SirExtension(
                    classDeclaration = variantClass,
                ).apply {
                    SirSimpleFunction(
                        identifier = "withInitialValue",
                        returnType = skieSwiftFlowWithInitialValue.self.toType(
                            variantClass.toType(
                                tTypeParameter.toTypeParameterUsage(),
                            )
                        ).toOpaque(),
                    ).apply {
                        documentation = """
                            |Returns a wrapper containing an initial value to be used in ``Observing``.
                            |
                            |- parameter initialValue: Initial value to be used until the first element is emitted by the flow.
                        """.trimMargin()

                        SirValueParameter(
                            label = "_",
                            name = "initialValue",
                            type = skieSwiftFlowWithInitialValue.elementTypeParameter.toTypeParameterUsage(),
                        )

                        bodyBuilder.add {
                            addStatement("SkieSwiftFlowWithInitialValueImpl(flow: self, initialValue: initialValue)")
                        }
                    }
                }
            }

            return skieSwiftFlowWithInitialValue
        }
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowObserver(): SirClass {
        return namespaceProvider.getSkieNamespaceFile("SkieSwiftFlowObserver").run {
            imports += "SwiftUI"

            SirClass(
                baseName = "SkieSwiftFlowObserver",
                kind = SirClass.Kind.Actor,
                visibility = SirVisibility.Internal,
                superTypes = listOf(
                    sirBuiltins.SwiftUI.ObservableObject.defaultType,
                )
            ).apply {
                // TODO: Should be `private(set)`
                SirProperty(
                    identifier = "values",
                    type = sirBuiltins.Swift.Array.toType(
                        SpecialSirType.Any.toNullable(),
                    ),
                    attributes = listOf(
                        "_Concurrency.MainActor",
                        "SwiftUI.Published"
                    ),
                    isMutable = true,
                )

                SirProperty(
                    identifier = "flows",
                    type = sirBuiltins.Swift.Array.toType(
                        sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toExistential(),
                    ),
                    visibility = SirVisibility.Private,
                )

                SirConstructor(
                    visibility = SirVisibility.Internal,
                ).apply {
                    SirValueParameter(
                        name = "flows",
                        type = sirBuiltins.Swift.Array.toType(
                            sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toExistential(),
                        ),
                    )

                    bodyBuilder.add {
                        addStatement("self.flows = flows")
                        addStatement("self._values = SwiftUI.Published(initialValue: Swift.Array(repeating: nil, count: flows.count))")
                    }
                }

                SirSimpleFunction(
                    identifier = "beginCollecting",
                    returnType = sirBuiltins.Swift.Void.defaultType,
                    visibility = SirVisibility.Internal,
                    isAsync = true,
                ).apply {
                    bodyBuilder.add {
                        addCode("await _Concurrency.withTaskGroup(of: Swift.Void.self) { taskGroup in%>")
                        beginControlFlow("for", "(index, flow) in flows.enumerated()")
                        addCode("taskGroup.addTask {%>")
                        addStatement("await self.collect(index: index, flow: flow)")
                        addStatement("")
                        beginControlFlow("if", "!_Concurrency.Task.isCancelled")
                        beginControlFlow("if", "await self.values[index] == nil")
                        addStatement("""Swift.print("WARNING: Flow \(flow) with index \(index) hasn't produced a value before finishing.")""")
                        endControlFlow("if")
                        endControlFlow("if")
                        addCode("%<}")
                        endControlFlow("for")
                        addCode("%<}")
                    }
                }

                SirSimpleFunction(
                    identifier = "collect",
                    returnType = sirBuiltins.Swift.Void.defaultType,
                    visibility = SirVisibility.Private,
                    isAsync = true,
                ).apply {
                    SirValueParameter(
                        name = "index",
                        type = sirBuiltins.Swift.Int.defaultType,
                    )

                    SirValueParameter(
                        name = "flow",
                        type = sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toOpaque(),
                    )

                    bodyBuilder.add {
                        beginControlFlow("do", "")
                        beginControlFlow("for", "try await newValue in flow")
                        addStatement("await set(value: newValue, for: index)")
                        endControlFlow("for")
                        nextControlFlow("catch", "")
                        addStatement("skieFlowDoesNotThrow(error: error)")
                        endControlFlow("do")
                    }
                }

                SirSimpleFunction(
                    identifier = "set",
                    returnType = sirBuiltins.Swift.Void.defaultType,
                    visibility = SirVisibility.Private,
                    attributes = listOf(
                        "_Concurrency.MainActor",
                    )
                ).apply {
                    SirValueParameter(
                        label = "value",
                        name = "newValue",
                        type = SpecialSirType.Any,
                    )

                    SirValueParameter(
                        label = "for",
                        name = "index",
                        type = sirBuiltins.Swift.Int.defaultType,
                    )

                    bodyBuilder.add {
                        addStatement("values[index] = newValue")
                    }
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun generateObserveSkieSwiftFlowsView(skieSwiftFlowObserver: SirClass) {
        namespaceProvider.getSkieNamespaceFile("ObserveSkieSwiftFlows").apply {
            imports += "SwiftUI"

            SirClass(
                baseName = "ObserveSkieSwiftFlows",
                kind = SirClass.Kind.Struct,
                visibility = SirVisibility.Internal,
                superTypes = listOf(
                    sirBuiltins.SwiftUI.View.defaultType,
                ),
                attributes = listOf(
                    availability,
                ),
            ).apply {
                val contentTypeParameter = SirTypeParameter(
                    name = "Content",
                    bounds = listOf(
                        sirBuiltins.SwiftUI.View.defaultType.toConformanceBound(),
                    )
                )

                val contentFactoryType = LambdaSirType(
                    valueParameterTypes = listOf(
                        sirBuiltins.Swift.Array.toType(
                            SpecialSirType.Any.toNullable(),
                        )
                    ),
                    returnType = contentTypeParameter.toTypeParameterUsage(),
                    isEscaping = false,
                )

                SirProperty(
                    identifier = "content",
                    type = contentFactoryType,
                    visibility = SirVisibility.Private,
                )

                SirProperty(
                    identifier = "observer",
                    type = skieSwiftFlowObserver.defaultType,
                    visibility = SirVisibility.Private,
                    attributes = listOf(
                        "SwiftUI.ObservedObject"
                    ),
                    isMutable = true,
                )

                SirConstructor(
                    visibility = SirVisibility.Internal,
                ).apply {
                    SirValueParameter(
                        name = "flows",
                        type = sirBuiltins.Swift.Array.toType(
                            sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toExistential(),
                        )
                    )

                    SirValueParameter(
                        name = "content",
                        type = contentFactoryType.copy(isEscaping = true),
                        attributes = listOf(
                            "SwiftUI.ViewBuilder",
                        )
                    )

                    bodyBuilder.add {
                        addStatement("self.content = content")
                        addStatement("self.observer = %T(flows: flows)", skieSwiftFlowObserver.defaultType.evaluate().swiftPoetTypeName)
                    }
                }

                SirProperty(
                    identifier = "body",
                    type = sirBuiltins.SwiftUI.View.defaultType.toOpaque(),
                    visibility = SirVisibility.Internal,
                ).apply {
                    SirGetter().apply {
                        bodyBuilder.add {
                            addCode("content(observer.values)%>")
                            addCode(".task {%>")
                            addStatement("await observer.beginCollecting()")
                            addCode("%<}%<")
                        }
                    }
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun generateObservingSwiftUIView(): Observing {
        return namespaceProvider.getSkieNamespaceFile("Observing").run {
            imports += "SwiftUI"

            SirClass(
                baseName = "Observing",
                kind = SirClass.Kind.Struct,
                visibility = SirVisibility.Public,
                attributes = listOf(availability),
                superTypes = listOf(sirBuiltins.SwiftUI.View.defaultType),
            ).run {
                documentation = """
                    |This SwiftUI view allows observing SKIE-bridged flows.
                    |
                    |In the example below, we use ``Observing`` to show a SwiftUI ``Text`` with the latest value of a StateFlow.
                    |
                    |```swift
                    |struct ExampleView: View {
                    |   let viewModel = SharedViewModel()
                    |
                    |   var body: some View {
                    |       Observing(viewModel.counter) { counter in
                    |           Text("Tick \(counter)")
                    |       }
                    |   }
                    |}
                    |```
                    |
                    |In addition to StateFlows, you can also observe other Flows (i.e. regular Flow and SharedFlow).
                    |The example below shows two possible ways to observe a Flow.
                    |
                    |```swift
                    |struct ExampleView: View {
                    |   let viewModel = SharedViewModel()
                    |
                    |    var body: some View {
                    |        // Observing a Flow with an "initial content" view showing ProgressView.
                    |        Observing(viewModel.ticking) {
                    |            ProgressView("Waiting for a first value")
                    |        } content: { tick in
                    |            Text("Tick #\(tick)")
                    |        }
                    |
                    |        // Observing a Flow with an attached initial value.
                    |        Observing(viewModel.ticking.withInitialValue(0)) { tick in
                    |            Text("Tick #\(tick)")
                    |        }
                    |    }
                    |}
                    |```
                    |
                    |Notice the second usage doesn't provide two view builder closures.
                    |Instead, we attach an initial value to the `ticking` flow.
                    |This initial value will then be passed to the content view builder closure,
                    |until a new value is received from the flow itself.
                    |
                    |You can observe multiple flows using the same ``Observing`` view.
                    |You can also mix and match StateFlow and other Flow kinds.
                    |StateFlow behaves the same way as a Flow with an attached initial value.
                """.trimMargin()

                val values = SirTypeParameter(
                    name = "Values",
                )

                val initialContent = SirTypeParameter(
                    name = "InitialContent",
                    bounds = listOf(sirBuiltins.SwiftUI.View.defaultType.toConformanceBound()),
                )

                val content = SirTypeParameter(
                    name = "Content",
                    bounds = listOf(sirBuiltins.SwiftUI.View.defaultType.toConformanceBound()),
                )

                val initialContentFactoryType = LambdaSirType(
                    valueParameterTypes = emptyList(),
                    returnType = initialContent.toTypeParameterUsage(),
                    isEscaping = true
                )

                val contentFactoryType = LambdaSirType(
                    valueParameterTypes = listOf(values.toTypeParameterUsage()),
                    returnType = content.toTypeParameterUsage(),
                    isEscaping = true
                )

                val extractValuesType = LambdaSirType(
                    valueParameterTypes = listOf(sirBuiltins.Swift.Array.toType(SpecialSirType.Any.toNullable())),
                    returnType = values.toTypeParameterUsage().toNullable(),
                    isEscaping = true,
                )

                SirProperty(
                    identifier = "flows",
                    type = sirBuiltins.Swift.Array.toType(sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toExistential()),
                )

                SirProperty(
                    identifier = "initialContent",
                    type = initialContentFactoryType.copy(isEscaping = false),
                )

                SirProperty(
                    identifier = "content",
                    type = contentFactoryType.copy(isEscaping = false),
                )

                SirProperty(
                    identifier = "extractValues",
                    type = extractValuesType.copy(isEscaping = false),
                )

                SirConstructor(
                    attributes = listOf("_spi(SKIE)")
                ).apply {
                    documentation = """
                         |This initializer shouldn't be used directly.
                         |Instead, use one of the ``Observing`` functions.
                         |
                         |While it could be internal, it's intentionally left public under the SKIE spi.
                         |That allows for declaring additional initializers when more parameters are needed,
                         |without reimplementing the whole logic.
                    """.trimMargin()

                    SirValueParameter(
                        name = "flows",
                        type = sirBuiltins.Swift.Array.toType(sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toExistential()),
                    )

                    SirValueParameter(
                        name = "initialContent",
                        type = initialContentFactoryType,
                        attributes = listOf("SwiftUI.ViewBuilder"),
                    )

                    SirValueParameter(
                        name = "content",
                        type = contentFactoryType,
                        attributes = listOf("SwiftUI.ViewBuilder"),
                    )

                    SirValueParameter(
                        name = "extractValues",
                        type = extractValuesType,
                    )

                    bodyBuilder.add {
                        addCode("""
                            self.flows = flows
                            self.initialContent = initialContent
                            self.content = content
                            self.extractValues = extractValues
                        """.trimIndent())
                    }

                    SirProperty(
                        identifier = "body",
                        type = sirBuiltins.SwiftUI.View.defaultType.toOpaque(),
                    ).apply {
                        SirGetter().apply {
                            bodyBuilder.add {
                                addCode("""
                                    ObserveSkieSwiftFlows(flows: flows) { rawValues in
                                        if let values = extractValues(rawValues) {
                                            content(values)
                                        } else {
                                            initialContent()
                                        }
                                    }
                                """.trimIndent())
                            }
                        }
                    }
                }

                Observing(
                    self = this,
                    valuesTypeParameter = values,
                    initialContentTypeParameter = initialContent,
                    contentTypeParameter = content,
                )
            }

        }
    }

    context(SirPhase.Context)
    private fun generateFlowObservingInitializers(observing: Observing) {
        val asyncSequenceElementTypeParameter = sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element")
        namespaceProvider.getSkieNamespaceFile("Observing+Flow").apply {
            imports += "SwiftUI"

            SirExtension(
                classDeclaration = observing.self,
                attributes = listOf(
                    availability,
                )
            ).apply {
                (1..maxFlowsOverload).forEach { flowCount ->
                    val flowRange = 1..flowCount
                    SirConstructor().apply {
                        documentation = """
                            |An instance observing ${if (flowCount > 1) "$flowCount flows" else "a single flow"}. Look up the ``Observing`` view documentation for more information.
                            |
                            ${if (flowCount > 1) flowRange.joinToString("\n") { "|- parameter flow$it: #$it flow to observe." } else "|- parameter flow1: The flow to observe."}
                            |- parameter initialContent: View that's shown until the first element is emitted by the flow.
                            |- parameter content: View that's shown once a value is received from the flow and will be called for each new received value.
                        """.trimMargin()

                        val flowTypeParameters = flowRange.map {
                            SirTypeParameter(
                                name = "Flow$it",
                                bounds = listOf(
                                    sirBuiltins.Skie.SkieSwiftFlowProtocol.defaultType.toConformanceBound(),
                                ),
                            )
                        }

                        SirConditionalConstraint(
                            typeParameter = observing.valuesTypeParameter,
                            bounds = listOf(
                                TupleSirType(
                                    flowTypeParameters.map {
                                        it.toTypeParameterUsage().typeParameter(
                                            asyncSequenceElementTypeParameter,
                                        )
                                    }
                                ).toEqualityBound(),
                            ),
                        )

                        val flowValueParameters = flowTypeParameters.mapIndexed { index, flowTypeParameter ->
                            SirValueParameter(
                                label = "_",
                                name = "flow${index + 1}",
                                type = flowTypeParameter.toTypeParameterUsage(),
                            )
                        }

                        SirValueParameter(
                            attributes = listOf("SwiftUI.ViewBuilder"),
                            name = "initialContent",
                            type = LambdaSirType(
                                valueParameterTypes = emptyList(),
                                returnType = observing.initialContentTypeParameter.toTypeParameterUsage(),
                                isEscaping = true
                            )
                        )

                        SirValueParameter(
                            attributes = listOf("SwiftUI.ViewBuilder"),
                            name = "content",
                            type = LambdaSirType(
                                valueParameterTypes = flowTypeParameters.map {
                                    it.toTypeParameterUsage().typeParameter(
                                        asyncSequenceElementTypeParameter,
                                    )
                                },
                                returnType = observing.contentTypeParameter.toTypeParameterUsage(),
                                isEscaping = true
                            )
                        )

                        bodyBuilder.add {
                            +"self.init("
                            indented {
                                +"flows: [${flowValueParameters.joinToString { it.name }}],\n"
                                +"initialContent: initialContent,\n"
                                +"content: content\n"
                            }
                            ") { values in" {
                                flowValueParameters.forEachIndexed { index, parameter ->
                                    addStatement(
                                        "guard let flowValue${index + 1}: %T = assertingSkieSwiftFlowValueUnwrap(value: values[$index]) else { return nil }",
                                        flowTypeParameters[index].toTypeParameterUsage().typeParameter(
                                            asyncSequenceElementTypeParameter
                                        ).evaluate().swiftPoetTypeName
                                    )
                                }

                                if (flowCount > 1) {
                                    addStatement("return %L", flowRange.joinToString(prefix = "(", postfix = ")") { "flowValue$it" })
                                } else {
                                    addStatement("return flowValue1")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun generateStateFlowObservingInitializers(observing: Observing, skieSwiftFlowWithInitialValue: SkieSwiftFlowWithInitialValue) {
        val asyncSequenceElementTypeParameter = sirBuiltins._Concurrency.AsyncSequence.getTypeParameter("Element")

        namespaceProvider.getSkieNamespaceFile("Observing+StateFlow").apply {
            imports += "SwiftUI"

            SirExtension(
                classDeclaration = observing.self,
                attributes = listOf(
                    availability,
                ),
            ).apply {
                SirConditionalConstraint(
                    typeParameter = observing.initialContentTypeParameter,
                    bounds = listOf(
                        sirBuiltins.SwiftUI.EmptyView.defaultType.toEqualityBound(),
                    )
                )

                (1..maxFlowsOverload).forEach { flowCount ->
                    val flowRange = 1..flowCount
                    SirConstructor().apply {
                        documentation = """
                            |An instance observing ${if (flowCount > 1) "$flowCount flows" else "a single flow"} with an attached initial value. Look up the ``Observing`` view documentation for more information.
                            |
                            ${if (flowCount > 1) flowRange.joinToString("\n") { "|- parameter flow$it: #$it flow to observe." } else "|- parameter flow1: The flow to observe."}
                            |- parameter content: View that's shown for the initial value and then called again for each new received value from the flow.
                        """.trimMargin()

                        val flowTypeParameters = flowRange.map {
                            SirTypeParameter(
                                name = "Flow$it",
                                bounds = listOf(
                                    skieSwiftFlowWithInitialValue.self.defaultType.toConformanceBound(),
                                )
                            )
                        }

                        SirConditionalConstraint(
                            typeParameter = observing.valuesTypeParameter,
                            bounds = listOf(
                                TupleSirType(
                                    flowTypeParameters.map {
                                        it.toTypeParameterUsage().typeParameter(
                                            asyncSequenceElementTypeParameter,
                                        )
                                    }
                                ).toEqualityBound()
                            )
                        )

                        val flowValueParameters = flowTypeParameters.mapIndexed { index, flowTypeParameter ->
                            SirValueParameter(
                                label = "_",
                                name = "flow${index + 1}",
                                type = flowTypeParameter.toTypeParameterUsage(),
                            )
                        }

                        SirValueParameter(
                            attributes = listOf("SwiftUI.ViewBuilder"),
                            name = "content",
                            type = LambdaSirType(
                                valueParameterTypes = flowTypeParameters.map {
                                    it.toTypeParameterUsage().typeParameter(
                                        asyncSequenceElementTypeParameter
                                    )
                                },
                                returnType = observing.contentTypeParameter.toTypeParameterUsage(),
                                isEscaping = true,
                            )
                        )

                        bodyBuilder.add {
                            +"self.init("
                            indented {
                                +"flows: [${flowValueParameters.joinToString { "${it.name}.flow" }}],\n"
                                +"initialContent: SwiftUI.EmptyView.init,\n"
                                +"content: content\n"
                            }
                            ") { values in" {
                                flowValueParameters.forEachIndexed { index, parameter ->
                                    addStatement(
                                        "let flowValue${index + 1}: %T = assertingSkieSwiftFlowValueUnwrap(value: values[$index]) ?? ${parameter.name}.initialValue",
                                        flowTypeParameters[index].toTypeParameterUsage().typeParameter(
                                            asyncSequenceElementTypeParameter
                                        ).evaluate().swiftPoetTypeName
                                    )
                                }
                                if (flowCount > 1) {
                                    addStatement("return %L", flowRange.joinToString(prefix = "(", postfix = ")") { "flowValue$it" })
                                } else {
                                    addStatement("return flowValue1")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun generateAssertingSkieSwiftFlowValueUnwrap() {
        namespaceProvider.getSkieNamespaceFile("assertingSkieSwiftFlowValueUnwrap").apply {
            val tParameter = SirTypeParameter(
                name = "T",
                parent = SirTypeParameterParent.None,
            )

            SirSimpleFunction(
                identifier = "assertingSkieSwiftFlowValueUnwrap",
                returnType = tParameter.toTypeParameterUsage().toNullable(),
                visibility = SirVisibility.Internal,
            ).apply {
                tParameter.parent = this

                SirValueParameter(
                    name = "value",
                    type = SpecialSirType.Any.toNullable(),
                )

                bodyBuilder.add {
                    beginControlFlow("if", "let value = value")
                    beginControlFlow("if", "let expectedValue = value as? T")
                    addStatement("return expectedValue")
                    nextControlFlow("else", "")
                    addStatement("""Swift.assertionFailure("Value \(value) wasn't nil, but wasn't \(T.self). This is a SKIE bug, please report it.")""")
                    addStatement("return nil")
                    endControlFlow("if")
                    nextControlFlow("else", "")
                    addStatement("return nil")
                    endControlFlow("if")
                }
            }
        }
    }

    context(SirPhase.Context)
    private fun generateSkieFlowDoesNotThrow() {
        namespaceProvider.getSkieNamespaceFile("skieFlowDoesNotThrow").apply {
            SirSimpleFunction(
                identifier = "skieFlowDoesNotThrow",
                returnType = sirBuiltins.Swift.Never.defaultType,
                visibility = SirVisibility.Internal,
            ).apply {
                SirValueParameter(
                    name = "error",
                    type = sirBuiltins.Swift.Error.defaultType,
                )

                SirValueParameter(
                    name = "function",
                    type = sirBuiltins.Swift.StaticString.defaultType,
                    defaultValue = "#function",
                )

                bodyBuilder.add {
                    addStatement("""
                        |Swift.fatalError(""${'"'}
                        |    SKIE flows don't really throw, but Swift before 6.0 doesn't know.
                        |    We're using a protocol extending `AsyncSequence` which is a `@rethrows` protocol before Swift 6.0.
                        |    Even though all our implementation are non-throwing, Swift can't figure it out.
                        |    However, if your code crashes on this, please report a bug to SKIE (https://github.com/touchlab/skie).
                        |    Error: \(error).
                        |    Function: \(function).
                        |""${'"'})
                    """.trimMargin())
                }
            }
        }
    }
}
