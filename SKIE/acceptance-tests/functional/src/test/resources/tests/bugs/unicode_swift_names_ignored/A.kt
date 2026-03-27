package tests.bugs.unicode_swift_names_ignored

import kotlin.experimental.ExperimentalObjCName

sealed interface A

class Å: A
class ℬ: A
enum class C: A {
    C1;

    val ℙℛoperty: Int = 1

    fun mẹthoδ(): Int = -1
}
enum class Δ: A {
    δ1;
}
