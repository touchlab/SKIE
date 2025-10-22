package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.annotations.SkieVisibility
import co.touchlab.skie.kir.descriptor.allExposedMembers
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.descriptorProvider
import co.touchlab.skie.phases.descriptorReporter
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

object ValidateSkieVisibilityAnnotationsPhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        descriptorProvider.allExposedMembers.forEach {
            validate(it)
        }

        descriptorProvider.exposedClasses.forEach {
            validate(it)
        }
    }

    context(ClassExportPhase.Context)
    private fun validate(declarationDescriptor: DeclarationDescriptor) {
        val visibilityAnnotations = declarationDescriptor.annotations.filter {
            it.fqName?.asString()?.startsWith(SkieVisibility::class.qualifiedName!!) == true
        }

        if (visibilityAnnotations.size > 1) {
            descriptorReporter.warning(
                "Multiple ${SkieVisibility::class.simpleName} annotations used simultaneously. This is not allowed and may result in undefined behavior. " +
                    "This warning might become an error in the future.",
                declarationDescriptor,
            )
        }
    }
}
