package co.touchlab.skie.plugin.shim.impl

import org.jetbrains.kotlin.konan.properties.resolvablePropertyString
import java.util.Properties

/**
 * Kotlin < 2.4.20 provides `resolvablePropertyString` in the `org.jetbrains.kotlin.konan.properties` package.
 */
internal fun Properties.resolvablePropertyStringCompat(key: String, suffix: String?): String? =
    resolvablePropertyString(key, suffix)
