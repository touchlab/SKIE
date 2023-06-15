package co.touchlab.skie.plugin.shim


expect class ShimTestImpl(): ShimTest {
    override val hello: String

    override fun world(): ShimTest
}
