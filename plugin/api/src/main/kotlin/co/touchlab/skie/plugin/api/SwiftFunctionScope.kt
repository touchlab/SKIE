package co.touchlab.skie.plugin.api

import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface SwiftFunctionScope {
    val FunctionDescriptor.swiftName: SwiftFunctionName

    val FunctionDescriptor.isHiddenFromSwift: Boolean

    val FunctionDescriptor.isRemovedFromSwift: Boolean
}
