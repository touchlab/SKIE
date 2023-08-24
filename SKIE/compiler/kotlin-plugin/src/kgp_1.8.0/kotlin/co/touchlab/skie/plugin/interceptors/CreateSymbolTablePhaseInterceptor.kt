@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.plugin.api.MutableDescriptorProviderKey
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.SkieCompilationScheduler
import co.touchlab.skie.plugin.generator.internal.SkieCompilerConfigurationKey
import co.touchlab.skie.plugin.generator.internal.util.ExposedModulesProvider
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeMutableDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.intercept.SameTypePhaseInterceptor
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportReflector
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
import org.jetbrains.kotlin.library.KLIB_PROPERTY_SHORT_NAME
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class CreateSymbolTablePhaseInterceptor: SameTypePhaseInterceptor<KonanContext, Unit> {
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
            namespaceProvider = NamespaceProvider(context.skieContext.module),
            reporter = Reporter(context.configuration),
        )

        skieScheduler.runClassExportingPhases()

        // TODO: This doesn't feel right in the slightest. It's done to have the same API as Kotlin 1.8.20, but is it okay?
        descriptorProvider.reload(ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface)

        context.configuration.put(MutableDescriptorProviderKey, descriptorProvider)
        SkieCompilerConfigurationKey.SkieScheduler.put(skieScheduler, context.configuration)
    }
}
