package co.touchlab.skie.plugin.api.type

import org.jetbrains.kotlin.descriptors.SourceFile

interface SwiftSourceFileScope {

    val SourceFile.swiftName: SwiftTypeName

    val SourceFile.isHiddenFromSwift: Boolean

    val SourceFile.isRemovedFromSwift: Boolean

    val SourceFile.swiftBridgeType: SwiftBridgedName?
}
