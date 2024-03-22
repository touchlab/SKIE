package co.touchlab.skie.util.parallel

suspend fun <T, R> Collection<T>.parallelFlatMap(optimalChunkSize: Int = 100, transform: suspend (T) -> Iterable<R>): List<R> =
    parallelMap(optimalChunkSize, transform).flatten()
