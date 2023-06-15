package co.touchlab.skie.plugin.shim

interface ShimTest {
    val hello: String

    fun world(): ShimTest

    companion object {
        lateinit var instance: ShimTest
    }
}
