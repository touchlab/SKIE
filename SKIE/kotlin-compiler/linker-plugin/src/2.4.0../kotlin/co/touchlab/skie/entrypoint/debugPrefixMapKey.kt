@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.config.NativeConfigurationKeys

internal val debugPrefixMapKey: CompilerConfigurationKey<Map<String, String>>
    get() = NativeConfigurationKeys.DEBUG_PREFIX_MAP
