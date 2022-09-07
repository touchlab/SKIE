package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality

internal val ClassDescriptor.isSealed: Boolean
    get() = this.modality == Modality.SEALED

internal val ClassDescriptor.isVisibleFromSwift: Boolean
    get() = this.visibility.isPublicAPI