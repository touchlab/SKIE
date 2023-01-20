package co.touchlab.skie.util

import org.jetbrains.kotlin.descriptors.ModuleDescriptor

val ModuleDescriptor.swiftIdentifier: String
    get() = (this.stableName ?: this.name).asStringStripSpecialMarkers().toValidSwiftIdentifier()



