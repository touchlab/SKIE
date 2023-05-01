public final class SkieSwiftStateFlow<T>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftFlowIterator<T>

    public typealias Element = T

    public typealias _ObjectiveCType = SkieKotlinStateFlow<Swift.AnyObject>

    internal let delegate: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_StateFlow

    internal init(internal flow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_StateFlow) {
        delegate = flow
    }

    public var replayCache: [T] {
        return delegate.replayCache as! [T]
    }

    public var value: T {
        return delegate.value as! T
    }

    public func makeAsyncIterator() -> SkieSwiftFlowIterator<T> {
        return SkieSwiftFlowIterator(flow: delegate)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinStateFlow(delegate)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftStateFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftStateFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftStateFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftStateFlow<T> {
        return SkieSwiftStateFlow(internal: source!)
    }
}
