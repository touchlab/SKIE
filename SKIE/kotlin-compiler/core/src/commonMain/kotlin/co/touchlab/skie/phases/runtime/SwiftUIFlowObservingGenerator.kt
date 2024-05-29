package co.touchlab.skie.phases.runtime

import co.touchlab.skie.phases.SirPhase
import org.intellij.lang.annotations.Language

object SwiftUIFlowObservingGenerator {

    private val availability = "@available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *)"
    private val maxFlowsOverload = 5

    context(SirPhase.Context)
    fun generate() {
        generateCollectFlowModifiers()
        generateSkieSwiftFlowWithInitialValue()
        generateSkieSwiftFlowObserver()
        generateObserveSkieSwiftFlowsView()
        generateObservingSwiftUIView()
        generateFlowObservingInitializers()
        generateStateFlowObservingInitializers()
        generateAssertingSkieSwiftFlowValueUnwrap()
        generateSkieFlowDoesNotThrow()
    }

    context(SirPhase.Context)
    private fun generateCollectFlowModifiers() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SwiftUI.View+collect").content = """
            import SwiftUI

            $availability
            extension SwiftUI.View {
                /**
                 A view modifier used to collect a SKIE-bridged Flow into a SwiftUI Binding.

                 The flow is being collected using the `task` modifier,
                 sharing the same lifecycle.

                 In the following example we collect a `Flow<Int>` property `counter`
                 from the `SharedViewModel` into a `@State` property in our view.

                 ```swift
                 struct ExampleView: View {
                    let viewModel = SharedViewModel()

                    @State
                    var counter: KotlinInt = 0

                    var body: some View {
                        Text("Tick #\(counter)")
                            .collect(flow: viewModel.counter, into: ${'$'}counter)
                    }
                 }
                 ```

                 - parameter flow: A SKIE-bridged Flow you with to collect.
                 - parameter binding: A binding to a property where each new value will be set to.
                */
                public func collect<Flow: SkieSwiftFlowProtocol>(flow: Flow, into binding: SwiftUI.Binding<Flow.Element>) -> some SwiftUI.View {
                    collect(flow: flow) { newValue in
                        binding.wrappedValue = newValue
                    }
                }

                /**
                 A view modifier used to collect a SKIE-bridged Flow into a SwiftUI Binding, transforming the value before assigning.

                 The flow is being collected using the `task` modifier,
                 sharing the same lifecycle.

                 In the following example we collect a `Flow<Int>` property `counter`
                 from the `SharedViewModel` into a `@State` property in our view.

                 ```swift
                 struct ExampleView: View {
                    let viewModel = SharedViewModel()

                    @State
                    var counter: Int = 0

                    var body: some View {
                        Text("Tick #\(counter)")
                            .collect(flow: viewModel.counter, into: ${'$'}counter)
                    }
                 }
                 ```

                 - parameter flow: A SKIE-bridged Flow you with to collect.
                 - parameter binding: A binding to a property where each new value will be set to.
                 - parameter transform: An async closure to transform any value emitted by the flow into a one expected by the binding.
                                        Returning `nil` from this closure will reject the value.
                */
                public func collect<Flow: SkieSwiftFlowProtocol, U>(
                    flow: Flow,
                    into binding: SwiftUI.Binding<U>,
                    transform: @escaping (Flow.Element) async -> U?
                ) -> some SwiftUI.View {
                    collect(flow: flow) { newValue in
                        if let newTransformedValue = await transform(newValue) {
                            binding.wrappedValue = newTransformedValue
                        }
                    }
                }

