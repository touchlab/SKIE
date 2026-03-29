package co.touchlab.skie.acceptancetests.framework

import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.kotlin.cli.common.toBooleanLenient

@OptIn(ExperimentalCoroutinesApi::class)
fun testDispatcher(): CoroutineDispatcher =
    if (TestProperties["DISABLE_PARALLEL_TESTS"].toBooleanLenient() == true) {
        println("Parallel testing disabled!")
        Dispatchers.IO.limitedParallelism(1)
    } else {
        Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())
    }
