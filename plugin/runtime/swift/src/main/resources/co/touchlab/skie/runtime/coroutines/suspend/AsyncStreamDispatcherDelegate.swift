import Foundation

@available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)
class AsyncStreamDispatcherDelegate: Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_suspend_Skie_DispatcherDelegate {

    private let continuation: _Concurrency.AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable>.Continuation

    private let lock = Foundation.NSLock()

    private var isActive = true

    init(continuation: _Concurrency.AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable>.Continuation) {
        self.continuation = continuation
    }

    func dispatch(block: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable) {
        lock.lock()
        defer {
            lock.unlock()
        }

        if !isActive {
            fatalError("Cannot dispatch block after dispatcher is stopped. This error might have happened by leaking the dispatcher from the original job.")
        }

        continuation.yield(block)
    }

    func stop() {
        lock.lock()
        defer {
            lock.unlock()
        }

        isActive = false

        continuation.finish()
    }
}
