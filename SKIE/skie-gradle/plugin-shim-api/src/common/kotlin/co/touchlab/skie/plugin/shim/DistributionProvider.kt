package co.touchlab.skie.plugin.shim

import org.jetbrains.kotlin.konan.target.Distribution

interface DistributionProvider {

    fun provideDistribution(
        konanHome: String,
        propertyOverrides: Map<String, String>?,
    ): Distribution
}
