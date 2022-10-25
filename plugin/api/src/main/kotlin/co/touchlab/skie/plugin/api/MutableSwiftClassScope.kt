package co.touchlab.skie.plugin.api

import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface MutableSwiftClassScope : SwiftClassScope {
    override var ClassDescriptor.swiftName: MutableSwiftTypeName

    override var ClassDescriptor.isHiddenFromSwift: Boolean

    override var ClassDescriptor.isRemovedFromSwift: Boolean

    override var ClassDescriptor.swiftBridgeType: SwiftBridgedName?
}
