@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext

internal val PhaseContext.konanConfig: KonanConfig
    get() = config as KonanConfig
