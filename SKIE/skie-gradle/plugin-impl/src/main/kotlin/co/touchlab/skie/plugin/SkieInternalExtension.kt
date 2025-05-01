package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.shim.KgpShim
import co.touchlab.skie.plugin.shim.KgpShimLoader
import co.touchlab.skie.plugin.shim.SkieKotlinVariantResolver
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

abstract class SkieInternalExtension @Inject constructor(private val project: Project, val kotlinVersion: String, val kgpShim: KgpShim) {

    val targets: NamedDomainObjectContainer<SkieTarget>
        get() = kgpShim.skieTargets

    val isSkieEnabled: Boolean
        get() = project.skieExtension.isEnabled.get() && kgpShim.hostIsMac

    companion object {

        fun withExtension(project: Project, action: (SkieInternalExtension) -> Unit) {
            SkieKotlinVariantResolver.withSkieKotlinVersion(project) { kotlinVersion ->
                val kgpShim = KgpShimLoader.load(kotlinVersion, project) ?: return@withSkieKotlinVersion

                val extension = project.extensions.create(
                    "skieInternal",
                    SkieInternalExtension::class.java,
                    kotlinVersion,
                    kgpShim,
                )

                action(extension)
            }
        }
    }
}

val Project.skieInternalExtension: SkieInternalExtension
    get() = extensions.getByType(SkieInternalExtension::class.java)

val Project.kgpShim: KgpShim
    get() = skieInternalExtension.kgpShim
