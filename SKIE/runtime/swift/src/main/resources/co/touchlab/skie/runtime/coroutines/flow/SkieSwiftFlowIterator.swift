import Foundation

public class SkieSwiftFlowIterator<T>: _Concurrency.AsyncIteratorProtocol {

    public typealias Element = T

    private let iterator: SkieColdFlowIterator<AnyObject>

    init(flow: Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.Flow.__Kotlin) {
        iterator = SkieColdFlowIterator(flow: flow)
    }

    deinit {
        iterator.cancel()
    }

    public func next() async -> Element? {
        let hasNext = try? await skie(iterator).hasNext()

        if (hasNext?.boolValue ?? false) {
            return .some(iterator.next() as! Element)
        } else {
            return nil
        }
    }
}
