package co.touchlab.skie.util.parallel

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.ScheduledPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

context(ScheduledPhase.Context)
suspend fun <T, R> Collection<T>.parallelMap(optimalChunkSize: Int = 100, transform: suspend (T) -> R): List<R> {
    if (SkieConfigurationFlag.Build_ParallelSkieCompilation.isDisabled) {
        return map { transform(it) }
    }

    val input = this@parallelMap

    if (input.size <= 1) {
        return input.map { transform(it) }
    }

    return coroutineScope {
        val availableProcessors = Runtime.getRuntime().availableProcessors()

        val chunkSize = when {
            input.size <= availableProcessors -> 1
            input.size <= availableProcessors * optimalChunkSize -> input.size / availableProcessors
            else -> optimalChunkSize
        }

        withContext(Dispatchers.Default) {
            input.chunked(chunkSize).map { chunk -> async { chunk.map { transform(it) } } }.awaitAll().flatten()
        }
    }
}
