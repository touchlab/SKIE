@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.SkiePhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal val SkiePhase.Context.konanContext: KonanContext
    get() = commonBackendContext as KonanContext
