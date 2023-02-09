package co.touchlab.skie.util

import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

val ModuleDescriptor.swiftIdentifier: String
    get() = (this.stableName ?: this.name).asStringStripSpecialMarkers().toValidSwiftIdentifier()



