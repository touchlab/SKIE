import Foundation

class AsyncStreamDispatcherDelegate: SKIE_co_touchlab_skie_runtime_coroutines_Skie_DispatcherDelegate {

    private let continuation: AsyncStream<SKIE_kotlinx_coroutines_Runnable>.Continuation

    private let lock = NSLock()

    private var isActive = true

    init(continuation: AsyncStream<SKIE_kotlinx_coroutines_Runnable>.Continuation) {
        self.continuation = continuation
    }

    func dispatch(block: SKIE_kotlinx_coroutines_Runnable) {
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
