package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.type.translation.BuiltinSwiftBridgeableProvider
import co.touchlab.skie.api.model.type.translation.SwiftIrDeclarationRegistry
import co.touchlab.skie.api.model.type.translation.SwiftTranslationProblemCollector
import co.touchlab.skie.api.model.type.translation.SwiftTypeTranslator
import co.touchlab.skie.plugin.api.DescriptorProviderKey
import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.mutableDescriptorProvider
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.CompilerOutputKind

class SwiftLinkCompilePhase(
    private val config: KonanConfig,
    private val context: CommonBackendContext,
    private val namer: ObjCExportNamer,
) {

    // TODO Refactor to phases
    fun process(): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()
        val framework = FrameworkLayout(config.outputFile).also { it.cleanSkie() }
        val bridgeProvider = DescriptorBridgeProvider(namer)
        val swiftIrDeclarationRegistry = SwiftIrDeclarationRegistry(
            namer = namer,
        )
        val builtinSwiftBridgeableProvider = BuiltinSwiftBridgeableProvider(
            sdkPath = configurables.absoluteTargetSysRoot,
            declarationRegistry = swiftIrDeclarationRegistry,
        )
        val builtinKotlinDeclarations = BuiltinDeclarations.Kotlin(namer)

        finalizeDescriptorProvider()

        val translator = SwiftTypeTranslator(
            descriptorProvider = context.descriptorProvider,
            namer = namer,
            problemCollector = SwiftTranslationProblemCollector.Default(context),
            builtinSwiftBridgeableProvider = builtinSwiftBridgeableProvider,
            builtinKotlinDeclarations = builtinKotlinDeclarations,
            swiftIrDeclarationRegistry = swiftIrDeclarationRegistry,
        )

        val swiftModelScope = DefaultSwiftModelScope(
            namer = namer,
            descriptorProvider = context.descriptorProvider,
            bridgeProvider = bridgeProvider,
            translator = translator,
            declarationRegistry = swiftIrDeclarationRegistry,
        )

        SkieLinkingPhaseScheduler(
            skieContext = context.skieContext,
            skieModule = context.skieContext.module as DefaultSkieModule,
            context = context,
            framework = framework,
            swiftModelScope = swiftModelScope,
            builtinKotlinDeclarations = builtinKotlinDeclarations,
            configurables = configurables,
            config = config,
        ).runLinkingPhases()

        return context.skieContext.skieBuildDirectory.swiftCompiler.objectFiles.all.map { it.absolutePath }
    }

    private fun finalizeDescriptorProvider() {
        val finalizedDescriptorProvider = context.mutableDescriptorProvider.preventFurtherMutations()

        context.configuration.put(DescriptorProviderKey, finalizedDescriptorProvider)
    }
}
