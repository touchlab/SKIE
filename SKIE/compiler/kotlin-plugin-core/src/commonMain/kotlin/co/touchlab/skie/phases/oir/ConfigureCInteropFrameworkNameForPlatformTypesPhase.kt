package co.touchlab.skie.phases.oir

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase

object ConfigureCInteropFrameworkNameForPlatformTypesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allPlatformClasses.forEach {
            configureFrameworkName(it)
        }
    }

    context(SirPhase.Context)
    private fun configureFrameworkName(kirClass: KirClass) {
        kirClass.configuration[ClassInterop.CInteropFrameworkName] = kirClass.cinteropFrameworkName
    }

    private val KirClass.cinteropFrameworkName: String
        get() = if (this.kotlinIdentifier != "NSObject") {
            this.kotlinFqName.split(".")[1]
        } else {
            "Foundation"
        }
}
