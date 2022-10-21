package co.touchlab.swiftpack.api

import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface SwiftFunctionScope {
    val FunctionDescriptor.originalSwiftName: String

    val FunctionDescriptor.swiftName: String

    val FunctionDescriptor.isHiddenFromSwift: Boolean

    val FunctionDescriptor.isRemovedFromSwift: Boolean
}
