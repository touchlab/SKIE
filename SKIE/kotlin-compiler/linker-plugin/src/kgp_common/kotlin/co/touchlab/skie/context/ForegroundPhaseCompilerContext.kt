package co.touchlab.skie.context

import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.phases.ForegroundPhase
import org.jetbrains.kotlin.backend.konan.KonanConfig

interface ForegroundPhaseCompilerContext : ForegroundPhase.Context {

    val descriptorProvider: DescriptorProvider

    val descriptorConfigurationProvider: DescriptorConfigurationProvider

    val konanConfig: KonanConfig
}
