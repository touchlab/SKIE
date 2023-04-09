package co.touchlab.skie.plugin.generator.internal.analytics.hw

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SPHardwareData(
    @SerialName("boot_rom_version") val bootRomVersion: String,
    @SerialName("chip_type") val chipType: String,
    @SerialName("machine_model") val machineModel: String,
    @SerialName("machine_name") val machineName: String,
    @SerialName("number_processors") val numberProcessors: String,
    @SerialName("os_loader_version") val osLoaderVersion: String,
    @SerialName("physical_memory") val physicalMemory: String,
    @SerialName("platform_UUID") val platformUUID: String,
)
