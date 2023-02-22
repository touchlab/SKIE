package co.touchlab.skie.runtime.coroutines.suspend

import platform.Foundation.NSError

@Suppress("ClassName")
sealed class Skie_SuspendResult {

    data class Success(val value: Any?) : Skie_SuspendResult()

    data class Error(val error: NSError) : Skie_SuspendResult()

    object Canceled : Skie_SuspendResult()
}
