package co.touchlab.skie.test.util

enum class LinkMode {
    Dynamic,
    Static;

    val isStatic: Boolean
        get() = this == Static
}
