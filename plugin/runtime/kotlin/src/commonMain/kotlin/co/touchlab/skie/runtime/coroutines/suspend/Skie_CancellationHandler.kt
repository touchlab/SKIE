package co.touchlab.skie.runtime.coroutines.suspend

import co.touchlab.skie.runtime.coroutines.suspend.internal.withLock
import platform.Foundation.NSLock

@Suppress("ClassName")
class Skie_CancellationHandler {

    private var state = State.Active
    private var cancelCoroutine: ((String) -> Unit)? = null

    private val lock = NSLock()

    internal fun setCancellationCallback(callback: (String) -> Unit) {
        var callCallback = false

        lock.withLock {
            cancelCoroutine = callback

            if (state == State.WillBeCanceled) {
                state = State.WasCanceled
                callCallback = true
            }
        }

        if (callCallback) {
            callCancelCoroutine()
        }
    }

    fun cancel() {
        var callCallback = false

        lock.withLock {
            if (state == State.Active) {
                if (cancelCoroutine != null) {
                    state = State.WasCanceled
                    callCallback = true
                } else {
                    state = State.WillBeCanceled
                }
            }
        }

        if (callCallback) {
            callCancelCoroutine()
        }
    }

    private fun callCancelCoroutine() {
        cancelCoroutine!!.invoke("Canceled from Swift.")
    }

    private enum class State {
        Active, WillBeCanceled, WasCanceled
    }
}
