package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality

internal val ClassDescriptor.isSealed: Boolean
    get() = this.modality == Modality.SEALED

// FIXME Not correct - does not take exported Objc modules into account
internal val ClassDescriptor.isVisibleFromSwift: Boolean
    get() = this.visibility.isPublicAPI