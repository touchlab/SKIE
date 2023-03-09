import Foundation

public final class SkieSwiftOptionalSharedFlow<T: Swift.AnyObject>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftOptionalFlow<T>.Iterator

    public typealias Element = T?

    public typealias _ObjectiveCType = SkieKotlinOptionalSharedFlow<Swift.AnyObject>

    internal let coroutinesFlow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_SharedFlow

    private init(_ flow: SkieKotlinOptionalSharedFlow<Swift.AnyObject>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieKotlinSharedFlow<T>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieKotlinOptionalSharedFlow<T>) {
        coroutinesFlow = flow
    }

    public init(_ flow: SkieSwiftSharedFlow<T>) {
        coroutinesFlow = flow.coroutinesFlow
    }

    public var replayCache: [T?] {
        return coroutinesFlow.replayCache as! [T?]
    }

    public func makeAsyncIterator() -> SkieSwiftOptionalFlow<T>.Iterator {
        return SkieSwiftOptionalFlow<T>.Iterator(flow: coroutinesFlow)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinOptionalSharedFlow(coroutinesFlow)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalSharedFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalSharedFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalSharedFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalSharedFlow<T> {
        return SkieSwiftOptionalSharedFlow(source!)
    }
}
