package co.touchlab.skie.configuration

enum class AnalyticsTier(val configurationFlags: Set<SkieConfigurationFlag>) {

    None,

    Anonymous(
        SkieConfigurationFlag.Analytics_GradlePerformance,
        SkieConfigurationFlag.Analytics_GradleEnvironment,
        SkieConfigurationFlag.Analytics_CompilerEnvironment,
        SkieConfigurationFlag.Analytics_Hardware,
        SkieConfigurationFlag.Analytics_SkiePerformance,
        SkieConfigurationFlag.Analytics_SkieConfiguration,
        SkieConfigurationFlag.Analytics_CompilerConfiguration,
        SkieConfigurationFlag.Analytics_Git,
        SkieConfigurationFlag.Analytics_Project,
        SkieConfigurationFlag.Analytics_Modules,
    );

    constructor(vararg configurationFlags: SkieConfigurationFlag) : this(configurationFlags.toSet())

    constructor(
        parent: AnalyticsTier,
        vararg configurationFlags: SkieConfigurationFlag,
    ) : this(parent.configurationFlags + configurationFlags.toSet())
}
