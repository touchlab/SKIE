package co.touchlab.skie.plugin.api

import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface SwiftClassScope {
    val ClassDescriptor.swiftName: SwiftTypeName

    val ClassDescriptor.isHiddenFromSwift: Boolean

    val ClassDescriptor.isRemovedFromSwift: Boolean

    val ClassDescriptor.swiftBridgeType: SwiftBridgedName?
}
