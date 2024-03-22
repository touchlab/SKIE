package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.konan.target.AppleConfigurables

interface SkiePhase<C : SkiePhase.Context> {

    context(C)
    fun isActive(): Boolean = true

    context(C)
    suspend fun execute()

    interface Context : InitPhase.Context {

        override val context: Context

        val konanConfig: KonanConfig

        val configurables: AppleConfigurables
            get() = konanConfig.platform.configurables as AppleConfigurables

        val descriptorProvider: DescriptorProvider

        val kotlinBuiltins: KotlinBuiltIns
            get() = descriptorProvider.builtIns

        val extraDescriptorBuiltins: ExtraDescriptorBuiltins
            get() = descriptorProvider.extraDescriptorBuiltins
    }
}
