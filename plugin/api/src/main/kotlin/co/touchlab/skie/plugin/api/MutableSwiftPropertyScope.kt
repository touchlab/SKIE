package co.touchlab.skie.plugin.api

import org.jetbrains.kotlin.descriptors.PropertyDescriptor

interface MutableSwiftPropertyScope : SwiftPropertyScope {
    override var PropertyDescriptor.swiftName: String

    override var PropertyDescriptor.isHiddenFromSwift: Boolean

    override var PropertyDescriptor.isRemovedFromSwift: Boolean
}
