package co.touchlab.skie.configuration.features

import kotlinx.serialization.Serializable

@Serializable
class SkieFeatureSet(private val features: Set<SkieFeature> = emptySet()) {

    operator fun contains(feature: SkieFeature): Boolean =
        feature in features

    operator fun plus(enabledFeatures: SkieFeatureSet): SkieFeatureSet =
        SkieFeatureSet(this.features + enabledFeatures.features)
}
