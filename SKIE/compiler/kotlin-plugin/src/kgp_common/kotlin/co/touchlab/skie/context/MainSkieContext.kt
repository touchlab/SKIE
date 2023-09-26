@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.compilerinject.compilerplugin.SkieConfigurationKeys
import co.touchlab.skie.configuration.ConfigurationProvider
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.kir.ExposedModulesProvider
import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

class MainSkieContext(
    override val compilerConfiguration: CompilerConfiguration,
) : SkiePhase.Context {

    override val context: SkiePhase.Context
        get() = this

    override val skiePhaseScheduler: SkiePhaseScheduler = SkiePhaseScheduler()

    override val skieDirectories: SkieDirectories = compilerConfiguration.getNotNull(SkieConfigurationKeys.SkieDirectories)

    override val skieConfiguration: SkieConfiguration = run {
        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()

        SkieConfiguration.deserialize(serializedUserConfiguration)
    }

    override val configurationProvider: ConfigurationProvider = ConfigurationProvider(this)

    override val swiftCompilerConfiguration: SwiftCompilerConfiguration = SwiftCompilerConfiguration(
        sourceFilesDirectory = skieDirectories.buildDirectory.swift.directory,
        swiftVersion = compilerConfiguration.get(SkieConfigurationKeys.SwiftCompiler.swiftVersion, "5"),
        additionalFlags = compilerConfiguration.getList(SkieConfigurationKeys.SwiftCompiler.additionalFlags),
    )

    override val analyticsCollector: AnalyticsCollector = AnalyticsCollector(
        skieBuildDirectory = skieDirectories.buildDirectory,
        skieConfiguration = skieConfiguration,
    )

    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer = SkiePerformanceAnalytics.Producer(skieConfiguration)

    override val reporter: Reporter = Reporter(compilerConfiguration)

    override val objCTypeRenderer: ObjCTypeRenderer = ObjCTypeRenderer()

    override val framework: FrameworkLayout = run {
        val frameworkPath = compilerConfiguration.getNotNull(KonanConfigKeys.OUTPUT)

        FrameworkLayout(frameworkPath)
    }

    override lateinit var konanConfig: KonanConfig
        private set

    private lateinit var nativeMutableDescriptorProvider: NativeMutableDescriptorProvider

    override val descriptorProvider: MutableDescriptorProvider by ::nativeMutableDescriptorProvider

    lateinit var declarationBuilder: DeclarationBuilderImpl
        private set

    lateinit var namer: ObjCExportNamer
        private set

    internal fun initialize(
        konanConfig: KonanConfig,
        mainModuleDescriptor: ModuleDescriptor,
        exportedDependencies: Collection<ModuleDescriptor>,
        produceObjCExportInterface: () -> ObjCExportedInterface,
    ) {
        this.konanConfig = konanConfig

        val exposedModulesProvider = ExposedModulesProvider {
            setOf(mainModuleDescriptor) + exportedDependencies
        }

        val objCExportedInterface = produceObjCExportInterface()

        nativeMutableDescriptorProvider = NativeMutableDescriptorProvider(
            exposedModulesProvider,
            konanConfig,
            objCExportedInterface,
        )

        namer = objCExportedInterface.namer

        declarationBuilder = DeclarationBuilderImpl(mainModuleDescriptor, nativeMutableDescriptorProvider)
    }

    internal fun reloadDescriptorProvider(objCExportedInterface: ObjCExportedInterface) {
        nativeMutableDescriptorProvider.reload(objCExportedInterface)

        namer = objCExportedInterface.namer
    }

    internal fun finalizeDescriptorProvider(objCExportedInterface: ObjCExportedInterface) {
        nativeMutableDescriptorProvider.preventFurtherMutations(objCExportedInterface)

        namer = objCExportedInterface.namer
    }
}
