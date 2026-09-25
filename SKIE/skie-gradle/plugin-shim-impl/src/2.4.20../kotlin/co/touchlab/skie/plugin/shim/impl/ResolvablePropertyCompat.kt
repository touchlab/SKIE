package co.touchlab.skie.plugin.shim.impl

import org.jetbrains.kotlin.io.resolvablePropertyString
import java.util.Properties

internal fun Properties.resolvablePropertyStringCompat(key: String, suffix: String?): String? =
    resolvablePropertyString(key, suffix)
