package co.touchlab.skie.kir.element

import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.sir.element.SirFunction
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

sealed class KirFunction<S : SirFunction> : KirCallableDeclaration<S> {

    val name: String
        get() = descriptor.name.asString()

    abstract val errorHandlingStrategy: OirFunction.ErrorHandlingStrategy

    abstract override val baseDescriptor: FunctionDescriptor

    abstract val valueParameters: MutableList<KirValueParameter>

    abstract val defaultArgumentsOverloads: List<KirFunction<S>>

    override fun toString(): String = "${this::class.simpleName}: $descriptor"
}
