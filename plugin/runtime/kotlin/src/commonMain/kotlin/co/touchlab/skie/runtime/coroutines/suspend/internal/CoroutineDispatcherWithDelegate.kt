package co.touchlab.skie.runtime.coroutines.suspend.internal

import co.touchlab.skie.runtime.coroutines.suspend.Skie_DispatcherDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

internal class CoroutineDispatcherWithDelegate(private val delegate: Skie_DispatcherDelegate) : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        delegate.dispatch(block)
    }
}
