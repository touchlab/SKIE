package co.touchlab.skie.configuration

enum class AnalyticsTier(val features: Set<SkieFeature>) {

    All(
        SkieFeature.Analytics_Compiler,
        SkieFeature.Analytics_Gradle,
        SkieFeature.Analytics_Hardware,
        SkieFeature.Analytics_GradlePerformance,
        SkieFeature.Analytics_SkiePerformance,
        SkieFeature.Analytics_SkieConfiguration,
        SkieFeature.Analytics_Sysctl,
        SkieFeature.Analytics_OpenSource,
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
