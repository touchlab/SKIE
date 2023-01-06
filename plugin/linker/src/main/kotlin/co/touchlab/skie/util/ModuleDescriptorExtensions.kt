package co.touchlab.skie.util

import org.jetbrains.kotlin.descriptors.ModuleDescriptor

val ModuleDescriptor.stableIdentifier: String
    get() = (this.stableName ?: this.name).asStringStripSpecialMarkers().sanitizeForIdentifier()



