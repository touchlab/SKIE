@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import co.touchlab.skie.configuration.SwiftCompilerConfiguration.BuildType
import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.descriptor.NativeDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.BackgroundPhase
import co.touchlab.skie.phases.ScheduledPhase
import co.touchlab.skie.phases.configurables
import co.touchlab.skie.phases.util.StatefulScheduledPhase
import co.touchlab.skie.util.DescriptorReporter
import co.touchlab.skie.util.KotlinCompilerVersion
import co.touchlab.skie.util.TargetTriple
import co.touchlab.skie.util.current
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.FrontendServices
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import java.util.Collections

class MainSkieContext internal constructor(
    initPhaseContext: InitPhaseContext,
    override val konanConfig: KonanConfig,
    frontendServices: FrontendServices,
    val mainModuleDescriptor: ModuleDescriptor,
    exportedDependencies: Collection<ModuleDescriptor>,
) : ForegroundPhaseCompilerContext, BackgroundPhase.Context, CommonSkieContext by initPhaseContext {

    private val skieCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default) + CoroutineExceptionHandler { _, _ ->
        // Hides default stderr output because the exception is handled at the end of the job
    }

    private val jobs = Collections.synchronizedList(mutableListOf<Job>())

    private val statefulScheduledPhaseBodies = Collections.synchronizedMap(mutableMapOf<StatefulScheduledPhase<*>, MutableList<Any>>())

    override val context: MainSkieContext
        get() = this

    override val descriptorProvider: MutableDescriptorProvider = NativeDescriptorProvider(
        exposedModules = setOf(mainModuleDescriptor) + exportedDependencies,
        konanConfig = konanConfig,
        frontendServices = frontendServices,
    )

    override val descriptorConfigurationProvider: DescriptorConfigurationProvider = DescriptorConfigurationProvider(initPhaseContext.configurationProvider)

    val declarationBuilder: DeclarationBuilderImpl = DeclarationBuilderImpl(mainModuleDescriptor, descriptorProvider)

    override val descriptorReporter: DescriptorReporter = initPhaseContext.descriptorReporter

    lateinit var kirProvider: KirProvider

    lateinit var descriptorKirProvider: DescriptorKirProvider

    private val kotlinTargetTriple = configurables.targetTriple

    override val swiftCompilerConfiguration: SwiftCompilerConfiguration = SwiftCompilerConfiguration(
        // TODO To SkieConfiguration via RootScope Key
        swiftVersion = "5",
        // TODO To SkieConfiguration via RootScope Key
        additionalFlags = emptyList(),
        buildType = if (konanConfig.debug) BuildType.Debug else BuildType.Release,
        targetTriple = TargetTriple(
            architecture = kotlinTargetTriple.architecture,
            vendor = kotlinTargetTriple.vendor,
            os = kotlinTargetTriple.os,
            environment = kotlinTargetTriple.environment,
        ),
        bitcodeEmbeddingMode = when (konanConfig.configuration[KonanConfigKeys.BITCODE_EMBEDDING_MODE]) {
            BitcodeEmbedding.Mode.FULL -> SwiftCompilerConfiguration.BitcodeEmbeddingMode.Full
            BitcodeEmbedding.Mode.MARKER -> SwiftCompilerConfiguration.BitcodeEmbeddingMode.Marker
            BitcodeEmbedding.Mode.NONE, null -> SwiftCompilerConfiguration.BitcodeEmbeddingMode.None
        },
        absoluteSwiftcPath = if (KotlinCompilerVersion.current >= KotlinCompilerVersion.`2_0_0`) {
            configurables.absoluteTargetToolchain + "/bin/swiftc"
        } else {
            configurables.absoluteTargetToolchain + "/usr/bin/swiftc"
        },
        absoluteTargetSysRootPath = configurables.absoluteTargetSysRoot,
        osVersionMin = configurables.osVersionMin,
    )

    override fun launch(action: suspend () -> Unit) {
        val job = skieCoroutineScope.launch {
            action()
        }

        jobs.add(job)
    }

    override fun <CONTEXT : ScheduledPhase.Context> storeStatefulScheduledPhaseBody(phase: StatefulScheduledPhase<CONTEXT>, action: CONTEXT.() -> Unit) {
        val bodies = statefulScheduledPhaseBodies.getOrPut(phase) { mutableListOf() }

        bodies.add(action)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <CONTEXT : ScheduledPhase.Context> executeStatefulScheduledPhase(phase: StatefulScheduledPhase<CONTEXT>, context: CONTEXT) {
        val phaseBodies = statefulScheduledPhaseBodies[phase] ?: return

        phaseBodies.forEach { phaseBody ->
            (phaseBody as CONTEXT.() -> Unit).invoke(context)
        }
    }

    suspend fun awaitAllBackgroundJobs() {
        jobs.joinAll()

        jobs.forEach { job ->
            job.invokeOnCompletion { error ->
                if (error != null) {
                    throw error
                }
            }
        }
    }
}
