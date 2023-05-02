import Foundation

class AsyncStreamDispatcherDelegate: Skie.class__co_touchlab_skie_kotlin__co_touchlab_skie_runtime_coroutines_suspend_Skie_DispatcherDelegate {

    private let continuation: _Concurrency.AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable>.Continuation

    init(continuation: _Concurrency.AsyncStream<Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable>.Continuation) {
        self.continuation = continuation
    }

    func dispatch(block: Skie.class__org_jetbrains_kotlinx_kotlinx_coroutines_core__kotlinx_coroutines_Runnable) {
        let result = continuation.yield(block)

        if case .terminated = result {
            Swift.fatalError("Cannot dispatch blocks after the dispatcher is stopped. This error might have happened by leaking the dispatcher from the original job.")
        }
    }

    func stop() {
        continuation.finish()
    }
}
