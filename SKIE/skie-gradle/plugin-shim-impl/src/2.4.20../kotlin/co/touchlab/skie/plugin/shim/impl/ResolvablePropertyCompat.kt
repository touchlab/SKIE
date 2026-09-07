package co.touchlab.skie.plugin.shim.impl

import org.jetbrains.kotlin.io.resolvablePropertyString
import java.util.Properties

/**
 * Kotlin 2.4.20 moved `resolvablePropertyString` from `org.jetbrains.kotlin.konan.properties` to `org.jetbrains.kotlin.io`.
 */
internal fun Properties.resolvablePropertyStringCompat(key: String, suffix: String?): String? =
    resolvablePropertyString(key, suffix)
