package co.touchlab.skie.plugin.api

import org.jetbrains.kotlin.descriptors.PropertyDescriptor

interface SwiftPropertyScope {
    val PropertyDescriptor.originalSwiftName: String

    val PropertyDescriptor.swiftName: String

    val PropertyDescriptor.isHiddenFromSwift: Boolean

    val PropertyDescriptor.isRemovedFromSwift: Boolean
}
