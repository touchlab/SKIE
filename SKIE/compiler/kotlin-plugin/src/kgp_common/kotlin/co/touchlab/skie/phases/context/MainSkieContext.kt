@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.context

import co.touchlab.skie.compilerinject.plugin.SkieConfigurationKeys
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.swiftmodel.type.translation.impl.CommonBackendContextSwiftTranslationProblemCollector
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.config.CompilerConfiguration

class MainSkieContext(
    compilerConfiguration: CompilerConfiguration
) : SkiePhase.Context {

    init {
        val skieDirectories = configuration.getNotNull(SkieConfigurationKeys.SkieDirectories)

        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()
        val skieConfiguration = SkieConfiguration.deserialize(serializedUserConfiguration)

        val swiftCompilerConfiguration = SwiftCompilerConfiguration(
            sourceFilesDirectory = skieDirectories.buildDirectory.swift.directory,
            swiftVersion = configuration.get(SkieConfigurationKeys.SwiftCompiler.swiftVersion, "5"),
            additionalFlags = configuration.getList(SkieConfigurationKeys.SwiftCompiler.additionalFlags),
        )

        val skieContext = DefaultSkieContext(
            module = DefaultSkieModule(),
            skieConfiguration = skieConfiguration,
            swiftCompilerConfiguration = swiftCompilerConfiguration,
            skieDirectories = skieDirectories,
            frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
            analyticsCollector = AnalyticsCollector(
                skieBuildDirectory = skieDirectories.buildDirectory,
                skieConfiguration = skieConfiguration,
            ),
            skiePerformanceAnalyticsProducer = SkiePerformanceAnalytics.Producer(skieConfiguration),
            reporter = Reporter(configuration),
        )
    }

    lateinit var declarationBuilder: DeclarationBuilderImpl

    lateinit var descriptorProvider: NativeMutableDescriptorProvider

//     override val descriptorProvider: MutableDescriptorProvider

    val framework = FrameworkLayout(konanConfig.outputFile).also { it.cleanSkie() }

    val namer: ObjCExportNamer

    val problemCollector = CommonBackendContextSwiftTranslationProblemCollector(konanContext)

    internal fun reloadDescriptorProvider(objCExportedInterface: ObjCExportedInterface) {
        descriptorProvider.preventFurtherMutations(objCExportedInterface)
    }

    internal fun finalizeDescriptorProvider(objCExportedInterface: ObjCExportedInterface) {
        descriptorProvider.preventFurtherMutations(objCExportedInterface)
    }
}
