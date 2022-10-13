package co.touchlab.swiftgen.plugin.internal.validation.rules

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal interface ValidationRule<D : DeclarationDescriptor> {

    context(Reporter, Configuration)
    fun validate(descriptor: D)
}

context(Reporter, Configuration) internal fun <D : DeclarationDescriptor> Iterable<ValidationRule<D>>.validate(descriptor: D) {
    this.forEach {
        it.validate(descriptor)
    }
}
