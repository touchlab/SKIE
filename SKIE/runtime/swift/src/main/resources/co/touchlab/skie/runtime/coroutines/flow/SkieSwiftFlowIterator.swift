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
        do {
            let hasNext = try await skie(iterator).hasNext()

            if (hasNext.boolValue) {
                return .some(iterator.next() as! Element)
            } else {
                return nil
            }
        } catch is _Concurrency.CancellationError {
            await cancelTask()

            return nil
        } catch {
            Swift.fatalError("Unexpected error: \(error)")
        }
    }

    private func cancelTask() async {
        _Concurrency.withUnsafeCurrentTask { task in
            task?.cancel()
        }
    }
}
