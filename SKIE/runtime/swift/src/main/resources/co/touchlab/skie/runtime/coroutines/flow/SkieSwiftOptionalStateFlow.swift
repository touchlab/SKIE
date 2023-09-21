import Foundation

public final class SkieSwiftOptionalStateFlow<T>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftFlowIterator<T?>

    public typealias Element = T?

    public typealias _ObjectiveCType = SkieKotlinOptionalStateFlow<Swift.AnyObject>

    internal let delegate: Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.StateFlow.__Kotlin

    internal init(internal flow: Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.StateFlow.__Kotlin) {
        delegate = flow
    }

    public var replayCache: [T?] {
        return delegate.replayCache as! [T?]
    }

    public var value: T? {
        return delegate.value as! T?
    }

    public func makeAsyncIterator() -> SkieSwiftFlowIterator<T?> {
        return SkieSwiftFlowIterator(flow: delegate)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinOptionalStateFlow(delegate)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalStateFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalStateFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalStateFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalStateFlow<T> {
        return SkieSwiftOptionalStateFlow(internal: source!)
    }
}
