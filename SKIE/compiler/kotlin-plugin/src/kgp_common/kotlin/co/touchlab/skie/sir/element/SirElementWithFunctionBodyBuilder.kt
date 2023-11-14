package co.touchlab.skie.sir.element

import io.outfoxx.swiftpoet.FunctionSpec

interface SirElementWithFunctionBodyBuilder {

    val bodyBuilder: MutableList<FunctionSpec.Builder.() -> Unit>
}
