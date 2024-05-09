package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.shim.KgpShim
import co.touchlab.skie.plugin.shim.KgpShimLoader
import co.touchlab.skie.plugin.util.KotlinVersionResolver
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import javax.inject.Inject

abstract class SkieInternalExtension @Inject constructor(
    private val project: Project,
    val kotlinVersion: String,
    val kgpShim: KgpShim,
) {

    val targets: NamedDomainObjectContainer<SkieTarget>
        get() = kgpShim.targets

    val isSkieEnabled: Boolean
        get() = project.skieExtension.isEnabled.get() && kgpShim.hostIsMac

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

val Project.skieInternalExtension: SkieInternalExtension
    get() = extensions.getByType(SkieInternalExtension::class.java)

val Project.kgpShim: KgpShim
    get() = skieInternalExtension.kgpShim
