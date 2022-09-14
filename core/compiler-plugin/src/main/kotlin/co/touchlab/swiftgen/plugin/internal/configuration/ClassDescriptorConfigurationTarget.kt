package co.touchlab.swiftgen.plugin.internal.configuration

import co.touchlab.swiftgen.configuration.ConfigurationTarget
import co.touchlab.swiftgen.plugin.internal.util.findAnnotation
import co.touchlab.swiftgen.plugin.internal.util.hasAnnotation
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import kotlin.reflect.KClass

class ClassDescriptorConfigurationTarget(private val classDescriptor: ClassDescriptor) : ConfigurationTarget {

    override val fqName: String = classDescriptor.fqNameSafe.asString()

    override fun <T : Annotation> hasAnnotation(kClass: KClass<T>): Boolean =
        classDescriptor.hasAnnotation(kClass)

    override fun <T : Annotation> findAnnotation(kClass: KClass<T>): T? =
        classDescriptor.findAnnotation(kClass)
}
