package co.touchlab.skie.compilerinject.compilerplugin

import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptorRegistrar
import co.touchlab.skie.context.InitPhaseContext
import co.touchlab.skie.entrypoint.SkieIrGenerationExtension
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.spi.SkiePluginLoader
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SkieComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    @OptIn(ExperimentalTime::class)
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val initContext: InitPhase.Context

        val time = measureTime {
            val pluginLoader = SkiePluginLoader()

            initContext = InitPhaseContext(configuration, pluginLoader.pluginRegistrars)

            configuration.initPhaseContext = initContext

            IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))

            PhaseInterceptorRegistrar.setupPhaseInterceptors(configuration)

            pluginLoader.registerAll(initContext)
        }

        initContext.skiePerformanceAnalyticsProducer.logBlocking("InitSkiePhase", time)
    }
}
