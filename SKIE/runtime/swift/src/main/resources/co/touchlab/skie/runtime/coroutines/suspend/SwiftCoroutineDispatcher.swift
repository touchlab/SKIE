import Foundation

struct SwiftCoroutineDispatcher {

    static func dispatch<T>(
        coroutine: (Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendHandler.__Kotlin) -> Swift.Void
    ) async throws -> T {
        let cancellationHandler = Skie.co_touchlab_skie__runtime_kotlin.Skie_CancellationHandler.__Kotlin()

        return try await _Concurrency.withTaskCancellationHandler(operation: {
            try await dispatchCancellable(coroutine: coroutine, cancellationHandler: cancellationHandler)
        }, onCancel: {
            cancellationHandler.cancel()
        })
    }

    private static func dispatchCancellable<T>(
        coroutine: (Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendHandler.__Kotlin) -> Swift.Void,
        cancellationHandler: Skie.co_touchlab_skie__runtime_kotlin.Skie_CancellationHandler.__Kotlin
    ) async throws -> T {
        var result: Swift.Result<T, Swift.Error>? = nil

        let dispatcher = createDispatcher(coroutine: coroutine, cancellationHandler: cancellationHandler) {
            result = $0
        }

        await executeWithoutCancellation(dispatcher: dispatcher)

        return try unwrap(result: result)
    }

    private static func createDispatcher<T>(
        coroutine: (Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendHandler.__Kotlin) -> Swift.Void,
        cancellationHandler: Skie.co_touchlab_skie__runtime_kotlin.Skie_CancellationHandler.__Kotlin,
        onResult: @escaping (Swift.Result<T, Swift.Error>) -> Swift.Void
    ) -> _Concurrency.AsyncStream<Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.Runnable.__Kotlin> {
        return _Concurrency.AsyncStream<Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.Runnable.__Kotlin> { continuation in
            let dispatcherDelegate = AsyncStreamDispatcherDelegate(continuation: continuation)

            let suspendHandler = Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendHandler.__Kotlin(
                cancellationHandler: cancellationHandler,
                dispatcherDelegate: dispatcherDelegate,
                onResult: { suspendResult in
                    let result: Swift.Result<T, Swift.Error> = convertToResult(suspendResult: suspendResult)

                    onResult(result)

                    dispatcherDelegate.stop()
                }
            )

            coroutine(suspendHandler)
        }
    }

    private static func convertToResult<T>(
        suspendResult: Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendResult.__Kotlin
    ) -> Swift.Result<T, Swift.Error> {
        if let suspendResult = suspendResult as? Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendResult.Success.__Kotlin {
            if T.self == Swift.Void.self {
                return .success(Swift.Void() as! T)
            } else {
                return .success(suspendResult.value as! T)
            }
        } else if let suspendResult = suspendResult as? Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendResult.Error.__Kotlin {
            return .failure(suspendResult.error)
        } else if suspendResult is Skie.co_touchlab_skie__runtime_kotlin.Skie_SuspendResult.Canceled.__Kotlin {
            return .failure(_Concurrency.CancellationError())
        } else {
            fatalError("Unknown suspend result. This is most likely a bug in SKIE.")
        }
    }

    private static func executeWithoutCancellation(dispatcher: _Concurrency.AsyncStream<Skie.org_jetbrains_kotlinx__kotlinx_coroutines_core.Runnable.__Kotlin>) async {
        await _Concurrency.Task {
            for await block in dispatcher {
                block.run()
            }
        }.value
    }

    private static func unwrap<T>(result: Swift.Result<T, Swift.Error>?) throws -> T {
        if let result = result {
            switch result {
            case .success(let value):
                return value
            case .failure(let error):
                throw error
            }
        } else {
            fatalError("Suspend execution ended without result! This is most likely a bug in SKIE.")
        }
    }
}
