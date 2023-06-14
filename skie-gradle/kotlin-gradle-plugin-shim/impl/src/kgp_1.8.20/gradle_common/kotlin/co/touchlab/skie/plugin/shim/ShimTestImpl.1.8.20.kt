package co.touchlab.skie.plugin.shim

actual class ShimTestImpl actual constructor(): ShimTest {
    actual override val hello: String = "Hello"

    actual override fun world(): ShimTest {
        return this
    }
}
