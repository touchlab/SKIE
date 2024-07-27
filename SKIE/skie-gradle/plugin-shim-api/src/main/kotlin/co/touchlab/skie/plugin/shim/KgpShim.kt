package co.touchlab.skie.plugin.shim

import co.touchlab.skie.plugin.SkieTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.attributes.AttributeContainer
import java.io.File
import java.util.Properties

interface KgpShim {

    val skieTargets: NamedDomainObjectContainer<SkieTarget>

    val kotlinNativeTargets: NamedDomainObjectContainer<KotlinNativeTargetShim>

    val launchScheduler: LaunchScheduler

    val hostIsMac: Boolean

    fun getDistributionProperties(
        konanHome: String,
        propertyOverrides: Map<String, String>?,
    ): Properties

    fun getKonanHome(): File

    fun getKotlinPluginVersion(): String

    fun initializeShim()

    fun resolvablePropertyString(properties: Properties, key: String, suffix: String?): String?

    fun addKmpAttributes(attributeContainer: AttributeContainer, konanTarget: KonanTargetShim)

    fun configureEachFatFrameworkTask(action: FatFrameworkTaskShim.() -> Unit)
}
