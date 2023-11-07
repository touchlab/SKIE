package co.touchlab.skie.test

import kotlin.test.assertEquals

class GradleTests {

    @Smoke
    @SkieTest
    @OnlyFor(configurations = [BuildConfiguration.Debug])
    @OnlyIos
    fun `basic project`(target: BinaryTarget, config: BuildConfiguration, linkage: LinkMode) {
        println("Hello $target, $config!")
        assertEquals(BinaryTarget.IOS_X64, target)
        assertEquals(BuildConfiguration.Debug, config)
        // Run tests with param target
    }

}
