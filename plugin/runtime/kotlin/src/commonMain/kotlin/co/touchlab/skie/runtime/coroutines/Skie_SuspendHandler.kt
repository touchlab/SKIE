package co.touchlab.skie.runtime.coroutines

import co.touchlab.skie.runtime.coroutines.internal.CoroutineDispatcherWithDelegate
import co.touchlab.skie.runtime.coroutines.internal.toNSError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@Suppress("ClassName")
class Skie_SuspendHandler(
    private val cancellationHandler: Skie_CancellationHandler,
    dispatcherDelegate: Skie_DispatcherDelegate,
    private val onResult: (Skie_SuspendResult) -> Unit,
) {

    private val dispatcher = CoroutineDispatcherWithDelegate(dispatcherDelegate)

    internal fun launch(checkedExceptions: Array<KClass<out Throwable>>, coroutine: suspend () -> Any?) {
        CoroutineScope(dispatcher).launch {
            cancellationHandler.setCancellationCallback {
                cancel()
            }

            try {
                val result = coroutine.invoke()

                onResult(Skie_SuspendResult.Success(result))
            } catch (_: CancellationException) {
                onResult(Skie_SuspendResult.Canceled)
            } catch (e: Throwable) {
                if (e.isCheckedException(checkedExceptions)) {
                    throwSwiftException(e)
                } else {
                    throw e
                }
            }
        }
    }

    private fun Throwable.isCheckedException(checkedExceptions: Array<out KClass<out Throwable>>): Boolean =
        checkedExceptions.any { it.isInstance(this) }

    private fun throwSwiftException(e: Throwable) {
        val error = e.toNSError()

        onResult(Skie_SuspendResult.Error(error))
    }
}

