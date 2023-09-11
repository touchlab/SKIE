import Foundation

class AsyncStreamDispatcherDelegate: Skie.RuntimeKotlin.Skie_DispatcherDelegate.__Kotlin {

    private let continuation: _Concurrency.AsyncStream<Skie.KotlinxCoroutinesCore.Runnable.__Kotlin>.Continuation

    init(continuation: _Concurrency.AsyncStream<Skie.KotlinxCoroutinesCore.Runnable.__Kotlin>.Continuation) {
        self.continuation = continuation
    }

    func dispatch(block: Skie.KotlinxCoroutinesCore.Runnable.__Kotlin) {
        let result = continuation.yield(block)

        if case .terminated = result {
            Swift.fatalError("Cannot dispatch blocks after the dispatcher is stopped. This error might have happened by leaking the dispatcher from the original job.")
        }
    }

    func stop() {
        continuation.finish()
    }
}
