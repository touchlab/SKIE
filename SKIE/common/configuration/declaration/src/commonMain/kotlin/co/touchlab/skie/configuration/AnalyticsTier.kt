package co.touchlab.skie.configuration

enum class AnalyticsTier(val features: Set<SkieFeature>) {

    All(
        // WIP
        SkieFeature.Analytics_Identifying_Project,
        SkieFeature.Analytics_Tracking_Project,
        SkieFeature.Analytics_Anonymous_GradlePerformance,
        SkieFeature.Analytics_Anonymous_GradleEnvironment,
        SkieFeature.Analytics_Identifying_Git,
        SkieFeature.Analytics_Anonymous_Git,
        SkieFeature.Analytics_Tracking_Hardware,
        SkieFeature.Analytics_Anonymous_Hardware,
    ),

    NoIdentifyingData(
        // WIP
    ),

    Anonymous(
        // WIP
    ),

    None;

    constructor(vararg features: SkieFeature) : this(features.toSet())
}
