import Foundation

@available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)
class AsyncStreamDispatcherDelegate: Skie.co_touchlab_skie_runtime_coroutines_Skie_DispatcherDelegate {

    private let continuation: AsyncStream<Skie.kotlinx_coroutines_Runnable>.Continuation

    private let lock = NSLock()

    private var isActive = true

    init(continuation: AsyncStream<Skie.kotlinx_coroutines_Runnable>.Continuation) {
        self.continuation = continuation
    }

    func dispatch(block: Skie.kotlinx_coroutines_Runnable) {
        lock.withLock {
            if !isActive {
                fatalError("Cannot dispatch block after dispatcher is stopped. This error might have happened by leaking the dispatcher from the original job.")
            }

            continuation.yield(block)
        }
    }

    func stop() {
        lock.withLock {
            isActive = false

            continuation.finish()
        }
    }
}
