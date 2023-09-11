import Foundation

public final class SkieSwiftOptionalFlow<T>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftFlowIterator<T?>

    public typealias Element = T?

    public typealias _ObjectiveCType = SkieKotlinOptionalFlow<Swift.AnyObject>

    internal let delegate: Skie.KotlinxCoroutinesCore.Flow.__Kotlin

    internal init(internal flow: Skie.KotlinxCoroutinesCore.Flow.__Kotlin) {
        delegate = flow
    }

    public func makeAsyncIterator() -> SkieSwiftFlowIterator<T?> {
        return SkieSwiftFlowIterator(flow: delegate)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinOptionalFlow(delegate)
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
        return SkieSwiftOptionalFlow(internal: source!)
    }
}
