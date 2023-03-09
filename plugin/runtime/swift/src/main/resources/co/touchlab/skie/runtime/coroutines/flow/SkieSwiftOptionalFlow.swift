import Foundation

public final class SkieSwiftOptionalFlow<T: Swift.AnyObject>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftOptionalFlow<T>.Iterator

    public typealias Element = T?

    public typealias _ObjectiveCType = SkieKotlinOptionalFlow<Swift.AnyObject>

    internal let coroutinesFlow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_Flow

    private init(_ flow: SkieKotlinOptionalFlow<Swift.AnyObject>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieKotlinFlow<T>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieKotlinOptionalFlow<T>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieKotlinSharedFlow<T>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieKotlinOptionalSharedFlow<T>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieSwiftFlow<T>) {
        coroutinesFlow = flow.coroutinesFlow
    }

    public init(_ flow: SkieSwiftSharedFlow<T>) {
        coroutinesFlow = flow.coroutinesFlow
    }

    public init(_ flow: SkieSwiftOptionalSharedFlow<T>) {
        coroutinesFlow = flow.coroutinesFlow
    }

    public func makeAsyncIterator() -> SkieSwiftOptionalFlow<T>.Iterator {
        return SkieSwiftOptionalFlow<T>.Iterator(flow: coroutinesFlow)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinOptionalFlow(coroutinesFlow)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalFlow<T> {
        return SkieSwiftOptionalFlow(source!)
    }

    public class Iterator: AsyncIteratorProtocol {

        public typealias Element = T?

        private let iterator: SkieColdFlowIterator<T>

        init(flow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_Flow) {
            iterator = SkieColdFlowIterator(flow: flow)
        }

        deinit {
            iterator.cancel()
        }

        public func next() async -> Element? {
            let hasNext = try? await SkieColdFlowIteratorKt.hasNext(iterator)

            if (hasNext?.boolValue ?? false) {
                return .some(iterator.next())
            } else {
                return nil
            }
        }
    }
}
