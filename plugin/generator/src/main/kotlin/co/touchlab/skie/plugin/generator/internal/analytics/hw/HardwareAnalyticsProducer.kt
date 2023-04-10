package co.touchlab.skie.plugin.generator.internal.analytics.hw

import co.touchlab.skie.plugin.analytics.hw.HardwareAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.Command
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.Base64

object HardwareAnalyticsProducer : AnalyticsProducer {

    override val name: String = "hw"

    private val json = Json { ignoreUnknownKeys = true }

    override fun produce(): ByteArray =
        Json.encodeToString(getHwAnalytics()).toByteArray()

    private fun getHwAnalytics(): HardwareAnalytics {
        val hwData = queryHwData()

        val parsedData = json.decodeFromString<SPHardwareDataType>(hwData)

        val hardwareData = parsedData.spHardwareDataList.single()

        return hardwareData.toHardwareAnalytics()
    }

    private fun queryHwData(): String =
        Command("system_profiler", "SPHardwareDataType", "-json")
            .execute()
            .outputLines
            .joinToString(System.lineSeparator())
}

private fun SPHardwareData.toHardwareAnalytics(): HardwareAnalytics =
    HardwareAnalytics(
        bootRomVersion = this.bootRomVersion,
        chipType = this.chipType,
        machineModel = this.machineModel,
        machineName = this.machineName,
        numberOfProcessors = this.numberProcessors,
        osLoaderVersion = this.osLoaderVersion,
        physicalMemory = this.physicalMemory,
        hashedPlatformUUID = this.platformUUID.hashed(),
    )

private fun String.hashed(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    val hash = digest.digest(toByteArray())

    return Base64.getEncoder().encodeToString(hash)
}
