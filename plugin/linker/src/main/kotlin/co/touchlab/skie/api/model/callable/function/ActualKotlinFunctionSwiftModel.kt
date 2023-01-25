package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.api.model.callable.parameter.ActualKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

internal class ActualKotlinFunctionSwiftModel(
    override val descriptor: FunctionDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>,
    val core: KotlinFunctionSwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel {

    override var identifier: String by core::identifier

    override val parameters: List<MutableKotlinParameterSwiftModel> by lazy {
        core.getParameterCoresWithDescriptors(descriptor).map { (core, parameterDescriptor) ->
            ActualKotlinParameterSwiftModel(
                core,
                parameterDescriptor,
            ) {
                with(swiftModelScope) {
                    descriptor.getParameterType(parameterDescriptor, core.parameterBridge, receiver.swiftGenericExportScope)
                }
            }
        }
    }

    override var visibility: SwiftModelVisibility by core::visibility

    override val receiver: TypeSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.receiverTypeModel()
        }
    }

    override val objCSelector: String by core::objCSelector

    override val kind: KotlinFunctionSwiftModel.Kind
        get() = when (descriptor) {
            is ConstructorDescriptor -> KotlinFunctionSwiftModel.Kind.Constructor
            is PropertyGetterDescriptor -> KotlinFunctionSwiftModel.Kind.ConvertedGetter
            is PropertySetterDescriptor -> KotlinFunctionSwiftModel.Kind.ConvertedSetter
            else -> KotlinFunctionSwiftModel.Kind.SimpleFunction
        }

    override val original: KotlinFunctionSwiftModel = OriginalKotlinFunctionSwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility || parameters.any { it.isChanged }

    override val returnType: TypeSwiftModel
        get() = with(swiftModelScope) {
            core.descriptor.returnTypeModel(receiver.swiftGenericExportScope, core.methodBridge.returnBridge)
        }

    override fun toString(): String = descriptor.toString()
}
