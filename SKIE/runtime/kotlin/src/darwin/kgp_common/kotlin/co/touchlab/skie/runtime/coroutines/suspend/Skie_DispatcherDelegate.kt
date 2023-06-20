package co.touchlab.skie.runtime.coroutines.suspend

import kotlinx.coroutines.Runnable

@Suppress("ClassName")
interface Skie_DispatcherDelegate {

    fun dispatch(block: Runnable)
}