                /**
                 A view modifier used to collect a SKIE-bridged Flow and perform a closere with each received value.

                 The flow is being collected using the `task` modifier,
                 sharing the same lifecycle.

                 In the following example we collect a `Flow<Int>` property `counter`
                 from the `SharedViewModel`, print the received value
                 and add it to a `@State` property in our view.

                 ```swift
                 struct ExampleView: View {
                    let viewModel = SharedViewModel()

                    @State
                    var sum: Int = 0

                    var body: some View {
                        Text("Sum \(sum)")
                            .collect(flow: viewModel.counter) { value in
                                print("Received \(value)")
                                sum = value
                            }
                    }
                 }
                 ```

                 - parameter flow: A SKIE-bridged Flow you with to collect.
                 - parameter perform: An async closure to be invoked with each received value.
                */
                public func collect<Flow: SkieSwiftFlowProtocol>(flow: Flow, perform: @escaping (Flow.Element) async -> Swift.Void) -> some SwiftUI.View {
                    self.task {
                        do {
                            for try await item in flow {
                                await perform(item)
                            }
                        } catch {
                            skieFlowDoesNotThrow(error: error)
                        }
                    }
                }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowWithInitialValue() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieSwiftFlowWithInitialValue").content = """
            /**
             A helper protocol uniting StateFlows with regular Flows with an assigned initial value using ``SkieSwiftFlow/withInitialValue``.
             */
            public protocol SkieSwiftFlowWithInitialValue<Flow> {
                associatedtype Flow: SkieSwiftFlowProtocol
                associatedtype Element where Flow.Element == Element

                /// Internal use only. This is used by SKIE's SwiftUI Flow observation runtime.
                @_spi(SKIE)
                var flow: Flow { get }

                /// Internal use only. This is used by SKIE's SwiftUI Flow observation runtime.
                @_spi(SKIE)
                var initialValue: Element { get }
            }

            extension SkieSwiftFlowWithInitialValue {
                var flow: Flow {
                    Swift.fatalError("SkieSwiftFlowWithInitialValue has to be conformed to with @_spi(SKIE) enabled and this property implemented")
                }

                var initialValue: Element {
                    Swift.fatalError("SkieSwiftFlowWithInitialValue has to be conformed to with @_spi(SKIE) enabled and this property implemented")
                }
            }

            internal struct SkieSwiftFlowWithInitialValueImpl<Flow: SkieSwiftFlowProtocol>: SkieSwiftFlowWithInitialValue {
                let flow: Flow
                let initialValue: Flow.Element
            }

            extension SkieSwiftStateFlow: SkieSwiftFlowWithInitialValue {
                @_spi(SKIE)
                public var flow: SkieSwiftStateFlow<Element> {
                    self
                }

                @_spi(SKIE)
                public var initialValue: T {
                    value
                }
            }

            extension SkieSwiftMutableStateFlow: SkieSwiftFlowWithInitialValue {
                @_spi(SKIE)
                public var flow: SkieSwiftMutableStateFlow<Element> {
                    self
                }

                @_spi(SKIE)
                public var initialValue: Element {
                    value
                }
            }

            extension SkieSwiftFlow {
                /**
                 Returns a wrapper containing an initial value to be used in ``Observing``.

                 - parameter initialValue: Initial value to be used until the first element is emitted by the flow.
                 */
                public func withInitialValue(_ initialValue: Element) -> some SkieSwiftFlowWithInitialValue<SkieSwiftFlow<Element>> {
                    SkieSwiftFlowWithInitialValueImpl(flow: self, initialValue: initialValue)
                }
            }

            extension SkieSwiftSharedFlow {
                /**
                 Returns a wrapper containing an initial value to be used in ``Observing``.

                 - parameter initialValue: Initial value to be used until the first element is emitted by the flow.
                 */
                public func withInitialValue(_ initialValue: Element) -> some SkieSwiftFlowWithInitialValue<SkieSwiftSharedFlow<Element>> {
                    SkieSwiftFlowWithInitialValueImpl(flow: self, initialValue: initialValue)
                }
            }

