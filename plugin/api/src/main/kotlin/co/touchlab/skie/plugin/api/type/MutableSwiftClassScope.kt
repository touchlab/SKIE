package co.touchlab.skie.plugin.api.type

import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface MutableSwiftClassScope : SwiftClassScope {

    override val ClassDescriptor.swiftName: MutableSwiftTypeName

    override var ClassDescriptor.isHiddenFromSwift: Boolean

    override var ClassDescriptor.isRemovedFromSwift: Boolean

    override var ClassDescriptor.swiftBridgeType: SwiftBridgedName?
}
