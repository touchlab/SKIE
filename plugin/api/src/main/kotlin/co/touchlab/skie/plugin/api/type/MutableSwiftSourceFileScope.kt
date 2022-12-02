package co.touchlab.skie.plugin.api.type

import org.jetbrains.kotlin.descriptors.SourceFile

interface MutableSwiftSourceFileScope : SwiftSourceFileScope {

    override val SourceFile.swiftName: MutableSwiftTypeName

    override var SourceFile.isHiddenFromSwift: Boolean

    override var SourceFile.isRemovedFromSwift: Boolean

    override var SourceFile.swiftBridgeType: SwiftBridgedName?
}