            extension SkieSwiftMutableSharedFlow {
                /**
                 Returns a wrapper containing an initial value to be used in ``Observing``.

                 - parameter initialValue: Initial value to be used until the first element is emitted by the flow.
                 */
                public func withInitialValue(_ initialValue: Element) -> some SkieSwiftFlowWithInitialValue<SkieSwiftMutableSharedFlow<Element>> {
                    SkieSwiftFlowWithInitialValueImpl(flow: self, initialValue: initialValue)
                }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateSkieSwiftFlowObserver() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("SkieSwiftFlowObserver").content = """
            import SwiftUI

            internal actor SkieSwiftFlowObserver: SwiftUI.ObservableObject {
                @_Concurrency.MainActor
                @SwiftUI.Published
                private(set) var values: [Any?]

                private let flows: [any SkieSwiftFlowProtocol]

                internal init(flows: [any SkieSwiftFlowProtocol]) {
                    self.flows = flows
                    self._values = SwiftUI.Published(initialValue: Swift.Array(repeating: nil, count: flows.count))
                }

                internal func beginCollecting() async {
                    await _Concurrency.withTaskGroup(of: Swift.Void.self) { taskGroup in
                        for (index, flow) in flows.enumerated() {
                            taskGroup.addTask {
                                await self.collect(index: index, flow: flow)

                                if !_Concurrency.Task.isCancelled {
                                    if await self.values[index] == nil {
                                        Swift.print("WARNING: Flow \(flow) with index \(index) hasn't produced a value before finishing.")
                                    }
                                }
                            }
                        }
                    }
                }

                private func collect(index: Swift.Int, flow: some SkieSwiftFlowProtocol) async {
                    do {
                        for try await newValue in flow {
                            await set(value: newValue, for: index)
                        }
                    } catch {
                        skieFlowDoesNotThrow(error: error)
                    }
                }

                @_Concurrency.MainActor
                private func set(value newValue: Any, for index: Swift.Int) {
                    values[index] = newValue
                }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateObserveSkieSwiftFlowsView() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("ObserveSkieSwiftFlows").content = """
            import SwiftUI

            $availability
            internal struct ObserveSkieSwiftFlows<Content: SwiftUI.View>: SwiftUI.View {
                private let content: (_ values: [Any?]) -> Content

                @SwiftUI.ObservedObject
                private var observer: SkieSwiftFlowObserver

                internal init(flows: [any SkieSwiftFlowProtocol], @SwiftUI.ViewBuilder content: @escaping (_ values: [Any?]) -> Content) {
                    self.content = content
                    observer = SkieSwiftFlowObserver(flows: flows)
                }

