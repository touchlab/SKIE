package co.touchlab.skie.analytics.environment

data class CompilerEnvironmentAnalytics(
    val jvmVersion: String,
    val kotlinVersion: String,
    val availableProcessors: Int,
    val totalMemory: Long,
    val maxMemory: Long,
) {
    init {
//         Runtime.getRuntime().availableProcessors()
//         Runtime.getRuntime().totalMemory()
//         Runtime.getRuntime().maxMemory()
    }
}
