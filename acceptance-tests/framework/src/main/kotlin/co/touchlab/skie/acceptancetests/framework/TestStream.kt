package co.touchlab.skie.acceptancetests.framework

import java.util.stream.Stream

fun <T> Collection<T>.testStream(): Stream<T> = if (System.getenv("DISABLE_PARALLEL_TESTS") != null) {
    stream()
} else {
    parallelStream()
}
