package co.touchlab.skie.configuration

enum class AnalyticsTier(val features: Set<SkieFeature>) {

    All(
        // WIP
    ),

    NoTracking(
        // WIP
    ),

    Anonymous(
        // WIP
    ),

    None;

    constructor(vararg features: SkieFeature) : this(features.toSet())
}
