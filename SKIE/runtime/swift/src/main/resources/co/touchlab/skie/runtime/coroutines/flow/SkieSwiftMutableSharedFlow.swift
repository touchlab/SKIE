import Foundation

public final class SkieSwiftMutableSharedFlow<T>: _Concurrency.AsyncSequence, Swift._ObjectiveCBridgeable {

    public typealias AsyncIterator = SkieSwiftFlowIterator<T>

    public typealias Element = T

    public typealias _ObjectiveCType = SkieKotlinMutableSharedFlow<Swift.AnyObject>

    internal let delegate: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_MutableSharedFlow

    internal init(internal flow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_MutableSharedFlow) {
        delegate = flow
    }

    public var replayCache: [T] {
        return delegate.replayCache as! [T]
    }

    public func emit(value: T) async throws {
        try await delegate.emit(value: value)
    }

    public func tryEmit(value: T) -> Bool {
        return delegate.tryEmit(value: value)
    }

    public var subscriptionCount: SkieSwiftStateFlow<KotlinInt> {
        return bridgeSubscriptionCount(delegate.subscriptionCount)
    }

    public func resetReplayCache() {
        delegate.resetReplayCache()
    }

    public func makeAsyncIterator() -> SkieSwiftFlowIterator<T> {
        return SkieSwiftFlowIterator(flow: delegate)
    }

    public func _bridgeToObjectiveC() -> _ObjectiveCType {
        return SkieKotlinMutableSharedFlow(delegate)
    }

    public static func _forceBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftMutableSharedFlow<T>?) {
        result = fromObjectiveC(source)
    }

    public static func _conditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType, result: inout SkieSwiftMutableSharedFlow<T>?) -> Bool {
        result = fromObjectiveC(source)
        return true
    }

    public static func _unconditionallyBridgeFromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftMutableSharedFlow<T> {
        return fromObjectiveC(source)
    }

    private static func fromObjectiveC(_ source: _ObjectiveCType?) -> SkieSwiftMutableSharedFlow<T> {
        return SkieSwiftMutableSharedFlow(internal: source!)
    }
}

internal func bridgeSubscriptionCount(_ subscriptionCount: SkieSwiftStateFlow<KotlinInt>) -> SkieSwiftStateFlow<KotlinInt> {
    return subscriptionCount
}

internal func bridgeSubscriptionCount(_ subscriptionCount: any Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_StateFlow) -> SkieSwiftStateFlow<KotlinInt> {
    return SkieSwiftStateFlow(internal: subscriptionCount)
}