                internal var body: some SwiftUI.View {
                    content(observer.values)
                        .task {
                            await observer.beginCollecting()
                        }
                }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateObservingSwiftUIView() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("Observing").content = """
            import SwiftUI

            /**
             This SwiftUI view allows observing SKIE-bridged flows.

             In the example below, we use ``Observing`` to show a SwiftUI ``Text`` with the latest value of a StateFlow.

             ```swift
             struct ExampleView: View {
                let viewModel = SharedViewModel()

                var body: some View {
                    Observing(viewModel.counter) { counter in
                        Text("Tick \(counter)")
                    }
                }
             }
             ```

             In addition to StateFlows, you can also observe other Flows (i.e. regular Flow and SharedFlow).
             The example below shows two possible ways to observe a Flow.

             ```swift
             struct ExampleView: View {
                 let viewModel = SharedViewModel()

                 var body: some View {
                     // Observing a Flow with an "initial content" view showing ProgressView.
                     Observing(viewModel.ticking) {
                         ProgressView("Waiting for a first value")
                     } content: { tick in
                         Text("Tick #\(tick)")
                     }

                     // Observing a Flow with an attached initial value.
                     Observing(viewModel.ticking.withInitialValue(0)) { tick in
                         Text("Tick #\(tick)")
                     }
                 }
             }
             ```

             Notice the second usage doesn't provide two view builder closures.
             Instead we attach an initial value to the `ticking` flow.
             This initial value will then be passed to the content view builder closure,
             until a new value is received from the flow itself.

             You can observe multiple flows using the same ``Observing`` view.
             You can also mix and match StateFlow and other Flow kinds.
             StateFlow behaves the same way as a Flow with an attached initial value.

            */
            $availability
            public struct Observing<Values, InitialContent: SwiftUI.View, Content: SwiftUI.View>: SwiftUI.View {
                private let flows: [any SkieSwiftFlowProtocol]
                private let initialContent: () -> InitialContent
                private let content: (Values) -> Content
                private let extractValues: ([Any?]) -> Values?

                /**
                 This initializer shouldn't be used directly.
                 Instead use one of the ``Observing`` functions.

                 While it could be internal, it's intentionally left public under the SKIE spi.
                 That allows for declaring additional initializers when more parameters are needed,
                 without reimplementing the whole logic.
                */
                @_spi(SKIE)
                public init(
                    flows: [any SkieSwiftFlowProtocol],
                    @SwiftUI.ViewBuilder initialContent: @escaping () -> InitialContent,
                    @SwiftUI.ViewBuilder content: @escaping (Values) -> Content,
                    extractValues: @escaping ([Any?]) -> Values?
                ) {
                    self.flows = flows
                    self.initialContent = initialContent
                    self.content = content
                    self.extractValues = extractValues
                }

                public var body: some SwiftUI.View {
                    ObserveSkieSwiftFlows(flows: flows) { rawValues in
                        if let values = extractValues(rawValues) {
                            content(values)
                        } else {
                            initialContent()
                        }
                    }
                }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateFlowObservingInitializers() {
        @Language("swift")
        val singleFlow = """
            |$availability
            |extension Observing {
            |    /**
            |     An instance observing a single flow. Look up the ``Observing`` view documentation for more information.
            |
            |     - parameter flow: The flow to observe.
            |     - parameter initialContent: View that's shown until the first element is emitted by the flow.
            |     - parameter content: View that's shown once a value is received from the flow and will be called for each new received value.
            |     */
            |    public init<Flow>(
            |        _ flow: Flow,
            |        @SwiftUI.ViewBuilder initialContent: @escaping () -> InitialContent,
            |        @SwiftUI.ViewBuilder content: @escaping (Flow.Element) -> Content
            |    ) where Flow: SkieSwiftFlowProtocol, Values == (Flow.Element) {
            |        self.init(
            |            flows: [flow],
            |            initialContent: initialContent,
            |            content: content
            |        ) { values in
            |            guard let f1: Flow.Element = assertingSkieSwiftFlowValueUnwrap(value: values[0]) else { return nil }
            |            return f1
            |        }
            |    }
            |}
        """

        val multipleFlows = (2..maxFlowsOverload).map { flowCount ->
            val flowRange = (1..flowCount)
            """
                |    /**
                |     An instance observing $flowCount flows. Look up the ``Observing`` view documentation for more information.
                |
                      ${flowRange.joinToString("\n") { "|     - parameter flow$it: #$it flow to observe." }}
                |     - parameter initialContent: View that's shown until each of the flows emit at least one value.
                |     - parameter content: View that's once each flow produces at least oen value and will be called for each new received value from any of the flows.
                |    */
                |    public init<${flowRange.joinToString { "Flow$it" }}>(
                |        ${flowRange.joinToString { "_ flow$it: Flow$it" }},
                |        @SwiftUI.ViewBuilder initialContent: @escaping () -> InitialContent,
                |        @SwiftUI.ViewBuilder content: @escaping (${flowRange.joinToString { "Flow$it.Element" }}) -> Content
                |    ) where ${flowRange.joinToString { "Flow$it: SkieSwiftFlowProtocol" }}, Values == (${flowRange.joinToString { "Flow$it.Element" }}) {
                |        self.init(
                |            flows: [${flowRange.joinToString { "flow$it" }}],
                |            initialContent: initialContent,
                |            content: content
                |        ) { values in
                        ${
                flowRange.joinToString("\n") {
                    "|            guard let flowValue$it: Flow$it.Element = assertingSkieSwiftFlowValueUnwrap(value: values[${it - 1}]) else { return nil }"
                }
            }
                |            return (${flowRange.joinToString { "flowValue$it" }})
                |        }
                |    }"""
        }

        namespaceProvider.getSkieNamespaceWrittenSourceFile("Observing+Flow").content = """
            |import SwiftUI
            |
            |$singleFlow
            |
            |$availability
            |extension Observing {
                ${multipleFlows.joinToString("\n")}
            |}""".trimMargin()
    }

    context(SirPhase.Context)
    private fun generateStateFlowObservingInitializers() {
        @Language("swift")
        val singleStateFlow = """
            |$availability
            |extension Observing where InitialContent == SwiftUI.EmptyView {
            |    /**
            |     An instance observing a single flow with an attached initial value. Look up the ``Observing`` view documentation for more information.
            |
            |     - parameter flow: The flow to observe.
            |     - parameter content: View that's shown for the initial value and then called again for each new received value from the flow.
            |     */
            |    public init<Flow>(
            |        _ flow: Flow,
            |        @SwiftUI.ViewBuilder content: @escaping (Flow.Element) -> Content
            |    ) where Flow: SkieSwiftFlowWithInitialValue, Values == (Flow.Element) {
            |        self.init(
            |            flows: [flow.flow],
            |            initialContent: SwiftUI.EmptyView.init,
            |            content: content
            |        ) { values in
            |            let flowValue1: Flow.Element = assertingSkieSwiftFlowValueUnwrap(value: values[0]) ?? flow.initialValue
            |            return (flowValue1)
            |        }
            |    }
            |}
        """

        val multipleFlows = (2..maxFlowsOverload).map { flowCount ->
            val flowRange = (1..flowCount)
            """
                |    /**
                |     An instance observing $flowCount flows with attached initial values. Look up the ``Observing`` view documentation for more information.
                |
                      ${flowRange.joinToString("\n") { "|     - parameter flow$it: #$it flow to observe." }}
                |     - parameter content: View that's shown for the initial values and then called again for each new received value from any of the flows.
                |    */
                |    public init<${flowRange.joinToString { "Flow$it" }}>(
                |        ${flowRange.joinToString { "_ flow$it: Flow$it" }},
                |        @SwiftUI.ViewBuilder content: @escaping (${flowRange.joinToString { "Flow$it.Element" }}) -> Content
                |    ) where ${flowRange.joinToString { "Flow$it: SkieSwiftFlowWithInitialValue" }}, Values == (${flowRange.joinToString { "Flow$it.Element" }}) {
                |        self.init(
                |            flows: [${flowRange.joinToString { "flow$it.flow" }}],
                |            initialContent: SwiftUI.EmptyView.init,
                |            content: content,
                |            extractValues: { values in
                                 ${
                flowRange.joinToString("\n") {
                    "|                let flowValue$it: Flow$it.Element = assertingSkieSwiftFlowValueUnwrap(value: values[${it - 1}]) ?? flow$it.initialValue"
                }
            }
                |                return (${flowRange.joinToString { "flowValue$it" }})
                |            }
                |        )
                |    }"""
        }

        namespaceProvider.getSkieNamespaceWrittenSourceFile("Observing+StateFlow").content = """
            |import SwiftUI
            |
            |$singleStateFlow
            |
            |$availability
            |extension Observing where InitialContent == SwiftUI.EmptyView {
                ${multipleFlows.joinToString("\n")}
            |}""".trimMargin()
    }

    context(SirPhase.Context)
    private fun generateAssertingSkieSwiftFlowValueUnwrap() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("assertingSkieSwiftFlowValueUnwrap").content = """
            internal func assertingSkieSwiftFlowValueUnwrap<T>(value: Any?) -> T? {
                if let value = value {
                    if let expectedValue = value as? T {
                        return expectedValue
                    } else {
                        Swift.assertionFailure("Value \(value) wasn't nil, but wasn't \(T.self). This is a SKIE bug, please report it.")
                        return nil
                    }
                } else {
                    return nil
                }
            }
        """.trimIndent()
    }

    context(SirPhase.Context)
    private fun generateSkieFlowDoesNotThrow() {
        namespaceProvider.getSkieNamespaceWrittenSourceFile("skieFlowDoesNotThrow").content = """
            internal func skieFlowDoesNotThrow(error: Swift.Error, function: Swift.StaticString = #function) -> Swift.Never {
                Swift.fatalError(""${'"'}
                    SKIE flows don't really throw, but Swift before 6.0 doesn't know.
                    We're using a protocol extending `AsyncSequence` which is a `@rethrows` protocol before Swift 6.0.
                    Even though all our implementation are non-throwing, Swift can't figure it out.
                    However, if your code crashes on this, please report a bug to SKIE (https://github.com/touchlab/skie).
                    Error: \(error).
                    Function: \(function).
                ""${'"'})
            }
        """.trimIndent()
    }
}
