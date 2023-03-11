import Foundation

public final class SkieSwiftSharedFlow<T: Swift.AnyObject>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftFlow<T>.Iterator

    public typealias Element = T

    public typealias _ObjectiveCType = SkieKotlinSharedFlow<Swift.AnyObject>

    internal let delegate: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_SharedFlow

    internal init(_ flow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_SharedFlow) {
        delegate = flow
    }

    public var replayCache: [T] {
        return delegate.replayCache as! [T]
    }

    public func makeAsyncIterator() -> SkieSwiftFlow<T>.Iterator {
        return SkieSwiftFlow<T>.Iterator(flow: delegate)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinSharedFlow(delegate)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftSharedFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftSharedFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftSharedFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftSharedFlow<T> {
        return SkieSwiftSharedFlow(source!)
    }
}