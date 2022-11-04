package co.touchlab.skie.plugin.api.function

import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface MutableSwiftFunctionScope : SwiftFunctionScope {
    override val FunctionDescriptor.swiftName: MutableSwiftFunctionName

    override var FunctionDescriptor.isHiddenFromSwift: Boolean

    override var FunctionDescriptor.isRemovedFromSwift: Boolean
}
