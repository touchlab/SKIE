package co.touchlab.skie.configuration.features

import kotlinx.serialization.Serializable

@Serializable
class SkieFeatureSet(private val features: Set<SkieFeature> = emptySet()) {

    constructor(vararg features: SkieFeature) : this(features.toSet())

    operator fun contains(feature: SkieFeature): Boolean =
        feature in features

    operator fun plus(enabledFeatures: SkieFeatureSet): SkieFeatureSet =
        SkieFeatureSet(this.features + enabledFeatures.features)

    operator fun minus(disabledFeatures: SkieFeatureSet): SkieFeatureSet =
        SkieFeatureSet(this.features - disabledFeatures.features)

    fun intersect(other: SkieFeatureSet): SkieFeatureSet =
        SkieFeatureSet(this.features.intersect(other.features))

    fun isEmpty(): Boolean =
        features.isEmpty()
}
