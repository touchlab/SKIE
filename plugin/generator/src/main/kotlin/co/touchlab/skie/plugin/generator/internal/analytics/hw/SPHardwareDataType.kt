package co.touchlab.skie.plugin.generator.internal.analytics.hw

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SPHardwareDataType(
    @SerialName("SPHardwareDataType") val spHardwareDataList: List<SPHardwareData>,
)
