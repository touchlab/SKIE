package co.touchlab.skie.configuration

enum class AnalyticsTier(val configurationFlags: Set<SkieConfigurationFlag>) {

    None,

    Anonymous(
        SkieConfigurationFlag.Analytics_Anonymous_GradlePerformance,
        SkieConfigurationFlag.Analytics_Anonymous_GradleEnvironment,
        SkieConfigurationFlag.Analytics_Anonymous_CompilerEnvironment,
        SkieConfigurationFlag.Analytics_Anonymous_Hardware,
        SkieConfigurationFlag.Analytics_Anonymous_SkiePerformance,
        SkieConfigurationFlag.Analytics_Anonymous_SkieConfiguration,
        SkieConfigurationFlag.Analytics_Anonymous_CompilerConfiguration,
        SkieConfigurationFlag.Analytics_Anonymous_Git,
        SkieConfigurationFlag.Analytics_Anonymous_Project,
        SkieConfigurationFlag.Analytics_Anonymous_Declarations,
        SkieConfigurationFlag.Analytics_Anonymous_Libraries,
    ),

    All(
        Anonymous,
        SkieConfigurationFlag.Analytics_Identifying_SkieConfiguration,
        SkieConfigurationFlag.Analytics_Identifying_CompilerConfiguration,
        SkieConfigurationFlag.Analytics_Identifying_Git,
        SkieConfigurationFlag.Analytics_Identifying_Project,
        SkieConfigurationFlag.Analytics_Identifying_LocalModules,
    );

    constructor(vararg configurationFlags: SkieConfigurationFlag) : this(configurationFlags.toSet())

    constructor(
        parent: AnalyticsTier,
        vararg configurationFlags: SkieConfigurationFlag,
    ) : this(parent.configurationFlags + configurationFlags.toSet())
}
