package co.touchlab.skie.configuration

enum class AnalyticsTier(val features: Set<SkieFeature>) {

    None,

    Anonymous(
        SkieFeature.Analytics_Anonymous_GradlePerformance,
        SkieFeature.Analytics_Anonymous_GradleEnvironment,
        SkieFeature.Analytics_Anonymous_CompilerEnvironment,
        SkieFeature.Analytics_Anonymous_Hardware,
        SkieFeature.Analytics_Anonymous_SkiePerformance,
        SkieFeature.Analytics_Anonymous_SkieConfiguration,
        SkieFeature.Analytics_Anonymous_CompilerConfiguration,
        SkieFeature.Analytics_Anonymous_Git,
    ),

    NoIdentifyingData(
        Anonymous,
        SkieFeature.Analytics_Tracking_Project,
        SkieFeature.Analytics_Tracking_Hardware,
    ),

    All(
        NoIdentifyingData,
        SkieFeature.Analytics_Identifying_SkieConfiguration,
        SkieFeature.Analytics_Identifying_CompilerConfiguration,
        SkieFeature.Analytics_Identifying_Git,
        SkieFeature.Analytics_Identifying_Project,
    );

    constructor(vararg features: SkieFeature) : this(features.toSet())

    constructor(parent: AnalyticsTier, vararg features: SkieFeature) : this(parent.features + features.toSet())
}
