package co.touchlab.skie.phases.oir

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.configuration
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

object ConfigureCInteropFrameworkNameForPlatformTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        oirProvider.allExternalClassesAndProtocols.forEach {
            configureIfPlatformType(it)
        }
    }

    context(SirPhase.Context)
    private fun configureIfPlatformType(oirClass: OirClass) {
        val origin = oirClass.origin as? OirClass.Origin.CinteropType ?: error("Invalid origin for OirClass: $oirClass")

        val classDescriptor = origin.classDescriptor

        if (!classDescriptor.isPlatformType) {
            return
        }

        classDescriptor.configuration[ClassInterop.CInteropFrameworkName] = classDescriptor.cinteropFrameworkName
    }

    private val ClassDescriptor.isPlatformType: Boolean
        get() = this.fqNameSafe.pathSegments()[0].asString() == "platform"

    private val ClassDescriptor.cinteropFrameworkName: String
        get() = if (name.asString() != "NSObject") {
            this.fqNameSafe.pathSegments()[1].asString()
        } else {
            "Foundation"
        }
}
