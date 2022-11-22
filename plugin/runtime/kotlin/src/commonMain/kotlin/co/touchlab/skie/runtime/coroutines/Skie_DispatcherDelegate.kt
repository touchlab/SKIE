package co.touchlab.skie.runtime.coroutines

import kotlinx.coroutines.Runnable

@Suppress("ClassName")
interface Skie_DispatcherDelegate {

    fun dispatch(block: Runnable)
}
