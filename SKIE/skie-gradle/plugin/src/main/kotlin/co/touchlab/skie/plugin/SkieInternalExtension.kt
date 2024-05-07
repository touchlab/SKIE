package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.shim.KgpShim
import co.touchlab.skie.plugin.shim.KgpShimLoader
import co.touchlab.skie.plugin.util.KotlinVersionResolver
import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager
import javax.inject.Inject

internal abstract class SkieInternalExtension @Inject constructor(
    private val project: Project,
    val kotlinVersion: String,
    val kgpShim: KgpShim,
) {

    abstract val targets: NamedDomainObjectContainer<SkieTarget>

    val isSkieEnabled: Boolean
        get() = project.skieExtension.isEnabled.get() && HostManager.hostIsMac

    companion object {

        fun createExtension(project: Project): SkieInternalExtension? {
            val kotlinVersion = KotlinVersionResolver.resolve(project) ?: return null
            val kgpShim = KgpShimLoader.load(kotlinVersion, project) ?: return null

            return project.extensions.create(
                "skieInternal",
                SkieInternalExtension::class.java,
                kotlinVersion,
                kgpShim,
            )
        }
    }
}

internal val Project.skieInternalExtension: SkieInternalExtension
    get() = extensions.getByType(SkieInternalExtension::class.java)

internal val Project.kgpShim: KgpShim
    get() = skieInternalExtension.kgpShim
