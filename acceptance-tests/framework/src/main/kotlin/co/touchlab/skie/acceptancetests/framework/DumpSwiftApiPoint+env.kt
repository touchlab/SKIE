package co.touchlab.skie.acceptancetests.framework

import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint

fun DumpSwiftApiPoint.Companion.fromTestEnv(): Set<DumpSwiftApiPoint> {
    return System.getenv("debugDumpSwiftApiAt")
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.map {
            DumpSwiftApiPoint.values().find { point ->
                point.name.equals(it, ignoreCase = true)
            } ?: error("Invalid Swift API dump point $it")
        }
        ?.toSet() ?: emptySet()
}
