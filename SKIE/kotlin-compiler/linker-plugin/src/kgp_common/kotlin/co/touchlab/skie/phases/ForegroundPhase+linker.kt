package co.touchlab.skie.phases

import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.context.ForegroundPhaseCompilerContext
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.descriptor.cache.CachedObjCExportMapper
import co.touchlab.skie.util.DescriptorReporter
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.konan.target.AppleConfigurables

val ForegroundPhase.Context.descriptorProvider: DescriptorProvider
    get() = typedContext.descriptorProvider

val ForegroundPhase.Context.descriptorConfigurationProvider: DescriptorConfigurationProvider
    get() = typedContext.descriptorConfigurationProvider

val ForegroundPhase.Context.konanConfig: KonanConfig
    get() = typedContext.konanConfig

val ForegroundPhase.Context.mapper: CachedObjCExportMapper
    get() = descriptorProvider.mapper

val ForegroundPhase.Context.kotlinBuiltins: KotlinBuiltIns
    get() = descriptorProvider.builtIns

val ForegroundPhase.Context.extraDescriptorBuiltins: ExtraDescriptorBuiltins
    get() = descriptorProvider.extraDescriptorBuiltins

val ForegroundPhase.Context.configurables: AppleConfigurables
    get() = konanConfig.platform.configurables as AppleConfigurables

val ForegroundPhase.Context.descriptorReporter: DescriptorReporter
    get() = typedContext.descriptorReporter

private val ForegroundPhase.Context.typedContext: ForegroundPhaseCompilerContext
    get() = context as ForegroundPhaseCompilerContext
