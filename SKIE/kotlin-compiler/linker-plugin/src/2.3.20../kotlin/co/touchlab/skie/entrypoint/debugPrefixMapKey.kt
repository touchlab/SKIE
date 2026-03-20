@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val debugPrefixMapKey: CompilerConfigurationKey<Map<String, String>>
    get() = KonanConfigKeys.DEBUG_PREFIX_MAP
