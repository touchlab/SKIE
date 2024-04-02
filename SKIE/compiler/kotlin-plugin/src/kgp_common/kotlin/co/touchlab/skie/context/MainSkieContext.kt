@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.descriptor.NativeDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.SkiePhase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.jetbrains.kotlin.backend.konan.FrontendServices
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

class MainSkieContext internal constructor(
    initPhaseContext: InitPhase.Context,
    override val konanConfig: KonanConfig,
    frontendServices: FrontendServices,
    val mainModuleDescriptor: ModuleDescriptor,
    exportedDependencies: Collection<ModuleDescriptor>,
) : SkiePhase.Context, InitPhase.Context by initPhaseContext {

    private val skieCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default) + CoroutineExceptionHandler { _, _ ->
        // Hide default stderr output because the exception is handled at the end of the job
    }

    private val jobs = mutableListOf<Job>()

    override val context: SkiePhase.Context
        get() = this

    override val descriptorProvider: MutableDescriptorProvider = NativeDescriptorProvider(
        exposedModules = setOf(mainModuleDescriptor) + exportedDependencies,
        konanConfig = konanConfig,
        frontendServices = frontendServices,
    )

    val declarationBuilder: DeclarationBuilderImpl = DeclarationBuilderImpl(mainModuleDescriptor, descriptorProvider)

    override fun launch(action: suspend () -> Unit) {
        val job = skieCoroutineScope.launch {
            action()
        }

        jobs.add(job)
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
