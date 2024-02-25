package co.touchlab.skie.plugin.shim

import org.jetbrains.kotlin.konan.target.Distribution

object DistributionProviderImpl: DistributionProvider {
    override fun provideDistribution(
        konanHome: String,
        propertyOverrides: Map<String, String>?,
    ): Distribution {
        return Distribution(konanHome = konanHome, propertyOverrides = propertyOverrides)
    }
}
