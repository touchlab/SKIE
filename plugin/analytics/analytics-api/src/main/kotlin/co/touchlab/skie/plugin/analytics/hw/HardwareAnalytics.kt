package co.touchlab.skie.plugin.analytics.hw

import kotlinx.serialization.Serializable

@Serializable
data class HardwareAnalytics(
    val bootRomVersion: String,
    val chipType: String,
    val machineModel: String,
    val machineName: String,
    val numberOfProcessors: String,
    val osLoaderVersion: String,
    val physicalMemory: String,
    val hashedPlatformUUID: String,
)
