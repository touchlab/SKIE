package co.touchlab.swiftpack.api

import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface MutableSwiftFunctionScope: SwiftFunctionScope {
    override var FunctionDescriptor.swiftName: MutableSwiftFunctionName

    override var FunctionDescriptor.isHiddenFromSwift: Boolean

    override var FunctionDescriptor.isRemovedFromSwift: Boolean
}
