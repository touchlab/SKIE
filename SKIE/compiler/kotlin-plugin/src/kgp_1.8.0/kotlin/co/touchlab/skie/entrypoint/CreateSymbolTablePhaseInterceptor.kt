@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.plugin.SkieCompilerConfigurationKey
import co.touchlab.skie.compilerinject.reflection.MutableDescriptorProviderKey
import co.touchlab.skie.compilerinject.reflection.reflectors.ContextReflector
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.compilerinject.reflection.skieContext
import co.touchlab.skie.kir.ExposedModulesProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkieCompilationScheduler
import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.library.KLIB_PROPERTY_SHORT_NAME
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class CreateSymbolTablePhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = createSymbolTablePhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        val contextReflector = ContextReflector(context)
        contextReflector.librariesWithDependencies.forEach { library ->
            if (library.shortName == null) {
                library.manifestProperties.setProperty(
                    KLIB_PROPERTY_SHORT_NAME,
                    library.uniqueName.substringAfterLast(':'),
                )
            }
        }

        next(context, input)

        val exposedModulesProvider = ExposedModulesProvider {
            setOf(context.moduleDescriptor) + context.getExportedDependencies()
        }

        val descriptorProvider = NativeMutableDescriptorProvider(
            exposedModulesProvider,
            context.config,
            ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface,
        )

        val declarationBuilder = DeclarationBuilderImpl(context.moduleDescriptor, descriptorProvider)
        SkieCompilerConfigurationKey.DeclarationBuilder.put(declarationBuilder, context.config.configuration)

        val skieScheduler = SkieCompilationScheduler(
            config = context.config,
            skieContext = context.skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
        )

        skieScheduler.runClassExportingPhases()

        // TODO: This doesn't feel right in the slightest. It's done to have the same API as Kotlin 1.8.20, but is it okay?
        descriptorProvider.reload(ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface)

        context.configuration.put(MutableDescriptorProviderKey, descriptorProvider)
        SkieCompilerConfigurationKey.SkieScheduler.put(skieScheduler, context.configuration)
    }
}
