import Foundation

public final class SkieSwiftOptionalMutableStateFlow<T>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftFlowIterator<T?>

    public typealias Element = T?

    public typealias _ObjectiveCType = SkieKotlinOptionalMutableStateFlow<Swift.AnyObject>

    internal let delegate: Skie.KotlinxCoroutinesCore.MutableStateFlow.__Kotlin

    internal init(internal flow: Skie.KotlinxCoroutinesCore.MutableStateFlow.__Kotlin) {
        delegate = flow
    }

    public var replayCache: [T?] {
        return delegate.replayCache as! [T?]
    }

    public func emit(value: T?) async throws {
        try await delegate.emit(value: value)
    }

    public func tryEmit(value: T?) -> Bool {
        return delegate.tryEmit(value: value)
    }

    public var subscriptionCount: SkieSwiftStateFlow<KotlinInt> {
        return bridgeSubscriptionCount(delegate.subscriptionCount)
    }

    public var value: T? {
        get {
            return delegate.value as! T?
        }
        set {
            delegate.setValue(newValue)
        }
    }

    public func compareAndSet(expect: T?, update: T?) -> Bool {
        return delegate.compareAndSet(expect: expect, update: update)
    }

    public func makeAsyncIterator() -> SkieSwiftFlowIterator<T?> {
        return SkieSwiftFlowIterator(flow: delegate)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinOptionalMutableStateFlow(delegate)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalMutableStateFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftOptionalMutableStateFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalMutableStateFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftOptionalMutableStateFlow<T> {
        return SkieSwiftOptionalMutableStateFlow(internal: source!)
    }
}
