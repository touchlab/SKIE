package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.util.Command
import co.touchlab.skie.util.hashed

fun getHashedPlatformUUID(): String =
    Command("system_profiler", "SPHardwareDataType")
        .execute()
        .outputLines
        .single { it.contains("Hardware UUID") }
        .split(":")[1]
        .trim()
        .hashed()
