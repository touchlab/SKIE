@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.context.ClassExportPhaseContext
import co.touchlab.skie.context.DescriptorModificationPhaseContext
import co.touchlab.skie.context.KotlinIrPhaseContext
import co.touchlab.skie.context.LinkPhaseContext
import co.touchlab.skie.context.MainSkieContext
import co.touchlab.skie.context.SirPhaseContext
import co.touchlab.skie.context.SymbolTablePhaseContext
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.phases.InitPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.konan.FrontendServices
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import java.nio.file.Path

internal object EntrypointUtils {

    fun createMainSkieContext(
        initPhaseContext: InitPhase.Context,
        konanConfig: KonanConfig,
        frontendServices: FrontendServices,
        mainModuleDescriptor: ModuleDescriptor,
        exportedDependencies: Lazy<Collection<ModuleDescriptor>>,
    ): MainSkieContext =
        initPhaseContext.skiePerformanceAnalyticsProducer.logBlocking("CreateMainSkieContextPhase") {
            val mainSkieContext = MainSkieContext(
                initPhaseContext = initPhaseContext,
                konanConfig = konanConfig,
                frontendServices = frontendServices,
                mainModuleDescriptor = mainModuleDescriptor,
                exportedDependencies = exportedDependencies.value,
            )

            initPhaseContext.compilerConfiguration.mainSkieContext = mainSkieContext

            mainSkieContext
        }

    fun runClassExportPhases(
        mainSkieContext: MainSkieContext,
    ) {
        with(mainSkieContext) {
            skiePhaseScheduler.runClassExportPhases {
                ClassExportPhaseContext(mainSkieContext)
            }
        }
    }

    fun runDescriptorModificationPhases(
        mainSkieContext: MainSkieContext,
    ) {
        with(mainSkieContext) {
            skiePhaseScheduler.runDescriptorModificationPhases {
                DescriptorModificationPhaseContext(mainSkieContext)
            }
        }
    }

    fun runSymbolTablePhases(mainSkieContext: MainSkieContext, symbolTable: SymbolTable) {
        with(mainSkieContext) {
            skiePhaseScheduler.runSymbolTablePhases {
                SymbolTablePhaseContext(
                    mainSkieContext = mainSkieContext,
                    symbolTable = symbolTable,
                )
            }
        }
    }

    fun runKotlinIrPhases(mainSkieContext: MainSkieContext, moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        with(mainSkieContext) {
            skiePhaseScheduler.runKotlinIrPhases {
                KotlinIrPhaseContext(
                    mainSkieContext = mainSkieContext,
                    moduleFragment = moduleFragment,
                    pluginContext = pluginContext,
                )
            }
        }
    }

    fun launchSirPhases(mainSkieContext: MainSkieContext, objCExportedInterfaceProvider: ObjCExportedInterfaceProvider) {
        with(mainSkieContext) {
            skiePhaseScheduler.launchSirPhases {
                SirPhaseContext(mainSkieContext, objCExportedInterfaceProvider)
            }
        }
    }

    fun runLinkPhases(mainSkieContext: MainSkieContext, link: (additionalObjectFiles: List<Path>) -> Unit) {
        with(mainSkieContext) {
            skiePhaseScheduler.runLinkPhases {
                LinkPhaseContext(mainSkieContext, link)
            }
        }
    }
}
