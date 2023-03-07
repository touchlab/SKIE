package co.touchlab.skie.acceptancetests.framework

import org.jetbrains.kotlin.cli.common.toBooleanLenient
import java.util.stream.Stream

fun <T> Collection<T>.testStream(): Stream<T> = if (System.getenv("DISABLE_PARALLEL_TESTS").toBooleanLenient() == true) {
    println("Parallel testing disabled!")
    stream()
} else {
    parallelStream()
}
