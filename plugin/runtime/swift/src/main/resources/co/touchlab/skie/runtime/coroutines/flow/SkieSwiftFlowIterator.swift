import Foundation

public class SkieSwiftFlowIterator<T>: _Concurrency.AsyncIteratorProtocol {

    public typealias Element = T

    private let iterator: SkieColdFlowIterator<AnyObject>

    init(flow: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_flow_Flow) {
        iterator = SkieColdFlowIterator(flow: flow)
    }

    deinit {
        iterator.cancel()
    }

    public func next() async -> Element? {
        let hasNext = try? await SkieColdFlowIteratorKt.hasNext(iterator)

        if (hasNext?.boolValue ?? false) {
            return .some(iterator.next() as! Element)
        } else {
            return nil
        }
    }
}
