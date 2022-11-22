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
        val job = coroutine.launch()

        job.invokeOnCompletion { cause ->
            handleJobCompletion(cause, checkedExceptions)
        }

        cancellationHandler.setCancellationCallback {
            job.cancel(it)
        }
    }

    private fun (suspend () -> Any?).launch(): Job =
        CoroutineScope(dispatcher).launch {
            val result = invoke()

            onResult(Skie_SuspendResult.Success(result))
        }

    private fun handleJobCompletion(cause: Throwable?, checkedExceptions: Array<out KClass<out Throwable>>) {
        when {
            cause is CancellationException -> onResult(Skie_SuspendResult.Canceled)
            cause?.isCheckedException(checkedExceptions) == true -> {
                val error = cause.toNSError()

                onResult(Skie_SuspendResult.Error(error))
            }
            cause != null -> throw cause
        }
    }

    private fun Throwable.isCheckedException(checkedExceptions: Array<out KClass<out Throwable>>): Boolean =
        checkedExceptions.any { it.isInstance(this) }
}

