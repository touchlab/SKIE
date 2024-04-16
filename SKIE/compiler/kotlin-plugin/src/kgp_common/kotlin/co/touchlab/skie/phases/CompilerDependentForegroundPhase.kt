package co.touchlab.skie.phases

import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.descriptor.cache.CachedObjCExportMapper
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.konan.target.AppleConfigurables

interface CompilerDependentForegroundPhase<in C : CompilerDependentForegroundPhase.Context> : ForegroundPhase<C> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        val descriptorProvider: DescriptorProvider

        val descriptorConfigurationProvider: DescriptorConfigurationProvider

        val konanConfig: KonanConfig

        val mapper: CachedObjCExportMapper
            get() = descriptorProvider.mapper

        val kotlinBuiltins: KotlinBuiltIns
            get() = descriptorProvider.builtIns

        val extraDescriptorBuiltins: ExtraDescriptorBuiltins
            get() = descriptorProvider.extraDescriptorBuiltins

        val configurables: AppleConfigurables
            get() = konanConfig.platform.configurables as AppleConfigurables
    }
}
