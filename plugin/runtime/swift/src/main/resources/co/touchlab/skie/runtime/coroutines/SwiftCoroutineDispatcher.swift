import Foundation

struct SwiftCoroutineDispatcher {

    static func dispatch<T>(
        coroutine: (SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler) -> Void
    ) async throws -> T {
        let cancellationHandler = SKIE_co_touchlab_skie_runtime_coroutines_Skie_CancellationHandler()

        return try await withTaskCancellationHandler(operation: {
            try await dispatchCancellable(coroutine: coroutine, cancellationHandler: cancellationHandler)
        }, onCancel: {
            cancellationHandler.cancel()
        })
    }

    private static func dispatchCancellable<T>(
        coroutine: (SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler) -> Void,
        cancellationHandler: SKIE_co_touchlab_skie_runtime_coroutines_Skie_CancellationHandler
    ) async throws -> T {
        var result: Result<T, Error>? = nil

        let dispatcher = createDispatcher(coroutine: coroutine, cancellationHandler: cancellationHandler) {
            result = $0
        }

        await executeWithoutCancellation(dispatcher: dispatcher)

        return try unwrap(result: result)
    }

    private static func createDispatcher<T>(
        coroutine: (SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler) -> Void,
        cancellationHandler: SKIE_co_touchlab_skie_runtime_coroutines_Skie_CancellationHandler,
        onResult: @escaping (Result<T, Error>) -> Void
    ) -> AsyncStream<SKIE_kotlinx_coroutines_Runnable> {
        return AsyncStream<SKIE_kotlinx_coroutines_Runnable> { continuation in
            let dispatcherDelegate = AsyncStreamDispatcherDelegate(continuation: continuation)

            let suspendHandler = SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendHandler(
                cancellationHandler: cancellationHandler,
                dispatcherDelegate: dispatcherDelegate,
                onResult: { suspendResult in
                    let result: Result<T, Error> = convertToResult(suspendResult: suspendResult)

                    onResult(result)

                    dispatcherDelegate.stop()
                }
            )

            coroutine(suspendHandler)
        }
    }

    private static func convertToResult<T>(
        suspendResult: SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendResult
    ) -> Result<T, Error> {
        if let suspendResult = suspendResult as? SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendResult_Success {
            if T.self == Swift.Void.self {
                return .success(Swift.Void() as! T)
            } else {
                return .success(suspendResult.value as! T)
            }
        } else if let suspendResult = suspendResult as? SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendResult_Error {
            return .failure(suspendResult.error)
        } else if suspendResult is SKIE_co_touchlab_skie_runtime_coroutines_Skie_SuspendResult_Canceled {
            return .failure(CancellationError())
        } else {
            fatalError("Unknown suspend result. This is most likely a bug in SKIE.")
        }
    }

    private static func executeWithoutCancellation(dispatcher: AsyncStream<SKIE_kotlinx_coroutines_Runnable>) async {
        await Task {
            for await block in dispatcher {
                block.run()
            }
        }.value
    }

    private static func unwrap<T>(result: Result<T, Error>?) throws -> T {
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
